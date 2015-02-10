package net.coding.program.project.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chaochen on 15/1/30.
 */

@EFragment(R.layout.project_git_fragment_main)
public class ProjectGitFragmentMain extends ProjectGitFragment {

    private final String HOST_LIST_BRANCHES = Global.HOST + "/api/user/%s/project/%s/git/list_branches";
    private final String HOST_LIST_TAG = Global.HOST + "/api/user/%s/project/%s/git/list_tags";

    private ArrayList<String> mDataVers[] = new ArrayList[]{new ArrayList(), new ArrayList()};

    @ViewById
    TextView versionButton;

    @ViewById
    View versionLayout;

    @ViewById
    ExpandableListView versionList;

    @ViewById
    View expandableIndicator;

    // 父类已经使用了 init，子类就不能再用这个名字，否则 init 会调用两次
    @AfterViews
    protected void init2() {
        setHasOptionsMenu(true);

        String urlBranches = String.format(HOST_LIST_BRANCHES, mProjectObject.owner_user_name, mProjectObject.name);
        getNetwork(urlBranches, HOST_LIST_BRANCHES);

        String urlTag = String.format(HOST_LIST_TAG, mProjectObject.owner_user_name, mProjectObject.name);
        getNetwork(urlTag, HOST_LIST_TAG);
        versionList.setAdapter(versionAdapter);

        int left = Global.dpToPx(MyApp.sWidthDp - 40);
        int right = Global.dpToPx(MyApp.sWidthDp - 12);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            versionList.setIndicatorBounds(left, right);
        } else {
            versionList.setIndicatorBoundsRelative(left, right);
        }
        versionList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String data = (String) versionAdapter.getChild(groupPosition, childPosition);
                mVersion = data;

                versionButton.performClick();
                onRefresh();
                return true;
            }
        });
    }

    @Click
    void versionButton() {
        showList(versionLayout.getVisibility() != View.VISIBLE);
    }

    @Click
    void versionLayout() {
        showList(versionLayout.getVisibility() != View.VISIBLE);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_LIST_BRANCHES)) {
            parseVersion(mDataVers[0], code, respanse);
        } else if (tag.equals(HOST_LIST_TAG)) {
            parseVersion(mDataVers[1], code, respanse);
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }

    @Override
    protected void switchVersionSuccess() {
        showButtomToast(String.format("已切换到 %s", mVersion));
        versionButton.setText(mVersion);
    }

    private void parseVersion(ArrayList<String> data, int code, JSONObject respanse) {
        if (code == 0) {
            JSONArray array = respanse.optJSONArray("data");
            data.clear();
            for (int i = 0; i < array.length(); ++i) {
                data.add(array.optJSONObject(i).optString("name", ""));
            }

            ((BaseExpandableListAdapter) versionAdapter).notifyDataSetChanged();

        } else {
            showErrorMsg(code, respanse);
        }
    }

    ExpandableListAdapter versionAdapter = new BaseExpandableListAdapter() {
        @Override
        public int getGroupCount() {
            return mDataVers.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mDataVers[groupPosition].size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return "";
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mDataVers[groupPosition].get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewGroupHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.git_view_group, parent, false);
                holder = new ViewGroupHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewGroupHolder) convertView.getTag();
            }

            final String[] datas = new String[]{"branches", "tags"};
            final int[] iconsId = new int[]{R.drawable.icon_git_branch, R.drawable.icon_git_tag};

            holder.title.setText(datas[groupPosition]);
            holder.icon.setBackgroundResource(iconsId[groupPosition]);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.git_view_child, parent, false);
            }

            String data = (String) getChild(groupPosition, childPosition);
            ((TextView) convertView).setText(data);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    };

    private static class ViewGroupHolder {

        public ViewGroupHolder(View parent) {
            icon = parent.findViewById(R.id.icon);
            title = (TextView) parent.findViewById(R.id.title);
        }

        View icon;
        TextView title;
    }

    private void showList(boolean show) {
        Animation animation;
        Animation fadeIn;
        if (show) {
            animation = AnimationUtils.loadAnimation(getActivity(), R.anim.listview_top_down);
            fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.listview_fade_in);
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    expandableIndicator.setBackgroundResource(R.drawable.icon_git_indicator_up);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        } else {
            animation = AnimationUtils.loadAnimation(getActivity(), R.anim.listview_top_up);
            fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.listview_fade_out);
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    versionLayout.setVisibility(View.INVISIBLE);
                    expandableIndicator.setBackgroundResource(R.drawable.icon_git_indicator_down);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }

        versionList.startAnimation(animation);
        versionLayout.startAnimation(fadeIn);

        if (show) {
            versionLayout.setVisibility(View.VISIBLE);
        }
    }

}
