package net.coding.program.project.git;

import android.view.View;
import android.widget.ListView;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.comment.BaseCommentParam;
import net.coding.program.model.Commit;
import net.coding.program.model.Merge;
import net.coding.program.project.detail.merge.CommitFileListActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.common_refresh_listview)
//@OptionsMenu(R.menu.menu_commit_list)
public class CommitListActivity extends BackActivity {

    private static final String HOST_COMMITS = "HOST_COMMITS";

    @Extra
    Merge mMerge;

    @ViewById
    ListView listView;

    CommitsAdapter mAdapter;
    private View.OnClickListener mOnClickListItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Commit commit = (Commit) v.getTag();
            CommitFileListActivity_.intent(CommitListActivity.this).mCommit(commit).mProjectPath(mMerge.getProjectPath()).start();
        }
    };

    @AfterViews
    protected final void initCommitListActivity() {
        getSupportActionBar().setTitle(mMerge.getTitle());

        BaseCommentParam param = new BaseCommentParam(mOnClickListItem,
                new MyImageGetter(this), getImageLoad(), mOnClickUser);
        mAdapter = new CommitsAdapter(param);
        listView.setAdapter(mAdapter);

        getNetwork(mMerge.getHttpCommits(), HOST_COMMITS);
    }

    @ItemClick
    protected final void listView(Commit commit) {
        CommitFileListActivity_.intent(this).mCommit(commit).start();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_COMMITS)) {
            if (code == 0) {
                JSONArray jsonArray = respanse.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    Commit commit = new Commit(jsonArray.getJSONObject(i));
                    mAdapter.appendData(commit);
                }
                mAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }
}
