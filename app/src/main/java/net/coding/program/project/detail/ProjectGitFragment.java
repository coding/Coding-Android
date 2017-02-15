package net.coding.program.project.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.base.CustomMoreFragment;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.url.UrlCreate;
import net.coding.program.common.util.BlankViewHelp;
import net.coding.program.dialog.AlertDialogMessage;
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
import java.util.regex.Pattern;

/**
 * Created by yangzhen on 2014/10/25.
 */
@EFragment(R.layout.common_refresh_listview)
public class ProjectGitFragment extends CustomMoreFragment implements FootUpdate.LoadMore {

    public static final String MASTER = "master";
    private static final String HOST_GIT_TREE = "HOST_GIT_TREE";
    private static final String HOST_GIT_TREEINFO = "HOST_GIT_TREEINFO";
    private static final String HOST_GIT_NEW_FILE = "HOST_GIT_TREE_NEW_FILE";
    private static final String HOST_GIT_UPLOAD_FILE = "HOST_GIT_TREE_UPLOAD_FILE";

    @FragmentArg
    String mProjectPath;

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
    private String host_git_new_file = "";
    private String host_git_tree_upload_file = "";

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
                holder.icon.setImageResource(R.drawable.ic_project_code_folder);
            }else if(data.isExecutable()){
                holder.icon.setImageResource(R.drawable.ic_project_code_exe);
            } else if (data.isGitLink()) {
                holder.icon.setImageResource(R.drawable.ic_project_code_sub_module);
            }else {
                holder.icon.setImageResource(R.drawable.ic_project_code_file);
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
        System.out.println("mVersion:"+mVersion);
        initRefreshLayout();
        showDialogLoading();

        System.out.println("mGit:"+mGitFileInfoObject);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            GitFileInfoObject selectedFile = mData.get(position);
            if (selectedFile.isTree()) {
                GitTreeActivity_.intent(getActivity()).mProjectPath(mProjectPath).mVersion(mVersion).mGitFileInfoObject(selectedFile).start();
            } else {
                GitViewActivity_.intent(getActivity()).mProjectPath(mProjectPath).mVersion(mVersion).mGitFileInfoObject(selectedFile).start();
            }
        });
        listView.setVisibility(View.INVISIBLE);

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
    protected final void action_code_search(){
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

    @OptionsItem
    protected final void action_create_file(){
        AlertDialogMessage dialogMessage = new AlertDialogMessage(getActivity());
        String title = this.getString(R.string.create_file);
        String hint = this.getString(R.string.create_file_hint);
        dialogMessage.initDialog(title, hint, new AlertDialogMessage.OnBottomClickListener() {
            @Override
            public void onPositiveButton(String newName) {
                //^[a-zA-Z0-9\u4e00-\u9fa5\./_-]+$
//                String name = "^[a-zA-Z0-9\\u4e00-\\u9fa5\./_-]+$";
                String namePatternStr = "[,`~!@#$%^&*:;()''\"\"><|.\\ /=]";
                Pattern namePattern = Pattern.compile(namePatternStr);
                if (newName.equals("")) {
                    showButtomToast("名字不能为空");
                }else {
                    System.out.println("mGitFileInfoObject:"+mGitFileInfoObject);
                    host_git_new_file = UrlCreate.gitNewFile(mProjectPath, mVersion, pathStack.peek());
                    RequestParams params = new RequestParams();
                    params.put("title", newName);
                    params.put("content", "");
                    params.put("message", "new file" +""+ newName);
                    params.put("lastCommitSha", mGitFileInfoObject.lastCommitId);
                    postNetwork(host_git_new_file, params, HOST_GIT_NEW_FILE);
                }
            }

            @Override
            public void onNegativeButton() {

            }
        });
    }

    @OptionsItem
    protected final void action_upload_picture(){
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
            hideDialogLoading();
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
            hideDialogLoading();
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

                updateListview();
                switchVersionSuccess();
            } else {
                showErrorMsg(code, respanse);
                showProgressBar(false);
                boolean result = false;
                int ERROR_NO_CODE = 1209;
                if (code == ERROR_NO_CODE) {
                    result = true;
                }
                BlankViewHelp.setBlank(mData.size(), this, result, blankLayout, onClickRetry);
            }
        } else if (tag.equals(HOST_GIT_TREE)) {
            if (code == 0) {
                JSONObject jsonData = respanse.optJSONObject("data");
                if (jsonData.optBoolean("too_many_files")) {
                    showTooManyFilesAlert();
                    JSONArray jsonArray = jsonData.optJSONArray("files");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        GitFileInfoObject fileInfoObject = new GitFileInfoObject(jsonArray.optJSONObject(i));
                        mData.add(fileInfoObject);
                    }

                    JSONObject jsonObject = jsonData.optJSONObject("lastCommit");
                    String commitId = jsonData.optString("commitId");

                    mTooManyFiles = true;
                    hideDialogLoading();
                    setRefreshing(false);
                    updateListview();
                    switchVersionSuccess();
                    return;
                }

                host_git_treeinfo_url = UrlCreate.gitTreeinfo(mProjectPath, mVersion, pathStack.peek());
                getNetwork(host_git_treeinfo_url, HOST_GIT_TREEINFO);
            } else {
                hideDialogLoading();
                setRefreshing(false);
                if (code == NetworkImpl.ERROR_PERMISSION_DENIED) {
                    BlankViewDisplay.setBlank(0, this, true, blankLayout, onClickRetry, "无权访问\n请联系项目管理员进行代码权限设置");
                } else {
                    BlankViewDisplay.setBlank(0, this, true, blankLayout, onClickRetry);
                }
            }
        }else if(tag.equals(HOST_GIT_NEW_FILE)){
            if(code == 0){
                System.out.println("请求成功");
            }else{
                System.out.println("请求失败");
                showErrorMsg(code, respanse);
            }
        }
    }

    private void updateListview() {
        listView.setVisibility(mData.size() > 0 ? View.VISIBLE : View.INVISIBLE);
        adapter.notifyDataSetChanged();
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
