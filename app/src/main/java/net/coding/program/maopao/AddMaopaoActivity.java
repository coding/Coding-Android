package net.coding.program.maopao;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.coding.program.BaseFragmentActivity;
import net.coding.program.Global;
import net.coding.program.ImagePagerActivity_;
import net.coding.program.LoginActivity_;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.ListModify;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.StartActivity;
import net.coding.program.common.TextWatcherAt;
import net.coding.program.common.enter.EnterEmojiLayout;
import net.coding.program.common.photopick.PhotoPickActivity;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.Maopao;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

@EActivity(R.layout.activity_add_maopao)
@OptionsMenu(R.menu.add_maopao)
public class AddMaopaoActivity extends BaseFragmentActivity implements StartActivity {

    final int PHOTO_MAX_COUNT = 5;

    final String sendUrl = Global.HOST + "/api/tweet";

    String mIntentExtraString = null;

    @ViewById
    GridView gridView;

    int imageWidthPx;
    ImageSize mSize;

    PhotoOperate photoOperate = new PhotoOperate(this);

    EnterEmojiLayout mEnterLayout;
    EditText message;

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

        getActionBar().setDisplayHomeAsUpEnabled(true);

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
                    AlertDialog dialog = new AlertDialog.Builder(AddMaopaoActivity.this)
                            .setItems(R.array.camera_gallery, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        camera();
                                    } else {
                                        int count = PHOTO_MAX_COUNT - mData.size();
                                        if (count <= 0) {
                                            return;
                                        }
                                        Intent intent = new Intent(AddMaopaoActivity.this, PhotoPickActivity.class);
                                        intent.putExtra(PhotoPickActivity.EXTRA_MAX, count);
                                        startActivityForResult(intent, RESULT_REQUEST_PICK_PHOTO);
                                    }
                                }
                            }).show();

                    dialogTitleLineColor(dialog);

                } else {
                    Intent intent = new Intent(AddMaopaoActivity.this, ImagePagerActivity_.class);
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

//        new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                mEnterLayout.popKeyboard();
//            }
//        }.sendEmptyMessageDelayed(0, 0);
    }

    public static final int RESULT_REQUEST_IMAGE = 100;
    public static final int RESULT_REQUEST_FOLLOW = 1002;
    public static final int RESULT_REQUEST_PICK_PHOTO = 1003;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_PICK_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    ArrayList<PhotoPickActivity.ImageInfo> pickPhots = (ArrayList<PhotoPickActivity.ImageInfo>) data.getSerializableExtra("data");
                    for (PhotoPickActivity.ImageInfo item : pickPhots) {
                        Uri uri = Uri.parse(item.path);
                        File outputFile = photoOperate.scal(uri);
                        mData.add(new AddMaopaoActivity.PhotoData(outputFile));
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
                    mData.add(mData.size(), new AddMaopaoActivity.PhotoData(outputFile));
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

                String enter = message.getText().toString() + name + " ";
                message.setText(enter);

                message.requestFocus();
                message.setSelection(enter.length());
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (message.getText().toString().isEmpty() && adapter.getCount() <= 1) {
            finish();
        } else {
            showDialog("确定放弃此次冒泡机会？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
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
        showProgressBar(true);
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

        if (EmojiFilter.containsEmoji(content)) {
            showMiddleToast("暂不支持发表情");
            showProgressBar(false);
            return;
        }

        String photoTemplate = "\n![图片](%s)";
        for (PhotoData item : mData) {
            content += String.format(photoTemplate, item.serviceUri);
        }

        params.put("content", content);
        params.put("device", Build.MODEL);
        postNetwork(sendUrl, params, sendUrl);
    }

    final String HOST_IMAGE = Global.HOST + "/api/tweet/insert_image";

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
                finish();
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

    class PhotoData {
        Uri uri = Uri.parse("");
        String serviceUri = "";

        public PhotoData(File file) {
            uri = Uri.fromFile(file);
        }

    }

    ArrayList<PhotoData> mData = new ArrayList<PhotoData>();

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
                holder.image.setImageResource(R.drawable.make_maopao_add);
                holder.uri = "";

            } else {
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
                        mData.add(mData.size(), new AddMaopaoActivity.PhotoData(outputFile));
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
                            mData.add(new AddMaopaoActivity.PhotoData(outputFile));
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

}
