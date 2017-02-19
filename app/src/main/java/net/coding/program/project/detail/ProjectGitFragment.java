package net.coding.program.project.detail;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import net.coding.program.common.util.FileUtil;
import net.coding.program.dialog.AlertDialogMessage;
import net.coding.program.model.GitFileInfoObject;
import net.coding.program.model.GitLastCommitObject;
import net.coding.program.model.GitUploadPrepareObject;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.git.BranchCommitListActivity_;
import net.coding.program.search.SearchProjectGitActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Pattern;

import static net.coding.program.project.detail.AttachmentsActivity.FILE_SELECT_CODE;

/**
 * Created by yangzhen on 2014/10/25.
 */
@EFragment(R.layout.common_refresh_listview)
public class ProjectGitFragment extends CustomMoreFragment implements FootUpdate.LoadMore {

    public static final String MASTER = "master";
    private static final String HOST_GIT_TREE = "HOST_GIT_TREE";
    private static final String HOST_GIT_TREEINFO = "HOST_GIT_TREEINFO";
    private static final String HOST_GIT_NEW_FILE_PREPARE = "HOST_GIT_NEW_FILE_PREPARE";
    private static final String HOST_GIT_NEW_FILE = "HOST_GIT_TREE_NEW_FILE";
    private static final String HOST_GIT_UPLOAD_FILE_PREPARE = "HOST_GIT_UPLOAD_FILE_PREPARE";
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
    private String host_git_new_file_prepare = "";
    private String host_git_new_file = "";
    private String host_git_upload_file_prepare = "";
    private String host_git_upload_file = "";


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
            }else if (data.isImage()) {
                holder.icon.setImageResource(R.drawable.ic_git_img);
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
        initRefreshLayout();
        showDialogLoading();

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
        SearchProjectGitActivity_.intent(getActivity()).mProjectPath(mProjectPath).mVersion(mVersion).start();
        getActivity().overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
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
        //获取lastCommit
        host_git_new_file_prepare = UrlCreate.gitNewFile(mProjectPath, mVersion, pathStack.peek());
        getNetwork(host_git_new_file_prepare, HOST_GIT_NEW_FILE_PREPARE);
    }

    @OptionsItem
    protected final void action_upload_picture(){
        //获取lastCommitId
        host_git_upload_file_prepare = UrlCreate.gitUploadFile(mProjectPath, mVersion, pathStack.peek());
        getNetwork(host_git_upload_file_prepare, HOST_GIT_UPLOAD_FILE_PREPARE);
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
                JSONObject dataObject = respanse.getJSONObject("data");
                JSONArray getFileInfos = dataObject.getJSONArray("infos");
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
        }else if(tag.equals(HOST_GIT_NEW_FILE_PREPARE)){
            if(code == 0){
                JSONObject jsonData = respanse.optJSONObject("data");
                GitLastCommitObject lastCommitObject = new GitLastCommitObject(jsonData);
                newFile(lastCommitObject);
            }else{
                showErrorMsg(code, respanse);
            }
        }else if(tag.equals(HOST_GIT_NEW_FILE)){
            if(code == 0){
                onRefresh();
            }else{
                showErrorMsg(code, respanse);
            }
        }else if(tag.equals(HOST_GIT_UPLOAD_FILE_PREPARE)){
            if(code == 0){
                JSONObject jsonData = respanse.optJSONObject("data");
                uploadPrepareObject = new GitUploadPrepareObject(jsonData);
                uploadFile();
            }else{
                showErrorMsg(code, respanse);
            }
        }else if(tag.equals(HOST_GIT_UPLOAD_FILE)){
            hideDialogLoading();
            if (code == 0) {
                showButtomToast("上传成功");
                onRefresh();
            }else {
                showErrorMsg(code, respanse);
            }
        }
    }

    //新建文件
    private void newFile(GitLastCommitObject lastCommitObject) {
        AlertDialogMessage dialogMessage = new AlertDialogMessage(getActivity());
        String title = this.getString(R.string.create_file);
        String hint = this.getString(R.string.create_file_hint);
        dialogMessage.initDialog(title, hint, new AlertDialogMessage.OnBottomClickListener() {
            @Override
            public void onPositiveButton(String newName) {
                String namePatternStr = getString(R.string.file_name_pattern);
                Pattern namePattern = Pattern.compile(namePatternStr);
                if (newName.equals("")) {
                    showButtomToast(getString(R.string.name_not_null));
                }  else if (!namePattern.matcher(newName).find()) {
                    showButtomToast(getString(R.string.file_name_error));
                } else {
                    host_git_new_file = UrlCreate.gitNewFile(mProjectPath, mVersion, pathStack.peek());
                    RequestParams params = new RequestParams();
                    params.put("title", newName);
                    params.put("content", "");
                    params.put("message", "new file" +""+ newName);
                    params.put("lastCommitSha", lastCommitObject.commitId);
                    postNetwork(host_git_new_file, params, HOST_GIT_NEW_FILE);
                }
            }

            @Override
            public void onNegativeButton() {

            }
        });
    }

    //上传文件
    private void uploadFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的文件"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            showButtomToast("请安装文件管理器");
        }
    }

    @OnActivityResult(FILE_SELECT_CODE)
    void onResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = FileUtil.getPath(getActivity(), uri);
            File selectedFile = new File(path);
            postUploadFile(selectedFile);
        }
    }

    private GitUploadPrepareObject uploadPrepareObject;
    private  void postUploadFile(File selectedFile){
        showDialogLoading();
        host_git_upload_file = UrlCreate.gitUploadFile(mProjectPath, mVersion, pathStack.peek());
        RequestParams params = new RequestParams();
        try {
            params.put("files", selectedFile);
            params.put("message", "");
            params.put("lastCommitSha", uploadPrepareObject.lastCommit);
            postNetwork(host_git_upload_file, params, HOST_GIT_UPLOAD_FILE);
        } catch (Exception e) {
            e.printStackTrace();
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
