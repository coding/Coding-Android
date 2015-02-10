package net.coding.program.task;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.EditText;

import com.loopj.android.http.PersistentCookieStore;

import net.coding.program.BaseFragmentActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.TaskObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@EActivity(R.layout.activity_task_description)
public class TaskDescriptionActivity extends BaseFragmentActivity implements TaskDescrip {

    @Extra
    TaskObject.TaskDescription descriptionData;

    @ViewById
    EditText description;

    @Extra
    int taskId;

    String HOST_DESCRIPTION = Global.HOST + "/api/task/%s/description";


    boolean editMode = false;

    String preViewHtml = "";

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Global.syncCookie(this);

        Fragment fragment;
        String markdown = descriptionData.markdown;
        if (markdown.isEmpty()) {
            fragment = TaskDescripMdFragment_.builder().contentMd(markdown).build();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack("edit")
                    .commit();
        } else {
            fragment = TaskDescripHtmlFragment_.builder()
                    .contentMd(markdown)
                    .contentHtml(descriptionData.description)
                    .build();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_DESCRIPTION)) {
            hideProgressDialog();
            if (code == 0) {
                showButtomToast("修改描述成功");
                setResult(RESULT_OK);
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    static public class PreviewDialog extends DialogFragment {

        String content;

        public void setContent(String s) {
            content = s;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            View custom = getActivity().getLayoutInflater().inflate(R.layout.task_description_dialog, null);
            WebView webView = (WebView) custom.findViewById(R.id.webview);

            webView.getSettings().setJavaScriptEnabled(true);
            webView.setBackgroundColor(0);
            webView.getBackground().setAlpha(0);
            webView.getSettings().setDefaultTextEncodingName("UTF-8");
            webView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);


            builder.setView(custom)
                    .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });

            return builder.create();
        }
    }

    void showEdit(boolean show) {
//        if (show) {
//            description.setText(descriptionData.markdown);
//            description.setVisibility(View.VISIBLE);
//            descWeb.setVisibility(View.INVISIBLE);
//            startActionMode(mActionModeCallback);
//            Global.popSoftkeyboard(this, description, true);
//        } else {
//            description.setVisibility(View.INVISIBLE);
//            descWeb.setVisibility(View.VISIBLE);
//            Global.popSoftkeyboard(this, description, false);
//        }

        editMode = show;
    }


    @Override
    public void closeAndSave(String s) {
        Intent intent = new Intent();
        intent.putExtra("data", s);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public String createLocateHtml(String s) {
        try {
            final String bubble = Global.readTextFile(getAssets().open("topic-android"));
            return bubble.replace("${webview_content}", s);
        } catch (Exception e) {
            Global.errorLog(e);
            return "";
        }
    }
}
