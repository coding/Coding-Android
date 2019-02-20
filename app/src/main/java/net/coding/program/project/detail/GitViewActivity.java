package net.coding.program.project.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import net.coding.program.CodingGlobal;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.GitFileBlobObject;
import net.coding.program.common.model.GitFileInfoObject;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.CodingToolbarBackActivity;
import net.coding.program.common.url.UrlCreate;
import net.coding.program.pickphoto.detail.ImagePagerFragment;
import net.coding.program.pickphoto.detail.ImagePagerFragment_;
import net.coding.program.project.git.BranchCommitListActivity_;
import net.coding.program.project.git.EditCodeActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@EActivity(R.layout.activity_gitview)
public class GitViewActivity extends CodingToolbarBackActivity {
    private static final int RESULT_EDIT = 1;
    private static String TAG = GitViewActivity.class.getSimpleName();
    @Extra
    String mProjectPath;

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

    GitFileBlobObject mFile;

    @AfterViews
    protected final void initGitViewActivity() {
        setActionBarTitle(mGitFileInfoObject.name);

        client = MyAsyncHttpClient.createClient(GitViewActivity.this);

        urlBlob = String.format(urlBlob, mProjectPath, mVersion, Global.encodeUtf8(mGitFileInfoObject.path));
        webview.getSettings().setBuiltInZoomControls(true);
        Global.initWebView(webview);

        mArrayUri = new ArrayList<>();
        adapter = new ImagePager(getSupportFragmentManager());
        pager.setAdapter(adapter);

        showDialogLoading();
        getNetwork(urlBlob, urlBlob);
    }

    @Nullable
    @Override
    protected ProjectObject getProject() {
        return null;
    }

    @Override
    protected String getProjectPath() {
        return mProjectPath;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mFile != null) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.git_view, menu);

            MenuItem menuItemEdit = menu.findItem(R.id.action_edit);
            MenuItem menuItemDelete = menu.findItem(R.id.actionDelete);

            if (TextUtils.isEmpty(mFile.getCommitId()) || !mFile.canEdit) {
                menuItemEdit.setVisible(false);
                menuItemDelete.setVisible(false);
            } else if (mFile.getGitFileObject().mode.equals("image")) {
                menuItemEdit.setVisible(false);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @OptionsItem
    void action_edit() {
        EditCodeActivity_.intent(this)
                .mProjectPath(mProjectPath)
                .mGitFileInfoObject(mGitFileInfoObject)
                .mVersion(mVersion)
                .mGitAll(mFile)
                .startForResult(RESULT_EDIT);
    }


    @OptionsItem
    void action_history() {
        String peek = mGitFileInfoObject.path;
        if (peek.isEmpty() && mVersion.isEmpty()) {
            showButtomToast("没有Commit记录");
            return;
        }

        String commitUrl = UrlCreate.gitTreeCommit(mProjectPath, mVersion, peek);
        BranchCommitListActivity_.intent(this).mCommitsUrl(commitUrl).start();
//        RedPointTip.markUsed(getActivity(), RedPointTip.Type.CodeHistory);
    }

    @OptionsItem
    void actionDelete() {
        showDialog(String.format("确定删除文件 %s?", mGitFileInfoObject.name), (dialog, which) -> realDelete());
    }

    private void realDelete() {
        String peek = mGitFileInfoObject.path;
        if (peek.isEmpty() && mVersion.isEmpty()) {
            showButtomToast("无法删除");
            return;
        }

        String deleteUrl = UrlCreate.gitDeleteFile(mProjectPath, mVersion, peek);

        RequestParams params = new RequestParams();
        params.put("message", "delete file " + mGitFileInfoObject.name);
        params.put("lastCommitSha", mFile.getCommitId());
        MyAsyncHttpClient.post(this, deleteUrl, params, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                showProgressBar(false);

                showButtomToast("已删除文件 " + mGitFileInfoObject.name);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                showProgressBar(false);
            }
        });

        showProgressBar(true);
    }

//    @OptionsItem
//    void action_commit() {
//
//    }

    @OnActivityResult(RESULT_EDIT)
    void onResultEdit(int resultCode, @OnActivityResult.Extra GitFileBlobObject resultData) {
        if (resultCode == RESULT_OK) {
            mFile = resultData;
            bindUIByData();
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(urlBlob)) {
            hideProgressDialog();

            if (code == 0) {
                mFile = new GitFileBlobObject(respanse.getJSONObject("data"));
                bindUIByData();

            } else {
                hideProgressDialog();
                showErrorMsg(code, respanse);
            }
        }
    }


    public void bindUIByData() {
        if (mFile.getGitFileObject().mode.equals("image")) {
            try {
                mTempPicFile = File.createTempFile("Coding_", ".tmp", getCacheDir());
                mTempPicFile.deleteOnExit();
                String s = ProjectObject.translatePathToOld(mProjectPath);
                download(String.format(urlImage, s, mVersion, mFile.getGitFileObject().path));
            } catch (IOException e) {
                showButtomToast("图片无法下载");
            }

        } else {
            pager.setVisibility(View.GONE);
            CodingGlobal.setWebViewContent(webview, mFile.getGitFileObject());
        }

        invalidateOptionsMenu();
    }


    private void download(String url) {
        //url = "https://coding.net/api/project/5166/files/58705/download";
//        //File mFile = FileUtil.getDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mFileObject.name);
//        Log.d(TAG, "FileUrl:" + url);
//
//        client.get(GitViewActivity.this, url, new FileAsyncHttpResponseHandler(mTempPicFile) {
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
//                for (Header header : headers) {
//                    Log.v(TAG, "onFailure:" + statusCode + " " + header.getName() + ":" + header.getValue());
//                }
//                showButtomToast("下载失败");
//            }
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, File response) {
//                mArrayUri.add("file:///" + response.getAbsolutePath());
//                adapter.notifyDataSetChanged();
//                pager.setVisibility(View.VISIBLE);
//
//            }
//
//        });

        mArrayUri.add(url);
        adapter.notifyDataSetChanged();
        pager.setVisibility(View.VISIBLE);

    }

//    @Override
//    protected String getLink() {
//        String s = ProjectObject.translatePathToOld(mProjectPath);
//        return Global.HOST + s + "/git/blob/" + mVersion + "/" + mGitFileInfoObject.path;
//    }

    class ImagePager extends FragmentPagerAdapter {

        public ImagePager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            ImagePagerFragment_ fragment = new ImagePagerFragment_();
            Bundle bundle = new Bundle();
            bundle.putString("uri", mArrayUri.get(i));
            bundle.putBoolean("customMenu", false);
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
