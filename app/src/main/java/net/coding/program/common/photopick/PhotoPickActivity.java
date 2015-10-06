package net.coding.program.common.photopick;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import net.coding.program.common.ui.BaseActivity;
import net.coding.program.R;
import net.coding.program.maopao.MaopaoAddActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;


public class PhotoPickActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_MAX = "EXTRA_MAX";
    public static final String EXTRA_PICKED = "EXTRA_PICKED"; // mPickData
    private static final String RESTORE_FILEURI = "fileUri";
    public static DisplayImageOptions optionsImage = new DisplayImageOptions
            .Builder()
            .showImageOnLoading(R.drawable.ic_default_image)
            .showImageForEmptyUri(R.drawable.image_not_exist)
            .showImageOnFail(R.drawable.image_not_exist)
            .cacheInMemory(true)
            .cacheOnDisk(false)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.EXACTLY)
            .build();
    final int RESULT_PICK = 20;
    final int RESULT_CAMERA = 21;
    private final String allPhotos = "所有图片";
    private final String CameraItem = "CameraItem";
    MenuItem mMenuItem;
    int mFolderId = 0;
    private int mMaxPick = MaopaoAddActivity.PHOTO_MAX_COUNT;
    private LayoutInflater mInflater;
    private TextView mFoldName;
    private View mListViewGroup;
    private ListView mListView;

    //    LinkedHashMap<String, ArrayList<ImageInfo>> mFolders = new LinkedHashMap();
