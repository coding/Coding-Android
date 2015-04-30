package net.coding.program.project.detail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.loopj.android.http.RequestParams;

import net.coding.program.BaseActivity;
import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.DialogUtil;
import net.coding.program.common.Global;
import net.coding.program.model.TopicLabelObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Neutra on 2015/4/25.
 */
@EActivity(R.layout.activity_topic_label)
@OptionsMenu(R.menu.topic_label)
public class TopicLabelActivity extends BaseActivity {

    @Extra
    String ownerUser;
    @Extra
    String projectName;
    @Extra
    Integer topicId;
    @Extra
    List<TopicLabelObject> checkedLabels;

    @ViewById
    LinearLayout labelsList;
    @ViewById
    EditText editText;
    @ViewById
    View action_add, container;

    private static final String URI_GET_LABEL = "/api/user/%s/project/%s/topics/labels";
    private static final String URI_ADD_LABEL = "/api/user/%s/project/%s/topics/label";
    private static final String URI_REMOVE_LABEL = URI_ADD_LABEL + "/%s";
    private static final String URI_RENAME_LABEL = URI_REMOVE_LABEL;
    private static final String URI_SAVE_TOPIC_LABELS = "/api/user/%s/project/%s/topics/%s/labels";
    private static final String COLOR = "#701035";

    private DialogUtil.BottomPopupWindow mPopupWindow;

    private LinkedHashMap<Integer, TopicLabelObject> allLabels = new LinkedHashMap<>();
    private HashSet<Integer> checkedIds = new HashSet<>();
    private String oldCheckedIds;

    private String currentLabelName;
    private int currentLabelId;

    @AfterViews
    void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        checkedIds.clear();
        if (checkedLabels != null) {
            for (TopicLabelObject item : checkedLabels) {
                checkedIds.add(item.id);
            }
        }
        oldCheckedIds = getIds(checkedIds);

        beginLoadLabels();
    }

    @OptionsItem(android.R.id.home)
    void home() {
        onBackPressed();
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
        if (topicId != null) {
            beginSaveTopicLabels();
        } else {
            endSaveTopicLabels();
        }
    }


    private boolean isBuzy;

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
            String url = Global.HOST + String.format(URI_GET_LABEL, ownerUser, projectName);
            getNetwork(url, "URI_GET_LABEL");
        }
    }

    private void endLoadLabels(int code, JSONObject json) throws JSONException {
        if (code != 0) {
            showErrorMsg(code, json);
        } else {
            allLabels.clear();
            JSONArray array = json.getJSONArray("data");
            for (int i = 0, n = array.length(); i < n; i++) {
                TopicLabelObject data = new TopicLabelObject(array.optJSONObject(i));
                allLabels.put(data.id, data);
            }
            updateList();
        }
        unlockViews();
    }

    private void beginAddLebel(String name) {
        currentLabelName = name.trim();
        String url = Global.HOST + String.format(URI_ADD_LABEL, ownerUser, projectName);
        RequestParams body = new RequestParams();
        body.put("name", currentLabelName);
        body.put("color", COLOR);
        postNetwork(url, body, "URI_ADD_LABEL");
    }

    private void endAddLabel(int code, JSONObject json) throws JSONException {
        if (code != 0) {
            showErrorMsg(code, json);
        } else {
            currentLabelId = json.getInt("data");
            editText.setText("");
            allLabels.put(currentLabelId, new TopicLabelObject(currentLabelId, currentLabelName));
            updateList();
        }
        unlockViews();
    }

    private void beginRemoveLabel() {
        String url = Global.HOST + String.format(URI_REMOVE_LABEL, ownerUser, projectName, currentLabelId);
        deleteNetwork(url, "URI_REMOVE_LABEL");
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
        String url = Global.HOST + String.format(URI_RENAME_LABEL, ownerUser, projectName, currentLabelId);
        RequestParams body = new RequestParams();
        body.put("name", newName);
        body.put("color", COLOR);
        putNetwork(url, body, "URI_RENAME_LABEL");
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
        String newLabelIds = getIds(checkedIds);
        if (oldCheckedIds.equals(newLabelIds)) {
            endSaveTopicLabels();
        } else {
            if (lockViews()) {
                String url = Global.HOST + String.format(URI_SAVE_TOPIC_LABELS, ownerUser, projectName, topicId);
                RequestParams body = new RequestParams();
                body.put("label_id", newLabelIds);
                postNetwork(url, body, "URI_SAVE_TOPIC_LABELS");
            }
        }
    }

    private void endSaveTopicLabels() {
        ArrayList<TopicLabelObject> result = new ArrayList<>();
        for (int id : checkedIds) {
            TopicLabelObject labelObject = allLabels.get(id);
            if (labelObject != null) result.add(labelObject);
        }
        Intent data = new Intent();
        data.putExtra("labels", result);
        setResult(RESULT_OK, data);
        finish();
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

    private static String getIds(Collection<Integer> list) {
        StringBuilder labelIds = new StringBuilder();
        for (int id : list) labelIds.append(id + ",");
        if (labelIds.length() > 0) labelIds.deleteCharAt(labelIds.length() - 1);
        return labelIds.toString();
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
}
