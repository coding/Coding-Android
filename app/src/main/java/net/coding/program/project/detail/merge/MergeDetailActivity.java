package net.coding.program.project.detail.merge;

import android.content.DialogInterface;
import android.content.Intent;
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
    private static final String HOST_DELETE_COMMENT = "HOST_DELETE_COMMENT";

    MyImageGetter myImageGetter = new MyImageGetter(this);

    public static final int RESULT_COMMENT = 1;

    @AfterViews
    protected final void initMergeDetailActivity() {
        getSupportActionBar().setTitle(mMerge.getTitle());

        String uri = mMerge.getHttpComments();
        getNetwork(uri, HOST_MERGE_COMMENTS);

        View head = mInflater.inflate(R.layout.activity_merge_detail_head, null);
        initHead(head);
        listView.addHeaderView(head);
        View footer = mInflater.inflate(R.layout.activity_merge_detail_footer, null);
        listView.addFooterView(footer);
        initFooter(footer);

        BaseCommentHolder.BaseCommentParam param = new BaseCommentHolder.BaseCommentParam(mOnClickItem, myImageGetter, getImageLoad(), mOnClickUser);
        mAdapter = new MergeCommentAdaper(param);
        listView.setAdapter(mAdapter);
    }

    View.OnClickListener mOnClickItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final BaseComment item = (BaseComment) v.getTag();
            if (item.isMy()) {
                showDialog("merge", "删除评论?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = mMerge.getHttpDeleteComment(item);
                        deleteNetwork(url, HOST_DELETE_COMMENT, item);
                    }
                });
            } else {
                CommentActivity_.intent(MergeDetailActivity.this).mMerge(mMerge).startForResult(RESULT_COMMENT);
            }
        }
    };

    private void initHead(View head) {
        head.findViewById(R.id.itemCommit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommitListActivity_.intent(MergeDetailActivity.this).mMerge(mMerge).start();
            }
        });

        head.findViewById(R.id.itemFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void initFooter(View footer) {
        footer.findViewById(R.id.itemAddComment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentActivity_.intent(MergeDetailActivity.this).mMerge(mMerge).startForResult(RESULT_COMMENT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_COMMENT) {
            if (resultCode == RESULT_OK) {
                showMiddleToast("有");
                BaseComment comment = (BaseComment) data.getSerializableExtra("data");
                mAdapter.appendData(comment);
                mAdapter.notifyDataSetChanged();
            } else {
                showMiddleToast("没有");
            }
        }
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
        } else if (tag.equals(HOST_DELETE_COMMENT)) {
            if (code == 0) {
                mAdapter.removeDataUpdate(data);
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }
}
