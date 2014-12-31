package net.coding.program.task;

import android.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
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

    @FragmentArg
    String contentMd = "";

    @FragmentArg
    String contentHtml = "";

    @FragmentArg
    boolean preview = false;

    @AfterViews
    void init() {
        setHasOptionsMenu(true);

        if (contentHtml.isEmpty()) {
            mdToHtml();
        } else {
            displayWebView();
        }
    }

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
                hideProgressDialog();
            }
        });
    }

    @OptionsItem
    void action_edit() {
        Fragment fragment = TaskDescripMdFragment_.builder().contentMd(contentMd).build();
        getActivity().getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack("name")
                .commit();
    }
}
