package net.coding.program.project.detail.merge;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.comment.BaseCommentParam;
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

    public static final int RESULT_COMMENT = 1;
    private static final String HOST_MERGE_COMMENTS = "HOST_MERGE_COMMENTS";
    private static final String HOST_DELETE_COMMENT = "HOST_DELETE_COMMENT";
    @Extra
    Merge mMerge;
    @ViewById
    ListView listView;
    MergeCommentAdaper mAdapter;
    MyImageGetter myImageGetter = new MyImageGetter(this);
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

        BaseCommentParam param = new BaseCommentParam(mOnClickItem, myImageGetter, getImageLoad(), mOnClickUser);
        mAdapter = new MergeCommentAdaper(param);
        listView.setAdapter(mAdapter);
    }

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
                MergeFileListActivity_.intent(MergeDetailActivity.this).mMerge(mMerge).start();
            }
        });

        ((TextView) head.findViewById(R.id.title)).setText(mMerge.getTitleSpannable());

        ImageView imageView = (ImageView) head.findViewById(R.id.icon);
        iconfromNetwork(imageView, mMerge.getAuthor().avatar);

        String timeString = "创建于 " + Global.dayToNow(mMerge.getCreatedAt());
        ((TextView) head.findViewById(R.id.time)).setText(timeString);

        TextView styleView = (TextView) head.findViewById(R.id.mergeStyle);


        String[] styles = Merge.STYLES;
        final String[] styleStrings = new String[]{
                "已接受",
                "已拒绝",
                "可合并",
                "已取消"
        };
        final int[] styleColors = new int[]{
                0xff3bbd79, 0xfffb3b30, 0xff3bbd79, 0xff666666
        };
        for (int i = 0; i < styles.length; ++i) {
            if (mMerge.getMergeStatus().equals(styles[i])) {
                styleView.setText(styleStrings[i]);
                styleView.setTextColor(styleColors[i]);
            }
        }

        String src = mMerge.getSrcBranch();
        String desc = mMerge.getDescBranch();

        ((TextView) head.findViewById(R.id.branchSrc)).setText(src);
        ((TextView) head.findViewById(R.id.branchDesc)).setText(desc);

//        mMerge.getActionAuthor().
//        ((TextView) findViewById(R.id.mergeLog)).setText(R.id.mergeLog);
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
                mAdapter.appendDataUpdate(arrayData);
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
