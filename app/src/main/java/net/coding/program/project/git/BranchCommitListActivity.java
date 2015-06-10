package net.coding.program.project.git;

import android.view.View;
import android.widget.AbsListView;

import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.comment.BaseCommentParam;
import net.coding.program.common.widget.RefreshBaseActivity;
import net.coding.program.model.Commit;
import net.coding.program.project.detail.merge.CommitFileListActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;

@EActivity(R.layout.fragment_project_dynamic)
@OptionsMenu(R.menu.menu_branch_commit_list)
public class BranchCommitListActivity extends RefreshBaseActivity implements FootUpdate.LoadMore {

    private static final String HOST_COMMITS_PAGER = "HOST_COMMITS_PAGER";
    @ViewById
    protected ExpandableStickyListHeadersListView listView;
    @Extra
    String mCommitsUrl;
    CommitsAdapter mAdapter;

    private View.OnClickListener mOnClickListItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Commit commit = (Commit) v.getTag();
            CommitFileListActivity_.intent(BranchCommitListActivity.this).mCommit(commit).mProjectPath("").start();
        }
    };

    @AfterViews
    protected final void initBranchCommitListActivity() {
        BaseCommentParam param = new BaseCommentParam(mOnClickListItem,
                new MyImageGetter(this), getImageLoad(), mOnClickUser);
        mAdapter = new CommitsAdapter(param);
        listView.setAdapter(mAdapter);
        mFootUpdate.init(listView, mInflater, this);
        onRefresh();

        showDialogLoading();
        initRefreshLayout();

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    loadMore();
                }
            }
        });
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_COMMITS_PAGER)) {
            hideProgressDialog();
            setRefreshing(false);
            if (code == 0) {
                if (isLoadingFirstPage(HOST_COMMITS_PAGER)) {
                    mAdapter.clearData();
                }

                JSONArray jsonArray = respanse.getJSONObject("data").getJSONObject("commits").getJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    Commit commit = new Commit(jsonArray.getJSONObject(i));
                    mAdapter.appendData(commit);
                }
                mAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
            mFootUpdate.updateState(code, isLoadingLastPage(HOST_COMMITS_PAGER), mAdapter.getCount());
        }
    }

    @Override
    public void onRefresh() {
        initSetting();
        getNextPageNetwork(mCommitsUrl, HOST_COMMITS_PAGER);
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(mCommitsUrl, HOST_COMMITS_PAGER);
    }
}
