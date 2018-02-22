package net.coding.program.pickphoto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import net.coding.program.common.ImageInfo
import java.util.*

/**
 * Created by chenchao on 15/5/6.
 */
class FolderAdapter(mFolderData: ArrayList<ImageInfoExtra>) : BaseAdapter() {

    internal var mFolderData = ArrayList<ImageInfoExtra>()
    var select = ""
        private set

    init {
        this.mFolderData = mFolderData
    }

    fun setSelect(pos: Int) {
        if (pos >= count) {
            return
        }

        select = mFolderData[pos].getmName()
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return mFolderData.size
    }

    override fun getItem(position: Int): Any {
        return mFolderData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val mInflater = LayoutInflater.from(parent.context)
        val holder: ViewHolder
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.photopick_list_item, parent, false)
            holder = ViewHolder()
            holder.foldIcon = convertView!!.findViewById<View>(R.id.foldIcon) as ImageView
            holder.foldName = convertView.findViewById<View>(R.id.foldName) as TextView
            holder.photoCount = convertView.findViewById<View>(R.id.photoCount) as TextView
            holder.check = convertView.findViewById(R.id.check)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        val data = mFolderData[position]
        val uri = data.path
        val count = data.count

        holder.foldName!!.text = data.getmName()
        holder.photoCount!!.text = String.format("%s张", count)

        ImageLoader.getInstance().displayImage(ImageInfo.pathAddPreFix(uri), holder.foldIcon!!,
                PhotoPickActivity.optionsImage)

        if (data.getmName() == select) {
            holder.check!!.visibility = View.VISIBLE
        } else {
            holder.check!!.visibility = View.INVISIBLE
        }

        return convertView
    }

    internal class ViewHolder {
        var foldIcon: ImageView? = null
        var foldName: TextView? = null
        var photoCount: TextView? = null
        var check: View? = null
    }
}
