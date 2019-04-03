package net.coding.program.pickphoto.detail

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.lzyzsd.circleprogress.DonutProgress
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.FileAsyncHttpResponseHandler
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.assist.ImageSize
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import cz.msebera.android.httpclient.Header
import net.coding.program.common.Global
import net.coding.program.common.ImageInfo
import net.coding.program.common.model.AttachmentFileObject
import net.coding.program.common.network.MyAsyncHttpClient
import net.coding.program.common.ui.BaseFragment
import net.coding.program.pickphoto.R
import net.coding.program.pickphoto.R2
import net.coding.program.route.BlankViewDisplay
import org.androidannotations.annotations.*
import org.json.JSONException
import org.json.JSONObject
import pl.droidsonroids.gif.GifImageView
import java.io.File
import java.util.*

/**
 * Created by chaochen on 2014-9-7.
 * 图片显示控件
 */
@EFragment(R2.layout.activity_image_pager_item)
open class ImagePagerFragment : BaseFragment() {

    @ViewById
    protected lateinit var circleLoading: DonutProgress
    @ViewById
    protected lateinit var imageLoadFail: View
    @ViewById
    protected lateinit var rootLayout: ViewGroup
    @ViewById
    protected lateinit var blankLayout: View

    internal var image: View? = null
    internal var picCache: HashMap<String, AttachmentFileObject>? = null

    internal var mFile: File? = null

    @FragmentArg
    @JvmField
    protected final var uri: String? = null
    @FragmentArg
    @JvmField
    protected final var fileId: String? = null
    @FragmentArg
    @JvmField
    protected final var mProjectObjectId: Int = 0

    // 是否允许使用自己的菜单
    @FragmentArg
    @JvmField
    protected final var customMenu = true

    private val URL_FILES = ""
    private var client: AsyncHttpClient? = null

    fun setData(uriString: String) {
        uri = uriString
    }

    fun setData(fileId: String, mProjectObjectId: Int) {
        this.fileId = fileId
        this.mProjectObjectId = mProjectObjectId
    }

    @AfterViews
    protected fun init() {
        setHasOptionsMenu(customMenu)

        circleLoading!!.visibility = View.INVISIBLE
        if (uri != null) {
            showPhoto()
        }
    }

    private fun getPhotoFromNetwork() {
        getNetwork(URL_FILES, URL_FILES)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_empty, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Click
    protected fun rootLayout() {
        activity?.onBackPressed()
    }

    override fun onDestroyView() {
        if (image is GifImageView) {
            (image as GifImageView).setImageURI(null)
        }

        super.onDestroyView()
    }

    private fun showPhoto() {
        if (!isAdded) {
            return
        }

        val size = ImageSize(10000, 10000)
        imageLoad.imageLoader.loadImage(uri, size, optionsImage, object : SimpleImageLoadingListener() {

            override fun onLoadingStarted(imageUri: String?, view: View?) {
                circleLoading!!.visibility = View.VISIBLE
            }

            override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
                if (!isAdded) {
                    return
                }

                circleLoading!!.visibility = View.GONE
                imageLoadFail!!.visibility = View.VISIBLE
            }

            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                if (!isAdded) {
                    return
                }

                circleLoading!!.visibility = View.GONE

                val file: File
                if (ImageInfo.isLocalFile(uri)) {
                    file = ImageInfo.getLocalFile(uri)
                } else {
                    file = imageLoad.imageLoader.diskCache.get(imageUri)
                }
                if (Global.isGifByFile(file)) {
                    image = activity!!.layoutInflater.inflate(R.layout.imageview_gif, rootLayout, false)
                    rootLayout!!.addView(image)
                    image?.setOnClickListener { activity?.onBackPressed() }
                } else {
                    val photoView = activity!!.layoutInflater.inflate(R.layout.imageview_touch, rootLayout, false) as SubsamplingScaleImageView
                    photoView.orientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF
                    image = photoView
                    rootLayout!!.addView(image)
                    photoView.setOnClickListener { v -> activity?.onBackPressed() }
                }

                image?.setOnLongClickListener { v ->
                    AlertDialog.Builder(activity!!, R.style.MyAlertDialogStyle)
                            .setItems(arrayOf("保存到手机")) { dialog, which ->
                                if (which == 0) {
                                    if (client == null) {
                                        client = MyAsyncHttpClient.createClient(activity)
                                        client!!.get(activity, imageUri, object : FileAsyncHttpResponseHandler(mFile) {

                                            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable?, file1: File?) {
                                                if (!isResumed) {
                                                    return
                                                }
                                                client = null
                                                showButtomToast("保存失败")
                                            }

                                            override fun onSuccess(statusCode: Int, headers: Array<Header>?, file1: File?) {
                                                if (!isResumed) {
                                                    return
                                                }
                                                client = null
                                                try {
                                                    MediaStore.Images.Media.insertImage(context?.contentResolver, mFile?.path ?: "", mFile?.name ?: "", "from coding")
                                                    showButtomToast("图片已保存到本机，可以在系统相册中查看")
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }

                                                //                                                        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file1)));/**/
                                            }
                                        })
                                    }

                                }
                            }
                            .show()

                    true
                }

                try {
                    if (image is GifImageView) {
                        val uri1 = Uri.fromFile(file)
                        (image as GifImageView).setImageURI(uri1)
                    } else if (image is SubsamplingScaleImageView) {
                        val scaleImageView = this@ImagePagerFragment.image as SubsamplingScaleImageView
                        scaleImageView.setImage(ImageSource.uri(file.absolutePath))
                    }
                } catch (e: Exception) {
                    Global.errorLog(e)
                }

            }
        }
        ) { imageUri, view, current, total ->
            if (isAdded) {
                val progress = current * 100 / total
                circleLoading!!.progress = progress
            }
        }

        mFile = File(context!!.cacheDir, uri!!.replace(".*/(.*?)".toRegex(), "$1"))
        if (mFile?.exists() == true) {
            mFile?.delete()
        }
    }

    @Throws(JSONException::class)
    override fun parseJson(code: Int, response: JSONObject, tag: String, pos: Int, data: Any) {
        if (tag == URL_FILES) {
            if (code == 0) {
                setHasOptionsMenu(false)
                activity?.invalidateOptionsMenu()

                val file = response.getJSONObject("data").getJSONObject("file")
                val mFileObject = AttachmentFileObject(file)
                if (picCache != null) {
                    picCache!!.put(mFileObject.file_id, mFileObject)
                }
                uri = mFileObject.preview
                showPhoto()
            } else {
                setHasOptionsMenu(true)
                activity!!.invalidateOptionsMenu()
                showErrorMsg(code, response)
                if (code == HTTP_CODE_FILE_NOT_EXIST) {
                    BlankViewDisplay.setBlank(0, this, true, blankLayout, null)
                } else {
                    BlankViewDisplay.setBlank(0, this, false, blankLayout) { v -> getPhotoFromNetwork() }
                }
            }
        }
    }

    override fun onDestroy() {
        if (client != null) {
            client!!.cancelRequests(activity, true)
            client = null
        }

        super.onDestroy()
    }

    companion object {

        val HTTP_CODE_FILE_NOT_EXIST = 1304
        var optionsImage = DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.image_not_exist)
                .showImageOnFail(R.drawable.image_not_exist)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .cacheInMemory(false)
                .considerExifParams(true)
                .imageScaleType(ImageScaleType.NONE_SAFE)
                .build()
    }
}
