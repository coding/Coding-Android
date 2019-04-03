package net.coding.program.project.git;


import android.support.v4.app.Fragment;
import android.view.View;
import android.webkit.WebView;

import com.loopj.android.http.RequestParams;

import net.coding.program.CodingGlobal;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.GitFileBlobObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.url.UrlCreate;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_preview_code)
public class PreviewCodeFragment extends BaseFragment {

    @FragmentArg
    String projectPath;

    @ViewById
    View customLoadingView;

    @ViewById
    WebView webview;

    @AfterViews
    void initPreviewCodeFragment() {
        webview.getSettings().setBuiltInZoomControls(true);
        Global.initWebView(webview);
    }

    public void loadData() {
        GitFileBlobObject file = ((EditCodeActivity) getActivity()).getFile();
        if (file.getGitFileObject().lang.equals("markdown")) {

            String url = String.format("%s%s/git/blob-preview/%s%s", Global.HOST_API, projectPath,
                    file.getRef(), UrlCreate.pathEncode2(file.getGitFileObject().path));
            RequestParams params = new RequestParams();
            params.put("data", file.getGitFileObject().data);
            MyAsyncHttpClient.post(getActivity(), url, params, new MyJsonResponse(getActivity()) {
                @Override
                public void onMySuccess(JSONObject response) {
                    super.onMySuccess(response);
                    file.getGitFileObject().preview = response.optString("data");
                    CodingGlobal.setWebViewContent(webview, file.getGitFileObject());
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    customLoadingView.setVisibility(View.INVISIBLE);
                }
            });
            customLoadingView.setVisibility(View.VISIBLE);

        } else {
            CodingGlobal.setWebViewContent(webview, file.getGitFileObject());
        }
    }
}
