package net.coding.program.pickphoto

import android.content.Context
import android.database.Cursor
import android.view.View
import android.view.ViewGroup

/**
 * Created by chenchao on 15/5/6.
 * 第一个item是照相机
 */
class AllPhotoAdapter(context: Context, c: Cursor, autoRequery: Boolean, activity: PhotoPickActivity) : GridPhotoAdapter(context, c, autoRequery, activity) {

    private val SCREEN_WIDTH: Int

    init {
        SCREEN_WIDTH = context.resources.displayMetrics.widthPixels
    }

    override fun getCount(): Int = super.getCount() + 1

    override fun getItem(position: Int): Any {
        return if (position > 0) {
            super.getItem(position - 1)
        } else {
            super.getItem(position)
        }
    }

    override fun getItemId(position: Int): Long {
        return if (position > 0) {
            super.getItemId(position - 1)
        } else {
            -1
        }
    }

    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
        return if (position > 0) {
            super.getDropDownView(position - 1, convertView, parent)
        } else {
            getView(position, convertView, parent)
        }
    }

    override fun getViewTypeCount(): Int = 2

    override fun getItemViewType(position: Int): Int = if (position == 0) 0 else 1

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (position > 0) {
            return super.getView(position - 1, convertView, parent)
        } else {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.photopick_gridlist_item_camera2, parent, false)
                convertView!!.layoutParams.height = SCREEN_WIDTH / 3
                convertView.setOnClickListener { mActivity.camera() }
            }

            return convertView
        }
    }
}
