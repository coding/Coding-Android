package net.coding.program.common.base;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.project.detail.EditPreviewMarkdown;
import net.coding.program.project.detail.TopicEditFragment;
import net.coding.program.project.detail.TopicPreviewFragment;

public abstract class MDEditPreviewActivity extends BackActivity implements EditPreviewMarkdown {


    protected TopicEditFragment editFragment;
    protected TopicPreviewFragment previewFragment;

    protected void initEditPreviewFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, editFragment)
                .add(R.id.container, previewFragment)
                .commit();
    }

    @Override
    public void switchPreview() {
        getSupportFragmentManager().beginTransaction().show(previewFragment).hide(editFragment).commit();
    }

    @Override
    public void switchEdit() {
        getSupportFragmentManager().beginTransaction().show(editFragment).hide(previewFragment).commit();
    }

    public void reloadData() {
        editFragment.reloadData();
    }

    @Override
    public void onBackPressed() {
        if (editFragment.isContentModify()) {
            showDialog("确定放弃此次编辑？", (dialog, which) -> finish());
        } else {
            finish();
        }
    }
}
