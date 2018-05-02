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
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.LoadMore;
import net.coding.program.common.base.CustomMoreFragment;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.network.constant.MemberAuthority;
import net.coding.program.network.model.user.Member;
import net.coding.program.project.EventProjectModify;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static net.coding.program.project.detail.MembersListFragment.DataType.User;


@EFragment(R.layout.common_refresh_listview_divide)
public class MembersListFragment extends CustomMoreFragment implements LoadMore {

    static final int RESULT_ADD_USER = 111;
    static final int RESULT_MODIFY_AUTHORITY = 112;

    final String urlDeleteUser = Global.HOST_API + "/project/%d/kickout/%d";
    String urlMembers = Global.HOST_API + "/project/%d/members?pagesize=1000";
    String urlQuit = Global.HOST_API + "/project/%d/quit";
    @FragmentArg
    ProjectObject mProjectObject;
    @FragmentArg
    String mMergeUrl;
    @FragmentArg
    Type type = Type.Member;
    @FragmentArg
    DataType dataType;
    @ViewById
    ListView listView;
    ArrayList<Object> mSearchData = new ArrayList<>();
    ArrayList<Object> mData = new ArrayList<>();
    Member mMySelf = new Member();
    BaseAdapter adapter = new BaseAdapter() {
        private View.OnClickListener quitProject = v -> {
            String message = String.format("您确定要退出 %s 项目吗？", mProjectObject.name);
            showDialog("退出项目", message, (dialog1, which) -> {
                RequestParams params = new RequestParams();
                postNetwork(urlQuit, params, urlQuit);
            });
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
                holder.bottomLine = convertView.findViewById(R.id.bottomLine);
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

            if (object instanceof Member) {
                Member data = (Member) object;
                user = data.user;

                MemberAuthority memberType = data.getType();
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

            boolean isLast = mSearchData.size() - 1 == position;
            holder.bottomLine.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);
            if (isLast) {
                loadMore();
            }

            if (type == Type.Pick) {
                holder.btn.setVisibility(View.GONE);
            } else if (user.name.equals(GlobalData.sUserObject.name) &&
                    !GlobalData.isEnterprise() &&
                    (mProjectObject != null && mProjectObject.isPublic())) {
                // 只有公开项目还有退出按钮
                holder.btn.setImageResource(R.drawable.ic_member_list_quit);
                holder.btn.setOnClickListener(quitProject);
                if (object instanceof Member) {
                    Member data = (Member) object;
                    if (data.isOwner()) {
                        holder.btn.setVisibility(View.GONE);
                    } else {
                        holder.btn.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                holder.btn.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }
    };

    @ItemClick(R.id.listView)
    public void listViewItemClicked(Object object) {
        if (type == Type.Pick) {
            Intent intent = new Intent();
            UserObject userObject;

            if (object instanceof Member) {
                userObject = ((Member) object).user;
            } else {
                userObject = (UserObject) object;
            }

            intent.putExtra("name", userObject.name);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        } else if (dataType == DataType.User) {
            if (object instanceof UserObject) {
                String globalKey = ((UserObject) object).global_key;
                CodingCompat.instance().launchUserDetailActivity(getActivity(), globalKey);
            }
        } else {
            UserDynamicActivity_
                    .intent(getActivity())
                    .mProjectObject(mProjectObject)
                    .mMember((Member) object)
                    .start();
        }
    }

    @AfterViews
    protected void init() {
        initRefreshLayout();

        mData = new ArrayList<>();
        mSearchData = new ArrayList<>(mData);
        if (mSearchData.isEmpty()) {
            showDialogLoading();
        }

        listViewAddHeaderSection(listView);

        listView.setAdapter(adapter);


        if (type != Type.Pick) {
            listView.setOnItemLongClickListener((parent, view, position, id) -> {
                Member member = (Member) mSearchData.get((int) id);
//                    if (member.user.isMe()) {
//                        return true;
//                    }

                if (mMySelf.getType() != MemberAuthority.ower
                        && mMySelf.getType() != MemberAuthority.manager) {
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
                        if (member.getType() == MemberAuthority.manager
                                || member.getType() == MemberAuthority.ower) {
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

                new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
                        .setItems(items, clicks)
                        .show();
                return true;
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

    private void modifyMemberAuthority(Member member) {
        MemberAuthorityActivity_.intent(this)
                .authority(member.getType())
                .globayKey(member.user.global_key)
                .me(mMySelf)
                .projectId(mProjectObject.getId())
                .startForResult(RESULT_MODIFY_AUTHORITY);
    }

    private void modifyMemberAlias(Member member) {
        UserObject user = member.user;
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_input_alias, null);
        EditText input = (EditText) v.findViewById(R.id.edit1);
        input.setText(member.alias);
        new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
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

    private void removeMember(Member member) {
        UserObject user = member.user;
        new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
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
                if (item instanceof Member) {
                    user = ((Member) item).user;
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

    private boolean canManagerMember() {
        return mProjectObject != null && mProjectObject.canManagerMember();
    }

    @OptionsItem
    void action_add() {
        ArrayList<String> picks = new ArrayList<>();
        if (mSearchData != null && mSearchData.size() > 0 && mSearchData.get(0) instanceof Member) {
            for (Object item : mSearchData) {
                picks.add(((Member) item).user.global_key);
            }
        }
        CodingCompat.instance().launchAddMemberActivity(this, mProjectObject, picks, RESULT_ADD_USER);
    }

    @OnActivityResult(RESULT_ADD_USER)
    void onResultAddUser(int resultCode) {
        onRefresh();
    }

    @OnActivityResult(RESULT_MODIFY_AUTHORITY)
    void onResultModifyAuthority(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            onRefresh();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (type != Type.Pick) {
            if (canManagerMember()) {
                inflater.inflate(R.menu.users, menu);
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onRefresh() {
        initSetting();
        getNetwork(urlMembers, urlMembers);
    }

    private void parseUser(JSONArray members) {
        for (int i = 0; i < members.length(); ++i) {
            UserObject member = new UserObject(members.optJSONObject(i));
            mData.add(member);
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(urlMembers)) {
            hideDialogLoading();
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

                    if (dataType == User) {
                        parseUser(members);
                    } else {
                        for (int i = 0; i < members.length(); ++i) {
                            Member member = new Member(members.getJSONObject(i));
                            if (member.isOwner()) {
                                mData.add(0, member);
                            } else {
                                mData.add(member);
                            }

                            if (member.isMe()) {
                                mMySelf = member;
                            }

                        }
                    }

//                    AccountInfo.saveProjectMembers(getActivity(), (ArrayList) mData, mProjectObject.getId());
                } else { // merge 的at他人列表只用 data 包了一层
                    JSONArray members = respanse.getJSONArray("data");
                    parseUser(members);
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

                EventBus.getDefault().post(new EventProjectModify().setExit());

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

    public enum Type {
        Member,
        Pick,
    }

    public enum DataType {
        Member,
        User
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView alias;
        ImageView ic;
        ImageView btn;
        View bottomLine;
    }
}
