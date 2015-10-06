package net.coding.program.project.detail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.model.PostRequest;
import net.coding.program.model.TopicLabelObject;
import net.coding.program.model.TopicObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Neutra on 2015/4/25.
 * 标签编辑
 */
@EActivity(R.layout.activity_topic_label)
public class TopicLabelActivity extends BackActivity {

    private static String COLOR = "#701035";
//    @Extra
//    String ownerUser;
//    @Extra
//    String projectName;
//    @Extra
//    Integer topicId;

    @Extra
    LabelType labelType = LabelType.Topic;
    @Extra
    String projectPath; // 必填
    @Extra
    int id = 0; // 必填
    @Extra
    List<TopicLabelObject> checkedLabels; // 必填

    TopicObject.LabelUrl labelUrl;
    @ViewById
    LinearLayout labelsList;
    @ViewById
    EditText editText;
    @ViewById
    View action_add, container;
    @InstanceState
    String currentLabelName;
    @InstanceState
    int currentLabelId;
    private LinkedHashMap<Integer, TopicLabelObject> allLabels = new LinkedHashMap<>();
    private LinkedHashSet<Integer> checkedIds = new LinkedHashSet<>();
    private boolean saveTopicLabels = false;
    private boolean isBuzy;
    private MenuItem menuActionSave;

