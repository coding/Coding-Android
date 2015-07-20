package net.coding.program.subject;

import android.view.View;
import android.widget.ListView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.network.RefreshBaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * Created by david on 15-7-18.
 */
@EFragment(R.layout.fragment_topic_list)
public class SubjectListFragment extends RefreshBaseFragment {

    final String subjectFollowUrlFormat = Global.HOST_API + "/user/%s/tweet_topic/watched";
    final String subjectJoinUrlFormat = Global.HOST_API + "/user/%s/tweet_topic/joine";
    final String subjectFollowTag = "subject_follow";
    final String subjectJoinTag = "subject_join";


    @FragmentArg
    Type mType;
    @FragmentArg
    String userKey;

    @ViewById
    ListView listView;
    @ViewById
    View blankLayout;


    @AfterViews
    protected void init() {
        initRefreshLayout();

        setRefreshing(true);

        mFootUpdate.init(listView, mInflater, this);
//        listView.setAdapter(mAdapter);
    }


    public enum Type {
        follow, join
    }

    @Override
    public void onRefresh() {
        loadMore();
    }

    @Override
    public void loadMore() {
        String url = "";
        String tag = "";

        if (mType == Type.follow) {
            url = String.format(subjectFollowUrlFormat, userKey);
            tag = subjectFollowTag;
        } else if (mType == Type.join) {
            url = String.format(subjectJoinUrlFormat, userKey);
            tag = subjectJoinTag;
        }
        getNextPageNetwork(url, tag);
    }

    void initData() {
        loadMore();
    }


}



