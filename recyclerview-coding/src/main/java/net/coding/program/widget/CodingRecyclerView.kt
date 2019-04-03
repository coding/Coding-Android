package net.coding.program.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout

class CodingRecyclerView : FrameLayout {

    constructor(context: Context?) : super(context) {
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private fun initView() {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.coding_recyclerview, this, true)
    }

//    fun init(val build: ) {
//
//    }

    data class Param(val emptyMessage: String = "", val emptyMessageRes: Int = 0)


}