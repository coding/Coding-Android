package net.coding.program.task;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

import net.coding.program.BaseActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.TaskObject;
import net.coding.program.project.detail.TopicAddActivity;
import net.coding.program.project.detail.TopicAddActivity.TopicData;
import net.coding.program.project.detail.TopicEditFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_task_description)
public class TaskDescriptionActivity extends BaseActivity implements TaskDescrip, TopicEditFragment.SaveData {

    @Extra
    TaskObject.TaskDescription descriptionData;

    @ViewById
    EditText description;

    @Extra
    int taskId;

    @Extra
    int projectId;

    String HOST_DESCRIPTION = Global.HOST + "/api/task/%s/description";

    boolean editMode = false;

    String preViewHtml = "";

    TaskDespEditFragment editFragment;
    Fragment previewFragment;

    @AfterViews
    void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editFragment = TaskDespEditFragment_.builder().build();
        previewFragment = TaskDespPreviewFragment_.builder().build();

        String markdown = descriptionData.markdown;
        if (markdown.isEmpty()) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, editFragment).commit();
        } else {
            modifyData.content = markdown;
            getSupportFragmentManager().beginTransaction().replace(R.id.container, previewFragment).commit();
        }
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (editFragment.isContentModify()) {
            showDialog("任务描述", "确定放弃此次编辑？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        } else {
            finish();
        }
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

    private TopicData modifyData = new TopicData();

    @Override
    public void saveData(TopicData data) {
        modifyData = data;
    }

    @Override
    public TopicAddActivity.TopicData loadData() {
        return modifyData;
    }

    @Override
    public void switchPreview() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, previewFragment).commit();
    }

    @Override
    public void switchEdit() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, editFragment).commit();
    }

    @Override
    public void exit() {
        Intent intent = new Intent();
        intent.putExtra("data", modifyData.content);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public int getProjectId() {
        return projectId;
    }

    // 有任务的项目必定是私有项目
    @Override
    public boolean isProjectPublic() {
        return false;
    }
}
