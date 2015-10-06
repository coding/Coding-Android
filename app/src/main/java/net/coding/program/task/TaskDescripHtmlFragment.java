package net.coding.program.task;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.maopao.MaopaoDetailActivity;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.detail.TopicEditFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EFragment(R.layout.fragment_task_descrip_html)
public class TaskDescripHtmlFragment extends BaseFragment {

    private static final String TAG_HTTP_MD_PREVIEW = "TAG_HTTP_MD_PREVIEW";
    @ViewById
    WebView descWeb;
    @ViewById
    View loading;
    @FragmentArg
    String contentMd = "";
    @FragmentArg
    String contentHtml = "";
    @FragmentArg
    boolean preview = false;
    ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        int id = 0;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_task_description_pre, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_save:
                    ((TaskDescrip) getActivity()).closeAndSave(contentMd);
                    return true;

                case R.id.action_edit:
                    id = R.id.action_edit;
                    mActionMode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            manager.popBackStack();
            if (id == R.id.action_edit) {
                Fragment fragment = TaskDescripMdFragment_.builder().contentMd(contentMd).build();
                manager
                        .beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack("name")
                        .commit();
            } else {
                if (manager.getFragments().size() == 1) {
                    getActivity().finish();
                }
            }

        }
    };

    @AfterViews
    void init() {
        setHasOptionsMenu(true);

        Global.syncCookie(getActivity());

        if (contentHtml.isEmpty()) {
            mdToHtml();
            mActionMode = getActivity().startActionMode(mActionModeCallback);
        } else {
            displayWebView();
        }
    }

    private void displayWebView() {
        String locateHtml = ((TaskDescrip) getActivity()).createLocateHtml(contentHtml);

        descWeb.setWebViewClient(new MaopaoDetailActivity.CustomWebViewClient(getActivity(), locateHtml));
        descWeb.getSettings().setJavaScriptEnabled(true);
        descWeb.setBackgroundColor(0);
        descWeb.getBackground().setAlpha(0);
        descWeb.getSettings().setDefaultTextEncodingName("UTF-8");

        descWeb.loadDataWithBaseURL(Global.HOST, locateHtml, "text/html", "UTF-8", null);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (preview) {
        } else {
            inflater.inflate(R.menu.menu_task_description, menu);
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_HTTP_MD_PREVIEW)) {
            if (code == 0) {
                contentHtml = respanse.optString("data", "");
                displayWebView();
            } else {
                showButtomToast("发生错误");
            }

            loading.setVisibility(View.INVISIBLE);
        }
    }

    private void mdToHtml() {
        loading.setVisibility(View.VISIBLE);

        RequestParams params = new RequestParams();
        params.put("content", contentMd);
        String projectPath = ((TopicEditFragment.SaveData) getActivity()).getProjectPath();
        String uri = ProjectObject.getMdPreview(projectPath);
        postNetwork(uri, params, TAG_HTTP_MD_PREVIEW);
    }

    @OptionsItem
    void action_edit() {
        Fragment fragment = TaskDescripMdFragment_.builder().contentMd(contentMd).build();
        FragmentManager manager = getActivity().getSupportFragmentManager();
        manager.popBackStack();
        manager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack("name")
                .commit();
    }
}
