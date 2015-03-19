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
import net.coding.program.message.MessageListActivity_;
import net.coding.program.model.AccountInfo;
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

    String urlMembers = Global.HOST + "/api/project/%d/members?pagesize=1000";
    String urlQuit = Global.HOST + "/api/project/%d/quit";

    final String urlDeleteUser = Global.HOST + "/api/project/%d/kickout/%d";

    @FragmentArg
    ProjectObject mProjectObject;

    // 为true表示是用@选成员，为false表示项目成员列表
    @FragmentArg
    boolean mSelect;

    @ViewById
    ListView listView;

    ArrayList<TaskObject.Members> mSearchData = new ArrayList<TaskObject.Members>();
    ArrayList<TaskObject.Members> mData = new ArrayList<TaskObject.Members>();
    private AdapterView.OnItemClickListener mListClickJump;

    @AfterViews
    protected void init() {
        initRefreshLayout();

        mData = AccountInfo.loadProjectMembers(getActivity(), mProjectObject.getId());
        mSearchData = new ArrayList(mData);
        if (mSearchData.isEmpty()) {
            showDialogLoading();
        }

        listView.setAdapter(adapter);
        if (mSelect) {
            mListClickJump = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent();
                    intent.putExtra("name", mSearchData.get((int) id).user.name);
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
                            .mMember(mSearchData.get(position))
                            .start();

                }
            };
        }
        listView.setOnItemClickListener(mListClickJump);

        if (projectCreateByMe()) {
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
                    final TaskObject.Members members = mSearchData.get((int) id);
                    if (members.user.global_key.equals(MyApp.sUserObject.global_key)) {
                        return false;
                    }

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    AlertDialog dialog = builder.setItems(new String[]{"移除成员"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());

                            builder1.setMessage(String.format("确定移除 %s ?", members.user.name))
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String url = String.format(urlDeleteUser, mProjectObject.getId(), members.user.id);
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

        urlMembers = String.format(urlMembers, mProjectObject.getId());
        urlQuit = String.format(urlQuit, mProjectObject.getId());

        loadMore();

        setHasOptionsMenu(true);
    }

    public void search(String input) {
        mSearchData.clear();
        if (input.isEmpty()) {
            mSearchData.addAll(mData);
        } else {
            for (TaskObject.Members item : mData) {
                if (item.user.global_key.toLowerCase().contains(input) ||
                        item.user.name.toLowerCase().contains(input)) {
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

    static final int RESULT_ADD_USER = 111;

    private boolean projectCreateByMe() {
        return mProjectObject.owner_user_name.equals(MyApp.sUserObject.global_key);
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

                JSONArray members = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < members.length(); ++i) {
                    TaskObject.Members member = new TaskObject.Members(members.getJSONObject(i));
                    if (member.type == TaskObject.Members.MEMBER_TYPE_OWNER) {
                        mData.add(0, member);
                    } else {
                        mData.add(member);
                    }
                }

                AccountInfo.saveProjectMembers(getActivity(), mData, mProjectObject.getId());

                mSearchData.clear();
                mSearchData.addAll(mData);
                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(urlQuit)) {
            if (code == 0) {
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
                mSearchData.remove(pos);
                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    BaseAdapter adapter = new BaseAdapter() {

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

            TaskObject.Members data = mSearchData.get(position);
            holder.name.setText(data.user.name);
            if (data.type == TaskObject.Members.MEMBER_TYPE_OWNER) {
                //holder.desc.setText("(创建者)");
                holder.ic.setVisibility(View.VISIBLE);
            } else {
                holder.ic.setVisibility(View.GONE);
            }
            iconfromNetwork(holder.icon, data.user.avatar);
            holder.icon.setTag(data.user.global_key);

            if (mSearchData.size() - 1 == position) {
                loadMore();
            }

            if (mSelect) {
                holder.btn.setVisibility(View.GONE);
            } else if (data.user.name.equals(MyApp.sUserObject.name)) {
                holder.btn.setImageResource(R.drawable.ic_member_list_quit);
                holder.btn.setOnClickListener(quitProject);
                if (data.type == TaskObject.Members.MEMBER_TYPE_OWNER) {
                    holder.btn.setVisibility(View.GONE);
                } else {
                    holder.btn.setVisibility(View.VISIBLE);
                }
            } else {
                holder.btn.setImageResource(R.drawable.ic_send_message);
                holder.btn.setTag(data.user);
                holder.btn.setOnClickListener(sendMessage);
                holder.btn.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

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
                String.format(urlMembers, mProjectObject.getId());
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                AlertDialog dialog = builder.setTitle("确认退出项目")
                        .setMessage(String.format("您确定要退出 %s 项目吗？", mProjectObject.name))
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RequestParams params = new RequestParams();
                                postNetwork(urlQuit, params, urlQuit);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();

                CustomDialog.dialogTitleLineColor(getActivity(), dialog);
            }
        };
    };

    static class ViewHolder {
        ImageView icon;
        TextView name;
        ImageView ic;
        ImageView btn;
    }

    @Override
    protected View getAnchorView() {
        return listView;
    }

    @Override
    protected String getLink() {
        return Global.HOST + mProjectObject.project_path + "/members";
    }
}
