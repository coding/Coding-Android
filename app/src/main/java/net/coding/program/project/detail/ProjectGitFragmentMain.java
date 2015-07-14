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
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by chaochen on 15/1/30.
 */

@EFragment(R.layout.project_git_fragment_main)
public class ProjectGitFragmentMain extends ProjectGitFragment {

    private final String HOST_LIST_BRANCHES = Global.HOST_API + "%s/git/branches?pageSize=1000";
    private final String HOST_LIST_TAG = Global.HOST_API + "%s/git/list_tags";
    @ViewById
    TextView versionButton;
    @ViewById
    View versionLayout;
    @ViewById
    ExpandableListView versionList;
    @ViewById
    View expandableIndicator;
    private ArrayList<BranchItem> mDataVers[] = new ArrayList[]{new ArrayList<>(), new ArrayList<>()};
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

            BranchItem data = (BranchItem) getChild(groupPosition, childPosition);
            ((TextView) convertView).setText(data.name);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    };

    @AfterViews
    protected void initProjectGitFragmentMain() {
        setHasOptionsMenu(true);

        String urlBranches = String.format(HOST_LIST_BRANCHES, mProjectPath);
        getNetwork(urlBranches, HOST_LIST_BRANCHES);

        if (mVersion != null && !mVersion.isEmpty()) {
            switchVersion(mVersion);
        }

        String urlTag = String.format(HOST_LIST_TAG, mProjectPath);
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
                BranchItem data = (BranchItem) versionAdapter.getChild(groupPosition, childPosition);
                switchVersion(data.name);
                showList(versionLayout.getVisibility() != View.VISIBLE);
                return true;
            }
        });
    }

    @Click
    protected final void versionButton() {
        showList(versionLayout.getVisibility() != View.VISIBLE);
    }

    @Click
    protected final void versionLayout() {
        showList(versionLayout.getVisibility() != View.VISIBLE);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_LIST_BRANCHES)) {
            if (code == 0) {
                JSONObject jsonData = respanse.optJSONObject("data");
                if (jsonData == null) {
                    if (mDataVers[0].isEmpty()) {
                        hideProgressDialog();
                        getView().findViewById(R.id.top).setVisibility(View.INVISIBLE);
                        BlankViewDisplay.setBlank(0, this, true, blankLayout, onClickRetry);
                    }
                } else {
                    parseVersion(mDataVers[0], jsonData.optJSONArray("list"));
                }
            } else {
                showErrorMsg(code, respanse);
                hideProgressDialog();
            }
        } else if (tag.equals(HOST_LIST_TAG)) {
            if (code == 0) {
                parseVersion(mDataVers[1], respanse.optJSONArray("data"));
            } else {
                showErrorMsg(code, respanse);
                hideProgressDialog();
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }

    private void switchVersion(String name) {
        mVersion = name;
        onRefresh();
    }

    @Override
    protected void switchVersionSuccess() {
        showButtomToast(String.format("已切换到 %s", mVersion));
        versionButton.setText(mVersion);
    }

    private void parseVersion(ArrayList<BranchItem> data, JSONArray jsonArray) {
        data.clear();
        int len = jsonArray.length();
        for (int i = 0; i < len; ++i) {
            BranchItem item = new BranchItem(jsonArray.optJSONObject(i));
            data.add(item);

            if (item.is_default_branch && (mVersion == null || mVersion.isEmpty())) {
                switchVersion(item.name);
            }
        }

        ((BaseExpandableListAdapter) versionAdapter).notifyDataSetChanged();
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

    private static class ViewGroupHolder {

        View icon;
        TextView title;
        public ViewGroupHolder(View parent) {
            icon = parent.findViewById(R.id.icon);
            title = (TextView) parent.findViewById(R.id.title);
        }
    }

    public static final class BranchItem implements Serializable {
        String name = "";// "raml-doc",
        boolean is_default_branch; // false,
        boolean is_protected; // false

        BranchItem(JSONObject json) {
            name = json.optString("name", "");
            is_default_branch = json.optBoolean("is_default_branch");
            is_protected = json.optBoolean("is_protected");
        }
    }

}
