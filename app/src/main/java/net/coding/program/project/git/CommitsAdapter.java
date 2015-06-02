package net.coding.program.project.git;

import net.coding.program.common.comment.BaseCommentParam;
import net.coding.program.model.Commit;
import net.coding.program.project.detail.merge.SimpleData1Adaper;

/**
 * Created by chenchao on 15/5/29.
 */
public class CommitsAdapter extends SimpleData1Adaper<Commit> {

    public CommitsAdapter(BaseCommentParam param) {
        super(param);
    }
}
