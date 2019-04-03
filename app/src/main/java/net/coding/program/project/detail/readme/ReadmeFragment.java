package net.coding.program.project.detail.readme;


import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import net.coding.program.CodingGlobal;
import net.coding.program.R;
import net.coding.program.common.model.Depot;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.widget.LoadingView;
import net.coding.program.project.detail.merge.ReadmeEditActivity;
import net.coding.program.project.detail.merge.ReadmeEditActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EFragment(R.layout.fragment_readme)
public class ReadmeFragment extends BaseFragment {

    private static final int RESULT_EDIT = 1;

    @FragmentArg
    ProjectObject mProjectObject;

    @ViewById
    WebView webView;

    @ViewById
    View needReadme;

    @ViewById
    TextView readme;

    @ViewById
    LoadingView loadingView;

    private String hostGitTree;
    private String hostProjectGit;
    private ReadmeEditActivity.PostParam mPostParam;
    private MenuItem mMenuItem;
    private Depot mDepot;

    @AfterViews
    protected void init() {
        hostProjectGit = mProjectObject.getProjectGit();
        getNetwork(hostProjectGit);

        setHasOptionsMenu(true);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(hostGitTree)) {
            loadingView.setVisibility(View.GONE);
            if (code == 0) {
                JSONObject jsonData = respanse.optJSONObject("data");
                JSONObject readmeJson = jsonData.optJSONObject("readme");
                if (readmeJson == null) {
                    showEmptyReadme();

                } else {
                    String readmeHtml = readmeJson.optString("preview", "");
                    if (readmeHtml.isEmpty()) {
                        updateMenu(false);
                        showEmptyReadme();
                    } else {
                        mPostParam = new ReadmeEditActivity.PostParam(jsonData, mDepot.getDefault_branch());

                        updateMenu(true);
                        String readmeName = readmeJson.optString("name", "");
                        readme.setText(readmeName);

                        needReadme.setVisibility(View.GONE);
                        webView.setVisibility(View.VISIBLE);

                        CodingGlobal.setWebViewContent(webView, CodingGlobal.WebviewType.markdown, readmeHtml);
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
                mDepot = new Depot(jsonObject);
                hostGitTree = mProjectObject.getHttpGitTree(mDepot.getDefault_branch());
                getNetwork(hostGitTree, hostGitTree);
            } else {
                showProgressBar(false);
                showErrorMsg(code, respanse);
            }
        }
    }

    private void showEmptyReadme() {
        readme.setVisibility(View.GONE);
        needReadme.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
    }

    @OptionsItem
    void action_modify() {
        ReadmeEditActivity_.intent(this)
                .mProjectObject(mProjectObject)
                .mPostParam(mPostParam)
                .startForResult(RESULT_EDIT);
    }

    @OnActivityResult(RESULT_EDIT)
    void onResultEdit(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            init();
        }
    }

    private void updateMenu(boolean hasReadme) {
        mMenuItem.setVisible(hasReadme);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.readme_fragment, menu);
        mMenuItem = menu.findItem(R.id.action_modify);
        mMenuItem.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }
}
