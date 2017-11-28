package net.coding.program.project.detail;

import android.util.Log;
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

    @AfterViews
    protected void init() {
        hasOptionsMenu();
        mOldData = ((EditPreviewMarkdown) getActivity()).loadData();
        Log.e("TopicLabel", String.format("labels %s", mOldData.labels == null ? "null" : String.valueOf(mOldData.labels.size())));
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
        editPreviewMarkdown.saveData(new TopicData(title.getText().toString(), edit.getText().toString(), mOldData.labels));
        editPreviewMarkdown.switchPreview();
        Global.popSoftkeyboard(getActivity(), edit, false);
    }

    @OptionsItem(R.id.action_save)
    protected void actionSave() {
        EditPreviewMarkdown editPreviewMarkdown = (EditPreviewMarkdown) getActivity();
        editPreviewMarkdown.saveData(new TopicData(title.getText().toString(), edit.getText().toString(), mOldData.labels));
        editPreviewMarkdown.exit();
    }

    public boolean isContentModify() {
        // title 有可能为空，因为用户可能根本没有进入编辑界面
        if (title == null) {
            return false;
        }

        return !title.getText().toString().equals(mOldData.title) ||
                !edit.getText().toString().equals(mOldData.content);
    }

    public TopicData generalDraft() {
        return new TopicData(title.getText().toString(), edit.getText().toString(), mOldData.labels);
    }

}
