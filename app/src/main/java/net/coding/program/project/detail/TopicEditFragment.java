package net.coding.program.project.detail;

import android.util.Log;
import android.widget.EditText;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MDEditFragment;
import net.coding.program.model.TopicLabelObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EFragment(R.layout.fragment_topic_edit)
@OptionsMenu(R.menu.topic_detail_edit)
public class TopicEditFragment extends MDEditFragment {

    @ViewById
    protected EditText title;

    @ViewById
    protected TopicLabelBar labelBar;

    private TopicAddActivity.TopicData mOldData;

    @AfterViews
    protected void init() {
        hasOptionsMenu();
        mOldData = ((SaveData) getActivity()).loadData();
        Log.e("TopicLabel", String.format("labels %s", mOldData.labels == null ? "null" : String.valueOf(mOldData.labels.size())));
        title.setText(mOldData.title);
        String content = mOldData.content;
        edit.setText(content);
        edit.setSelection(content.length());
        updateLabels(mOldData.labels);
    }

    public void updateLabels(List<TopicLabelObject> labels) {
        if (labelBar != null && getActivity() != null) {
            labelBar.bind(labels, (TopicLabelBar.Controller) getActivity());
        }
    }

    @OptionsItem
    protected void action_preview() {
        SaveData saveData = (SaveData) getActivity();
        saveData.saveData(new TopicAddActivity.TopicData(title.getText().toString(), edit.getText().toString(), mOldData.labels));
        saveData.switchPreview();
        Global.popSoftkeyboard(getActivity(), edit, false);
    }

    @OptionsItem
    protected void action_save() {
        SaveData saveData = (SaveData) getActivity();
        saveData.saveData(new TopicAddActivity.TopicData(title.getText().toString(), edit.getText().toString(), mOldData.labels));
        saveData.exit();
    }

    public boolean isContentModify() {
        // title 有可能为空，因为用户可能根本没有进入编辑界面
        if (title == null) {
            return false;
        }

        //todo
        return !title.getText().toString().equals(mOldData.title) ||
                !edit.getText().toString().equals(mOldData.content);
    }

    public interface SaveData {
        void saveData(TopicAddActivity.TopicData data);

        TopicAddActivity.TopicData loadData();

        void switchPreview();

        void switchEdit();

        void exit();

        String getProjectPath();

        boolean isProjectPublic();

    }
}
