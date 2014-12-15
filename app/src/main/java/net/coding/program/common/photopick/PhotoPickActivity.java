package net.coding.program.common.photopick;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import net.coding.program.MyApp;
import net.coding.program.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;


public class PhotoPickActivity extends Activity {

    TextView mFoldName;
    View mListViewGroup;
    ListView mListView;
    GridView mGridView;
    LayoutInflater mInflater;

    public static final String EXTRA_MAX = "EXTRA_MAX";
    private int mMaxPick = 5;

    public static DisplayImageOptions optionsImage = new DisplayImageOptions
            .Builder()
            .showImageOnLoading(R.drawable.ic_default_image)
            .showImageForEmptyUri(R.drawable.ic_default_image)
            .showImageOnFail(R.drawable.ic_default_image)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .build();

    private TextView mPreView;

    public static class ImageInfo implements Serializable {
        public String path;

        public ImageInfo(String path) {
            this.path = path;
        }
    }

    LinkedHashMap<String, ArrayList<ImageInfo>> mFolders = new LinkedHashMap<String, ArrayList<ImageInfo>>();
    ArrayList<String> mFoldersData = new ArrayList<String>();

    ArrayList<ImageInfo> mPickData = new ArrayList<ImageInfo>();

    final String allPhotos = "所有图片";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_pick);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("图片");
        actionBar.setDisplayHomeAsUpEnabled(true);
        mMaxPick = getIntent().getIntExtra(EXTRA_MAX, 5);

        mInflater = getLayoutInflater();

        mGridView = (GridView) findViewById(R.id.gridView);
        mListView = (ListView) findViewById(R.id.listView);
        mListViewGroup = findViewById(R.id.listViewParent);
        mListViewGroup.setOnClickListener(mOnClickFoldName);
        mFoldName = (TextView) findViewById(R.id.foldName);
        findViewById(R.id.selectFold).setOnClickListener(mOnClickFoldName);

        String[] projection = {MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME};
        String selection = "";
        String[] selectionArgs = null;
        Cursor mImageExternalCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, MediaStore.MediaColumns.DATE_ADDED + " DESC");
        ArrayList<ImageInfo> allPhoto = new ArrayList<ImageInfo>();
        mFoldersData.add(allPhotos);

        while (mImageExternalCursor.moveToNext()) {
            String s0 = mImageExternalCursor.getString(0);
            String s1 = mImageExternalCursor.getString(1);
            String s2 = mImageExternalCursor.getString(2);

            String s = String.format("%s,%s,%s", s0, s1, s2);
            Log.d("", "sss " + s);
            if (s1.endsWith(".png") || s1.endsWith(".jpg") || s1.endsWith(".PNG") || s1.endsWith(".JPG")) {
                s1 = "file://" + s1;
            }
            ImageInfo imageInfo = new ImageInfo(s1);

            ArrayList<ImageInfo> value = mFolders.get(s2);
            if (value == null) {
                value = new ArrayList<ImageInfo>();
                mFolders.put(s2, value);
                mFoldersData.add(s2);
            }
            allPhoto.add(imageInfo);

            value.add(imageInfo);
        }
        mFolders.put(allPhotos, allPhoto);

        mPhotoAdapter.setData(mFolders.get(mFoldersData.get(0)));
        mListView.setAdapter(mFoldAdapter);
        mListView.setOnItemClickListener(mOnItemClick);

        mGridView.setAdapter(mPhotoAdapter);
        mGridView.setOnItemClickListener(mOnPhotoItemClick);
        // 必须这么刷一下，否则很卡，也许是ImageLoader某个地方的线程写的有问题，当然了，更有可能是我用的的有问题：），先这样吧
        mGridView.post(new Runnable() {
            @Override
            public void run() {
                mPhotoAdapter.notifyDataSetChanged();
            }
        });


        String folderName = mFoldersData.get(0);
        mFoldName.setText(folderName);

        mPreView = (TextView) findViewById(R.id.preView);
        mPreView.setOnClickListener(onClickPre);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        mPhotoAdapter.notifyDataSetChanged();
