package net.coding.program.project.detail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.loopj.android.http.RequestParams;

import net.coding.program.BaseActivity;
import net.coding.program.R;
import net.coding.program.common.DialogUtil;
import net.coding.program.common.Global;
import net.coding.program.model.TopicLabelObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Neutra on 2015/4/25.
 */
@EActivity(R.layout.activity_topic_label)
public class TopicLabelActivity extends BaseActivity {

    @Extra
    String projectId;
    @Extra
    int topicId;
    @Extra
    List<TopicLabelObject> checkedLabels;
    @ViewById
    LinearLayout labelsList;
    @ViewById
    EditText editText;
    @ViewById
    View action_add, container;

    private static final String URI_GET_LABEL = "/api/project/%s/topic/label";
    private static final String URI_ADD_LABEL = "/api/project/%s/topic/label";
    private static final String URI_REMOVE_LABEL = URI_ADD_LABEL + "/%s";
    private static final String URI_RENAME_LABEL = URI_REMOVE_LABEL;
    private static final String URI_ADD_TOPIC_LABEL = "/api/topic/%s/label/%s";
    private static final String URI_REMOVE_TOPIC_LABEL = URI_ADD_TOPIC_LABEL;
    private static final String COLOR = "#701035";

    private DialogUtil.BottomPopupWindow mPopupWindow;

    private LinkedHashMap<Integer, TopicLabelObject> allLabels = new LinkedHashMap<Integer, TopicLabelObject>();
    private HashSet<Integer> checkedIds = new HashSet<>();

    private String currentLabelName;
    private int currentLabelId;

    @AfterViews
    void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initBottomPop();

