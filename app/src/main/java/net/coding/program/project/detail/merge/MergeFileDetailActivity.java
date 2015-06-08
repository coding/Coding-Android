package net.coding.program.project.detail.merge;

import android.webkit.WebView;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.DiffFile;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_merge_file_detail)
//@OptionsMenu(R.menu.menu_merge_file_detail)
public class MergeFileDetailActivity extends BackActivity {

    public static final String HOST_COMMIT_FILE_DETAIL = "HOST_COMMIT_FILE_DETAIL";
    @Extra
    String mProjectPath;
    @Extra
    DiffFile.DiffSingleFile mSingleFile;

    @ViewById
    WebView webView;

    @AfterViews
    protected final void initMergeFileDetailActivity() {
        String url = mSingleFile.getHttpFileDiffDetail(mProjectPath);
//        getNetwork(url, HOST_COMMIT_FILE_DETAIL);

        Global.initWebView(webView);

//        webView.loadDataWithBaseURL(Global.HOST, bubble.replace(replaceString, content), "text/html", "UTF-8", null);
//        try {
////                    String s = respanse.optString("data");
//            Global.setWebViewContent(webView, "diff", "${diff-content}", url);
//        } catch (Exception e) {
//            Global.errorLog(e);
//        }

        Global.setWebViewContent(webView, "diff", "${diff-content}", url);
//        webView.loadUrl(url);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_COMMIT_FILE_DETAIL)) {
            if (code == 0) {
                try {
//                    String s = respanse.optString("data");
                    Global.setWebViewContent(webView, "diff", "${diff-content}", respanse.toString());
                } catch (Exception e) {
                    Global.errorLog(e);
                }

            } else {
                showErrorMsg(code, respanse);
            }
        }
    }
}
