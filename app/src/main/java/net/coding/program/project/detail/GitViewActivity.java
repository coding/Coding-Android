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

import net.coding.program.ImagePagerFragment;
import net.coding.program.ImagePagerFragment_;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.CustomMoreActivity;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.model.GitFileInfoObject;
import net.coding.program.model.GitFileObject;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@EActivity(R.layout.activity_gitview)
//@OptionsMenu(R.menu.users)
@OptionsMenu(R.menu.common_more)
public class GitViewActivity extends CustomMoreActivity {
    private static String TAG = GitViewActivity.class.getSimpleName();

    @Extra
    String mProjectPath;
//    ProjectObject mProjectObject;

    @Extra
    GitFileInfoObject mGitFileInfoObject;

    @Extra
    String mVersion = ProjectGitFragment.MASTER;

    @ViewById
    WebView webview;

    @ViewById
    ViewPager pager;
    int mPagerPosition = 0;

    ImagePager adapter;
    ArrayList<String> mArrayUri;
    AsyncHttpClient client;

    File mTempPicFile;

    String urlBlob = Global.HOST_API + "%s/git/blob/%s/%s";
    String urlImage = Global.HOST + "%s/git/raw/%s/%s";

    GitFileObject mFile;

    @AfterViews
    protected final void initGitViewActivity() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mGitFileInfoObject.name);

        client = MyAsyncHttpClient.createClient(GitViewActivity.this);

        urlBlob = String.format(urlBlob, mProjectPath, mVersion, Global.encodeUtf8(Global.encodeUtf8(mGitFileInfoObject.path)));
        webview.getSettings().setBuiltInZoomControls(true);
        Global.initWebView(webview);

        mArrayUri = new ArrayList<>();
        adapter = new ImagePager(getSupportFragmentManager());
        pager.setAdapter(adapter);

        webview.getSettings().setDefaultTextEncodingName("UTF-8");
        showDialogLoading();
        getNetwork(urlBlob, urlBlob);
    }

    @OptionsItem(android.R.id.home)
    protected final void annotaionClose() {
        onBackPressed();
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
                        String s = ProjectObject.translatePathToOld(mProjectPath);
                        download(String.format(urlImage, s, mVersion, mFile.path));
                    } catch (IOException e) {
                        showButtomToast("图片无法下载");
                    }


                } else {
                    pager.setVisibility(View.GONE);
                    Global.setWebViewContent(webview, mFile);
                }

            } else {
                hideProgressDialog();
                showErrorMsg(code, respanse);
            }
        }
    }

//    private String readTextFile(InputStream inputStream) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        byte buf[] = new byte[1024];
//        int len;
//        try {
//            while ((len = inputStream.read(buf)) != -1) {
//                outputStream.write(buf, 0, len);
//            }
//            outputStream.close();
//            inputStream.close();
//
//        } catch (IOException e) {
//            Global.errorLog(e);
//        }
//        return outputStream.toString();
//    }

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

    @Override
    protected String getLink() {
        String s = ProjectObject.translatePathToOld(mProjectPath);
        return Global.HOST + s + "/git/blob/" + mVersion + "/" + mGitFileInfoObject.path;
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
}
