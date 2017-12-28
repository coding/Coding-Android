package net.coding.program.pickphoto

import net.coding.program.common.ImageInfo

/**
 * Created by chenchao on 15/5/6.
 */
class ImageInfoExtra(name: String, val mImageInfo: ImageInfo ?, countParam: Int) {
    var count = 0
    private var mName = ""

    val path: String
        get() = mImageInfo!!.path

    init {
        mName = name
        count = countParam
    }

    fun getmName(): String {
        return mName
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as ImageInfoExtra?

        return if (count != that!!.count) false else mImageInfo == that!!.mImageInfo

    }

    override fun hashCode(): Int {
        var result = mImageInfo!!.hashCode()
        result = 31 * result + count
        return result
    }
}
