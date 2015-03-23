package net.coding.program.project.detail;


import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MDEditFragment;
import net.coding.program.common.network.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;


@EFragment(R.layout.fragment_topic_edit)
@OptionsMenu(R.menu.topic_detail_edit)
public class TopicEditFragment extends MDEditFragment {

    @ViewById
    protected EditText title;

    private TopicAddActivity.TopicData mOldData;

    @AfterViews
    protected void init() {
        hasOptionsMenu();

        mOldData = ((SaveData) getActivity()).loadData();
        title.setText(mOldData.title);
        edit.setText(mOldData.content);
    }

    @OptionsItem
    protected void action_preview() {
        SaveData saveData = (SaveData) getActivity();
        saveData.saveData(new TopicAddActivity.TopicData(title.getText().toString(), edit.getText().toString()));
        saveData.switchPreview();
        Global.popSoftkeyboard(getActivity(), edit, false);
    }

    @OptionsItem
    protected void action_save() {
        SaveData saveData = (SaveData) getActivity();
        saveData.saveData(new TopicAddActivity.TopicData(title.getText().toString(), edit.getText().toString()));
        saveData.exit();
    }

    public boolean isContentModify() {
        // title 有可能为空，因为用户可能根本没有进入编辑界面
        if (title == null) {
            return false;
        }

        return !title.getText().toString().equals(mOldData.title) ||
                        !edit.getText().toString().equals(mOldData.content);
    }

    public interface SaveData {
        public void saveData(TopicAddActivity.TopicData data);

        public TopicAddActivity.TopicData loadData();

        public void switchPreview();

        public void switchEdit();

        public void exit();

        public int getProjectId();

        boolean isProjectPublic();

    }
}
