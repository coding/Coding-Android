package net.coding.program.subject;


import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.maopao.MaopaoListBaseFragment;
import net.coding.program.model.Maopao;
import net.coding.program.model.Subject;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

@EFragment(R.layout.subject_detail_maopao_list)
public class SubjectDetailFragment extends MaopaoListBaseFragment {

    final String maopaoUrlFormat = Global.HOST_API + "/public_tweets/topic/%s?last_id=%s&sort=new";
    final String maopaoUrlFirstFormat = Global.HOST_API + "/public_tweets/topic/%s?&sort=new";
    final String maopaoUrlTopFormat = Global.HOST_API + "/public_tweets/topic/%s/top";
    final String maopaoUrlHotJoinedFormat = Global.HOST_API + "/tweet_topic/%s/hot_joined";
    final String maopaoUrlDetailFormat = Global.HOST_API + "/tweet_topic/%s";

    final String topicWatchUrl = Global.HOST_API + "/tweet_topic/%s/watch";
    final String topicUnWatchUrl = Global.HOST_API + "/tweet_topic/%s/unwatch";

    @FragmentArg
    Subject.SubjectDescObject subjectDescObject;
    @FragmentArg
    int topicId;

    View mListHeaderView;

    @Override
    protected void setActionTitle() {}

    private TextView mSubjectNameTv;
    private TextView mSubjectDescTv;
    private TextView mFollowTv;
    private TextView mJoinedPeopleTv;
    private FlowLayout mAllJoinedPeopleLayout;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.subject_detail_follow_btn:
                    if (subjectDescObject != null) {
                        if (subjectDescObject.watched) {
                            deleteNetwork(String.format(topicUnWatchUrl, subjectDescObject.id), topicUnWatchUrl);
                        } else {
                            postNetwork(String.format(topicWatchUrl, subjectDescObject.id), null, topicWatchUrl);
                        }
                    }
                    break;
                case R.id.subject_detail_view_all:
                    if (subjectDescObject != null)
                        SubjectUsersActivity_.intent(getActivity()).topicId(subjectDescObject.id).start();
                    break;
            }
        }
    };

    @AfterViews
    protected void init() {
        mIsToMaopaoTopic = true;
        initMaopaoListBaseFragmen();
    }

    @Override
    protected void initMaopaoType() {
        mListHeaderView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_subject_detail_header, null);
        mSubjectNameTv = (TextView) mListHeaderView.findViewById(R.id.subject_detail_title);
        mSubjectDescTv = (TextView) mListHeaderView.findViewById(R.id.subject_detail_desc);
        mFollowTv = (TextView) mListHeaderView.findViewById(R.id.subject_detail_follow_btn);
        mJoinedPeopleTv = (TextView) mListHeaderView.findViewById(R.id.subject_detail_view_all);
        mFollowTv.setOnClickListener(mOnClickListener);
        mJoinedPeopleTv.setOnClickListener(mOnClickListener);

        mAllJoinedPeopleLayout = (FlowLayout) mListHeaderView.findViewById(R.id.subject_detail_all_join);


        listView.addHeaderView(mListHeaderView);
    }

    @Override
    protected String getMaopaoUrlFormat() {
        return maopaoUrlFormat;
    }

    @Override
    protected void initData() {
        if (subjectDescObject != null) {
            fillHeaderViewData();
            getNetwork(String.format(maopaoUrlTopFormat, subjectDescObject.id), maopaoUrlTopFormat);
            getNetwork(String.format(maopaoUrlHotJoinedFormat, subjectDescObject.id), maopaoUrlHotJoinedFormat);
            getNetwork(createUrl(), maopaoUrlFormat);
        } else {
            if (topicId > 0) {
                getNetwork(String.format(maopaoUrlDetailFormat, topicId), maopaoUrlDetailFormat);
            }
        }
    }

    private void fillHeaderViewData() {
        if (subjectDescObject != null) {
            mSubjectNameTv.setText("#" + subjectDescObject.name + "#");
            mSubjectDescTv.setText(String.format("%s人参与/%s人关注", subjectDescObject.speackers, subjectDescObject.watchers));
            // 更新topic的关注状态
            updateTopicFollowState();
        }
    }

    private void updateTopicFollowState() {
        if (subjectDescObject != null) {
            if (subjectDescObject.watched)
                mFollowTv.setBackgroundResource(R.drawable.topic_unfollow);
            else
                mFollowTv.setBackgroundResource(R.drawable.topic_follow);
        }
    }

    private int getUserAvatarCount() {
        int count = 7;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (displayMetrics != null) {
            int width = displayMetrics.widthPixels;
            count = (width - getPxValue(10.66f)) / getPxValue(46.66f);
        }
        if (count > 8)
            count = 8;
        return count;
    }

    private int getPxValue(float dipValue) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, getResources().getDisplayMetrics()) + 0.5f);
    }

    @Override
    protected String createUrl() {
        if (subjectDescObject != null) {
            if (id == UPDATE_ALL_INT) {
                return String.format(maopaoUrlFirstFormat, subjectDescObject.id);
            } else
                return String.format(maopaoUrlFormat, subjectDescObject.id, id);
        }
        return "";
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(maopaoUrlTopFormat)) {
            if (code == 0) {
                JSONObject json = respanse.optJSONObject("data");
                if (json != null) {
                    Maopao.MaopaoObject item = new Maopao.MaopaoObject(json);
                    mIsToMaopaoTopic = true;
                    if (id == UPDATE_ALL_INT) {
                        mData.clear();
                        mData.add(0, item);
                        id = item.id;
                    } else {
                        if (!mData.contains(item))
                            mData.add(0, item);
                        else {
                            int index = mData.indexOf(item);
                            item = mData.remove(index);
                            mIsToMaopaoTopic = true;
                            mData.add(0, item);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }

            }
        } else if (tag.equals(maopaoUrlDetailFormat)) {
            if (code == 0) {
                JSONObject json = respanse.optJSONObject("data");
                if (json != null) {
                    subjectDescObject = new Subject.SubjectDescObject(json);
                    initData();
                }

            }
        } else if (tag.equals(maopaoUrlHotJoinedFormat)) {
            if (code == 0) {
                JSONArray json = respanse.optJSONArray("data");
                if (json != null) {
                    CircleImageView circleImageView;
                    UserObject userObject;
                    FlowLayout.LayoutParams layoutParams;
                    int countLimit = getUserAvatarCount();
                    int size = countLimit > json.length() ? json.length() : countLimit;
                    for (int i = 0; i < size; i++) {
                        userObject = new UserObject(json.optJSONObject(i));
                        circleImageView = new CircleImageView(getActivity());
                        circleImageView.setTag(userObject.global_key);
                        circleImageView.setOnClickListener(mOnClickUser);
                        layoutParams = new FlowLayout.LayoutParams(getPxValue(40f), getPxValue(40f));
                        layoutParams.weight = 1;
                        layoutParams.newLine = false;
                        layoutParams.setMargins(10, 0, 10, 0);
                        circleImageView.setLayoutParams(layoutParams);
                        iconfromNetwork(circleImageView, userObject.avatar);
                        mAllJoinedPeopleLayout.addView(circleImageView);
                    }
                }

            }
        } else if (tag.equals(topicWatchUrl)) {
            if (code == 0) {
                if (subjectDescObject != null) {
                    subjectDescObject.watched = true;
                    updateTopicFollowState();
                }
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(topicUnWatchUrl)) {
            if (code == 0) {
                if (subjectDescObject != null) {
                    subjectDescObject.watched = false;
                    updateTopicFollowState();
                }
            } else {
                showErrorMsg(code, respanse);
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }



}
