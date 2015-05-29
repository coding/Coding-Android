package net.coding.program.project.git;

import android.widget.ListView;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.comment.BaseCommentHolder;
import net.coding.program.model.Commit;
import net.coding.program.model.Merge;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_commit_list)
@OptionsMenu(R.menu.menu_commit_list)
public class CommitListActivity extends BackActivity {

    @Extra
    Merge mMerge;

    @ViewById
    ListView listView;

    public static final String HOST_COMMITS = "HOST_COMMITS";

    CommitsAdapter mAdapter;

    @AfterViews
    protected final void initCommitListActivity() {
        getSupportActionBar().setTitle(mMerge.getTitle());

        BaseCommentHolder.BaseCommentParam param = new BaseCommentHolder.BaseCommentParam(null,
                new MyImageGetter(this), getImageLoad(), mOnClickUser);
        mAdapter = new CommitsAdapter(param);
        listView.setAdapter(mAdapter);

        getNetwork(mMerge.getHttpCommits(), HOST_COMMITS);
    }


    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_COMMITS)) {
            if (code == 0) {
                JSONArray jsonArray = respanse.getJSONObject("data").getJSONObject("pull_request").getJSONArray("commits");

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
