package net.coding.program.pickphoto

import android.app.Activity
import android.app.LoaderManager
import android.content.CursorLoader
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.content.Loader
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.tbruyelle.rxpermissions2.RxPermissions
import net.coding.program.common.CameraPhotoUtil
import net.coding.program.common.ImageInfo
import net.coding.program.common.util.PermissionUtil
import net.coding.program.common.widget.FileProviderHelp
import java.io.File
import java.util.*

class PhotoPickActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private val RESULT_PICK = 20
    private val RESULT_CAMERA = 21
    private val allPhotos = "所有图片"
    private val CameraItem = "CameraItem"
    private var mMenuItem: MenuItem? = null
    internal var mFolderId = 0
    private var mMaxPick = Config.MAX_COUNT
    private var mInflater: LayoutInflater? = null
    private var mFoldName: TextView? = null
    private var mListViewGroup: View? = null
    private var mListView: ListView? = null

    //    LinkedHashMap<String, ArrayList<ImageInfo>> mFolders = new LinkedHashMap();
    //    ArrayList<String> mFoldersName = new ArrayList<>();
    internal var mOnClickFoldName: View.OnClickListener = View.OnClickListener {
        if (mListViewGroup!!.visibility == View.VISIBLE) {
            hideFolderList()
        } else {
            showFolderList()
        }
    }
    private var mGridView: GridView? = null
    private var mPreView: TextView? = null
    private var mPickData = ArrayList<ImageInfo>()
    private var mFolderAdapter: FolderAdapter? = null

    internal var mOnPhotoItemClick = AdapterView.OnItemClickListener { parent, view, position, id ->
        val intent = Intent(this@PhotoPickActivity, PhotoPickDetailActivity::class.java)

        intent.putExtra(PhotoPickDetailActivity.PICK_DATA, mPickData)
        intent.putExtra(PhotoPickDetailActivity.EXTRA_MAX, mMaxPick)
        var folderParam = ""
        if (isAllPhotoMode) {
            // 第一个item是照相机
            intent.putExtra(PhotoPickDetailActivity.PHOTO_BEGIN, position - 1)
        } else {
            intent.putExtra(PhotoPickDetailActivity.PHOTO_BEGIN, position)
            folderParam = mFolderAdapter!!.select
        }
        intent.putExtra(PhotoPickDetailActivity.FOLDER_NAME, folderParam)
        this@PhotoPickActivity.startActivityForResult(intent, RESULT_PICK)
    }

    private var photoAdapter: GridPhotoAdapter? = null
    private val projection = arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.WIDTH, MediaStore.Images.ImageColumns.HEIGHT)
    private val onClickPre = View.OnClickListener {
        if (mPickData.size == 0) {
            return@OnClickListener
        }

        val intent = Intent(this@PhotoPickActivity, PhotoPickDetailActivity::class.java)
        intent.putExtra(PhotoPickDetailActivity.FOLDER_NAME, mFolderAdapter!!.select)
        intent.putExtra(PhotoPickDetailActivity.PICK_DATA, mPickData)
        intent.putExtra(PhotoPickDetailActivity.ALL_DATA, mPickData)
        intent.putExtra(PhotoPickDetailActivity.EXTRA_MAX, mMaxPick)
        startActivityForResult(intent, RESULT_PICK)
    }
    private val mOnItemClick = AdapterView.OnItemClickListener { parent, view, position, id ->
        mFolderAdapter!!.setSelect(id.toInt())
        val folderName = mFolderAdapter!!.select
        mFoldName!!.text = folderName
        hideFolderList()

        if (mFolderId != position) {
            loaderManager.destroyLoader(mFolderId)
            mFolderId = position
        }
        loaderManager.initLoader(mFolderId, Bundle.EMPTY, this@PhotoPickActivity)
    }
    private var fileUri: Uri? = null
    private var fileTemp: File? = null

    /*
     * 选择了listview的第一个项，gridview的第一个是照相机
     */
    private val isAllPhotoMode: Boolean
        get() = mFolderId == 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_pick)

        val actionBar = supportActionBar
        actionBar!!.setTitle("图片")
        actionBar.setDisplayHomeAsUpEnabled(true)

        mMaxPick = intent.getIntExtra(EXTRA_MAX, 6)
        val extraPicked = intent.getSerializableExtra(EXTRA_PICKED)

        if (extraPicked != null) {
            mPickData = extraPicked as ArrayList<ImageInfo>
        }

        mInflater = layoutInflater
        mGridView = findViewById<View>(R.id.gridView) as GridView
        mListView = findViewById<View>(R.id.listView) as ListView
        mListViewGroup = findViewById(R.id.listViewParent)
        mListViewGroup!!.setOnClickListener(mOnClickFoldName)
        mFoldName = findViewById<View>(R.id.foldName) as TextView
        mFoldName!!.text = allPhotos

        findViewById<View>(R.id.selectFold).setOnClickListener(mOnClickFoldName)

        mPreView = findViewById<View>(R.id.preView) as TextView
        mPreView!!.setOnClickListener(onClickPre)

        initListView1()
        initListView2()
    }

    fun getmMaxPick(): Int {
        return mMaxPick
    }

    fun clickPhotoItem(v: View) {
        val tag = v.tag as GridViewCheckTag
        if ((v as CheckBox).isChecked) {
            if (mPickData.size >= mMaxPick) {
                v.isChecked = false
                val s = String.format("最多只能选择%s张", mMaxPick)
                Toast.makeText(this, s, Toast.LENGTH_LONG).show()
                return
            }

            addPicked(tag.path)
            tag.iconFore.visibility = View.VISIBLE
        } else {
            removePicked(tag.path)
            tag.iconFore.visibility = View.INVISIBLE
        }
        (mListView!!.adapter as BaseAdapter).notifyDataSetChanged()

        updatePickCount()
    }

    private fun initListView1() {
        val needInfos = arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)


        val mNames = LinkedHashMap<String, Int>()
        val mData = LinkedHashMap<String, ImageInfo>()
        val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, needInfos, "", null, MediaStore.MediaColumns.DATE_ADDED + " DESC")

        while (cursor!!.moveToNext()) {
            val name = cursor.getString(2)
            if (!mNames.containsKey(name)) {
                mNames.put(name, 1)
                val imageInfo = ImageInfo(cursor.getString(1))
                mData.put(name, imageInfo)
            } else {
                val newCount = mNames[name]?.plus(1) ?: 1
                mNames.put(name, newCount)
            }
        }

        val mFolderData = ArrayList<ImageInfoExtra>()
        if (cursor.moveToFirst()) {
            val imageInfo = ImageInfo(cursor.getString(1))
            val allImagesCount = cursor.count
            mFolderData.add(ImageInfoExtra(allPhotos, imageInfo, allImagesCount))
        }

        for (item in mNames.keys) {
            val info = mData[item]
            val count = mNames[item]
            mFolderData.add(ImageInfoExtra(item, info, count!!))
        }
        cursor.close()

        mFolderAdapter = FolderAdapter(mFolderData)
        mListView!!.adapter = mFolderAdapter
        mListView!!.onItemClickListener = mOnItemClick
    }

    private fun initListView2() {
        loaderManager.initLoader(0, Bundle(), this)
    }

    private fun showFolderList() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.listview_up)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.listview_fade_in)

        mListView!!.startAnimation(animation)
        mListViewGroup!!.startAnimation(fadeIn)
        mListViewGroup!!.visibility = View.VISIBLE
    }

    private fun hideFolderList() {
        val animation = AnimationUtils.loadAnimation(this@PhotoPickActivity, R.anim.listview_down)
        val fadeOut = AnimationUtils.loadAnimation(this@PhotoPickActivity, R.anim.listview_fade_out)
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                mListViewGroup!!.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        mListView!!.startAnimation(animation)
        mListViewGroup!!.startAnimation(fadeOut)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_photo_pick, menu)
        mMenuItem = menu.getItem(0)
        updatePickCount()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_finish) {
            send()
            return true
        } else if (id == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun send() {
        if (mPickData.isEmpty()) {
            setResult(Activity.RESULT_CANCELED)
        } else {
            val intent = Intent()
            intent.putExtra("data", mPickData)
            setResult(Activity.RESULT_OK, intent)
        }

        finish()
    }

    fun camera() {
        RxPermissions(this)
                .request(*PermissionUtil.CAMERA_STORAGE)
                .subscribe { granted ->
                    if (granted!!) {
                        if (mPickData.size >= mMaxPick) {
                            val s = String.format("最多只能选择%s张", mMaxPick)
                            Toast.makeText(this@PhotoPickActivity, s, Toast.LENGTH_LONG).show()
                        } else {
                            fileTemp = CameraPhotoUtil.getCacheFile(this)
                            fileUri = FileProviderHelp.getUriForFile(this, fileTemp)

                            val intentFromCapture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//7.0及以上
                                intentFromCapture.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
                                intentFromCapture.addFlags(FLAG_GRANT_WRITE_URI_PERMISSION)
                            }

                            startActivityForResult(intentFromCapture, RESULT_CAMERA)
                        }
                    }
                }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        fileUri = savedInstanceState.getParcelable(RESTORE_FILEURI)

        val filePath = savedInstanceState.getString(RESTORE_FILE_PATH)
        if (filePath is String) {
            fileTemp = File(filePath)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        if (fileUri is Uri) {
            outState!!.putParcelable(RESTORE_FILEURI, fileUri)
        }

        if (fileTemp is File) {
            outState!!.putString(RESTORE_FILE_PATH, fileTemp?.path)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RESULT_PICK) {
            if (resultCode == Activity.RESULT_OK) {
                mPickData = data!!.getSerializableExtra("data") as ArrayList<ImageInfo>
                photoAdapter!!.notifyDataSetChanged()

                val send = data.getBooleanExtra("send", false)
                if (send) {
                    send()
                }
            }
        } else if (requestCode == RESULT_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                val itme = ImageInfo(fileTemp?.path)
                mPickData.add(itme)
                send()
            }
        }

        updatePickCount()

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun addPicked(path: String) {
        if (!isPicked(path)) {
            mPickData.add(ImageInfo(path))
        }
    }

    fun isPicked(path: String): Boolean {
        for (item in mPickData) {
            if (item.path == path) {
                return true
            }
        }

        return false
    }

    private fun removePicked(path: String) {
        for (i in mPickData.indices) {
            if (mPickData[i].path == path) {
                mPickData.removeAt(i)
                return
            }
        }
    }

    private fun updatePickCount() {
        val format = "完成(%d/%d)"
        mMenuItem?.title = String.format(format, mPickData.size, mMaxPick)

        val formatPreview = "预览"
        mPreView!!.text = formatPreview
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor> {
        var where = ""
        if (!isAllPhotoMode) {
            val select = (mListView!!.adapter as FolderAdapter).select
            where = String.format("%s='%s'",
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    select
            )
        }

        return CursorLoader(
                this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                where, null,
                MediaStore.MediaColumns.DATE_ADDED + " DESC")
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        if (isAllPhotoMode) {
            photoAdapter = AllPhotoAdapter(this, data, false, this)
        } else {
            photoAdapter = GridPhotoAdapter(this, data, false, this)
        }
        mGridView!!.adapter = photoAdapter
        mGridView!!.onItemClickListener = mOnPhotoItemClick
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        photoAdapter!!.swapCursor(null)
    }

    internal class GridViewCheckTag(var iconFore: View) {
        var path = ""
    }

    companion object {

        val EXTRA_MAX = "EXTRA_MAX"
        val EXTRA_PICKED = "EXTRA_PICKED" // mPickData
        private val RESTORE_FILEURI = "fileUri"
        private val RESTORE_FILE_PATH = "filePath"
        var optionsImage = DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_default_image)
                .showImageForEmptyUri(R.drawable.image_not_exist)
                .showImageOnFail(R.drawable.image_not_exist)
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build()
    }
}