        checkedIds.clear();
        if (checkedLabels != null) {
            for (TopicLabelObject item : checkedLabels) {
                checkedIds.add(item.id);
            }
        }
        beginLoadLabels();
    }

    @OptionsItem(android.R.id.home)
    void home() {
        onBackPressed();
    }


    private boolean isBuzy;

    private boolean lockViews() {
        if (!isBuzy) {
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
        action_add.setEnabled(true);
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

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        // 不同操作可能URI一样，METHOD不一样item
        if ("URI_GET_LABEL".equals(tag)) {
            endLoadLabels(code, respanse);
        } else if ("URI_ADD_LABEL".equals(tag)) {
            endAddLabel(code, respanse);
        } else if ("URI_REMOVE_LABEL".equals(tag)) {
            endRemoveLabel(code, respanse);
        } else if ("URI_RENAME_LABEL".equals(tag)) {
            endRenameLabel(code, respanse);
        } else if ("URI_ADD_TOPIC_LABEL".equals(tag)) {
            endAddTopicLabel(code, respanse);
        } else if ("URI_REMOVE_TOPIC_LABEL".equals(tag)) {
            endRemoveTopicLabel(code, respanse);
        }
    }

    private void beginLoadLabels() {
        if (lockViews()) {
            String url = Global.HOST + String.format(URI_GET_LABEL, projectId);
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
        String url = Global.HOST + String.format(URI_ADD_LABEL, projectId);
        RequestParams body = new RequestParams();
        body.put("name", currentLabelName);
        body.put("color", COLOR);
        postNetwork(url, body, "URI_ADD_LABEL");
    }

    private void endAddLabel(int code, JSONObject json) throws JSONException {
        editText.setEnabled(true);
        action_add.setEnabled(true);
        if (code != 0) {
            showErrorMsg(code, json);
            unlockViews();
        } else {
            currentLabelId = json.getInt("data");
            editText.setText("");
            allLabels.put(currentLabelId, new TopicLabelObject(currentLabelId, currentLabelName));
            updateList();
            if (topicId > 0) {
                beginAddTopicLabel();
            } else {
                endAddTopicLabel();
                unlockViews();
            }
        }
    }

    private void beginRemoveLabel() {
        String url = Global.HOST + String.format(URI_REMOVE_LABEL, projectId, currentLabelId);
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
        String url = Global.HOST + String.format(URI_RENAME_LABEL, projectId, currentLabelId);
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

    private void beginAddTopicLabel() {
        String url = Global.HOST + String.format(URI_ADD_TOPIC_LABEL, topicId, currentLabelId);
        postNetwork(url, new RequestParams(), "URI_ADD_TOPIC_LABEL");
    }

    private void endAddTopicLabel() {
        checkedIds.add(currentLabelId);
        onTopicLabelsChange();
    }

    private void endAddTopicLabel(int code, JSONObject json) {
        if (code != 0) {
            showErrorMsg(code, json);
        } else {
            endAddTopicLabel();
        }
        unlockViews();
    }

    private void beginRemoveTopicLabel() {
        String url = Global.HOST + String.format(URI_REMOVE_TOPIC_LABEL, topicId, currentLabelId);
        deleteNetwork(url, "URI_REMOVE_TOPIC_LABEL");
    }

    private void endRemoveTopicLabel() {
        checkedIds.remove(currentLabelId);
        onTopicLabelsChange();
    }

    private void endRemoveTopicLabel(int code, JSONObject json) {
        if (code != 0) {
            showErrorMsg(code, json);
        } else {
            endRemoveTopicLabel();
        }
        unlockViews();
    }

    public void initBottomPop() {
        if (mPopupWindow == null) {
            ArrayList<DialogUtil.BottomPopupItem> popupItemArrayList = new ArrayList<DialogUtil.BottomPopupItem>();
            DialogUtil.BottomPopupItem renameItem = new DialogUtil.BottomPopupItem("重命名", R.drawable.ic_popup_attachment_rename);
            popupItemArrayList.add(renameItem);
            DialogUtil.BottomPopupItem deleteItem = new DialogUtil.BottomPopupItem("删除", R.drawable.ic_popup_attachment_delete_selector);
            popupItemArrayList.add(deleteItem);
            mPopupWindow = DialogUtil.initBottomPopupWindow(this, "编辑标签", popupItemArrayList, onPopupItemClickListener);
        }
    }

    private AdapterView.OnItemClickListener onPopupItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 0:
                    doRename();
                    break;
                case 1:
                    doDelete();
                    break;
            }
            mPopupWindow.dismiss();
        }
    };

    public void showPop(View view) {
        initBottomPop();
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
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
        labelsList.removeAllViews();
        boolean isFirst = true;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (TopicLabelObject item : allLabels.values()) {
            if (isFirst) {
                isFirst = false;
            } else {
                addDivider(inflater);
            }
            addLabel(item);
        }
        container.setVisibility(labelsList.getChildCount() > 0 ? View.VISIBLE : View.GONE);
    }

    private void addLabel(final TopicLabelObject data) {
        final TopicLabelItemView view = TopicLabelItemView_.build(this);
        ;
        view.bind(data, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLabelId = view.data.id;
                currentLabelName = view.data.name;
                showPop(v);
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (topicId > 0) {
                    if (lockViews()) {
                        currentLabelId = view.data.id;
                        if (checkedIds.contains(view.data.id)) {
                            beginRemoveTopicLabel();
                        } else {
                            beginAddTopicLabel();
                        }
                    }
                } else {
                    currentLabelId = view.data.id;
                    if (checkedIds.contains(view.data.id)) {
                        endRemoveTopicLabel();
                    } else {
                        endAddTopicLabel();
                    }
                }
            }
        });
        view.setChecked(checkedIds.contains(data.id));
        labelsList.addView(view);
    }

    private void addDivider(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.activity_topic_label_divider, labelsList, false);
        labelsList.addView(view);
    }

    private boolean topicLabelChanged;

    private void onTopicLabelsChange() {
        topicLabelChanged = true;
        updateCheckState();
        setResult(RESULT_OK);
    }

    @Override
    public void finish() {
        if (topicLabelChanged) {
            ArrayList<TopicLabelObject> result = new ArrayList<>();
            for (int id : checkedIds) {
                TopicLabelObject labelObject = allLabels.get(id);
                if (labelObject != null) result.add(labelObject);
            }
            Intent data = new Intent();
            data.putExtra("labels", result);
            setResult(RESULT_OK, data);
        }
        super.finish();
    }
}
