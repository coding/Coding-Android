package net.coding.program.task;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import net.coding.program.R;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.common.network.MyAsyncHttpClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;

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
//            getSupportFragmentManager().popBackStack();

            Fragment fragment = TaskDescripMdFragment_.builder().contentMd(contentMd).build();
            FragmentManager manager = getActivity().getSupportFragmentManager();
            manager.popBackStack();
            if (id == R.id.action_edit) {
                manager
                        .beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack("name")
                        .commit();
            }
        }
    };

    private void displayWebView() {
        descWeb.getSettings().setJavaScriptEnabled(true);
        descWeb.setBackgroundColor(0);
        descWeb.getBackground().setAlpha(0);
        descWeb.getSettings().setDefaultTextEncodingName("UTF-8");
        descWeb.loadDataWithBaseURL(null, ((TaskDescrip) getActivity()).createLocateHtml(contentHtml), "text/html", "UTF-8", null);
    }

    final String HOST_PREVIEW = "https://coding.net/api/markdown/preview";

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (preview) {
        } else {
            inflater.inflate(R.menu.menu_task_description, menu);
        }
    }

    private void mdToHtml() {
        loading.setVisibility(View.VISIBLE);

        RequestParams params = new RequestParams();
        params.put("content", contentMd);
        postNetwork(HOST_PREVIEW, params, HOST_PREVIEW);

        AsyncHttpClient client = MyAsyncHttpClient.createClient(getActivity());
        client.post(HOST_PREVIEW, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showButtomToast("发生错误 " + statusCode + " " + responseString);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                contentHtml = responseString;
                displayWebView();
            }

            @Override
            public void onFinish() {
                loading.setVisibility(View.INVISIBLE);
            }
        });
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
