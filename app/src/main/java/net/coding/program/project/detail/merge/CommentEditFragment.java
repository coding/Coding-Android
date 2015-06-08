package net.coding.program.project.detail.merge;


import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.project.detail.TopicEditFragment;

import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_comment_edit)
public class CommentEditFragment extends TopicEditFragment {

    public boolean isEmpty() {
        return Global.isEmptyContainSpace(title);
    }
}