    public static void removeEmptyLabel(ArrayList<TopicLabelObject> list) {
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i).isEmpty()) {
                list.remove(i);
                --i;
            }
        }
    }

    @AfterViews
    protected final void initTopicLabelActivity() {
        if (labelType == LabelType.Topic) {
            labelUrl = new TopicObject.TopicLabelUrl(projectPath, id);
        } else {
            labelUrl = new TopicObject.TaskLabelUrl(projectPath, id);
        }

        checkedIds.clear();
        if (checkedLabels == null) checkedLabels = new ArrayList<>();
        for (TopicLabelObject item : checkedLabels) {
            checkedIds.add(item.id);
        }

        beginLoadLabels();
    }

    @Click
    void action_add() {
        if (lockViews()) {
            String name = editText.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                showButtomToast("名字不能为空");
                unlockViews();
                return;
            }
            action_add.setEnabled(false);
            editText.setEnabled(false);
            beginAddLebel(name);
        }
    }

    @OptionsItem
    void action_save() {
        if (id != 0) {
            beginSaveTopicLabels();
        } else {
            saveTopicLabels = true;
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(net.coding.program.R.menu.topic_label, menu);
        menuActionSave = menu.findItem(R.id.action_save);
        menuActionSave.setEnabled(false);

        return super.onCreateOptionsMenu(menu);
    }

    private void updateMenuActionSave() {
        if (isLabelsChanged(false)) {
            menuActionSave.setEnabled(true);
            menuActionSave.setIcon(R.drawable.ic_menu_ok);
        } else {
            menuActionSave.setEnabled(false);
            menuActionSave.setIcon(R.drawable.ic_menu_ok_unable);
        }
    }

    private boolean lockViews() {
        if (!isBuzy) {
            editText.setEnabled(false);
            action_add.setEnabled(false);
            isBuzy = true;
            showDialogLoading();
            return true;
        }
        return false;
    }

    private void unlockViews() {
        hideProgressDialog();
        isBuzy = false;
        editText.setEnabled(true);
        action_add.setEnabled(true);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        // 不同操作可能URI一样，METHOD不一样
        if ("URI_GET_LABEL".equals(tag)) {
            endLoadLabels(code, respanse);
        } else if ("URI_ADD_LABEL".equals(tag)) {
            endAddLabel(code, respanse);
        } else if ("URI_REMOVE_LABEL".equals(tag)) {
            endRemoveLabel(code, respanse);
        } else if ("URI_RENAME_LABEL".equals(tag)) {
            endRenameLabel(code, respanse);
        } else if ("URI_SAVE_TOPIC_LABELS".equals(tag)) {
            endSaveTopicLabels(code, respanse);
        }
    }

    private void beginLoadLabels() {
        if (lockViews()) {
            getNetwork(labelUrl.getLabels(), "URI_GET_LABEL");
        }
    }

    private void endLoadLabels(int code, JSONObject json) throws JSONException {
        if (code == 0) {
            allLabels.clear();
            JSONArray array = json.getJSONArray("data");
            for (int i = 0, n = array.length(); i < n; i++) {
                TopicLabelObject data = new TopicLabelObject(array.optJSONObject(i));
                if (!data.isEmpty()) {
                    allLabels.put(data.id, data);
                }
            }
            updateList();
        } else {
            showErrorMsg(code, json);
        }
        unlockViews();
    }

    private void beginAddLebel(String name) {
        currentLabelName = name.trim();
        COLOR = String.format("#%06X", new Random().nextInt(0xffffff));
        PostRequest post = labelUrl.addLabel(currentLabelName, COLOR);
        postNetwork(post.url, post.params, "URI_ADD_LABEL");
    }

    private void endAddLabel(int code, JSONObject json) throws JSONException {
        if (code == 0) {
            currentLabelId = json.getInt("data");
            editText.setText("");
            allLabels.put(currentLabelId, new TopicLabelObject(currentLabelId, currentLabelName, Color.parseColor(COLOR)));
            updateList();
            showButtomToast("添加标签成功^^");
        } else {
            showErrorMsg(code, json);
        }
        unlockViews();
    }

    private void beginRemoveLabel() {
        deleteNetwork(labelUrl.removeLabel(currentLabelId), "URI_REMOVE_LABEL");
    }

    private void endRemoveLabel(int code, JSONObject json) {
        if (code != 0) {
            showErrorMsg(code, json);
        } else {
            allLabels.remove(currentLabelId);
            updateList();
        }
        unlockViews();
    }

    private void beginRenameLabel(String newName) {
        currentLabelName = newName;
        PostRequest postRename = labelUrl.renameLabel(currentLabelId, newName, COLOR);
        putNetwork(postRename.url, postRename.params, "URI_RENAME_LABEL");
    }

    private void endRenameLabel(int code, JSONObject json) {
        if (code != 0) {
            showErrorMsg(code, json);
        } else {
            if (allLabels.containsKey(currentLabelId)) {
                allLabels.get(currentLabelId).name = currentLabelName;
            }
            updateList();
        }
        unlockViews();
    }

    private void beginSaveTopicLabels() {
        if (!isLabelsChanged(false)) {
            endSaveTopicLabels();
        } else {
            if (lockViews()) {
                PostRequest postSave = labelUrl.saveTopic(checkedIds);
                postNetwork(postSave.url, postSave.params, "URI_SAVE_TOPIC_LABELS");
            }
        }
    }

    private void endSaveTopicLabels() {
        saveTopicLabels = true;
        finish();
    }

    private boolean isLabelsChanged(boolean checkNames) {
        if (checkedIds.size() != checkedLabels.size()) return true;
        for (TopicLabelObject oldItem : checkedLabels) {
            if (!checkedIds.contains(oldItem.id)) return true;
            if (checkNames) {
                TopicLabelObject newItem = allLabels.get(oldItem.id);
                if (newItem == null || !newItem.name.equals(oldItem.name)) return true;
            }
        }
        return false;
    }

    @Override
    public void finish() {
        if (saveTopicLabels) {
            // 保存时直接返回新标签列表
            ArrayList<TopicLabelObject> result = new ArrayList<>();
            for (int id : checkedIds) {
                TopicLabelObject labelObject = allLabels.get(id);
                if (labelObject != null) result.add(labelObject);
            }
            Intent data = new Intent();
            data.putExtra("labels", result);
            setResult(RESULT_OK, data);
        } else {
            // 不保存时（后退），返回原选中的标签列表的最新状态（可能被删除或者重命名）
            if (isLabelsChanged(true)) {
                ArrayList<TopicLabelObject> result = new ArrayList<>();
                if (checkedLabels != null) {
                    for (TopicLabelObject item : checkedLabels) {
                        TopicLabelObject newItem = allLabels.get(item.id);
                        if (newItem != null) result.add(newItem);
                    }
                }
                Intent data = new Intent();
                data.putExtra("labels", result);
                setResult(RESULT_OK, data);
            }
        }
        super.finish();
    }

    private void endSaveTopicLabels(int code, JSONObject json) {
        if (code != 0) {
            showErrorMsg(code, json);
        } else {
            endSaveTopicLabels();
        }
        unlockViews();
    }

    private void endAddTopicLabel() {
        checkedIds.add(currentLabelId);
        onTopicLabelsChange();
    }

    private void endRemoveTopicLabel() {
        checkedIds.remove(currentLabelId);
        onTopicLabelsChange();
    }

    public void showPop(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setItems(R.array.topic_label_action, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                doRename();
                                break;
                            case 1:
                                doDelete();
                                updateMenuActionSave();
                                break;
                        }
                    }
                }).show();
        CustomDialog.dialogTitleLineColor(this, dialog);
    }

    private void doDelete() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("删除标签").setMessage(String.format("确定要删除标签“%s”么？", currentLabelName))
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (lockViews()) beginRemoveLabel();
                    }
                }).setNegativeButton("取消", null).create();
        dialog.show();
        dialogTitleLineColor(dialog);
    }

    private void doRename() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_input, null);
        final EditText input = (EditText) view.findViewById(R.id.value);
        input.setText(currentLabelName);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("重命名")
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString().trim();
                        if (TextUtils.isEmpty(newName)) {
                            showButtomToast("名字不能为空");
                            return;
                        }
                        if (lockViews()) beginRenameLabel(newName);
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
        dialogTitleLineColor(dialog);
        input.requestFocus();
    }

    private void updateCheckState() {
        for (int i = 0, n = labelsList.getChildCount(); i < n; i++) {
            View view = labelsList.getChildAt(i);
            if (view instanceof TopicLabelItemView) {
                TopicLabelItemView itemView = (TopicLabelItemView) view;
                itemView.setChecked(checkedIds.contains(itemView.data.id));
            }
        }
    }

    private void updateList() {
        LinkedList<View> cachedDividers = new LinkedList<>();
        LinkedList<TopicLabelItemView> cachedViews = new LinkedList<>();
        for (int i = 0, n = labelsList.getChildCount(); i < n; i++) {
            View view = labelsList.getChildAt(i);
            if (view instanceof TopicLabelItemView) {
                cachedViews.add((TopicLabelItemView) view);
            } else {
                cachedDividers.add(view);
            }
        }
        labelsList.removeAllViews();
        boolean isFirst = true;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (TopicLabelObject item : allLabels.values()) {
            if (isFirst) {
                isFirst = false;
            } else {
                addDivider(inflater, cachedDividers.poll());
            }
            addLabel(item, cachedViews.poll());
        }
        container.setVisibility(labelsList.getChildCount() > 0 ? View.VISIBLE : View.GONE);
    }

    private void addLabel(final TopicLabelObject data, TopicLabelItemView view) {
        if (view == null) {
            view = TopicLabelItemView_.build(this);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TopicLabelItemView view = (TopicLabelItemView) v;
                    currentLabelId = view.data.id;
                    currentLabelName = view.data.name;
                    showPop(v);
                    return true;
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TopicLabelItemView view = (TopicLabelItemView) v;
                    currentLabelId = view.data.id;
                    if (checkedIds.contains(view.data.id)) {
                        endRemoveTopicLabel();
                    } else {
                        endAddTopicLabel();
                    }
                    updateMenuActionSave();
                }
            });
        }
        view.bind(data);
        view.setChecked(checkedIds.contains(data.id));
        labelsList.addView(view);
    }

    private void addDivider(LayoutInflater inflater, View view) {
        if (view == null)
            view = inflater.inflate(R.layout.activity_topic_label_divider, labelsList, false);
        labelsList.addView(view);
    }

    private void onTopicLabelsChange() {
        updateCheckState();
    }

    public enum LabelType {
        Topic, Task
    }
}
