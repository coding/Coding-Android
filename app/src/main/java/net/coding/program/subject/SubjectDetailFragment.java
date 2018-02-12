package net.coding.program.subject;


import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.model.Maopao;
import net.coding.program.common.model.Subject;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.widget.MemberIcon;
import net.coding.program.maopao.MaopaoListBaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    TextView mSubjectDetailJoin;
    private TextView mJoinedPeopleTv;
    private FlowLayout mAllJoinedPeopleLayout;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.subject_detail_view_all:
                    if (subjectDescObject != null)
                        SubjectUsersActivity_.intent(getActivity()).topicId(subjectDescObject.id).start();
                    break;
            }
        }
    };

    @Override
    protected void setActionTitle() {
    }

    @AfterViews
    protected void init() {
        mIsToMaopaoTopic = true;
        initMaopaoListBaseFragmen(null);
    }

    @Override
    protected void initMaopaoType() {
        mListHeaderView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_subject_detail_header, null);
        mJoinedPeopleTv = (TextView) mListHeaderView.findViewById(R.id.subject_detail_view_all);
        mSubjectDetailJoin = (TextView) mListHeaderView.findViewById(R.id.subject_detail_join);
        mJoinedPeopleTv.setOnClickListener(mOnClickListener);

        mAllJoinedPeopleLayout = (FlowLayout) mListHeaderView.findViewById(R.id.subject_detail_all_join);

        listView.setNormalHeader(mListHeaderView);
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
            getActionBarActivity().setTitle("#" + subjectDescObject.name + "#");
            mSubjectDetailJoin.setText(String.format("%s人参与", subjectDescObject.speackers));
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
            if (id == Global.UPDATE_ALL_INT) {
                return String.format(maopaoUrlFirstFormat, subjectDescObject.id);
            } else
                return String.format(maopaoUrlFormat, subjectDescObject.id, id);
        }

        return String.format(maopaoUrlFirstFormat, topicId);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(maopaoUrlTopFormat)) {
            if (code == 0) {
                JSONObject json = respanse.optJSONObject("data");
                if (json != null) {
                    Maopao.MaopaoObject item = new Maopao.MaopaoObject(json);
                    mIsToMaopaoTopic = true;
                    if (id == Global.UPDATE_ALL_INT) {
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
                    MemberIcon circleImageView;
                    UserObject userObject;
                    FlowLayout.LayoutParams layoutParams;
                    int countLimit = getUserAvatarCount();
                    int size = countLimit > json.length() ? json.length() : countLimit;
                    for (int i = 0; i < size; i++) {
                        userObject = new UserObject(json.optJSONObject(i));
                        circleImageView = new MemberIcon(getActivity());
                        circleImageView.setTag(userObject);
                        circleImageView.setOnClickListener(GlobalCommon.mOnClickUser);
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
                }
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(topicUnWatchUrl)) {
            if (code == 0) {
                if (subjectDescObject != null) {
                    subjectDescObject.watched = false;
                }
            } else {
                showErrorMsg(code, respanse);
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }


}
