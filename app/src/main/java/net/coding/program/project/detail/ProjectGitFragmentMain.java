package net.coding.program.project.detail;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.util.BlankViewHelp;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by chaochen on 15/1/30.
 * 代码页面的主入口页面
 */

@EFragment(R.layout.project_git_fragment_main)
public class ProjectGitFragmentMain extends ProjectGitFragment {

    private final String HOST_LIST_BRANCHES = Global.HOST_API + "%s/git/branches?pageSize=1000";
    private final String HOST_LIST_TAG = Global.HOST_API + "%s/git/list_tags";

    @ViewById
    View versionLayout, expandableIndicator, linearLayout, tvBranches, tvTags;
    @ViewById
    TextView versionButton;
    //    @ViewById
//    ExpandableListView versionList;
    @ViewById
    ListView versionListView;

    @ViewById(R.id.top)
    View topLayout; // branch 选择

    private ArrayList<BranchItem> mDataVers[] = new ArrayList[]{new ArrayList<>(), new ArrayList<>()};
    private List<BranchItem> mData = new ArrayList<>();
    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.git_view_child, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            BranchItem data = mData.get(position);
            holder.title.setText(data.name);

            if (mVersion.equals(data.name)) {
                holder.title.setSelected(true);
                holder.icon.setVisibility(View.VISIBLE);
            } else {
                holder.title.setSelected(false);
                holder.icon.setVisibility(View.GONE);
            }
            return convertView;
        }

    };
    private List<BranchItem> mBranchesData = new ArrayList<>();

    //    ExpandableListAdapter versionAdapter = new BaseExpandableListAdapter() {
//        @Override
//        public int getGroupCount() {
//            return mDataVers.length;
//        }
//
//        @Override
//        public int getChildrenCount(int groupPosition) {
//            return mDataVers[groupPosition].size();
//        }
//
//        @Override
//        public Object getGroup(int groupPosition) {
//            return "";
//        }
//
//        @Override
//        public Object getChild(int groupPosition, int childPosition) {
//            return mDataVers[groupPosition].get(childPosition);
//        }
//
//        @Override
//        public long getGroupId(int groupPosition) {
//            return groupPosition;
//        }
//
//        @Override
//        public long getChildId(int groupPosition, int childPosition) {
//            return childPosition;
//        }
//
//        @Override
//        public boolean hasStableIds() {
//            return false;
//        }
//
//        @Override
//        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup
//        parent) {
//            ViewGroupHolder holder;
//            if (convertView == null) {
//                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.git_view_group, parent, false);
//                holder = new ViewGroupHolder(convertView);
//                convertView.setTag(holder);
//            } else {
//                holder = (ViewGroupHolder) convertView.getTag();
//            }
//
//            final String[] datas = new String[]{"branches", "tags"};
//            final int[] iconsId = new int[]{R.drawable.icon_git_branch, R.drawable.icon_git_tag};
//
//            holder.title.setText(datas[groupPosition]);
//            holder.icon.setBackgroundResource(iconsId[groupPosition]);
//
//            return convertView;
//        }
//
//        @Override
//        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.git_view_child, parent, false);
//            }
//
//            BranchItem data = (BranchItem) getChild(groupPosition, childPosition);
//            ((TextView) convertView.findViewById(R.id.name)).setText(data.name);
//
//            return convertView;
//        }
//
//        @Override
//        public boolean isChildSelectable(int groupPosition, int childPosition) {
//            return true;
//        }
//
//    };
    private List<BranchItem> mTagsData = new ArrayList<>();
    private View.OnClickListener onClickRetry = v -> {
        String urlBranches = String.format(HOST_LIST_BRANCHES, mProjectPath);
        getNetwork(urlBranches, HOST_LIST_BRANCHES);

        String urlTag = String.format(HOST_LIST_TAG, mProjectPath);
        getNetwork(urlTag, HOST_LIST_TAG);

        showProgressBar(true);
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

        versionListView.setAdapter(adapter);
        versionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BranchItem data = (BranchItem) adapter.getItem(position);
                switchVersion(data.name);
                showList(versionLayout.getVisibility() != View.VISIBLE);
            }
        });

        updateTab(tvBranches);
