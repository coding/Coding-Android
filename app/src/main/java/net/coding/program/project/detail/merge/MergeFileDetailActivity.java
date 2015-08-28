package net.coding.program.project.detail.merge;

import android.util.Log;
import android.webkit.WebView;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.DiffFile;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_merge_file_detail)
@OptionsMenu(R.menu.menu_merge_file_detail)
public class MergeFileDetailActivity extends BackActivity {

    public static final String HOST_COMMIT_FILE_DETAIL = "HOST_COMMIT_FILE_DETAIL";
    private static final String TAG_HTTP_ALL_COMMENTS = "TAG_HTTP_ALL_COMMENTS";

    @Extra
    String mProjectPath;
    @Extra
    DiffFile.DiffSingleFile mSingleFile;
    @Extra
    int mergeIid = 0;

    @ViewById
    WebView webView;
    String mRef = "";
    private JSONObject mCommentsData;

    @AfterViews
    protected final void initMergeFileDetailActivity() {
        getSupportActionBar().setTitle(mSingleFile.getName());

        String url;
        if (mergeIid != 0) {
            url = mSingleFile.getHttpFileDiffComment(mProjectPath, mergeIid);
        } else {
            url = mSingleFile.getHttpFileDiffComment(mProjectPath);
        }
        getNetwork(url, TAG_HTTP_ALL_COMMENTS);
    }

    @OptionsItem
    protected final void action_source() {
        if (mRef.isEmpty()) {
            showButtomToast("稍等，正在载入数据");
        } else {
            String url = mSingleFile.getHttpSourceFile(mProjectPath, mRef);
            Log.d("", url);
            SourceActivity_.intent(this).url(url).start();
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_COMMIT_FILE_DETAIL)) {
            if (code == 0) {
                try {
//                    String s = respanse.optString("data");
//                    this.data = respanse.optJSONObject("data");

                    respanse.remove("code");
                    mRef = respanse.optJSONObject("data").optString("linkRef", "");
                    Global.initWebView(webView);
                    Global.setWebViewContent(webView, "diff", "${diff-content}",
                            respanse.toString(), "${comments}", mCommentsData.toString());

                } catch (Exception e) {
                    Global.errorLog(e);
                }

            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_HTTP_ALL_COMMENTS)) {
            if (code == 0) {
                String url;
                if (mergeIid != 0) {
                    url = mSingleFile.getHttpFileDiffDetail(mProjectPath, mergeIid);
                } else {
                    url = mSingleFile.getHttpFileDiffDetail(mProjectPath);
                }
                Log.d("", "url Get " + url);
                getNetwork(url, HOST_COMMIT_FILE_DETAIL);


                mCommentsData = respanse;
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }
}