//    }

    View.OnClickListener onClickPre = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPickData.size() == 0) {
                return;
            }

            Intent intent = new Intent(PhotoPickActivity.this, PhotoPickDetailActivity.class);
            intent.putExtra(PhotoPickDetailActivity.ALL_DATA, mPickData);
            intent.putExtra(PhotoPickDetailActivity.PICK_DATA, mPickData);
            intent.putExtra(PhotoPickDetailActivity.EXTRA_MAX, mMaxPick);
            startActivityForResult(intent, RESULT_PICK);
        }
    };

    ListView.OnItemClickListener mOnItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String folderName = mFoldersData.get((int) id);
            mPhotoAdapter.setData(mFolders.get(folderName));
            mPhotoAdapter.notifyDataSetChanged();
            mFoldName.setText(folderName);
            mFoldAdapter.notifyDataSetChanged();
            hideFolderList();
        }
    };

    GridView.OnItemClickListener mOnPhotoItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(PhotoPickActivity.this, PhotoPickDetailActivity.class);
            intent.putExtra(PhotoPickDetailActivity.ALL_DATA, mPhotoAdapter.getData());
            intent.putExtra(PhotoPickDetailActivity.PICK_DATA, mPickData);
            intent.putExtra(PhotoPickDetailActivity.EXTRA_MAX, mMaxPick);
            intent.putExtra(PhotoPickDetailActivity.PHOTO_BEGIN, (int) id);
            startActivityForResult(intent, RESULT_PICK);
        }
    };

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

    MenuItem mMenuItem;

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

    final int RESULT_PICK = 20;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_PICK) {
            if (resultCode == RESULT_OK) {
                ArrayList<ImageInfo> pickArray = (ArrayList<ImageInfo>) data.getSerializableExtra("data");
                mPickData = pickArray;

                mPhotoAdapter.notifyDataSetChanged();

                boolean send = data.getBooleanExtra("send", false);
                if (send) {
                    send();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean isPicked(String path) {
        for (ImageInfo item : mPickData) {
            if (item.path.equals(path)) {
                return true;
            }
        }

        return false;
    }

    private void addPicked(String path) {
        if (!isPicked(path)) {
            mPickData.add(new ImageInfo(path));
        }
    }

    private void removePicked(String path) {
        for (int i = 0; i < mPickData.size(); ++i) {
            if (mPickData.get(i).path.equals(path)) {
                mPickData.remove(i);
                return;
            }
        }
    }

    class GridAdapter extends BaseAdapter {

        ArrayList<ImageInfo> mData = new ArrayList<ImageInfo>();

        public void setData(ArrayList<ImageInfo> data) {
            mData = data;
        }

        public ArrayList<ImageInfo> getData() {
            return mData;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GridViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.photopick_gridlist_item, parent, false);
                convertView.getLayoutParams().height = MyApp.sWidthPix / 3;

                holder = new GridViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.iconFore = (ImageView) convertView.findViewById(R.id.iconFore);
                holder.check = (CheckBox) convertView.findViewById(R.id.check);
                GridViewCheckTag checkTag = new GridViewCheckTag(holder.iconFore);
                holder.check.setTag(checkTag);
                holder.check.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GridViewCheckTag tag = (GridViewCheckTag) v.getTag();
                        if (((CheckBox) v).isChecked()) {
                            if (mPickData.size() >= mMaxPick) {
                                ((CheckBox) v).setChecked(false);
                                String s = String.format("最多只能选择%d张", mMaxPick);
                                Toast.makeText(PhotoPickActivity.this, s, Toast.LENGTH_LONG).show();
                                return;
                            }

                            addPicked(tag.path);
                            tag.iconFore.setVisibility(View.VISIBLE);
                        } else {
                            removePicked(tag.path);
                            tag.iconFore.setVisibility(View.INVISIBLE);
                        }
                        mFoldAdapter.notifyDataSetChanged();

                        updatePickCount();
                    }
                });
                convertView.setTag(holder);
            } else {
                holder = (GridViewHolder) convertView.getTag();
            }

            ImageInfo data = (ImageInfo) getItem(position);
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(data.path, holder.icon, optionsImage);

            ((GridViewCheckTag) holder.check.getTag()).path = data.path;

            boolean picked = isPicked(data.path);
            holder.check.setChecked(picked);
            holder.iconFore.setVisibility(picked ? View.VISIBLE : View.INVISIBLE);

            return convertView;
        }
    }

    ;

    private void updatePickCount() {
        String format = "完成(%d/%d)";
        mMenuItem.setTitle(String.format(format, mPickData.size(), mMaxPick));

        String formatPreview = "预览(%d/%d)";
        mPreView.setText(String.format(formatPreview, mPickData.size(), mMaxPick));
    }

    GridAdapter mPhotoAdapter = new GridAdapter();

    static class GridViewCheckTag {
        View iconFore;
        String path = "";

        GridViewCheckTag(View iconFore) {
            this.iconFore = iconFore;
        }
    }

    static class GridViewHolder {
        ImageView icon;
        ImageView iconFore;
        CheckBox check;
    }

    BaseAdapter mFoldAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mFoldersData.size();
        }

        @Override
        public Object getItem(int position) {
            return mFoldersData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.photopick_list_item, parent, false);
                holder = new ViewHolder();
                holder.foldIcon = (ImageView) convertView.findViewById(R.id.foldIcon);
                holder.foldName = (TextView) convertView.findViewById(R.id.foldName);
                holder.photoCount = (TextView) convertView.findViewById(R.id.photoCount);
                holder.check = convertView.findViewById(R.id.check);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String name = (String) getItem(position);
            ArrayList<ImageInfo> imageInfos = mFolders.get(name);
            String uri = imageInfos.get(0).path;
            int count = imageInfos.size();

            holder.foldName.setText(name);
            holder.photoCount.setText(String.format("%d张", count));
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(uri, holder.foldIcon, optionsImage);

            if (mFoldName.getText().toString().equals(name)) {
                holder.check.setVisibility(View.VISIBLE);
            } else {
                holder.check.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

    };

    static class ViewHolder {
        ImageView foldIcon;
        TextView foldName;
        TextView photoCount;
        View check;
    }
}
