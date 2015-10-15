package net.coding.program.project.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.base.CustomMoreFragment;
import net.coding.program.common.url.UrlCreate;
import net.coding.program.model.GitFileInfoObject;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.git.BranchCommitListActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by yangzhen on 2014/10/25.
 */
@EFragment(R.layout.common_refresh_listview)
public class ProjectGitFragment extends CustomMoreFragment implements FootUpdate.LoadMore {

    public static final String MASTER = "master";
    private static final String HOST_GIT_TREE = "HOST_GIT_TREE";
    private static final String HOST_GIT_TREEINFO = "HOST_GIT_TREEINFO";
    @FragmentArg
    String mProjectPath;
//    ProjectObject mProjectObject;

    @FragmentArg
    GitFileInfoObject mGitFileInfoObject;
    @FragmentArg
    String mVersion = "";
    @ViewById
    ListView listView;
    @ViewById
    View blankLayout;
    private ArrayList<GitFileInfoObject> mData = new ArrayList<>();
    private String host_git_tree_url = "";
    private String host_git_treeinfo_url = "";
    private String commentFormat = "%s 发布于%s";
    private boolean mTooManyFiles = false;

    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.project_git_tree_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.comment = (TextView) convertView.findViewById(R.id.comment);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            GitFileInfoObject data = mData.get(position);
            holder.name.setText(data.name);
            if (data.isTree()) {
                holder.icon.setImageResource(R.drawable.ic_project_git_folder);
            } else {
                holder.icon.setImageResource(R.drawable.ic_project_git_file);
            }

            if (data.lastCommitDate == 0) {
                holder.comment.setText("");
            } else {
                holder.comment.setText(String.format(commentFormat, data.lastCommitter.name, Global.dayToNow(data.lastCommitDate)));
            }
            /*if (position == mData.size() - 1) {
                loadMore();
            }*/

            return convertView;
        }

    };
    private Stack<String> pathStack = new Stack<String>();
    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    @AfterViews
    protected final void initProjectGitFragment() {
        initRefreshLayout();
        showDialogLoading();

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GitFileInfoObject selectedFile = mData.get(position);
                if (selectedFile.isTree()) {
                    GitTreeActivity_.intent(getActivity()).mProjectPath(mProjectPath).mVersion(mVersion).mGitFileInfoObject(selectedFile).start();
                } else {
                    GitViewActivity_.intent(getActivity()).mProjectPath(mProjectPath).mVersion(mVersion).mGitFileInfoObject(selectedFile).start();
                }
            }
        });

        if (mGitFileInfoObject == null) {
            pathStack.push("");
        } else {
            pathStack.push(mGitFileInfoObject.path);
            getActionBarActivity().getSupportActionBar().setTitle(mGitFileInfoObject.name);
        }

        if (!mVersion.isEmpty()) {
            host_git_tree_url = UrlCreate.gitTree(mProjectPath, mVersion, pathStack.peek());
            getNetwork(host_git_tree_url, HOST_GIT_TREE);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mVersion = savedInstanceState.getString("mVersion", MASTER);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_project_git, menu);
        int icon = R.drawable.ic_menu_history;
        menu.findItem(R.id.action_history).setIcon(icon);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
    }

    @OptionsItem
    protected final void action_history() {
        String peek = pathStack.peek();
        if (peek.isEmpty() && mVersion.isEmpty()) {
            showButtomToast("没有Commit记录");
            return;
        }

        String commitUrl = UrlCreate.gitTreeCommit(mProjectPath, mVersion, peek);
        BranchCommitListActivity_.intent(this).mCommitsUrl(commitUrl).start();
//        RedPointTip.markUsed(getActivity(), RedPointTip.Type.CodeHistory);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mVersion", mVersion);
    }

    @Override
    public void onRefresh() {
        if (mTooManyFiles) {
            showTooManyFilesAlert();
            hideProgressDialog();
            setRefreshing(false);
            return;
        }

        initSetting();
        host_git_treeinfo_url = UrlCreate.gitTreeinfo(mProjectPath, mVersion, pathStack.peek());
        getNetwork(host_git_treeinfo_url, HOST_GIT_TREEINFO);
    }

    @Override
    public void loadMore() {
        if (mTooManyFiles) {
            return;
        }

        host_git_treeinfo_url = UrlCreate.gitTreeinfo(mProjectPath, mVersion, pathStack.peek());
        getNextPageNetwork(host_git_treeinfo_url, HOST_GIT_TREEINFO);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_GIT_TREEINFO)) {
            hideProgressDialog();
            setRefreshing(false);
            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mData.clear();
                }
                JSONArray getFileInfos = respanse.getJSONObject("data").getJSONArray("infos");

                for (int i = 0; i < getFileInfos.length(); ++i) {
                    GitFileInfoObject fileInfoObject = new GitFileInfoObject(getFileInfos.getJSONObject(i));
                    mData.add(fileInfoObject);
                }

                adapter.notifyDataSetChanged();
                switchVersionSuccess();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_GIT_TREE)) {
            if (code == 0) {
                JSONObject jsonData = respanse.optJSONObject("data");
                if (jsonData.optBoolean("too_many_files", false) == true) {
                    showTooManyFilesAlert();
                    JSONArray jsonArray = jsonData.optJSONArray("files");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        GitFileInfoObject fileInfoObject = new GitFileInfoObject(jsonArray.optJSONObject(i));
                        mData.add(fileInfoObject);
                    }

                    mTooManyFiles = true;
                    hideProgressDialog();
                    setRefreshing(false);
                    adapter.notifyDataSetChanged();
                    switchVersionSuccess();
                    return;
                }

                host_git_treeinfo_url = UrlCreate.gitTreeinfo(mProjectPath, mVersion, pathStack.peek());
                getNetwork(host_git_treeinfo_url, HOST_GIT_TREEINFO);
            } else {
                hideProgressDialog();
                setRefreshing(false);
                BlankViewDisplay.setBlank(0, this, true, blankLayout, onClickRetry);
            }
        }
    }

    private void showTooManyFilesAlert() {
        showMiddleToast("该目录下文件太多，这里最多显示出 500 个文件，如需要查看所有文件，请使用电脑 Clone 到本地查看。");
    }

    protected void switchVersionSuccess() {
    }

    @Override
    protected String getLink() {
        String head = Global.HOST + ProjectObject.translatePathToOld(mProjectPath);
        if (pathStack.peek().isEmpty()) {
            return head + "/git";
        } else {
            return head + "/git/tree/" + mVersion + "/" + pathStack.peek();
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView comment;
    }
}
