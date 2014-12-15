package net.coding.program.project.detail;

import android.webkit.WebView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.model.AttachmentFileObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 展示某一项目文档目录下面HTML文件和Markdown类型文件的Activity
 * HTML文件就纯展示
 * Markdown文件拿到生成的HTML代码之后，使用了从网页版Coding中扒来的css样式,直接写在markdown-html模板里
 * Created by yangzhen
 */
@EActivity(R.layout.activity_attachments_html)
//@OptionsMenu(R.menu.users)
public class AttachmentsHtmlDetailActivity extends AttachmentsDetailBaseActivity {

    @ViewById
    WebView webview;

    String urlFiles = Global.HOST + "/api/project/%s/files/%s/view";
    String urlPages = Global.HOST + "/api/project/%s/files/image/%s?folderId=%s&orderByDesc=true";
    String urlMdPreview = Global.HOST + "/api/markdown/preview";

    AttachmentFileObject mFiles = new AttachmentFileObject();

    String markdown;

    @AfterViews
    void init() {
        super.init();

        try {
            markdown = readTextFile(getAssets().open("markdown-html"));
        } catch (Exception e) {
            Global.errorLog(e);
        }

        urlFiles = String.format(urlFiles, mProjectObjectId, mAttachmentFileObject.file_id);
        urlPages = String.format(urlFiles, mProjectObjectId, mAttachmentFileObject.file_id, mAttachmentFolderObject.file_id);

        webview.getSettings().setJavaScriptEnabled(true);
        //webview.setBackgroundColor(0);
        //webview.getBackground().setAlpha(0);

        webview.getSettings().setDefaultTextEncodingName("UTF-8");
        showDialogLoading();
        getNetwork(urlFiles, urlFiles);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        super.parseJson(code, respanse, tag, pos, data);
        if (tag.equals(urlFiles)) {
            if (code == 0) {
                JSONObject file = respanse.getJSONObject("data").getJSONObject("file");
                mFiles = new AttachmentFileObject(file);
                String content = respanse.getJSONObject("data").optString("content");
                if (mFiles.isHtml()) {
                    hideProgressDialog();
                    webview.loadDataWithBaseURL("about:blank", content, "text/html", "utf-8", null);
                } else if (mFiles.isMd()) {
                    RequestParams params = new RequestParams();
                    params.put("content", content);
                    postMdContent(urlMdPreview, params);
                }

            } else {
                hideProgressDialog();
                showErrorMsg(code, respanse);
            }
        }
    }

    @OptionsItem
    public void action_add() {

    }

    private void postMdContent(String url, RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                hideProgressDialog();
                webview.loadDataWithBaseURL("about:blank", markdown.replace("${webview_content}", new String(response)), "text/html", "UTF-8", null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                hideProgressDialog();
                showButtomToast("连接服务器失败，请检查网络或稍后重试");
            }

            @Override
            public void onRetry(int retryNo) {
            }
        });
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

}
