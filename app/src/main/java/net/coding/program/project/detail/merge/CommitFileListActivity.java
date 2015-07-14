package net.coding.program.project.detail.merge;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.Global;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.comment.BaseCommentParam;
import net.coding.program.model.BaseComment;
import net.coding.program.model.Commit;
import net.coding.program.model.DiffFile;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.activity_commit_file_list)
//@OptionsMenu(R.menu.menu_commit_file_list)
public class CommitFileListActivity extends BackActivity {

    public static final int RESULT_COMMENT = 1;
    private static final String HOST_COMMIT_FILES = "HOST_COMMIT_FILES";
    private static final String HOST_MERGE_COMMENTS = "HOST_MERGE_COMMENTS";
    private static final String HOST_DELETE_COMMENT = "HOST_DELETE_COMMENT";
    private static final String HOST_COMMIT_DETAIL = "HOST_COMMIT_DETAIL";

    @Extra
    Commit mCommit;
    @Extra
    String mProjectPath = "";
    @Extra
    String mCommitUrl;

    @ViewById
    ListView listView;
    CommitFileAdapter mAdapter;
    private View mListHead;

    private View.OnClickListener mOnClickItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final BaseComment item = (BaseComment) v.getTag();
            if (item.isMy()) {
                showDialog("merge", "删除评论?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = Commit.getHttpDeleteComment(mProjectPath, item.id);
                        deleteNetwork(url, HOST_DELETE_COMMENT, item);
                    }
                });
            } else {
                startSendCommentActivity();
            }
        }
    };

    @ItemClick
    public final void listView(Object data) {
        if (data instanceof DiffFile.DiffSingleFile) {
            DiffFile.DiffSingleFile fileData = (DiffFile.DiffSingleFile) data;
            MergeFileDetailActivity_.intent(CommitFileListActivity.this).mProjectPath(mProjectPath).mSingleFile(fileData).start();
        }
    }

    @AfterViews
    protected final void initCommitFileListActivity() {
        if (mCommit != null) {
            initByCommit();
        } else {
            String s = mCommitUrl.split("#")[0];
            s = s.replace("/u/", "/api/user/")
                    .replace("/p/", "/project/");
            int start = s.indexOf("/user/");
            int end = s.indexOf("/git/");
            mProjectPath = s.substring(start, end);

            getNetwork(s, HOST_COMMIT_DETAIL);
            showDialogLoading();
        }
    }

    private void initByCommit() {
        BaseCommentParam param = new BaseCommentParam(new ClickSmallImage(this), mOnClickItem, new MyImageGetter(this), getImageLoad(), mOnClickUser);
        mAdapter = new CommitFileAdapter(param);

        initListhead();
        initListFooter();

        listView.setAdapter(mAdapter);

        getNetwork(mCommit.getHttpFiles(mProjectPath), HOST_COMMIT_FILES);
        getNetwork(mCommit.getHttpComments(mProjectPath), HOST_MERGE_COMMENTS);
    }

    private void initListFooter() {
        View footer = mInflater.inflate(R.layout.activity_merge_detail_footer, null);
        listView.addFooterView(footer);
        footer.findViewById(R.id.itemAddComment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSendCommentActivity();
            }
        });
    }

    private void startSendCommentActivity() {
        CommentActivity.CommitCommentParam param = new CommentActivity.CommitCommentParam(mProjectPath, mCommit.getCommitId());
        CommentActivity_.intent(CommitFileListActivity.this).mCommitParam(param).startForResult(RESULT_COMMENT);
    }

    private void initListhead() {
        mListHead = mInflater.inflate(R.layout.commit_file_list_head, listView, false);
        listView.addHeaderView(mListHead);

        bindData(mListHead, R.id.title, mCommit.getTitle());
        bindData(mListHead, R.id.icon, mCommit.getIcon());
        bindData(mListHead, R.id.name, mCommit.getName());
        bindData(mListHead, R.id.time, Global.dayToNow(mCommit.getCommitTime(), "创建%s"));
        bindData(mListHead, R.id.mergeId, mCommit.getCommitIdPrefix());

        String preString = "";
        bindData(mListHead, R.id.preView, preString);
    }

    private void bindData(View view, int textViewId, String text) {
        View v = view.findViewById(textViewId);
        if (v instanceof TextView) {
            TextView textview = (TextView) v;
            textview.setText(text);
        } else if (v instanceof ImageView) {
            ImageView icon = (ImageView) v;
            imagefromNetwork(icon, text);
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_COMMIT_FILES)) {
            if (code == 0) {
                DiffFile diffFile = new DiffFile(respanse.getJSONObject("data"));

                findViewById(R.id.preView);
                String s = String.format("%d 个文件，共 %d 新增和 %d 删除", diffFile.getFileCount(),
                        diffFile.getInsertions(), diffFile.getDeletions());
                bindData(mListHead, R.id.preView, s);

                mAdapter.setFilesCount(diffFile.getFiles().size());
                mAdapter.insertDataUpdate((ArrayList) diffFile.getFiles());
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(HOST_MERGE_COMMENTS)) {
            if (code == 0) {
                JSONArray json = respanse.getJSONObject("data").getJSONArray("commitComments");
                ArrayList<Object> arrayData = new ArrayList<>();
                for (int i = 0; i < json.length(); ++i) {
                    BaseComment comment = new BaseComment(json.getJSONObject(i));
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
        } else if (tag.equals(HOST_COMMIT_DETAIL)) {
            hideProgressDialog();
            if (code == 0) {
                JSONObject jsonDetail = respanse.getJSONObject("data").getJSONObject("commitDetail");
                mCommit = new Commit(jsonDetail);
                initByCommit();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_COMMENT) {
            if (resultCode == RESULT_OK) {
                BaseComment comment = (BaseComment) data.getSerializableExtra("data");
                mAdapter.appendSingeDataUpdate(comment);
            }
        }
    }
}
