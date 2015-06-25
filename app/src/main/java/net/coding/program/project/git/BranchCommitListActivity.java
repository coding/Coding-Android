package net.coding.program.project.git;

import android.view.View;
import android.widget.AbsListView;

import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.comment.BaseCommentParam;
import net.coding.program.common.widget.RefreshBaseActivity;
import net.coding.program.model.Commit;
import net.coding.program.project.detail.merge.CommitFileListActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;

@EActivity(R.layout.fragment_project_dynamic)
public class BranchCommitListActivity extends RefreshBaseActivity implements FootUpdate.LoadMore {

    private static final String HOST_COMMITS_PAGER = "HOST_COMMITS_PAGER";
    @ViewById
    protected ExpandableStickyListHeadersListView listView;
    @Extra
    String mCommitsUrl;

    CommitsAdapter mAdapter;

    CommitPage mCommitPage;

    private View.OnClickListener mOnClickListItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Commit commit = (Commit) v.getTag();
            int start = mCommitsUrl.indexOf("/user/");
            int end = mCommitsUrl.indexOf("/git/");
            CommitFileListActivity_.intent(BranchCommitListActivity.this).mCommit(commit)
                    .mProjectPath(mCommitsUrl.substring(start, end)).start();
        }
    };

    @AfterViews
    protected final void initBranchCommitListActivity() {
        mCommitPage = new CommitPage(mCommitsUrl);

        BaseCommentParam param = new BaseCommentParam(new ClickSmallImage(this), mOnClickListItem,
                new MyImageGetter(this), getImageLoad(), mOnClickUser);
        mAdapter = new CommitsAdapter(param);
        listView.setAdapter(mAdapter);
        mFootUpdate.init(listView, mInflater, this);
        onRefresh();

        showDialogLoading();

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
                if (mCommitPage.isLoadingFirstPage()) {
                    mAdapter.clearData();
                }

                JSONObject jsonCommits = respanse.getJSONObject("data").getJSONObject("commits");
                mCommitPage.setNextPage(jsonCommits);
                JSONArray jsonArray = jsonCommits.getJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    Commit commit = new Commit(jsonArray.getJSONObject(i));
                    mAdapter.appendData(commit);
                }
                mAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }

            mCommitPage.setLoading(false);
            mFootUpdate.updateState(code, mCommitPage.isLoadAll(), mAdapter.getCount());
        }
    }

    @Override
    public void onRefresh() {
        initSetting();
        mCommitPage.reset();
        loadMore();
    }

    @Override
    public void loadMore() {
        if (mCommitPage.isLoadAll()) {
            return;
        }

        String nextPageUrl = mCommitPage.getNextPageUrl();
        if (mCommitPage.isLoading()) {
            return;
        }

        mCommitPage.setLoading(true);
        getNetwork(nextPageUrl, HOST_COMMITS_PAGER);
    }

    private static class CommitPage {
        final String mCommitsUrl;
        boolean mIsEnd = false;
        int mNextPage = 1;
        boolean mLoading = false;

        public CommitPage(String url) {
            mCommitsUrl = url;
            reset();
        }

        public boolean isLoading() {
            return mLoading;
        }

        public void setLoading(boolean loading) {
            mLoading = loading;
        }

        public boolean isLoadingFirstPage() {
            return mNextPage == 1;
        }

        public boolean isLoadAll() {
            return mIsEnd;
        }

        public void reset() {
            mNextPage = 1;
            mIsEnd = false;
        }

        public void setNextPage(JSONObject json) {
            ++mNextPage;
            int count = json.optInt("pageSize", 0);
            int realData = json.optJSONArray("list").length();
            if (realData < count) {
                mIsEnd = true;
            }
        }

        public String getNextPageUrl() {
            return String.format("%spage=%d", mCommitsUrl, mNextPage);
        }
    }

//    class PageCommitAdapter extends CommitsAdapter {
//        public PageCommitAdapter(BaseCommentParam param) {
//            super(param);
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if (getCount() - 1 <= position) {
//                loadMore();
//            }
//
//            return super.getView(position, convertView, parent);
//        }
//    }
}
