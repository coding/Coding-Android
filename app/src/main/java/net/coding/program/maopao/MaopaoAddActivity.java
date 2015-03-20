package net.coding.program.maopao;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.coding.program.BaseActivity;
import net.coding.program.ImagePagerActivity_;
import net.coding.program.LoginActivity_;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ListModify;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.StartActivity;
import net.coding.program.common.TextWatcherAt;
import net.coding.program.common.enter.EnterEmojiLayout;
import net.coding.program.common.enter.SimpleTextWatcher;
import net.coding.program.common.photopick.PhotoPickActivity;
import net.coding.program.maopao.item.LocationCoord;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.LocationObject;
import net.coding.program.model.Maopao;
import net.coding.program.third.EmojiFilter;

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
import java.io.Serializable;
import java.util.ArrayList;

@EActivity(R.layout.activity_maopao_add)
public class MaopaoAddActivity extends BaseActivity implements StartActivity {

    final int PHOTO_MAX_COUNT = 6;

    final String sendUrl = Global.HOST + "/api/tweet";

    String mIntentExtraString = null;

    @ViewById
    GridView gridView;
    @ViewById
    TextView locationText;
    @InstanceState
    LocationObject currentLocation = LocationObject.undefined();

    int imageWidthPx;
    ImageSize mSize;

    PhotoOperate photoOperate = new PhotoOperate(this);

    EnterEmojiLayout mEnterLayout;
    EditText message;

    private Uri fileUri;

