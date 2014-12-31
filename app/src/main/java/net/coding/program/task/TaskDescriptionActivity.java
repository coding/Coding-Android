package net.coding.program.task;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.DialogFragment;
import android.app.Fragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

import net.coding.program.BaseFragmentActivity;
import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.model.TaskObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_task_description)
public class TaskDescriptionActivity extends BaseFragmentActivity implements TaskDescrip {

    @Extra
    TaskObject.TaskDescription descriptionData;

    @ViewById
    EditText description;

    @Extra
    String taskId;

    String HOST_DESCRIPTION = Global.HOST + "/api/task/%s/description";


    boolean editMode = false;

    String preViewHtml = "";

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment fragment;
        if (taskId.isEmpty()) {
            fragment = TaskDescripMdFragment_.builder().build();
        } else {
            fragment = TaskDescripHtmlFragment_.builder()
                    .contentMd(descriptionData.markdown)
                    .contentHtml(descriptionData.description)
                    .build();
        }

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();


//        if (taskId.isEmpty()) {
//            action_edit();
//            description.setText(descriptionData.markdown);
//
//        } else {
//            HOST_DESCRIPTION = String.format(HOST_DESCRIPTION, taskId);
//
//
//            description.setText(descriptionData.markdown);
//        }
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        if (editMode) {
//            getMenuInflater().inflate(R.menu.menu_task_edit_description, menu);
//        } else {
//            getMenuInflater().inflate(R.menu.menu_task_description, menu);
//        }
//
//        return true;
//    }

//    @OptionsItem
//    void action_edit() {
//        showEdit(true);
//    }
//
//    @OptionsItem
//    void action_save() {
//        closeAndSave("");
//    }

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


    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.clear();
            mode.getMenu().clear();
            mode.getMenuInflater().inflate(R.menu.task_description_edit, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_cancel:
                    closeAndSave("");
                    return true;

                case R.id.action_previewk:
                    showDialogLoading();

                    return true;

                case R.id.action_clear:
                    description.setText("");
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            showEdit(false);
        }
    };

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
