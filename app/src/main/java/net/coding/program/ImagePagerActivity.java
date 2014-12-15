/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package net.coding.program;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import net.coding.program.common.CustomDialog;
import net.coding.program.common.umeng.UmengFragmentActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

@EActivity(R.layout.activity_image_pager)
public class ImagePagerActivity extends UmengFragmentActivity {

    private static final String STATE_POSITION = "STATE_POSITION";

    DisplayImageOptions options;

    @ViewById
    ViewPager pager;

    @Extra
    int mPagerPosition;

    @Extra
    ArrayList<String> mArrayUri;

    @Extra
    boolean isPrivate;

    @Extra
    String mSingleUri;

    @Extra
    boolean needEdit;

    ImagePager adapter;

    ArrayList<String> mDelUrls = new ArrayList<String>();

    @AfterViews
    void init() {
        if (needEdit) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setIcon(android.R.color.transparent);
        } else {
            getActionBar().hide();
        }

        if (mSingleUri != null) {
            mArrayUri = new ArrayList<String>();
            mArrayUri.add(mSingleUri);
            mPagerPosition = 0;
        }

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_default_image)
                .showImageOnFail(R.drawable.ic_default_image)
                .resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        if (isPrivate) {

        } else {
            initPager();
        }
    }

    private void initPager() {
        adapter = new ImagePager(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setCurrentItem(mPagerPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (needEdit) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.photo_pager, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (mDelUrls.isEmpty()) {
            setResult(RESULT_CANCELED);
        } else {
            Intent intent = new Intent();
            intent.putExtra("mDelUrls", mDelUrls);
            setResult(RESULT_OK, intent);
        }

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_del_maopao:
                final int selectPos = pager.getCurrentItem();
                AlertDialog dialog = new AlertDialog.Builder(this).setTitle("确定删除？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String s = mArrayUri.remove(selectPos);
                                mDelUrls.add(s);
                                if (mArrayUri.isEmpty()) {
                                    onBackPressed();
                                } else {
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                CustomDialog.dialogTitleLineColor(this, dialog);

                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_POSITION, pager.getCurrentItem());
    }

    class ImagePager extends FragmentPagerAdapter {

        public ImagePager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            ImagePagerFragment_ fragment = new ImagePagerFragment_();
            Bundle bundle = new Bundle();
            bundle.putString("uri", mArrayUri.get(i));
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImagePagerFragment fragment = (ImagePagerFragment) super.instantiateItem(container, position);
            fragment.setData(mArrayUri.get(position));
            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mArrayUri.size();
        }
    }

    ;
}