/**
 * ****************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */
package net.coding.program.pickphoto.detail

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import net.coding.program.common.ui.BackActivity
import net.coding.program.common.util.PermissionUtil
import net.coding.program.pickphoto.R
import net.coding.program.pickphoto.R2
import org.androidannotations.annotations.*
import java.util.*

@EActivity(R2.layout.activity_image_pager)
open class ImagePagerActivity : BackActivity() {

    private val SAVE_INSTANCE_INDEX = "SAVE_INSTANCE_INDEX"
    internal lateinit var options: DisplayImageOptions

    @Extra
    @JvmField
    protected final var mPagerPosition: Int = 0
    @Extra
    @JvmField
    protected final var mArrayUri = ArrayList<String>()
    @Extra
    @JvmField
    protected final var isPrivate: Boolean = false
    @Extra
    @JvmField
    protected final var mSingleUri: String? = null
    @Extra
    @JvmField
    protected final var needEdit: Boolean = false

    @ViewById(R2.id.pager)
    protected lateinit var pager: ViewPager

    internal lateinit var adapter: ImagePager

    internal var mDelUrls = ArrayList<String>()

    internal lateinit var mMenuImagePos: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mPagerPosition = savedInstanceState.getInt(SAVE_INSTANCE_INDEX, mPagerPosition)
        }
    }

    @AfterViews
    protected fun initImagePagerActivity() {
        if (needEdit) {
            val supportActionBar = supportActionBar
            supportActionBar!!.setIcon(android.R.color.transparent)

            val v = layoutInflater.inflate(R.layout.image_pager_action_custom, null)
            mMenuImagePos = v.findViewById<View>(R.id.imagePos) as TextView
            supportActionBar.customView = v

            supportActionBar.setDisplayShowCustomEnabled(true)
            pager!!.setBackgroundColor(resources.getColor(R.color.stand_bg))

            pager!!.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                }

                override fun onPageSelected(position: Int) {
                    setPosDisplay(position)
                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })
            setPosDisplay(0)
        } else {
            supportActionBar!!.hide()
            pager!!.setBackgroundColor(resources.getColor(R.color.black))
        }

        if (mSingleUri != null) {
            mArrayUri = ArrayList()
            mArrayUri.add(mSingleUri!!)
            mPagerPosition = 0
        }

        options = DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_default_image)
                .showImageOnFail(R.drawable.ic_default_image)
                .resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .displayer(FadeInBitmapDisplayer(300))
                .build()

        if (!isPrivate) {
            initPager()
        }

    }

    private fun setPosDisplay(position: Int) {
        val pos = String.format("%s/%s", position + 1, mArrayUri.size)
        mMenuImagePos.text = pos
    }

    private fun initPager() {
        adapter = ImagePager(supportFragmentManager)
        pager!!.adapter = adapter
        pager!!.currentItem = mPagerPosition
    }

    public override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putInt(SAVE_INSTANCE_INDEX, pager!!.currentItem)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mPagerPosition = savedInstanceState.getInt(SAVE_INSTANCE_INDEX, mPagerPosition)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (needEdit) {
            val menuInflater = menuInflater
            menuInflater.inflate(R.menu.photo_pager, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        if (mDelUrls.isEmpty()) {
            setResult(Activity.RESULT_CANCELED)
        } else {
            val intent = Intent()
            intent.putExtra("mDelUrls", mDelUrls)
            setResult(Activity.RESULT_OK, intent)
        }

        finish()
    }

    @OptionsItem(R2.id.action_del_maopao)
    protected fun actionRelMaopao() {
        val selectPos = pager!!.currentItem
        val dialog = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle("图片")
                .setMessage("确定删除？")
                .setPositiveButton("确定") { dialog, which ->
                    val s = mArrayUri.removeAt(selectPos)
                    mDelUrls.add(s)
                    if (mArrayUri.isEmpty()) {
                        onBackPressed()
                    } else {
                        setPosDisplay(pager!!.currentItem)
                        adapter.notifyDataSetChanged()
                    }
                }
                .setNegativeButton("取消") { dialog, which -> }
                .show()
    }

    internal inner class ImagePager(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(i: Int): Fragment {
            return ImagePagerFragment_.builder()
                    .uri(mArrayUri[i])
                    .customMenu(false)
                    .build()
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as ImagePagerFragment
            fragment.setData(mArrayUri[position])
            return fragment
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }

        override fun getCount(): Int {
            return mArrayUri.size
        }
    }
}