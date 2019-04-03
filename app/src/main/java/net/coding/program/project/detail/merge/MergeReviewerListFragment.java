package net.coding.program.project.detail.merge;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
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

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.LoadMore;
import net.coding.program.common.base.CustomMoreFragment;
import net.coding.program.common.model.Merge;
import net.coding.program.common.model.UserObject;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.network.constant.MemberAuthority;
import net.coding.program.network.model.user.Member;
import net.coding.program.project.detail.MembersSelectActivity_;
import net.coding.program.route.BlankViewDisplay;

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
import java.util.List;


@EFragment(R.layout.common_refresh_listview)
public class MergeReviewerListFragment extends CustomMoreFragment implements LoadMore {

    static final int RESULT_ADD_USER = 111;
    static final int RESULT_MODIFY_AUTHORITY = 112;

    static final String TAG_URL_REVIEWER = "TAG_URL_REVIEWER";
    static final String TAG_URL_REVIEW_GOOD = "TAG_URL_REVIEWGOOD";
    static final String TAG_URL_REVIEW_BAD = "TAG_URL_REVIEWBAD";
    static final String TAG_URL_ADD_REVIEWER = "TAG_URL_ADD_REVIEWER";
    static final String TAG_URL_DEL_REVIEWER = "TAG_URL_DEL_REVIEWER";
    static final String TAG_URL_MEMBER = "TAG_URL_MEMBER";

    String urlMembers;
    @FragmentArg
    Merge mMerge;
    // 为true表示是添加或删除评审者，为false表示评审者列表
    @FragmentArg
    boolean mSelect;
    @ViewById
    ListView listView;

    @ViewById
    View blankLayout;

    ArrayList<Object> mSearchData = new ArrayList<>();
    ArrayList<Merge.Reviewer> mReviewers = new ArrayList<>();
    ArrayList<Member> mMembers = new ArrayList<>();
    ArrayList<String> mReviewerKey = new ArrayList<>();
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
                convertView = mInflater.inflate(R.layout.fragment_reviewers_list_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.alias = (TextView) convertView.findViewById(R.id.alias);
                //holder.desc = (TextView) convertView.findViewById(R.id.desc);
                holder.ic = (ImageView) convertView.findViewById(R.id.ic);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.invitee = convertView.findViewById(R.id.is_invitee);
//                holder.icon.setOnClickListener(mOnClickUser);
//                holder.icon.setFocusable(false);
                holder.reviewerStatus = (TextView) convertView.findViewById(R.id.reviewer_status);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Object object = mSearchData.get(position);
            UserObject user = null;
            holder.ic.setVisibility(View.GONE);

            if (mSelect) {
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
                }

                if (user != null) {
                    holder.reviewerStatus.setText("");
                    holder.reviewerStatus.setBackgroundResource(R.drawable.select_reviewer_list_checked);

                    if (mReviewerKey.contains(user.global_key)) {
                        holder.reviewerStatus.setVisibility(View.VISIBLE);
                    } else {
                        holder.reviewerStatus.setVisibility(View.GONE);
                    }

                    if (user.global_key.equals(GlobalData.sUserObject.global_key)) {

                    }
                }
                holder.invitee.setVisibility(View.GONE);
            } else {
                if (object instanceof Merge.Reviewer) {
                    Merge.Reviewer reviewer = (Merge.Reviewer) object;
                    user = reviewer.user;
                    holder.ic.setVisibility(View.GONE);
                    holder.alias.setVisibility(View.GONE);

                    String volunteer = reviewer.volunteer;
                    int value = reviewer.value;

                    if (value > 0) {
                        if (TextUtils.equals(volunteer, "invitee")) {
                            holder.invitee.setVisibility(View.VISIBLE);
                        } else {
                            holder.invitee.setVisibility(View.GONE);
                        }
                        holder.reviewerStatus.setText("+1");
                        holder.reviewerStatus.setTextColor(getResources().getColor(R.color.font_green));
                    } else {
                        holder.reviewerStatus.setText("未评审");
                        holder.reviewerStatus.setTextColor(getResources().getColor(R.color.font_3));
                        holder.invitee.setVisibility(View.GONE);
                    }
                }

                holder.reviewerStatus.setBackgroundColor(0);
                holder.reviewerStatus.setVisibility(View.VISIBLE);

                if (user != null && user.global_key.equals(GlobalData.sUserObject.global_key)) {

                }
            }

            if (user != null) {
                holder.name.setText(user.name);
                iconfromNetwork(holder.icon, user.avatar);
                holder.icon.setTag(user.global_key);
            }
            if (mSearchData.size() - 1 == position) {
                loadMore();
            }


