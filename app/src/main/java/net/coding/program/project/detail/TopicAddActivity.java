package net.coding.program.project.detail;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TopicLabelObject;
import net.coding.program.model.TopicObject;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_topic_add)
public class TopicAddActivity extends BackActivity implements TopicEditFragment.SaveData, TopicLabelBar.Controller {

    final String HOST_TOPIC_NEW = Global.HOST_API + "/project/%s/topic?parent=0";
    final String HOST_TOPIC_EDIT = Global.HOST_API + "/topic/%d";
    final int RESULT_LABEL = 1000;
    @Extra
    protected ProjectObject projectObject;
    @Extra
    protected TopicObject topicObject;
    @InstanceState
    protected boolean labelsHasChanged;
    String url = "";
    String HOST_TOPIC_DETAIL_CONTENT = Global.HOST_API + "/topic/%d?type=1";
    TopicEditFragment editFragment;
    TopicPreviewFragment previewFragment;
    private TopicData modifyData = new TopicData();

    @NonNull
    public static StringBuilder getLabelsParam(List<TopicLabelObject> labels) {
        StringBuilder pickLabels = new StringBuilder();
        if (labels != null && labels.size() > 0) {
            pickLabels.append(labels.get(0).id);
            for (int i = 1; i < labels.size(); ++i) {
                pickLabels.append(",");
                pickLabels.append(labels.get(i).id);
            }
        }
        return pickLabels;
    }

    @AfterViews
    protected void initTopicAddActivity() {
        ActionBar actionBar = getSupportActionBar();

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

    @Override
    public void onBackPressed() {
        if (labelsHasChanged || editFragment.isContentModify()) {
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

    private boolean isNewTopic() {
        return topicObject == null;
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        showProgressBar(false);
        if (tag.equals(HOST_TOPIC_NEW) ||
                tag.equals(HOST_TOPIC_EDIT)) {
            if (code == 0) {
                if (tag.equals(HOST_TOPIC_NEW)) {
                    umengEvent(UmengEvent.TOPIC, "新建讨论");
                } else if (tag.equals(HOST_TOPIC_EDIT)) {
                    umengEvent(UmengEvent.TOPIC, "修改讨论");
                }

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
                modifyData = new TopicData(topicObject);

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

    @Override
    public boolean canShowLabels() {
        return true;
    }

    @Override
    public boolean canEditLabels() {
        return true;
    }

    @Override
    public void onEditLabels(TopicLabelBar view) {
        TopicLabelActivity_.intent(this)
                .labelType(TopicLabelActivity.LabelType.Topic)
                .projectPath(projectObject.getProjectPath())
                .id(isNewTopic() ? 0 : topicObject.id)
                .checkedLabels(modifyData != null ? modifyData.labels : isNewTopic() ? null : topicObject.labels)
                .startForResult(RESULT_LABEL);
    }

    @Override
    public void onRemoveLabel(TopicLabelBar view, int labelId) {
        view.removeLabel(labelId);
        for (TopicLabelObject item : modifyData.labels) {
            if (item.id == labelId) {
                modifyData.labels.remove(item);
                break;
            }
        }
        labelsHasChanged = true;
        saveLabelsIfCancel();
    }

    private void saveLabelsIfCancel() {
        Intent intent = new Intent();
        intent.putExtra("labels", new ArrayList<>(modifyData.labels));
        setResult(RESULT_CANCELED, intent);
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

        StringBuilder pickLabels = getLabelsParam(modifyData.labels);
        RequestParams params = new RequestParams();
        params.put("title", titleString);
        params.put("content", contentString);
        params.put("label", pickLabels);

        if (isNewTopic()) {
            postNetwork(url, params, HOST_TOPIC_NEW);
        } else {
            putNetwork(url, params, HOST_TOPIC_EDIT);
        }

        showProgressBar(true, getSendingTip());
    }

    @Override
    public String getProjectPath() {
        return projectObject.getProjectPath();
    }

    @Override
    public boolean isProjectPublic() {
        return projectObject.isPublic();
    }

    @OnActivityResult(RESULT_LABEL)
    protected void onResultLabel(int code, @OnActivityResult.Extra ArrayList<TopicLabelObject> labels) {
        if (code == RESULT_OK) {
            modifyData.labels = labels;
            editFragment.updateLabels(modifyData.labels);
            previewFragment.updateLabels(modifyData.labels);
            labelsHasChanged = true;
            saveLabelsIfCancel();
        }
    }

    public static class TopicData implements Serializable {
        public List<TopicLabelObject> labels = new ArrayList<>();
        public String title = "";
        public String content = "";

        public TopicData(TopicObject topicObject) {
            this.title = topicObject.title;
            this.content = topicObject.content;
            this.labels = topicObject.labels;
        }

        public TopicData(String title, String content, List<TopicLabelObject> labels) {
            this.title = title;
            this.content = content;
            this.labels = labels;
        }

        public TopicData() {
        }
    }
}
