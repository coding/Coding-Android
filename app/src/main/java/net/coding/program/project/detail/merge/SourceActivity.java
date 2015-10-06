package net.coding.program.project.detail.merge;

import android.webkit.WebView;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.GitFileObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_source)
public class SourceActivity extends BackActivity {

    public static final String HOST_FILE_SOURCE = "HOST_FILE_SOURCE";

    @ViewById
    WebView webView;

    @Extra
    String url;

    @AfterViews
    protected final void initSourceActivity() {
        showDialogLoading();

        webView.getSettings().setBuiltInZoomControls(true);
        Global.initWebView(webView);
        getNetwork(url, HOST_FILE_SOURCE);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_FILE_SOURCE)) {
            hideProgressDialog();
            if (code == 0) {
                GitFileObject file = new GitFileObject(respanse.optJSONObject("data").optJSONObject("file"));
                Global.setWebViewContent(webView, file);
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }
}
