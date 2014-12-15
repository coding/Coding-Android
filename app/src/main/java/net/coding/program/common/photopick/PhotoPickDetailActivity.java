package net.coding.program.common.photopick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import net.coding.program.ImagePagerFragment_;
import net.coding.program.R;

import java.util.ArrayList;

public class PhotoPickDetailActivity extends FragmentActivity {

    public static final String PICK_DATA = "PICK_DATA";
    public static final String ALL_DATA = "ALL_DATA";
    public static final String PHOTO_BEGIN = "PHOTO_BEGIN";
    public static final String EXTRA_MAX = "EXTRA_MAX";

    private ArrayList<PhotoPickActivity.ImageInfo> mPickPhotos;
    private ArrayList<PhotoPickActivity.ImageInfo> mAllPhotos;

    private ViewPager mViewPager;
    private CheckBox mCheckBox;

    private int mMaxPick = 5;
    private MenuItem mMenuSend;
    private final String actionbarTitle = "%d/%d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_pick_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        mPickPhotos = (ArrayList<PhotoPickActivity.ImageInfo>) extras.getSerializable(PICK_DATA);
        mAllPhotos = (ArrayList<PhotoPickActivity.ImageInfo>) extras.getSerializable(ALL_DATA);
        int mBegin = extras.getInt(PHOTO_BEGIN, 0);
        mMaxPick = extras.getInt(EXTRA_MAX, 5);

        ImagesAdapter adapter = new ImagesAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(mBegin);

        mCheckBox = (CheckBox) findViewById(R.id.checkbox);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateDisplay(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = mViewPager.getCurrentItem();
                String uri = mAllPhotos.get(pos).path;

                if (((CheckBox) v).isChecked()) {
                    if (mPickPhotos.size() >= mMaxPick) {
                        ((CheckBox) v).setChecked(false);
                        String s = String.format("最多只能选择%d张", mMaxPick);
                        Toast.makeText(PhotoPickDetailActivity.this, s, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addPicked(uri);
                } else {
                    removePicked(uri);
                }

                updateDataPickCount();
            }
        });

        updateDisplay(mBegin);
    }

    private void updateDisplay(int pos) {
        String uri = mAllPhotos.get(pos).path;
        mCheckBox.setChecked(isPicked(uri));
        getActionBar().setTitle(String.format(actionbarTitle, pos + 1, mAllPhotos.size()));
    }

    private boolean isPicked(String path) {
        for (PhotoPickActivity.ImageInfo item : mPickPhotos) {
            if (item.path.equals(path)) {
                return true;
            }
        }

        return false;
    }

    private void addPicked(String path) {
        if (!isPicked(path)) {
            mPickPhotos.add(new PhotoPickActivity.ImageInfo(path));
        }
    }

    private void removePicked(String path) {
        for (int i = 0; i < mPickPhotos.size(); ++i) {
            if (mPickPhotos.get(i).path.equals(path)) {
                mPickPhotos.remove(i);
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        selectAndSend(false);
    }

    private void selectAndSend(boolean send) {
        Intent intent = new Intent();
        intent.putExtra("data", mPickPhotos);
        intent.putExtra("send", send);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo_pick_detail, menu);
        mMenuSend = menu.getItem(0);
        updateDataPickCount();

        return true;
    }

    private void updateDataPickCount() {
        String send = String.format("发送(%d/%d)", mPickPhotos.size(), mMaxPick);
        mMenuSend.setTitle(send);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            selectAndSend(false);
            return true;
        } else if (id == R.id.action_send) {
            selectAndSend(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class ImagesAdapter extends FragmentStatePagerAdapter {

        ImagesAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ImagePagerFragment_ fragment = new ImagePagerFragment_();
            Bundle bundle = new Bundle();
            bundle.putString("uri", mAllPhotos.get(position).path);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return mAllPhotos.size();
        }
    }

}