            return convertView;
        }
    };

    @AfterViews
    protected void init() {
        getActivity().setTitle(mSelect ? "添加评审者" : "评审者");

        initRefreshLayout();
        resetAllData();
        if (mSearchData.isEmpty()) {
            showDialogLoading();
        }

        listView.setAdapter(adapter);
        AdapterView.OnItemClickListener mListClickJump;
        if (mSelect) {
            mListClickJump = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Member member = (Member) mSearchData.get(position);
                    if (!mReviewerKey.contains(member.user.global_key)) {
                        addReviewer(member);
                    } else {
                        for (Merge.Reviewer r : mReviewers) {
                            if (r.user.global_key.equals(member.user.global_key)) {
                                if (mReviewerKey.contains(member.user.global_key)) {
                                    removeReviewer(r);
                                }
                                break;
                            }
                        }
//                        showButtomToast("已是评审者，长按移除评审者。");
                    }
                }
            };
        } else {
            mListClickJump = (parent, view, position, id) -> {
                Merge.Reviewer reviewer = (Merge.Reviewer) mSearchData.get(position);
                if (reviewer.user.global_key.equals(GlobalData.sUserObject.global_key) && !isDealed()) {
                    if (reviewer.value <= 0) {
                        postNetwork(mMerge.getHttpReviewGood(), new RequestParams(), TAG_URL_REVIEW_GOOD, 0, reviewer);
                    } else {
                        deleteNetwork(mMerge.getHttpReviewGood(), TAG_URL_REVIEW_BAD, reviewer);
                    }
                } else {
                    CodingCompat.instance().launchUserDetailActivity(getActivity(),
                            reviewer.user.global_key);
                }

            };
        }
        listView.setOnItemClickListener(mListClickJump);

        if (!mSelect && !isDealed()) {
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
                    Merge.Reviewer reviewer = null;
                    if (mSelect) {
                        Member member = (Member) mSearchData.get(position);
                        for (Merge.Reviewer r : mReviewers) {
                            if (r.user.global_key.equals(member.user.global_key)) {
                                if (mReviewerKey.contains(member.user.global_key))
                                    reviewer = r;
                                else
                                    break;
                                break;
                            }
                        }
                    } else {
                        reviewer = (Merge.Reviewer) mSearchData.get(position);
                    }

                    if (reviewer != null && reviewer.volunteer.equals("invitee")) {
                        String[] items;
                        DialogInterface.OnClickListener clicks;
                        items = new String[]{
                                "移除评审者"
                        };
                        final Merge.Reviewer finalReviewer = reviewer;
                        clicks = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeReviewer(finalReviewer);
                            }
                        };

                        new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
                                .setItems(items, clicks)
                                .show();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

        if (mMerge != null) {
            urlMembers = Global.HOST_API + mMerge.getProjectPath() + "/members?pagesize=1000";
            setHasOptionsMenu(true);
            onRefresh();
        } else {  // mMergeUrl 不为空
            getActivity().finish();
        }
    }

    private void removeReviewer(Merge.Reviewer reviewer) {
        UserObject user = reviewer.user;
        deleteNetwork(mMerge.getHttpDelReviewer() + "?user_id=" + user.id, TAG_URL_DEL_REVIEWER, reviewer);
    }

    private void addReviewer(Member member) {
        UserObject user = member.user;

        RequestParams params = new RequestParams("user_id", String.valueOf(user.id));
        postNetwork(mMerge.getHttpAddReviewer(), params, TAG_URL_ADD_REVIEWER, 0, member);
    }

    public void search(String input) {
        mSearchData.clear();
        if (input.isEmpty()) {
            resetAllData();
        } else {
            for (Object item : mSelect ? mMembers : mReviewers) {
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

    private boolean requestCreateByMe() {
        return mMerge.authorIsMe();
    }


    @OptionsItem
    void action_add() {
        Intent intent = new Intent(getActivity(), MembersSelectActivity_.class);
        intent.putExtra("mMerge", mMerge);
        intent.putExtra("mSelect", true);
        startActivityForResult(intent, RESULT_ADD_USER);
    }

    @OnActivityResult(RESULT_ADD_USER)
    void onResultAddUser(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            onRefresh();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mSelect && !isDealed()) {
            inflater.inflate(R.menu.users, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean isDealed() {
        return mMerge.isMergeTreate() || mMerge.isCanceled();
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!mSelect) {
            menu.findItem(R.id.action_search).setVisible(false);
        }

    }

    @Override
    public void onRefresh() {
        initSetting();
        loadMore();
        getNetwork(mMerge.getHttpReviewers(), TAG_URL_REVIEWER);
    }

    @Override
    public void loadMore() {
        if (mSelect) {
            getNextPageNetwork(urlMembers, TAG_URL_MEMBER);
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_URL_REVIEWER)) {
            List theData = mSelect ? mMembers : mReviewers;

            hideDialogLoading();
            setRefreshing(false);

            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mReviewers.clear();
                }

                JSONObject dataObject = respanse.optJSONObject("data");
                if (dataObject != null) {
                    JSONArray members;
                    members = respanse.getJSONObject("data").optJSONArray("reviewers");
                    mReviewerKey.clear();
                    if (members != null) {
                        for (int i = 0; i < members.length(); ++i) {
                            Merge.Reviewer member = new Merge.Reviewer(members.optJSONObject(i));
                            if (member.user.global_key.equals(mMerge.getAuthor().global_key)) {
                                continue;
                            }
                            mReviewers.add(member);
                            mReviewerKey.add(member.user.global_key);
                        }
                    }
                    members = respanse.optJSONObject("data").optJSONArray("volunteer_reviewers");
                    for (int i = 0; i < members.length(); ++i) {
                        Merge.Reviewer volunteer = new Merge.Reviewer(members.getJSONObject(i));
                        mReviewers.add(volunteer);
                    }

//                    AccountInfo.saveProjectMembers(getActivity(), (ArrayList) mData, mProjectObject.getId());
                }

                resetAllData();
                BlankViewDisplay.setBlank(theData.size(), this, true, blankLayout, null);

                adapter.notifyDataSetChanged();
            } else {
                BlankViewDisplay.setBlank(theData.size(), this, true, blankLayout, null);

                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_URL_MEMBER)) {
            hideDialogLoading();
            setRefreshing(false);

            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mMembers.clear();
                }

                JSONObject dataObject = respanse.optJSONObject("data");
                // 项目成员的数据是 data - list，包了两层
                if (dataObject != null) {
                    JSONArray members;
                    members = respanse.getJSONObject("data").getJSONArray("list");

                    for (int i = 0; i < members.length(); ++i) {
                        Member member = new Member(members.getJSONObject(i));
                        if (member.user.global_key.equals(mMerge.getAuthor().global_key)) {
                            continue;
                        }
                        if (member.getType() == MemberAuthority.limited) {
                            continue;
                        }
                        if (member.isOwner()) {
                            mMembers.add(0, member);
                        } else {
                            mMembers.add(member);
                        }
                    }
                }
                resetAllData();
                BlankViewDisplay.setBlank(mMembers.size(), this, true, blankLayout, null);
                adapter.notifyDataSetChanged();
            } else {
                BlankViewDisplay.setBlank(mMembers.size(), this, true, blankLayout, null);
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(TAG_URL_DEL_REVIEWER)) {
            getActivity().setResult(Activity.RESULT_OK);
            showProgressBar(false);
            if (code == 0) {
                Merge.Reviewer reviewer = (Merge.Reviewer) data;
                mReviewers.remove(reviewer);
                mReviewerKey.remove(reviewer.user.global_key);
                reviewer.volunteer = "volunteer";
                if (reviewer.value == 0) {
                    mSearchData.remove(reviewer);
                }
                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_URL_ADD_REVIEWER)) {
            getActivity().setResult(Activity.RESULT_OK);
            showProgressBar(false);
            if (code == 0) {

                Member member = (Member) data;
                UserObject user = member.user;
                Merge.Reviewer reviewer = null;
                for (Merge.Reviewer r : mReviewers) {
                    if (r.user.global_key.equals(member.user.global_key)) {
                        reviewer = r;
                        if (!mReviewerKey.contains(user.global_key)) {
                            mReviewerKey.add(user.global_key);
                        }
                        break;
                    }
                }
                if (reviewer == null) {
                    reviewer = new Merge.Reviewer(user);
                    mReviewers.add(reviewer);
                    mReviewerKey.add(user.global_key);
                }

                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_URL_REVIEW_GOOD) || tag.equals(TAG_URL_REVIEW_BAD)) {
            getActivity().setResult(Activity.RESULT_OK);
            showProgressBar(false);
            if (code == 0) {
                onRefresh();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @Override
    protected String getLink() {
        return mMerge.getHttpReviewers();
    }

    private void resetAllData() {
        mSearchData.clear();
        mSearchData.addAll(mSelect ? mMembers : mReviewers);
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView alias;
        ImageView ic;
        TextView reviewerStatus;
        View invitee;
    }
}
