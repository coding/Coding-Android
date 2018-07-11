package net.coding.program.maopao;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ImageInfo;
import net.coding.program.common.ListModify;
import net.coding.program.common.PhoneType;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.StartActivity;
import net.coding.program.common.WeakRefHander;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.Maopao;
import net.coding.program.common.module.maopao.LocationObject;
import net.coding.program.common.module.maopao.MaopaoDraft;
import net.coding.program.common.module.maopao.PhotoData;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.util.FileUtil;
import net.coding.program.common.util.PermissionUtil;
import net.coding.program.common.widget.input.MaopaoInputHelp;
import net.coding.program.common.widget.input.SimpleTextWatcher;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.maopao.item.LocationCoord;
import net.coding.program.pickphoto.PhotoPickActivity;
import net.coding.program.pickphoto.detail.ImagePagerActivity_;
import net.coding.program.pickphoto.detail.ImagePagerFragment;
import net.coding.program.subject.SubjectNewActivity_;
import net.coding.program.third.EmojiFilter;
import net.coding.program.util.TextWatcherAt;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

@EActivity(R.layout.activity_maopao_add)
public class MaopaoAddActivity extends BackActivity implements StartActivity {

    public static final int RESULT_REQUEST_FOLLOW = 1002;
    public static final int RESULT_REQUEST_TOPIC = 1008;
    public static final int RESULT_REQUEST_PICK_PHOTO = 1003;
    public static final int RESULT_REQUEST_PHOTO = 1005;
    public static final int RESULT_REQUEST_LOCATION = 1006;
    public static final int RESULT_REQUEST_IMAGE = 1007;
    final String sendUrl = Global.HOST_API + "/tweet";
    final String HOST_IMAGE = Global.HOST_API + "/tweet/insert_image";
    String mIntentExtraString = null;

    @ViewById
    GridView gridView;
    @ViewById
    TextView locationText;

    @InstanceState
    LocationObject currentLocation = LocationObject.undefined();

    ImageSize mSize;
    PhotoOperate photoOperate = new PhotoOperate(this);

    MaopaoInputHelp input;

