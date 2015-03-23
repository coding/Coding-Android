package net.coding.program.project.detail;

import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.loopj.android.http.RequestParams;

import net.coding.program.BaseActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TopicObject;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

@EActivity(R.layout.activity_topic_add)
public class TopicAddActivity extends BaseActivity implements TopicEditFragment.SaveData {

    @Extra
    protected ProjectObject projectObject;

    @Extra
    protected TopicObject topicObject;

    private TopicData modifyData = new TopicData();

    final String HOST_TOPIC_NEW =  Global.HOST + "/api/project/%s/topic?parent=0";
    final String HOST_TOPIC_EDIT = Global.HOST + "/api/topic/%d";

    String url = "";

    String HOST_TOPIC_DETAIL_CONTENT = Global.HOST + "/api/topic/%d?type=1";

    TopicEditFragment editFragment;
    Fragment previewFragment;

    @AfterViews
    protected void init() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        editFragment = TopicEditFragment_.builder().build();
        previewFragment = TopicPreviewFragment_.builder().build();
        if (isNewTopic()) {
            actionBar.setTitle(R.string.topic_create);
            url = String.format(HOST_TOPIC_NEW, getTopicId());
            getSupportFragmentManager().beginTransaction().replace(R.id.container, editFragment).commit();
        } else {
            actionBar.setTitle(R.string.topic_edit);
            url = String.format(HOST_TOPIC_EDIT, topicObject.id);
            getNetwork(String.format(HOST_TOPIC_DETAIL_CONTENT, topicObject.id), HOST_TOPIC_DETAIL_CONTENT);
        }
    }

    @OptionsItem(android.R.id.home)
    protected void back() {
        if (editFragment.isContentModify()) {
            showDialog("讨论", "确定放弃此次编辑？", new DialogInterface.OnClickListener() {
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
    public void onBackPressed() {
        back();
    }

    private boolean isNewTopic() {
        return topicObject == null;
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        showProgressBar(false);
        if (tag.equals(HOST_TOPIC_NEW) ||
                tag.equals(HOST_TOPIC_EDIT)) {
            if (code == 0) {
                Intent intent = new Intent();
                TopicObject topic = new TopicObject(respanse.getJSONObject("data"));
                intent.putExtra("topic", topic);
                setResult(RESULT_OK, intent);
                finish();
                showSuccess();
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(HOST_TOPIC_DETAIL_CONTENT)) {
            if (code == 0) {
                topicObject = new TopicObject(respanse.optJSONObject("data"));
                modifyData = new TopicData(topicObject.title, topicObject.content);

                getSupportFragmentManager().beginTransaction().replace(R.id.container, editFragment).commit();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    protected int getTopicId() {
        return projectObject.getId();
    }

    protected void showSuccess() {
    }

    protected String getSendingTip() {
        return "正在发表讨论...";
    }

    public static class TopicData implements Serializable {
        public String title = "";
        public String content = "";

        public TopicData(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public TopicData() {
        }
    }

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
        String titleString = modifyData.title;
        if (EmojiFilter.containsEmptyEmoji(this, titleString, "标题不能为空", "标题不能包含表情")) {
            return;
        }

        String contentString = modifyData.content;
        if (EmojiFilter.containsEmptyEmoji(this, contentString, "内容不能为空", "内容不能包含表情")) {
            return;
        }

        RequestParams params = new RequestParams();
        params.put("title", titleString);
        params.put("content", contentString);

        if (isNewTopic()) {
            postNetwork(url, params, HOST_TOPIC_NEW);
        } else {
            putNetwork(url, params, HOST_TOPIC_EDIT);
        }

        showProgressBar(true, getSendingTip());
    }

    @Override
    public int getProjectId() {
        return projectObject.getId();
    }

    @Override
    public boolean isProjectPublic() {
        return projectObject.isPublic();
    }
}
