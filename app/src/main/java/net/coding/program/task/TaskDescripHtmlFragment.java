package net.coding.program.task;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.maopao.MaopaoDetailActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.apache.http.cookie.Cookie;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieManager;
import java.util.List;

@EFragment(R.layout.fragment_task_descrip_html)
public class TaskDescripHtmlFragment extends BaseFragment {

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

    private void displayWebView() {
        descWeb.setWebViewClient(new MaopaoDetailActivity.CustomWebViewClient(getActivity()));
        descWeb.getSettings().setJavaScriptEnabled(true);
        descWeb.setBackgroundColor(0);
        descWeb.getBackground().setAlpha(0);
        descWeb.getSettings().setDefaultTextEncodingName("UTF-8");

        descWeb.loadDataWithBaseURL(Global.HOST, ((TaskDescrip) getActivity()).createLocateHtml(contentHtml), "text/html", "UTF-8", null);
    }

    public static final String HOST_PREVIEW = Global.HOST + "/api/markdown/preview";

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (preview) {
        } else {
            inflater.inflate(R.menu.menu_task_description, menu);
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_PREVIEW)) {
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
        postNetwork(HOST_PREVIEW, params, HOST_PREVIEW);
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
