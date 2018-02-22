package net.coding.program.pickphoto

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import net.coding.program.common.ImageInfo
import net.coding.program.pickphoto.detail.ImagePagerFragment_
import java.util.*

class PhotoPickDetailActivity : AppCompatActivity() {
    private val actionbarTitle = "%d/%d"
    internal var mCursor: Cursor? = null
    private var mPickPhotos: ArrayList<ImageInfo>? = null
    private var mAllPhotos: ArrayList<ImageInfo>? = null
    private var mViewPager: ViewPager? = null
    private var mCheckBox: CheckBox? = null
    private var mMaxPick = Config.MAX_COUNT
    private var mMenuSend: MenuItem? = null

    internal val imageCount: Int
        get() = if (mAllPhotos != null) {
            mAllPhotos!!.size
        } else if (mCursor != null) {
            mCursor!!.count
        } else {
            0
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_pick_detail)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val extras = intent.extras
        mPickPhotos = extras!!.getSerializable(PICK_DATA) as? ArrayList<ImageInfo>
        mAllPhotos = extras.getSerializable(ALL_DATA) as? ArrayList<ImageInfo>

        val mBegin = extras.getInt(PHOTO_BEGIN, 0)
        mMaxPick = extras.getInt(EXTRA_MAX, 5)
        if (mAllPhotos == null) {
            val folderName = extras.getString(FOLDER_NAME, "")
            var where = folderName
            if (!folderName.isEmpty()) {
                where = "${MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME}='$folderName'"
            }
            mCursor = contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA),
                    where, null,
                    MediaStore.MediaColumns.DATE_ADDED + " DESC")
        }

        val adapter = ImagesAdapter(supportFragmentManager)
        mViewPager = findViewById<View>(R.id.viewPager) as ViewPager
        mViewPager!!.adapter = adapter
        mViewPager!!.currentItem = mBegin

        mCheckBox = findViewById<View>(R.id.checkbox) as CheckBox
        mViewPager!!.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                updateDisplay(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        mCheckBox!!.setOnClickListener(View.OnClickListener { v ->
            val pos = mViewPager!!.currentItem

            val uri = getImagePath(pos)

            if ((v as CheckBox).isChecked) {
                if (mPickPhotos!!.size >= mMaxPick) {
                    v.isChecked = false
                    val s = "最多只能选择 $mMaxPick 张"
                    Toast.makeText(this@PhotoPickDetailActivity, s, Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }

                addPicked(uri)
            } else {
                removePicked(uri)
            }

            updateDataPickCount()
        })

        updateDisplay(mBegin)
    }

    override fun onDestroy() {
        if (mCursor != null) {
            mCursor!!.close()
            mCursor = null
        }

        super.onDestroy()
    }

    private fun updateDisplay(pos: Int) {
        val uri = getImagePath(pos)
        mCheckBox!!.isChecked = isPicked(uri)
        supportActionBar!!.setTitle(String.format(actionbarTitle, pos + 1, imageCount))
    }

    private fun isPicked(path: String): Boolean {
        for (item in mPickPhotos!!) {
            if (item.path == path) {
                return true
            }
        }

        return false
    }

    private fun addPicked(path: String) {
        if (!isPicked(path)) {
            mPickPhotos!!.add(ImageInfo(path))
        }
    }

    private fun removePicked(path: String) {
        for (i in mPickPhotos!!.indices) {
            if (mPickPhotos!![i].path == path) {
                mPickPhotos!!.removeAt(i)
                return
            }
        }
    }

    override fun onBackPressed() {
        selectAndSend(false)
    }

    private fun selectAndSend(send: Boolean) {
        val intent = Intent()
        intent.putExtra("data", mPickPhotos)
        intent.putExtra("send", send)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_photo_pick_detail, menu)
        mMenuSend = menu.getItem(0)
        updateDataPickCount()

        return true
    }

    private fun updateDataPickCount() {
        val send = String.format("确定(%s/%s)", mPickPhotos!!.size, mMaxPick)
        mMenuSend!!.setTitle(send)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            selectAndSend(false)
            return true
        } else if (id == R.id.action_send) {
            selectAndSend(true)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    internal fun getImagePath(pos: Int): String {
        if (mAllPhotos != null) {
            return mAllPhotos!![pos].path
        } else {
            var path = ""
            if (mCursor!!.moveToPosition(pos)) {
                path = ImageInfo.pathAddPreFix(mCursor!!.getString(1))
            }
            return path
        }
    }

    internal inner class ImagesAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            val fragment = ImagePagerFragment_()
            val bundle = Bundle()

            val path = getImagePath(position)
            bundle.putString("uri", ImageInfo.pathAddPreFix(path))
            fragment.arguments = bundle
            return fragment
        }

        override fun getCount(): Int {
            return imageCount
        }
    }

    companion object {

        val PICK_DATA = "PICK_DATA"
        val ALL_DATA = "ALL_DATA"
        val FOLDER_NAME = "FOLDER_NAME"
        val PHOTO_BEGIN = "PHOTO_BEGIN"
        val EXTRA_MAX = "EXTRA_MAX"
    }

}
