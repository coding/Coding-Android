package net.coding.program.project.detail.merge;

import android.view.View;
import android.widget.ListView;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.comment.BaseCommentHolder;
import net.coding.program.model.BaseComment;
import net.coding.program.model.Merge;
import net.coding.program.project.git.CommitListActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.activity_merge_detail)
//@OptionsMenu(R.menu.menu_merge_detail)
public class MergeDetailActivity extends BackActivity {

    @Extra
    Merge mMerge;

    @ViewById
    ListView listView;

    MergeCommentAdaper mAdapter;

    private static final String HOST_MERGE_COMMENTS = "HOST_MERGE_COMMENTS";

    MyImageGetter myImageGetter = new MyImageGetter(this);

    @AfterViews
    protected final void initMergeDetailActivity() {
        getSupportActionBar().setTitle(mMerge.getTitle());

        String uri = mMerge.getHttpComments();
        getNetwork(uri, HOST_MERGE_COMMENTS);

        BaseCommentHolder.BaseCommentParam param = new BaseCommentHolder.BaseCommentParam(null, myImageGetter, getImageLoad(), mOnClickUser);
        mAdapter = new MergeCommentAdaper(param);

        View head = mInflater.inflate(R.layout.activity_merge_detail_head, null);
        initHead(head);
        listView.addHeaderView(head);
        View footer = mInflater.inflate(R.layout.activity_merge_detail_footer, null);
        listView.addFooterView(footer);
        listView.setAdapter(mAdapter);
    }

    private void initHead(View head) {
        head.findViewById(R.id.itemCommit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommitListActivity_.intent(MergeDetailActivity.this).mMerge(mMerge).start();
            }
        });
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_MERGE_COMMENTS)) {
            if (code == 0) {
                JSONArray json = respanse.getJSONArray("data");
                ArrayList<BaseComment> arrayData = new ArrayList<>();
                for (int i = 0; i < json.length(); ++i) {
                    BaseComment comment = new BaseComment(json.getJSONArray(i).getJSONObject(0));
                    arrayData.add(comment);
                }
                mAdapter.appendData(arrayData);
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @Click
    protected final void itemCommit() {
    }

    @Click
    protected final void itemFile() {
        CommitListActivity_.intent(this).mMerge(mMerge).start();
    }

    @Click
    protected final void itemAddComment() {
    }
}
