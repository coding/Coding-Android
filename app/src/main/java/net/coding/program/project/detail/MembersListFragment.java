package net.coding.program.project.detail;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.FootUpdate;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.CustomMoreFragment;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.message.MessageListActivity_;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TaskObject;
import net.coding.program.model.UserObject;
import net.coding.program.project.ProjectFragment;
import net.coding.program.project.ProjectHomeActivity;
import net.coding.program.user.AddFollowActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


@EFragment(R.layout.common_refresh_listview)
public class MembersListFragment extends CustomMoreFragment implements FootUpdate.LoadMore {

    static final int RESULT_ADD_USER = 111;
    static final int RESULT_MODIFY_AUTHORITY = 112;

    final String urlDeleteUser = Global.HOST_API + "/project/%d/kickout/%d";
    String urlMembers = Global.HOST_API + "/project/%d/members?pagesize=1000";
    String urlQuit = Global.HOST_API + "/project/%d/quit";

    @FragmentArg
    ProjectObject mProjectObject;
    @FragmentArg
    String mMergeUrl;
    // 为true表示是用@选成员，为false表示项目成员列表
    @FragmentArg
    boolean mSelect;
    @ViewById
    ListView listView;

    ArrayList<Object> mSearchData = new ArrayList<>();
    ArrayList<Object> mData = new ArrayList<>();

    TaskObject.Members mMySelf = new TaskObject.Members();

    BaseAdapter adapter = new BaseAdapter() {

        private View.OnClickListener sendMessage = v -> {
            UserObject user = (UserObject) v.getTag();
            Intent intent = new Intent(getActivity(), MessageListActivity_.class);
            intent.putExtra("mUserObject", user);
            startActivity(intent);
        };
        private View.OnClickListener quitProject = v -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle("退出项目")
                    .setMessage(String.format("您确定要退出 %s 项目吗？", mProjectObject.name))
                    .setPositiveButton("确定", (dialog1, which) -> {
                        RequestParams params = new RequestParams();
                        postNetwork(urlQuit, params, urlQuit);
                    })
                    .setNegativeButton("取消", null)
                    .show();

        };

        @Override
        public int getCount() {
            return mSearchData.size();
        }