//    ArrayList<String> mFoldersName = new ArrayList<>();
    View.OnClickListener mOnClickFoldName = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mListViewGroup.getVisibility() == View.VISIBLE) {
                hideFolderList();
            } else {
                showFolderList();
            }
        }

    };
    private GridView mGridView;
    private TextView mPreView;
    private ArrayList<ImageInfo> mPickData = new ArrayList<>();
    private FolderAdapter mFolderAdapter;
    GridView.OnItemClickListener mOnPhotoItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(PhotoPickActivity.this, PhotoPickDetailActivity.class);

            intent.putExtra(PhotoPickDetailActivity.PICK_DATA, mPickData);
            intent.putExtra(PhotoPickDetailActivity.EXTRA_MAX, mMaxPick);
            String folderParam = "";
            if (isAllPhotoMode()) {
                // 第一个item是照相机
                intent.putExtra(PhotoPickDetailActivity.PHOTO_BEGIN, position - 1);
            } else {
                intent.putExtra(PhotoPickDetailActivity.PHOTO_BEGIN, position);
                folderParam = mFolderAdapter.getSelect();
            }
            intent.putExtra(PhotoPickDetailActivity.FOLDER_NAME, folderParam);
            startActivityForResult(intent, RESULT_PICK);
        }
    };
    private GridPhotoAdapter photoAdapter;
    private String[] projection = {
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT
    };
    private View.OnClickListener onClickPre = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPickData.size() == 0) {
                return;
            }

            Intent intent = new Intent(PhotoPickActivity.this, PhotoPickDetailActivity.class);
            intent.putExtra(PhotoPickDetailActivity.FOLDER_NAME, mFolderAdapter.getSelect());
            intent.putExtra(PhotoPickDetailActivity.PICK_DATA, mPickData);
            intent.putExtra(PhotoPickDetailActivity.ALL_DATA, mPickData);
            intent.putExtra(PhotoPickDetailActivity.EXTRA_MAX, mMaxPick);
            startActivityForResult(intent, RESULT_PICK);
        }
    };
    private ListView.OnItemClickListener mOnItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mFolderAdapter.setSelect((int) id);
            String folderName = mFolderAdapter.getSelect();
            mFoldName.setText(folderName);
            hideFolderList();

            if (mFolderId != position) {
                getLoaderManager().destroyLoader(mFolderId);
                mFolderId = position;
            }
            getLoaderManager().initLoader(mFolderId, null, PhotoPickActivity.this);
        }
    };
    private Uri fileUri;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_pick);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("图片");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mMaxPick = getIntent().getIntExtra(EXTRA_MAX, 6);
        Object extraPicked = getIntent().getSerializableExtra(EXTRA_PICKED);

        if (extraPicked != null) {
            mPickData = (ArrayList<ImageInfo>) extraPicked;
        }

        mInflater = getLayoutInflater();
        mGridView = (GridView) findViewById(R.id.gridView);
        mListView = (ListView) findViewById(R.id.listView);
        mListViewGroup = findViewById(R.id.listViewParent);
        mListViewGroup.setOnClickListener(mOnClickFoldName);
        mFoldName = (TextView) findViewById(R.id.foldName);
        mFoldName.setText(allPhotos);

        findViewById(R.id.selectFold).setOnClickListener(mOnClickFoldName);

        mPreView = (TextView) findViewById(R.id.preView);
        mPreView.setOnClickListener(onClickPre);

        initListView1();
        initListView2();
    }

    public int getmMaxPick() {
        return mMaxPick;
    }

    public void clickPhotoItem(View v) {
        GridViewCheckTag tag = (GridViewCheckTag) v.getTag();
        if (((CheckBox) v).isChecked()) {
            if (mPickData.size() >= mMaxPick) {
                ((CheckBox) v).setChecked(false);
                String s = String.format("最多只能选择%d张", mMaxPick);
                Toast.makeText(this, s, Toast.LENGTH_LONG).show();
                return;
            }

            addPicked(tag.path);
            tag.iconFore.setVisibility(View.VISIBLE);
        } else {
            removePicked(tag.path);
            tag.iconFore.setVisibility(View.INVISIBLE);
        }
        ((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();

        updatePickCount();
    }

    private void initListView1() {
        final String[] needInfos = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
        };


        LinkedHashMap<String, Integer> mNames = new LinkedHashMap<>();
        LinkedHashMap<String, ImageInfo> mData = new LinkedHashMap<>();
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, needInfos, "", null, MediaStore.MediaColumns.DATE_ADDED + " DESC");

        while (cursor.moveToNext()) {
            String name = cursor.getString(2);
            if (!mNames.containsKey(name)) {
                mNames.put(name, 1);
                ImageInfo imageInfo = new ImageInfo(cursor.getString(1));
                mData.put(name, imageInfo);
            } else {
                int newCount = mNames.get(name) + 1;
                mNames.put(name, newCount);
            }
        }

        ArrayList<ImageInfoExtra> mFolderData = new ArrayList<>();
        if (cursor.moveToFirst()) {
            ImageInfo imageInfo = new ImageInfo(cursor.getString(1));
            int allImagesCount = cursor.getCount();
            mFolderData.add(new ImageInfoExtra(allPhotos, imageInfo, allImagesCount));
        }

        for (String item : mNames.keySet()) {
            ImageInfo info = mData.get(item);
            Integer count = mNames.get(item);
            mFolderData.add(new ImageInfoExtra(item, info, count));
        }
        cursor.close();

        mFolderAdapter = new FolderAdapter(mFolderData);
        mListView.setAdapter(mFolderAdapter);
        mListView.setOnItemClickListener(mOnItemClick);
    }

    private void initListView2() {
        getLoaderManager().initLoader(0, null, this);
    }

    private void showFolderList() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.listview_up);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.listview_fade_in);

        mListView.startAnimation(animation);
        mListViewGroup.startAnimation(fadeIn);
        mListViewGroup.setVisibility(View.VISIBLE);
    }

    private void hideFolderList() {
        Animation animation = AnimationUtils.loadAnimation(PhotoPickActivity.this, R.anim.listview_down);
        Animation fadeOut = AnimationUtils.loadAnimation(PhotoPickActivity.this, R.anim.listview_fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mListViewGroup.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mListView.startAnimation(animation);
        mListViewGroup.startAnimation(fadeOut);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo_pick, menu);
        mMenuItem = menu.getItem(0);
        updatePickCount();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_finish) {
            send();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void send() {
        if (mPickData.isEmpty()) {
            setResult(Activity.RESULT_CANCELED);
        } else {
            Intent intent = new Intent();
            intent.putExtra("data", mPickData);
            setResult(Activity.RESULT_OK, intent);
        }

        finish();
    }

    public void camera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = CameraPhotoUtil.getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, RESULT_CAMERA);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        fileUri = savedInstanceState.getParcelable(RESTORE_FILEURI);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (fileUri != null) {
            outState.putParcelable(RESTORE_FILEURI, fileUri);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_PICK) {
            if (resultCode == RESULT_OK) {
                mPickData = (ArrayList<ImageInfo>) data.getSerializableExtra("data");
                photoAdapter.notifyDataSetChanged();

                boolean send = data.getBooleanExtra("send", false);
                if (send) {
                    send();
                }
            }
        } else if (requestCode == RESULT_CAMERA) {
            if (resultCode == RESULT_OK) {
                ImageInfo itme = new ImageInfo(fileUri.toString());
                mPickData.add(itme);
                send();
            }
        }

        updatePickCount();

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addPicked(String path) {
        if (!isPicked(path)) {
            mPickData.add(new ImageInfo(path));
        }
    }

    public boolean isPicked(String path) {
        for (ImageInfo item : mPickData) {
            if (item.path.equals(path)) {
                return true;
            }
        }

        return false;
    }

    private void removePicked(String path) {
        for (int i = 0; i < mPickData.size(); ++i) {
            if (mPickData.get(i).path.equals(path)) {
                mPickData.remove(i);
                return;
            }
        }
    }

    private void updatePickCount() {
        String format = "完成(%d/%d)";
        mMenuItem.setTitle(String.format(format, mPickData.size(), mMaxPick));

        String formatPreview = "预览(%d/%d)";
        mPreView.setText(String.format(formatPreview, mPickData.size(), mMaxPick));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String where;
        if (!isAllPhotoMode()) {
            String select = ((FolderAdapter) mListView.getAdapter()).getSelect();
            where = String.format("%s='%s'",
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    select
            );
        } else {
            where = "";
        }

        return new CursorLoader(
                this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                where,
                null,
                MediaStore.MediaColumns.DATE_ADDED + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (isAllPhotoMode()) {
            photoAdapter = new AllPhotoAdapter(this, data, false, this);
        } else {
            photoAdapter = new GridPhotoAdapter(this, data, false, this);
        }
        mGridView.setAdapter(photoAdapter);
        mGridView.setOnItemClickListener(mOnPhotoItemClick);
    }

    /*
     * 选择了listview的第一个项，gridview的第一个是照相机
     */
    private boolean isAllPhotoMode() {
        return mFolderId == 0;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        photoAdapter.swapCursor(null);
    }

    static class GridViewCheckTag {
        View iconFore;
        String path = "";

        GridViewCheckTag(View iconFore) {
            this.iconFore = iconFore;
        }
    }
}
