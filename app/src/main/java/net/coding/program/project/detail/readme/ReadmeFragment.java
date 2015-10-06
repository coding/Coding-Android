package net.coding.program.project.detail.readme;


import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.maopao.MaopaoDetailActivity;
import net.coding.program.model.Depot;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@EFragment(R.layout.fragment_readme)
public class ReadmeFragment extends BaseFragment {

    @FragmentArg
    ProjectObject mProjectObject;

    @ViewById
    WebView webView;

    @ViewById
    View needReadme;

    @ViewById
    TextView readme;

    private String hostGitTree;
    private String hostProjectGit;

    @AfterViews
    protected void init() {
        hostProjectGit = mProjectObject.getProjectGit();
        getNetwork(hostProjectGit);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(hostGitTree)) {
            if (code == 0) {
                JSONObject readmeJson = respanse.optJSONObject("data").optJSONObject("readme");
                if (readmeJson == null) {
                    showEmptyReadme();

                } else {
                    String readmeHtml = readmeJson.optString("preview", "");
                    if (readmeHtml.isEmpty()) {
                        showEmptyReadme();

                    } else {
                        String readmeName = readmeJson.optString("name", "");
                        readme.setText(readmeName);

                        needReadme.setVisibility(View.GONE);
                        webView.setVisibility(View.VISIBLE);

                        Global.initWebView(webView);

                        String bubble = "${webview_content}";
                        try {
                            bubble = readTextFile(getResources().getAssets().open("markdown"));
                        } catch (Exception e) {
                            Global.errorLog(e);
                        }

                        webView.loadDataWithBaseURL(null, bubble.replace("${webview_content}", readmeHtml), "text/html", "UTF-8", null);
                        webView.setWebViewClient(new MaopaoDetailActivity.CustomWebViewClient(getActivity(), readmeHtml));
                    }
                }
            } else if (code == 1209) {
                showEmptyReadme();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(hostProjectGit)) {
            if (code == 0) {
                JSONObject jsonObject = respanse.getJSONObject("data").getJSONObject("depot");
                Depot depot = new Depot(jsonObject);
                hostGitTree = mProjectObject.getHttpGitTree(depot.getDefault_branch());
                getNetwork(hostGitTree, hostGitTree);
            } else {
                showProgressBar(false);
                showErrorMsg(code, respanse);
            }
        }
    }

    private void showEmptyReadme() {
        readme.setText("README.md");
        needReadme.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
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
