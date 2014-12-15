package net.coding.program.project.detail;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.FootUpdate;
import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.model.GitFileInfoObject;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
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
//@OptionsMenu(R.menu.project_attachment_folder)
public class ProjectGitFragment extends RefreshBaseFragment implements FootUpdate.LoadMore {
    private ArrayList<GitFileInfoObject> mData = new ArrayList<GitFileInfoObject>();

    private String HOST_GIT_TREE = Global.HOST + "/api/user/%s/project/%s/git/tree/master/%s";
    //https://coding.net/api/user/8206503/project/AndroidCoding/git/tree/master
    private String HOST_GIT_TREEINFO = Global.HOST + "/api/user/%s/project/%s/git/treeinfo/master/%s";
    private String host_git_tree_url = "";
    private String host_git_treeinfo_url = "";
    //https://coding.net/api/user/8206503/project/AndroidCoding/git/treeinfo/master

    private String commentFormat = "%s 发布于%s";

    private Stack<String> pathStack = new Stack<String>();

    @FragmentArg
    ProjectObject mProjectObject;

    @FragmentArg
    GitFileInfoObject mGitFileInfoObject;

    @ViewById
    ListView listView;

    @ViewById
    View blankLayout;

    @AfterViews
    protected void init() {
        super.init();
        showDialogLoading();
        //HOST_GIT_TREEINFO = String.format(HOST_GIT_TREEINFO, mProjectObject.owner_user_name);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GitFileInfoObject selectedFile = mData.get(position);
                if (selectedFile.isTree()) {
                    GitTreeActivity_.intent(getActivity()).mProjectObject(mProjectObject).mGitFileInfoObject(selectedFile).start();
                } else {
                    GitViewActivity_.intent(getActivity()).mProjectObject(mProjectObject).mGitFileInfoObject(selectedFile).start();
                }
                //AttachmentsActivity_.intent(getActivity()).mAttachmentFolderObject(mData.get(position)).mProjectObjectId(mProjectObject.id).startForResult(RESULT_REQUEST_FILES);
            }
        });

        /*listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //showButtomToast("rename");
                doRename(position, mData.get(position));
                return true;
            }
        });*/

        if (mGitFileInfoObject == null) {
            pathStack.push("");
        } else {
            pathStack.push(mGitFileInfoObject.path);
            getActivity().getActionBar().setTitle(mGitFileInfoObject.name);
        }


        host_git_tree_url = String.format(HOST_GIT_TREE, mProjectObject.owner_user_name, mProjectObject.name, pathStack.peek());
        getNetwork(host_git_tree_url, HOST_GIT_TREE);


    }

    @Override
    public void onRefresh() {
        initSetting();
        host_git_treeinfo_url = String.format(HOST_GIT_TREEINFO, mProjectObject.owner_user_name, mProjectObject.name, pathStack.peek());
        getNetwork(host_git_treeinfo_url, HOST_GIT_TREEINFO);
    }

    @Override
    public void loadMore() {
        host_git_treeinfo_url = String.format(HOST_GIT_TREEINFO, mProjectObject.owner_user_name, mProjectObject.name, pathStack.peek());
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
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_GIT_TREE)) {
            if (code == 0) {
                host_git_treeinfo_url = String.format(HOST_GIT_TREEINFO, mProjectObject.owner_user_name, mProjectObject.name, pathStack.peek());
                getNetwork(host_git_treeinfo_url, HOST_GIT_TREEINFO);
            } else {
                hideProgressDialog();
                setRefreshing(false);
                BlankViewDisplay.setBlank(0, this, true, blankLayout, onClickRetry);
            }
        }
    }

    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

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
            if (data.isTree())
                holder.icon.setImageResource(R.drawable.ic_project_git_folder);
            else
                holder.icon.setImageResource(R.drawable.ic_project_git_file);

            holder.comment.setText(String.format(commentFormat, data.lastCommitter.name, Global.dayToNow(data.lastCommitDate)));
            /*if (position == mData.size() - 1) {
                loadMore();
            }*/

            return convertView;
        }

    };

    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView comment;
    }

}