    android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            uploadMaopao();
        }
    };

    @AfterViews
    void init() {
        imageWidthPx = Global.dpToPx(100);
        mSize = new ImageSize(imageWidthPx, imageWidthPx);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEnterLayout = new EnterEmojiLayout(this, null);
        message = mEnterLayout.content;
        if (mIntentExtraString != null) {
            message.setText(mIntentExtraString);
        }

        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == mData.size()) {
                    int count = PHOTO_MAX_COUNT - mData.size();
                    if (count <= 0) {
                        return;
                    }

                    Intent intent = new Intent(MaopaoAddActivity.this, PhotoPickActivity.class);
                    intent.putExtra(PhotoPickActivity.EXTRA_MAX, count);
                    startActivityForResult(intent, RESULT_REQUEST_PICK_PHOTO);

                } else {
                    Intent intent = new Intent(MaopaoAddActivity.this, ImagePagerActivity_.class);
                    ArrayList<String> arrayUri = new ArrayList<String>();
                    for (PhotoData item : mData) {
                        arrayUri.add(item.uri.toString());
                    }
                    intent.putExtra("mArrayUri", arrayUri);
                    intent.putExtra("mPagerPosition", position);
                    intent.putExtra("needEdit", true);
                    startActivityForResult(intent, RESULT_REQUEST_IMAGE);
                }
            }
        });

        message.addTextChangedListener(new TextWatcherAt(this, this, RESULT_REQUEST_FOLLOW));
        message.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateAddButton();
            }
        });

        MaopaoDraft draft = AccountInfo.loadMaopaoDraft(this);
        if (!draft.isEmpty()) {
            mEnterLayout.setText(draft.getInput());
            mData = draft.getPhotos();
            adapter.notifyDataSetChanged();
        }

        locationText.setText(currentLocation.name);
    }

    private void updateAddButton() {
        enableSendButton(!message.getText().toString().isEmpty() ||
                mData.size() > 0);
    }

    @Override
    protected void onStop() {
        MaopaoDraft draft = new MaopaoDraft(mEnterLayout.getContent(), mData);
        AccountInfo.saveMaopaoDraft(this, draft);

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

    private MenuItem mMenuAdd;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_maopao, menu);

        mMenuAdd = menu.findItem(R.id.action_add);
        updateAddButton();

        return super.onCreateOptionsMenu(menu);
    }

    public static final int RESULT_REQUEST_IMAGE = 100;
    public static final int RESULT_REQUEST_FOLLOW = 1002;
    public static final int RESULT_REQUEST_PICK_PHOTO = 1003;
    public static final int RESULT_REQUEST_PHOTO = 1005;
    public static final int RESULT_REQUEST_LOCATION = 1006;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_PICK_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    ArrayList<PhotoPickActivity.ImageInfo> pickPhots = (ArrayList<PhotoPickActivity.ImageInfo>) data.getSerializableExtra("data");
                    for (PhotoPickActivity.ImageInfo item : pickPhots) {
                        Uri uri = Uri.parse(item.path);
                        File outputFile = photoOperate.scal(uri);
                        mData.add(new MaopaoAddActivity.PhotoData(outputFile));
                    }
                } catch (Exception e) {
                    showMiddleToast("缩放图片失败");
                    Global.errorLog(e);
                }
                adapter.notifyDataSetChanged();
            }
        } else if (requestCode == RESULT_REQUEST_PHOTO) {
            if (resultCode == RESULT_OK) {
                try {
                    File outputFile = photoOperate.scal(fileUri);
                    mData.add(mData.size(), new MaopaoAddActivity.PhotoData(outputFile));
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
                mEnterLayout.insertText(name);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        updateAddButton();
    }

    @Override
    public void onBackPressed() {
        if (message.getText().toString().isEmpty() && adapter.getCount() <= 1) {
            finish();
        } else {
            showDialog("冒泡", "放弃此次冒泡机会？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishWithoutSave();
                }
            });
        }
    }

    @OptionsItem(android.R.id.home)
    void home() {
        onBackPressed();
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

            File file = new File(Global.getPath(this, uri));
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
        if(currentLocation != null && !TextUtils.isEmpty(locationText.getText())) {
            String locationName = currentLocation.type == LocationObject.Type.City?
                    currentLocation.name : currentLocation.city + MaopaoLocationArea.MAOPAO_LOCATION_DIVIDE + currentLocation.name;
            params.put("location", ensureLength(locationName,32));
            params.put("coord", ensureLength(LocationCoord.from(currentLocation).toString(),32));
            params.put("address", ensureLength(currentLocation.address,64));
        }
        postNetwork(sendUrl, params, sendUrl);
    }

    private static String ensureLength(String src,int maxLength){
        if(TextUtils.isEmpty(src)) return "";
        if(src.length() <= maxLength) return src;
        if(maxLength < 1) throw new IllegalArgumentException("maxLength");
        return src.substring(0, maxLength - 1) + "…";
    }

    final String HOST_IMAGE = Global.HOST + "/api/tweet/insert_image";

    private void finishWithoutSave() {
        // 清空输入的数据，因为在onDestroy时如果检测到有数据会保存
        mEnterLayout.clearContent();
        mData.clear();
        finish();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(sendUrl)) {
            showProgressBar(false);
            if (code == 0) {
                JSONObject jsonData = respanse.getJSONObject("data");
                Maopao.MaopaoObject maopaoObject = new Maopao.MaopaoObject(jsonData);
                maopaoObject.owner = MyApp.sUserObject;

                Intent intent = new Intent();
                intent.putExtra(ListModify.TYPE, ListModify.Add);
                intent.putExtra(ListModify.DATA, maopaoObject);
                setResult(Activity.RESULT_OK, intent);

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

    public static class PhotoData {
        Uri uri = Uri.parse("");
        String serviceUri = "";

        public PhotoData(File file) {
            uri = Uri.fromFile(file);
        }

        public PhotoData(PhotoDataSerializable data) {
            uri = Uri.parse(data.uriString);
            serviceUri = data.serviceUri;
        }
    }

    // 因为PhotoData包含Uri，不能直接序列化，所以有了这个类
    public static class PhotoDataSerializable implements Serializable {
        String uriString = "";
        String serviceUri = "";

        public PhotoDataSerializable(PhotoData data) {
            uriString = data.uri.toString();
            serviceUri = data.serviceUri;
        }
    }

    public static class MaopaoDraft implements Serializable {
        private String input = "";

        private ArrayList<PhotoDataSerializable> photos = new ArrayList();

        public MaopaoDraft() {}

        public MaopaoDraft(String input, ArrayList<PhotoData> photos) {
            this.input = input;
            this.photos = new ArrayList();
            for (PhotoData item : photos) {
                this.photos.add(new PhotoDataSerializable(item));
            }
        }

        public boolean isEmpty() {
            return input.isEmpty() && photos.isEmpty();
        }

        public String getInput() {
            return input;
        }

        public ArrayList<PhotoData> getPhotos() {
            ArrayList<PhotoData> data = new ArrayList();
            for (PhotoDataSerializable item : photos) {
                data.add(new PhotoData(item));
            }

            return data;
        }
    }

    ArrayList<PhotoData> mData = new ArrayList();

    BaseAdapter adapter = new BaseAdapter() {

        public int getCount() {
            return mData.size() + 1;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        ArrayList<ViewHolder> holderList = new ArrayList<ViewHolder>();

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
                if (getCount() == (PHOTO_MAX_COUNT + 1)) {
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

        class ViewHolder {
            ImageView image;
            String uri = "";
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String mGlobalKey = AccountInfo.loadAccount(this).global_key;
        if (mGlobalKey.isEmpty()) {
            Intent intent = new Intent(this, LoginActivity_.class);
            this.startActivity(intent);
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    File outputFile = null;
                    try {
                        outputFile = photoOperate.scal(imageUri);
                        mData.add(mData.size(), new MaopaoAddActivity.PhotoData(outputFile));
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
                            mData.add(new MaopaoAddActivity.PhotoData(outputFile));
                        }
                    } catch (Exception e) {
                        showMiddleToast("缩放图片失败");
                        Global.errorLog(e);
                    }
                    adapter.notifyDataSetChanged();
                }
                mIntentExtraString = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        }
    }

    @Click(R.id.locationText)
    void chooseLocation(){
        LocationSearchActivity_.intent(this).selectedLocation(currentLocation).startForResult(RESULT_REQUEST_LOCATION);
    }

    @OnActivityResult(RESULT_REQUEST_LOCATION)
    void on_RESULT_REQUEST_LOCATION(int result, @OnActivityResult.Extra LocationObject location){
        if(result == RESULT_OK){
            currentLocation = location;
            locationText.setText(currentLocation.name);
            locationText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(
                    currentLocation.type == LocationObject.Type.Undefined
                            ? R.drawable.ic_location_inactive
                            : R.drawable.ic_location_active), null,null,null);
        }
    }
}
