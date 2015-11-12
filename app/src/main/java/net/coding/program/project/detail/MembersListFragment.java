package net.coding.program.project.detail;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.FootUpdate;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.Global;
import net.coding.program.common.base.CustomMoreFragment;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.message.MessageListActivity_;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TaskObject;
import net.coding.program.model.UserObject;
import net.coding.program.project.ProjectFragment;
import net.coding.program.user.AddFollowActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


@EFragment(R.layout.common_refresh_listview)
public class MembersListFragment extends CustomMoreFragment implements FootUpdate.LoadMore {

    static final int RESULT_ADD_USER = 111;
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

    // TaskObject.Members
    ArrayList<Object> mSearchData = new ArrayList<>();
    ArrayList<Object> mData = new ArrayList<>();
    BaseAdapter adapter = new BaseAdapter() {

        private View.OnClickListener sendMessage = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserObject user = (UserObject) v.getTag();
                Intent intent = new Intent(getActivity(), MessageListActivity_.class);
                intent.putExtra("mUserObject", user);
                startActivity(intent);
            }
        };
        private View.OnClickListener quitProject = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showButtomToast("quit");
//                String.format(urlMembers, mProjectObject.getId());
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                AlertDialog dialog = builder.setTitle("退出项目")
                        .setMessage(String.format("您确定要退出 %s 项目吗？", mProjectObject.name))
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RequestParams params = new RequestParams();
                                postNetwork(urlQuit, params, urlQuit);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();

                CustomDialog.dialogTitleLineColor(getActivity(), dialog);
            }
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
                if (data.type == TaskObject.Members.MEMBER_TYPE_OWNER) {
                    //holder.desc.setText("(创建者)");
                    holder.ic.setVisibility(View.VISIBLE);
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
                    if (data.type == TaskObject.Members.MEMBER_TYPE_OWNER) {
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

        if (projectCreateByMe()) {
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
                    final UserObject user = getUser(mSearchData.get((int) id));
                    if (user.global_key.equals(MyApp.sUserObject.global_key)) {
                        return false;
                    }

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    AlertDialog dialog = builder.setItems(new String[]{"移除成员"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());

                            builder1.setMessage(String.format("确定移除 %s ?", user.name))
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String url = String.format(urlDeleteUser, mProjectObject.getId(), user.id);
                                            postNetwork(url, new RequestParams(), urlDeleteUser, (int) id, null);
                                            showProgressBar(true);
                                        }
                                    })
                                    .setNegativeButton("取消", null)
                                    .create().show();

                        }
                    }).show();
                    CustomDialog.dialogTitleLineColor(getActivity(), dialog);

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

    private UserObject getUser(Object object) {
        if (object instanceof UserObject) {
            return (UserObject) object;
        } else {
            return ((TaskObject.Members) object).user;
        }
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
        return mProjectObject != null &&
             mProjectObject.owner_user_name.equals(MyApp.sUserObject.global_key);
    }

    @OptionsItem
    void action_add() {
        Intent intent = new Intent(getActivity(), AddFollowActivity_.class);
        intent.putExtra("mProjectObject", mProjectObject);
        startActivityForResult(intent, RESULT_ADD_USER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_ADD_USER) {
            if (resultCode == Activity.RESULT_OK) {
                initSetting();
                loadMore();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
                        if (member.type == TaskObject.Members.MEMBER_TYPE_OWNER) {
                            mData.add(0, member);
                        } else {
                            mData.add(member);
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
                getActivity().sendBroadcast(intent);
                getActivity().onBackPressed();
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(urlDeleteUser)) {
            showProgressBar(false);
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "移除成员");
                mSearchData.remove(pos);
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
        ImageView ic;
        ImageView btn;
    }
}
