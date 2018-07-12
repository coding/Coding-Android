package net.coding.program.project.detail;

import android.widget.EditText;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MDEditFragment;
import net.coding.program.common.model.TopicLabelObject;
import net.coding.program.common.model.topic.TopicData;

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

    private TopicData mOldData;
    private TopicData newData = new TopicData();

    @AfterViews
    protected void initTopicEditFragment() {
        reloadData();
    }

    public void reloadData() {
        mOldData = ((EditPreviewMarkdown) getActivity()).loadData();
        title.setText(mOldData.title);
        String content = mOldData.content;
        edit.setText(content);
        edit.setSelection(content.length());
        updateLabels(mOldData.labels);
    }

    public void updateLabels(List<TopicLabelObject> labels) {
        if (labelBar != null && getActivity() instanceof TopicLabelBar.Controller) {
            labelBar.bind(labels, (TopicLabelBar.Controller) getActivity());
        }
    }

    public void switchPreview() {
        actionPreview();
    }

    @OptionsItem(R.id.action_preview)
    protected void actionPreview() {
        EditPreviewMarkdown editPreviewMarkdown = (EditPreviewMarkdown) getActivity();
        newData.title = title.getText().toString();
        newData.content = edit.getText().toString();
        newData.labels = mOldData.labels;
        editPreviewMarkdown.saveData(newData);
        editPreviewMarkdown.switchPreview();
        Global.popSoftkeyboard(getActivity(), edit, false);
    }

    @OptionsItem(R.id.action_save)
    protected void actionSave() {
        EditPreviewMarkdown editPreviewMarkdown = (EditPreviewMarkdown) getActivity();
        newData.title = title.getText().toString();
        newData.content = edit.getText().toString();
        newData.labels = mOldData.labels;
        editPreviewMarkdown.saveData(newData);
        editPreviewMarkdown.exit();
    }

    public boolean isContentModify() {
        String titleString = "";
        String contentString = "";
        // title 有可能为空，因为用户可能根本没有进入编辑界面
        if (title == null) {
            titleString = newData.title;
            contentString = newData.content;
        } else {
            titleString = title.getText().toString();
            contentString = edit.getText().toString();
        }

        return !titleString.equals(mOldData.title) ||
                !contentString.equals(mOldData.content);
    }

    public TopicData generalDraft() {
        return new TopicData(title.getText().toString(), edit.getText().toString(), mOldData.labels);
    }

}
