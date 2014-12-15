package net.coding.program.project.detail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import net.coding.program.BaseFragmentActivity;
import net.coding.program.Global;
import net.coding.program.ImagePagerFragment;
import net.coding.program.ImagePagerFragment_;
import net.coding.program.R;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.model.GitFileInfoObject;
import net.coding.program.model.GitFileObject;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

@EActivity(R.layout.activity_attachments_html)
//@OptionsMenu(R.menu.users)
public class GitViewActivity extends BaseFragmentActivity {
    private static String TAG = GitViewActivity.class.getSimpleName();

    @Extra
    ProjectObject mProjectObject;

    @Extra
    GitFileInfoObject mGitFileInfoObject;

    @ViewById
    WebView webview;

    @ViewById
    ViewPager pager;
    int mPagerPosition = 0;
    ImagePager adapter;
    ArrayList<String> mArrayUri;
    AsyncHttpClient client;

    File mTempPicFile;

    String urlBlob = Global.HOST + "/api/user/%s/project/%s/git/blob/master/%s";
    //https://coding.net/api/user/bluishoul/project/AppBubbleDetail/git/blob/master%252F.bowerrc
    String urlImage = Global.HOST + "/u/%s/p/%s/git/raw/master/%s";
    //https://coding.net/u/8206503/p/AndroidCoding/git/raw/master/app/src/main/res/drawable-xxhdpi/actionbar_item_normal.png

    GitFileObject mFile;

    String template;

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(mGitFileInfoObject.name);

        client = MyAsyncHttpClient.createClient(GitViewActivity.this);

        urlBlob = String.format(urlBlob, mProjectObject.owner_user_name, mProjectObject.name, mGitFileInfoObject.path);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setBuiltInZoomControls(true);

        //webview.setBackgroundColor(0);
        //webview.getBackground().setAlpha(0);
        mArrayUri = new ArrayList<String>();
        adapter = new ImagePager(getSupportFragmentManager());
        pager.setAdapter(adapter);

        webview.getSettings().setDefaultTextEncodingName("UTF-8");
        showDialogLoading();
        getNetwork(urlBlob, urlBlob);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(urlBlob)) {
            hideProgressDialog();

            if (code == 0) {

                JSONObject file = respanse.getJSONObject("data").getJSONObject("file");
                mFile = new GitFileObject(file);

                if (mFile.mode.equals("image")) {

                    try {

                        mTempPicFile = File.createTempFile("Coding_", ".tmp", getCacheDir());
                        mTempPicFile.deleteOnExit();
                        download(String.format(urlImage, mProjectObject.owner_user_name, mProjectObject.name, mFile.path));
                    } catch (IOException e) {
                        showButtomToast("图片无法下载");
                    }


                } else {
                    pager.setVisibility(View.GONE);
                    if (mFile.lang.equals("markdown")) {
                        try {
                            template = readTextFile(getAssets().open("markdown"));
                            webview.loadDataWithBaseURL("about:blank", template.replace("${webview_content}", mFile.preview), "text/html", "UTF-8", null);

                        } catch (Exception e) {
                            Global.errorLog(e);
                        }
                    } else {
                        try {
                            template = readTextFile(getAssets().open("code"));
                            webview.loadDataWithBaseURL("about:blank", template.replace("${file_code}", mFile.data).replace("${file_lang}", mFile.lang), "text/html", "UTF-8", null);
                        } catch (Exception e) {
                            Global.errorLog(e);
                        }
                    }
                }

            } else {
                hideProgressDialog();
                showErrorMsg(code, respanse);
            }
        }
    }

    private String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
        }
        return outputStream.toString();
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

    private void download(String url) {
        //url = "https://coding.net/api/project/5166/files/58705/download";
        //File mFile = FileUtil.getDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mFileObject.name);
        Log.d(TAG, "FileUrl:" + url);

        client.get(GitViewActivity.this, url, new FileAsyncHttpResponseHandler(mTempPicFile) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                for (Header header : headers) {
                    Log.v(TAG, "onFailure:" + statusCode + " " + header.getName() + ":" + header.getValue());
                }
                showButtomToast("下载失败");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File response) {
                Log.v(TAG, "onSuccess:" + statusCode + " " + headers.toString());
                mArrayUri.add("file:///" + response.getAbsolutePath());
                adapter.notifyDataSetChanged();
                pager.setVisibility(View.VISIBLE);

            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                Log.v(TAG, String.format("Progress %d from %d (%2.0f%%)", bytesWritten, totalSize, (totalSize > 0) ? (bytesWritten * 1.0 / totalSize) * 100 : -1));
            }
        });
    }

}
