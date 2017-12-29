package net.coding.program.pickphoto

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CursorAdapter
import android.widget.ImageView

import com.nostra13.universalimageloader.core.ImageLoader

import net.coding.program.common.ImageInfo

/**
 * Created by chenchao on 15/5/6.
 *
 */
open class GridPhotoAdapter(context: Context, c: Cursor, autoRequery: Boolean, var mActivity: PhotoPickActivity) : CursorAdapter(context, c, autoRequery) {

    val itemWidth: Int
    var mInflater: LayoutInflater

    init {
        mInflater = LayoutInflater.from(context)
        val spacePix = context.resources.getDimensionPixelSize(R.dimen.pickimage_gridlist_item_space)
        val screenWidth = context.resources.displayMetrics.widthPixels
        itemWidth = (screenWidth - spacePix * 4) / 3
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val convertView = mInflater.inflate(R.layout.photopick_gridlist_item, parent, false)
        val layoutParams = convertView.layoutParams
        layoutParams.height = itemWidth
        layoutParams.width = itemWidth
        convertView.layoutParams = layoutParams


        val holder = GridViewHolder()
        holder.icon = convertView.findViewById<View>(R.id.icon) as ImageView
        val viewIconFore = convertView.findViewById<View>(R.id.iconFore)
        holder.iconFore = viewIconFore as ImageView
        holder.check = convertView.findViewById<View>(R.id.check) as CheckBox
        val checkTag = PhotoPickActivity.GridViewCheckTag(viewIconFore)
        holder.check!!.tag = checkTag
        holder.check!!.setOnClickListener { mActivity.clickPhotoItem(it) }
        convertView.tag = holder

        val iconParam = holder.icon!!.layoutParams
        iconParam.width = itemWidth
        iconParam.height = itemWidth
        holder.icon!!.layoutParams = iconParam

        return convertView
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val holder: GridViewHolder
        holder = view.tag as GridViewHolder

        val imageLoader = ImageLoader.getInstance()

        val path = ImageInfo.pathAddPreFix(cursor.getString(1))
        imageLoader.displayImage(path, holder.icon!!, PhotoPickActivity.optionsImage)

        (holder.check!!.tag as PhotoPickActivity.GridViewCheckTag).path = path

        val picked = mActivity.isPicked(path)
        holder.check!!.isChecked = picked
        holder.iconFore!!.visibility = if (picked) View.VISIBLE else View.INVISIBLE
    }

    internal class GridViewHolder {
        var icon: ImageView? = null
        var iconFore: ImageView? = null
        var check: CheckBox? = null
    }
}