//        versionList.setAdapter(versionAdapter);
//        int left = Global.dpToPx(MyApp.sWidthDp - 40);
//        int right = Global.dpToPx(MyApp.sWidthDp - 12);
//        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
//            versionList.setIndicatorBounds(left, right);
//        } else {
//            versionList.setIndicatorBoundsRelative(left, right);
//        }
//        versionList.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
//            BranchItem data = (BranchItem) versionAdapter.getChild(groupPosition, childPosition);
//            switchVersion(data.name);
//            showList(versionLayout.getVisibility() != View.VISIBLE);
//            return true;
//        });
    }

    @Click
    protected final void versionButton() {
        showList(versionLayout.getVisibility() != View.VISIBLE);
    }

    @Click
    protected final void versionLayout() {
        showList(versionLayout.getVisibility() != View.VISIBLE);
    }

    @Click
    protected final void tvBranches() {
        updateTab(tvBranches);
        setData(mBranchesData);
    }

    @Click
    protected final void tvTags() {
        updateTab(tvTags);
        setData(mTagsData);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_LIST_BRANCHES)) {
            if (code == 0) {
                JSONObject jsonData = respanse.optJSONObject("data");
                if (jsonData == null) {
                    if (mDataVers[0].isEmpty()) {
                        branchNotExist();
                    }
                } else {
                    JSONArray branchList = jsonData.optJSONArray("list");
                    if (branchList.length() > 0) {
                        JSONObject json = branchList.optJSONObject(0);
                        BranchItem item;
                        if (json != null) {
                            item = new BranchItem(json);
                        } else {
                            item = new BranchItem(branchList.optString(0));
                        }

                        if (TextUtils.isEmpty(item.name)) { //
                            branchNotExist();
                        } else {
                            parseVersion(mDataVers[0], branchList, tag);
                        }
                    } else {
                        branchNotExist();
                    }
                }
            } else {
                showErrorMsg(code, respanse);
                hideDialogLoading();

                if (code == 1200) {
                    setBlank(0, this, true, blankLayout, onClickRetry);
                } else {
                    setBlank(0, this, false, blankLayout, onClickRetry);
                }
            }
        } else if (tag.equals(HOST_LIST_TAG)) {
            if (code == 0) {
                parseVersion(mDataVers[1], respanse.optJSONArray("data"), tag);
            } else {
                showErrorMsg(code, respanse);
                hideDialogLoading();
            }
        } else {
            if (code != 0) {
                hideDialogLoading();
            }
            super.parseJson(code, respanse, tag, pos, data);
        }
    }

    private void branchNotExist() {
        hideDialogLoading();
        getView().findViewById(R.id.top).setVisibility(View.INVISIBLE);
        setBlank(0, this, true, blankLayout, onClickRetry);
    }

    private void setBlank(int itemSize, Object fragment, boolean request, View v, View.OnClickListener onClick) {
        topLayout.setVisibility(View.GONE);
        BlankViewHelp.setBlank(itemSize, fragment, request, v, onClick);
    }

    private void switchVersion(String name) {
        mVersion = name;
        adapter.notifyDataSetChanged();
        loadGitTree();
    }

    @Override
    protected void switchVersionSuccess() {
        versionButton.setText(mVersion);
    }

    private void parseVersion(List<BranchItem> data, JSONArray jsonArray, String tag) {
        data.clear();
        int len = jsonArray.length();
        for (int i = 0; i < len; ++i) {
            JSONObject jsonItem = jsonArray.optJSONObject(i);
            BranchItem item;
            if (jsonItem != null) {
                item = new BranchItem(jsonItem);
            } else {
               item = new BranchItem(jsonArray.optString(i));
            }

            data.add(item);

            if (item.is_default_branch && (mVersion == null || mVersion.isEmpty())) {
                switchVersion(item.name);
            }
        }

        if (tag.equals(HOST_LIST_BRANCHES)) {
            mBranchesData.clear();
            mBranchesData.addAll(data);
            setData(mBranchesData);
        } else if (tag.equals(HOST_LIST_TAG)) {
            mTagsData.clear();
            mTagsData.addAll(data);
        }

//        ((BaseExpandableListAdapter) versionAdapter).notifyDataSetChanged();
    }

    private void setData(List<BranchItem> datas) {
        mData.clear();
        mData.addAll(datas);
        adapter.notifyDataSetChanged();
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

        linearLayout.setAnimation(animation);
//        versionListView.setAnimation(animation);
//        versionList.startAnimation(animation);
        versionLayout.startAnimation(fadeIn);

        if (show) {
            versionLayout.setVisibility(View.VISIBLE);
        }
    }

    private void updateTab(View view) {
        switch (view.getId()) {
            case R.id.tvBranches:
                tvBranches.setSelected(true);
                tvTags.setSelected(false);
                break;
            case R.id.tvTags:
                tvBranches.setSelected(false);
                tvTags.setSelected(true);
                break;
        }
    }

    private static class ViewGroupHolder {
        View icon;
        TextView title;

        public ViewGroupHolder(android.view.View parent) {
            icon = parent.findViewById(R.id.icon);
            title = (TextView) parent.findViewById(R.id.title);
        }
    }

    private static class ViewHolder {
        TextView title;
        ImageView icon;

        public ViewHolder(android.view.View parent) {
            icon = (ImageView) parent.findViewById(R.id.icon);
            title = (TextView) parent.findViewById(R.id.name);
        }
    }

    public static final class BranchItem implements Serializable {
        String name = "";// "raml-doc",
        boolean is_default_branch;
        boolean is_protected;

        BranchItem(JSONObject json) {
            name = json.optString("name", "");
            is_default_branch = json.optBoolean("is_default_branch");
            is_protected = json.optBoolean("is_protected");
        }

        BranchItem(String path) {
            int index = path.lastIndexOf("/");
            if (index != -1) {
                name = path.substring(index + 1, path.length());
            } else {
                name = path;
            }
            if (name.equalsIgnoreCase("master")) {
                is_default_branch = true;
            }
        }
    }

}