        @Override
        public Object getItem(int position) {
            return mSearchData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.fragment_members_list_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.alias = (TextView) convertView.findViewById(R.id.alias);
                //holder.desc = (TextView) convertView.findViewById(R.id.desc);
                holder.ic = (ImageView) convertView.findViewById(R.id.ic);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
//                holder.icon.setOnClickListener(mOnClickUser);
//                holder.icon.setFocusable(false);
                holder.btn = (ImageView) convertView.findViewById(R.id.btn);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Object object = mSearchData.get(position);
            UserObject user;
            holder.ic.setVisibility(View.GONE);

            if (object instanceof TaskObject.Members) {
                TaskObject.Members data = (TaskObject.Members) object;
                user = data.user;

                TaskObject.Members.Type memberType = data.getType();
                int iconRes = memberType.getIcon();
                if (iconRes == 0) {
                    holder.ic.setVisibility(View.GONE);
                } else {
                    holder.ic.setVisibility(View.VISIBLE);
                    holder.ic.setImageResource(iconRes);
                }

                if (!data.alias.isEmpty()) {
                    holder.alias.setText(data.alias);
                    holder.alias.setVisibility(View.VISIBLE);
                } else {
                    holder.alias.setVisibility(View.GONE);
                }
            } else { //  (object instanceof UserObject)
                user = (UserObject) object;
            }

            holder.name.setText(user.name);
            iconfromNetwork(holder.icon, user.avatar);
            holder.icon.setTag(user.global_key);

            if (mSearchData.size() - 1 == position) {
                loadMore();
            }

            if (mSelect) {
                holder.btn.setVisibility(View.GONE);
            } else if (user.name.equals(MyApp.sUserObject.name)) {
                holder.btn.setImageResource(R.drawable.ic_member_list_quit);
                holder.btn.setOnClickListener(quitProject);
                if (object instanceof TaskObject.Members) {
                    TaskObject.Members data = (TaskObject.Members) object;
                    if (data.isOwner()) {
                        holder.btn.setVisibility(View.GONE);
                    } else {
                        holder.btn.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                holder.btn.setImageResource(R.drawable.ic_send_message);
                holder.btn.setTag(user);
                holder.btn.setOnClickListener(sendMessage);
                holder.btn.setVisibility(View.VISIBLE);
            }

            return convertView;
        }
    };

    @AfterViews
    protected void init() {
        initRefreshLayout();

        mData = new ArrayList<>();
        mSearchData = new ArrayList<>(mData);
        if (mSearchData.isEmpty()) {
            showDialogLoading();
        }

        listView.setAdapter(adapter);
        AdapterView.OnItemClickListener mListClickJump;
        if (mSelect) {
            mListClickJump = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent();
                    UserObject userObject;
                    Object object = mSearchData.get((int) id);
                    if (object instanceof TaskObject.Members) {
                        userObject = ((TaskObject.Members) object).user;
                    } else {
                        userObject = (UserObject) object;
                    }

                    intent.putExtra("name", userObject.name);
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                }
            };
        } else {
            mListClickJump = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    UserDynamicActivity_
                            .intent(getActivity())
                            .mProjectObject(mProjectObject)
                            .mMember((TaskObject.Members) mSearchData.get(position))
                            .start();

                }
            };
        }
        listView.setOnItemClickListener(mListClickJump);

        if (!mSelect) {
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
                    TaskObject.Members member = (TaskObject.Members) mSearchData.get((int) id);
//                    if (member.user.isMe()) {
//                        return true;
//                    }

                    if (mMySelf.getType() != TaskObject.Members.Type.ower
                            && mMySelf.getType() != TaskObject.Members.Type.manager) {
                        return true;
                    }

                    String[] items;
                    DialogInterface.OnClickListener clicks;
                    switch (mMySelf.getType()) {
                        case ower:
                            if (member.isMe()) {
                                items = new String[]{
                                        "修改备注"
                                };
                            } else {
                                items = new String[]{
                                        "修改备注",
                                        "设置权限",
                                        "移除成员"
                                };
                            }
                            clicks = (dialog1, which) -> {
                                if (which == 0) {
                                    modifyMemberAlias(member);
                                } else if (which == 1) {
                                    modifyMemberAuthority(member);
                                } else {
                                    removeMember(member);
                                }
                            };
                            break;
                        case manager:
                            if (member.getType() == TaskObject.Members.Type.manager
                                    || member.getType() == TaskObject.Members.Type.ower) {
                                items = new String[]{
                                        "修改备注"
                                };
                                clicks = (dialog1, which) -> {
                                    if (which == 0) {
                                        modifyMemberAlias(member);
                                    }
                                };
                            } else {
                                items = new String[]{
                                        "修改备注",
                                        "设置权限",
                                        "移除成员"
                                };
                                clicks = (dialog1, which) -> {
                                    if (which == 0) {
                                        modifyMemberAlias(member);
                                    } else if (which == 1) {
                                        modifyMemberAuthority(member);
                                    } else {
                                        removeMember(member);
                                    }
                                };
                            }
                            break;
                        default:
                            return true;
                    }

                    new AlertDialog.Builder(getActivity())
                            .setItems(items, clicks)
                            .show();
                    return true;
                }
            });
        }

        if (mProjectObject != null) {
            urlMembers = String.format(urlMembers, mProjectObject.getId());
            urlQuit = String.format(urlQuit, mProjectObject.getId());
        } else {  // mMergeUrl 不为空
            urlMembers = mMergeUrl;
        }

        loadMore();

        setHasOptionsMenu(true);
    }

    private void modifyMemberAuthority(TaskObject.Members member) {
        MemberAuthorityActivity_.intent(this)
                .member(member)
                .me(mMySelf)
                .projectId(mProjectObject.getId())
                .startForResult(RESULT_MODIFY_AUTHORITY);
    }

    private void modifyMemberAlias(TaskObject.Members member) {
        UserObject user = member.user;
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_input_alias, null);
        EditText input = (EditText) v.findViewById(R.id.edit1);
        input.setText(member.alias);
        new AlertDialog.Builder(getActivity())
                .setMessage("修改备注")
                .setView(v)
                .setPositiveButton("确定", (dialog2, which1) -> {
                    String inputString = input.getText().toString();
                    String url = String.format(Global.HOST_API + "/project/%s/members/update_alias/%s",
                            mProjectObject.getId(), user.id);
                    RequestParams params = new RequestParams();
                    params.put("alias", inputString);
                    MyAsyncHttpClient.post(getActivity(), url, params, new MyJsonResponse(getActivity()) {
                        @Override
                        public void onMySuccess(JSONObject response) {
                            super.onMySuccess(response);
                            member.alias = inputString;
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFinish() {
                            showProgressBar(false);
                        }
                    });

                    showProgressBar(true);
                })
                .setNegativeButton("取消", null)
                .show();

    }

    private void removeMember(TaskObject.Members member) {
        UserObject user = member.user;
        new AlertDialog.Builder(getActivity())
                .setMessage(String.format("确定移除 %s ?", user.name))
                .setPositiveButton("确定", (dialog2, which1) -> {
                    String url = String.format(urlDeleteUser, mProjectObject.getId(), user.id);
                    postNetwork(url, new RequestParams(), urlDeleteUser, -1, member);
                    showProgressBar(true);
                })
                .setNegativeButton("取消", null)
                .create().show();
    }

    public void search(String input) {
        mSearchData.clear();
        if (input.isEmpty()) {
            mSearchData.addAll(mData);
        } else {
            for (Object item : mData) {
                UserObject user;
                if (item instanceof TaskObject.Members) {
                    user = ((TaskObject.Members) item).user;
                } else {
                    user = (UserObject) item;
                }

                if (user.global_key.toLowerCase().contains(input) ||
                        user.name.toLowerCase().contains(input)) {
                    mSearchData.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(urlMembers, urlMembers);
    }

    private boolean projectCreateByMe() {
        String myGlobalKey = MyApp.sUserObject.global_key;
        return mProjectObject != null
                && (mProjectObject.owner_user_name.equals(myGlobalKey)
                    || mProjectObject.getOwner().global_key.equals(myGlobalKey));
    }

    @OptionsItem
    void action_add() {
        Intent intent = new Intent(getActivity(), AddFollowActivity_.class);
        intent.putExtra("mProjectObject", mProjectObject);
        startActivityForResult(intent, RESULT_ADD_USER);
    }

    @OnActivityResult(RESULT_ADD_USER)
    void onResultAddUser(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            onRefresh();
        }
    }

    @OnActivityResult(RESULT_MODIFY_AUTHORITY)
    void onResultModifyAuthority(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            onRefresh();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mSelect) {
            if (projectCreateByMe()) {
                inflater.inflate(R.menu.users, menu);
            } else {
                inflater.inflate(R.menu.common_more, menu);
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onRefresh() {
        initSetting();
        getNetwork(urlMembers, urlMembers);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(urlMembers)) {
            hideProgressDialog();
            setRefreshing(false);

            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mData.clear();
                }

                JSONObject dataObject = respanse.optJSONObject("data");
                // 项目成员的数据是 data - list，包了两层
                if (dataObject != null) {
                    JSONArray members;
                    members = respanse.getJSONObject("data").getJSONArray("list");

                    for (int i = 0; i < members.length(); ++i) {
                        TaskObject.Members member = new TaskObject.Members(members.getJSONObject(i));
                        if (member.isOwner()) {
                            mData.add(0, member);
                        } else {
                            mData.add(member);
                        }

                        if (member.isMe()) {
                            mMySelf = member;
                        }

                    }

//                    AccountInfo.saveProjectMembers(getActivity(), (ArrayList) mData, mProjectObject.getId());
                } else { // merge 的at他人列表只用 data 包了一层
                    JSONArray members = respanse.getJSONArray("data");
                    for (int i = 0; i < members.length(); ++i) {
                        UserObject member = new UserObject(members.getJSONObject(i));
                        mData.add(member);
                    }
                }

                mSearchData.clear();
                mSearchData.addAll(mData);
                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(urlQuit)) {
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "退出项目");

                showButtomToast("成功退出项目");
                Intent intent = new Intent();
                intent.setAction(ProjectFragment.RECEIVER_INTENT_REFRESH_PROJECT);
                intent.setAction(ProjectHomeActivity.BROADCAST_CLOSE);
                getActivity().sendBroadcast(intent);
                getActivity().onBackPressed();
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(urlDeleteUser)) {
            showProgressBar(false);
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "移除成员");
                mSearchData.remove(data);
                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @Override
    protected String getLink() {
        return Global.HOST + mProjectObject.project_path + "/members";
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView alias;
        ImageView ic;
        ImageView btn;
    }
}