    EditText message;
    ArrayList<PhotoData> mData = new ArrayList<>();
    android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            uploadMaopao();
        }
    };
    BaseAdapter adapter = new BaseAdapter() {

        ArrayList<ViewHolder> holderList = new ArrayList<>();

        public int getCount() {
            return mData.size() + 1;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                holder.image = (ImageView) mInflater.inflate(R.layout.image_make_maopao, parent, false);
                holderList.add(holder);
                holder.image.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position == getCount() - 1) {
                if (getCount() == (Global.PHOTO_MAX_COUNT + 1)) {
                    holder.image.setVisibility(View.INVISIBLE);

                } else {
                    holder.image.setVisibility(View.VISIBLE);
                    holder.image.setImageResource(R.drawable.make_maopao_add);
                    holder.uri = "";
                }

            } else {
                holder.image.setVisibility(View.VISIBLE);
                PhotoData photoData = mData.get(position);
                Uri data = photoData.uri;
                holder.uri = data.toString();

                ImageLoader.getInstance().loadImage(data.toString(), mSize, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        for (ViewHolder item : holderList) {
                            if (item.uri.equals(imageUri)) {
                                item.image.setImageBitmap(loadedImage);
                            }
                        }
                    }
                });
            }

            return holder.image;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            gridView.setVisibility(getCount() > 1 ? View.VISIBLE : View.GONE);
        }

        class ViewHolder {
            ImageView image;
            String uri = "";
        }

    };

    private Uri fileUri = null;
    private MenuItem mMenuAdd;
    private boolean mFirstFocus = true;

    private static String ensureLength(String src, int maxLength) {
        if (TextUtils.isEmpty(src)) return "";
        if (src.length() <= maxLength) return src;
        if (maxLength < 1) throw new IllegalArgumentException("maxLength");
        return src.substring(0, maxLength - 1) + "…";
    }

    @AfterViews
    protected final void initMaopaoAddActivity() {
        int px = (int) getResources().getDimension(R.dimen.image_add_maopao_width);
        mSize = new ImageSize(px, px);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        input = new MaopaoInputHelp(findViewById(R.id.rootLayout));
        message = input.editText;

        if (mIntentExtraString != null) {
            message.setText(mIntentExtraString);
        }

        gridView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == mData.size()) {
                startPhotoPickActivity();

            } else {
                Intent intent = new Intent(MaopaoAddActivity.this, ImagePagerActivity_.class);
                ArrayList<String> arrayUri = new ArrayList<>();
                for (PhotoData item : mData) {
                    arrayUri.add(item.uri.toString());
                }
                intent.putExtra("mArrayUri", arrayUri);
                intent.putExtra("mPagerPosition", position);
                intent.putExtra("needEdit", true);
                startActivityForResult(intent, RESULT_REQUEST_IMAGE);
            }
        });

        gridView.setOnTouchListener((v, event) -> {
            Global.popSoftkeyboard(MaopaoAddActivity.this, message, false);
            return false;
        });

        message.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateAddButton();
            }
        });

        MaopaoDraft draft = AccountInfo.loadMaopaoDraft(this);
        if (!draft.isEmpty()) {
            message.setText(draft.getInput());
            mData = draft.getPhotos();
            adapter.notifyDataSetChanged();
            currentLocation = draft.getLocation();
            updateLocationText();
        }

        locationText.setText(currentLocation.name);

        setPopTopicIconShow();

        message.setOnClickListener(v -> input.popKeyboard());

        message.setOnFocusChangeListener((v, hasFocus) -> {
            if (mFirstFocus && hasFocus) {
                mFirstFocus = false;
                input.popKeyboard();
            }
        });

        WeakRefHander hander = new WeakRefHander(msg -> {
            if (MaopaoAddActivity.this.isFinishing()) {
                return false;
            }

            input.popKeyboard();
            return false;
        });
        hander.start(0, 500);
    }

    private void setPopTopicIconShow() {
        int icon = R.drawable.pop_topic;
        ((ImageView) findViewById(R.id.popTopic)).setImageResource(icon);
    }

    @SuppressLint("CheckResult")
    private void startPhotoPickActivity() {
        new RxPermissions(this)
                .request(PermissionUtil.STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        int count = Global.PHOTO_MAX_COUNT - mData.size();
                        if (count <= 0) {
                            showButtomToast(String.format("最多能添加%s张图片", Global.PHOTO_MAX_COUNT));
                        } else {
                            Intent intent = new Intent(MaopaoAddActivity.this, PhotoPickActivity.class);
                            intent.putExtra(PhotoPickActivity.Companion.getEXTRA_MAX(), Global.PHOTO_MAX_COUNT);

                            ArrayList<ImageInfo> pickImages = new ArrayList<>();
                            for (PhotoData item : mData) {
                                pickImages.add(item.mImageinfo);
                            }
                            intent.putExtra(PhotoPickActivity.Companion.getEXTRA_PICKED(), pickImages);
                            startActivityForResult(intent, RESULT_REQUEST_PICK_PHOTO);
                        }
                    }
                });
    }

    private void updateAddButton() {
        enableSendButton(!Global.isEmptyContainSpace(message) ||
                mData.size() > 0);
    }

    @Override
    protected void onStop() {
        MaopaoDraft draft = new MaopaoDraft(message.getText().toString(), mData, currentLocation);
        AccountInfo.saveMaopaoDraft(this, draft);

        input.closeEnterPanel();

        super.onStop();
    }

    private void enableSendButton(boolean enable) {
        if (mMenuAdd == null) {
            return;
        }

        if (enable) {
            mMenuAdd.setIcon(R.drawable.ic_menu_ok);
            mMenuAdd.setEnabled(true);
        } else {
            mMenuAdd.setIcon(R.drawable.ic_menu_ok_unable);
            mMenuAdd.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_maopao, menu);

        mMenuAdd = menu.findItem(R.id.action_add);
        updateAddButton();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_PICK_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    mData.clear();

                    @SuppressWarnings("unchecked")
                    ArrayList<ImageInfo> pickPhots = (ArrayList<ImageInfo>) data.getSerializableExtra("data");

                    List<String> photos = new ArrayList<>();
                    for (ImageInfo item : pickPhots) {
                        photos.add(item.getPath());
                    }

                    List<File> zipPhotos = new ArrayList<>();
                    List<ImageInfo> oldPhotos = new ArrayList<>();
                    Luban.with(MaopaoAddActivity.this)
                            .load(photos)                                   // 传人要压缩的图片列表
                            .ignoreBy(300)                                  // 小于 300k 的图片不压缩
                            .setCompressListener(new OnCompressListener() { //设置回调

                                int zipCount = 0;

                                @Override
                                public void onStart() {
                                }

                                @Override
                                public void onSuccess(File file) {
                                    zipPhotos.add(file);
                                    oldPhotos.add(pickPhots.get(zipCount));

                                    zipCount++;

                                    if (zipCount >= photos.size()) {
                                        for (int i = 0; i < zipPhotos.size(); ++i) {
                                            File item = zipPhotos.get(i);
                                            ImageInfo oldInfo = oldPhotos.get(i);
                                            mData.add(new PhotoData(item, oldInfo));
                                        }

                                        updateAddButton();
                                    }
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    zipCount++;
                                }

                            }).launch();    //启动压缩

                } catch (Exception e) {
                    showMiddleToast("缩放图片失败");
                    Global.errorLog(e);
                }

            }
        } else if (requestCode == RESULT_REQUEST_PHOTO) {
            if (resultCode == RESULT_OK) {
                try {
                    ImageInfo imageInfo = new ImageInfo(fileUri.getPath());
                    File outputFile = photoOperate.scal(fileUri);
                    mData.add(new PhotoData(outputFile, imageInfo));
                    adapter.notifyDataSetChanged();

                } catch (Exception e) {
                    showMiddleToast("缩放图片失败");
                    Global.errorLog(e);
                }
            }
        } else if (requestCode == RESULT_REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> delUris = data.getStringArrayListExtra("mDelUrls");
                for (String item : delUris) {
                    for (int i = 0; i < mData.size(); ++i) {
                        if (mData.get(i).uri.toString().equals(item)) {
                            mData.remove(i);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        } else if (requestCode == RESULT_REQUEST_FOLLOW) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra("name");
                input.insertText("@" + name);
            }
        } else if (requestCode == RESULT_REQUEST_TOPIC) {
            if (resultCode == RESULT_OK) {
                String topicName = data.getStringExtra("topic_name");
                if (!TextUtils.isEmpty(topicName) && message != null) {
//                    message.setText(topicName);
//                    message.setSelection(topicName.length());

                    message.getEditableText().insert(0, topicName);
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        updateAddButton();
    }

    @Override
    public void onBackPressed() {
        if (message.getText().toString().isEmpty() && adapter.getCount() <= 1) {
            finishAndHideKeyboard();
        } else {
            showDialog("冒泡", "保存为草稿？", (dialog, which) -> finishAndHideKeyboard(),
                    (dialog, which) -> finishWithoutSave(),
                    "保存",
                    "不保存"
            );
        }
    }

    private void finishAndHideKeyboard() {
        message.postDelayed(() -> {
            Global.popSoftkeyboard(MaopaoAddActivity.this, message, false);
            finish();
        }, 50);
    }

    @OptionsItem
    void action_add() {
        showProgressBar(true, "正在发表冒泡...");
        uploadMaopao();
    }

    void uploadMaopao() {
        for (PhotoData item : mData) {
            if (item.serviceUri.isEmpty()) {
                uploadImage(item.uri);
                return;
            }
        }

        uploadText();
    }

    void uploadImage(Uri uri) {
        RequestParams requestParams = new RequestParams();
        try {
            File file = new File(FileUtil.getPath(this, uri));
            requestParams.put("tweetImg", file);
            postNetwork(HOST_IMAGE, requestParams, HOST_IMAGE, -1, uri.toString());

        } catch (Exception e) {
            Global.errorLog(e);
            showProgressBar(false);
        }
    }

    void uploadText() {
        RequestParams params = new RequestParams();
        String content = message.getText().toString();

        if (EmojiFilter.containsEmoji(this, content)) {
            showProgressBar(false);
            return;
        }

        String photoTemplate = "\n![图片](%s)";
        for (PhotoData item : mData) {
            content += String.format(photoTemplate, item.serviceUri);
        }

        params.put("content", content);
        params.put("device", Build.MODEL);
        if (currentLocation != null && !TextUtils.isEmpty(locationText.getText())) {
            String locationName = currentLocation.type == LocationObject.Type.City ?
                    currentLocation.name : currentLocation.city + MaopaoLocationArea.MAOPAO_LOCATION_DIVIDE + currentLocation.name;
            params.put("location", ensureLength(locationName, 32));
            params.put("coord", ensureLength(LocationCoord.from(currentLocation).toString(), 32));
            params.put("address", ensureLength(currentLocation.address, 64));
        }
        postNetwork(sendUrl, params, sendUrl);
    }

    private void finishWithoutSave() {
        // 清空输入的数据，因为在onDestroy时如果检测到有数据会保存
        input.clearContent();
        mData.clear();
        finishAndHideKeyboard();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(sendUrl)) {
            showProgressBar(false);
            if (code == 0) {
                umengEvent(UmengEvent.MAOPAO, "发冒泡");

                JSONObject jsonData = respanse.getJSONObject("data");
                Maopao.MaopaoObject maopaoObject = new Maopao.MaopaoObject(jsonData);
                maopaoObject.owner = GlobalData.sUserObject;
                maopaoObject.owner_id = GlobalData.sUserObject.id;

                Intent intent = new Intent();
                intent.putExtra(ListModify.TYPE, ListModify.Add);
                intent.putExtra(ListModify.DATA, maopaoObject);
                setResult(Activity.RESULT_OK, intent);

                showMiddleToast("发表成功");

                finishWithoutSave();
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(HOST_IMAGE)) {
            if (code == 0) {
                String fileUri = (String) data;
                for (int i = 0; i < mData.size(); ++i) {
                    PhotoData item = mData.get(i);
                    if (item.uri.toString().equals(fileUri)) {
                        item.serviceUri = respanse.getString("data");
                        break;
                    }
                }

                mHandler.sendEmptyMessage(0);
            } else {
                showProgressBar(false);
                showMiddleToast("上传图片失败");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String mGlobalKey = AccountInfo.loadAccount(this).global_key;
        if (mGlobalKey.isEmpty()) {
            Intent intent = new Intent(this, CodingCompat.instance().getLoginActivity());
            this.startActivity(intent);
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    File outputFile;
                    try {
                        outputFile = photoOperate.scal(imageUri);
                        mData.add(mData.size(), new PhotoData(outputFile, new ImageInfo(imageUri.toString())));
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        showMiddleToast("缩放图片失败");
                        Global.errorLog(e);
                    }
                }
                mIntentExtraString = intent.getStringExtra(Intent.EXTRA_TEXT);
            } else if (type.startsWith("text/")) {
                mIntentExtraString = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                ArrayList<Uri> imagesUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (imagesUris != null) {
                    try {
                        for (Uri uri : imagesUris) {
                            File outputFile = photoOperate.scal(uri);
                            mData.add(new PhotoData(outputFile, new ImageInfo(uri.toString())));
                        }
                    } catch (Exception e) {
                        showMiddleToast("缩放图片失败");
                        Global.errorLog(e);
                    }
                    adapter.notifyDataSetChanged();
                }
                mIntentExtraString = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        } else if ("android.intent.action.SEND.net.coding.program".equals(action) && type != null) {
            if (type.startsWith("image/")) {

                String imageUri = intent.getStringExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    ImageSize size = new ImageSize(GlobalData.sWidthPix, GlobalData.sHeightPix);
                    ImageLoader.getInstance().loadImage(imageUri, size, ImagePagerFragment.Companion.getOptionsImage(), new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            if (isFinishing()) {
                                return;
                            }

                            try {
                                File imageFile = ImageLoader.getInstance().getDiskCache().get(imageUri);
                                String path = "file://" + imageFile.getPath();
                                ImageInfo imageInfo = new ImageInfo(path);
                                File outputFile = photoOperate.scal(Uri.parse(path));
                                mData.add(new PhotoData(outputFile, imageInfo));
                                adapter.notifyDataSetChanged();

                            } catch (Exception e) {
                                showMiddleToast("缩放图片失败");
                                Global.errorLog(e);
                            }
                        }
                    });
                }

                mIntentExtraString = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        }
    }

    @SuppressLint("CheckResult")
    @Click(R.id.locationText)
    void chooseLocation() {
        if (PhoneType.isX86or64()) {
            showMiddleToast("定位功能不支持x86手机");
            return;
        }

        new RxPermissions(this)
                .request(PermissionUtil.LOCATION)
                .subscribe(granted -> {
                    if (granted) {
                        LocationSearchActivity_.intent(this).selectedLocation(currentLocation).startForResult(RESULT_REQUEST_LOCATION);
                    }
                });

    }

    @OnActivityResult(RESULT_REQUEST_LOCATION)
    void on_RESULT_REQUEST_LOCATION(int result, @OnActivityResult.Extra LocationObject location) {
        if (result == RESULT_OK) {
            currentLocation = location;
            updateLocationText();
        }
    }

    private void updateLocationText() {
        locationText.setText(currentLocation.name);
        locationText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(
                currentLocation.type == LocationObject.Type.Undefined
                        ? R.drawable.ic_location_inactive
                        : R.drawable.ic_location_active), null, null, null);
    }

    @Click
    protected final void popPhoto() {
        startPhotoPickActivity();
    }

    @Click
    protected final void popAt() {
        TextWatcherAt.startActivityAt(this, this, RESULT_REQUEST_FOLLOW);
    }

    @Click
    protected final void popTopic() {
        Intent intent = new Intent(this, SubjectNewActivity_.class);
        startActivityForResult(intent, RESULT_REQUEST_TOPIC);
    }

}
