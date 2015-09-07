package net.coding.program.project.detail;


import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.FootUpdate;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.base.CustomMoreFragment;
import net.coding.program.common.widget.FlowLabelLayout;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TopicObject;
import net.coding.program.project.detail.topic.TopicListDetailActivity_;
import net.coding.program.user.UserDetailActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.AnimationRes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_project_topic_list)
@OptionsMenu(R.menu.project_task)
public class TopicListFragment extends CustomMoreFragment implements FootUpdate.LoadMore {

    static final int RESULT_ADD = 1;
    static final int RESULT_DETAIL = 2;
    private static final int ID_TYPE_ALL = 0;
    private static final int ID_TYPE_MY = 1;
    private static final String TYPE_ALL = "全部讨论";
    private static final String TYPE_MY = "我的讨论";
    private static final int ID_LABEL_ALL = -1;
    private static final String LABEL_ALL = "全部标签";
    private static final String ORDER_REPLY_TIME = "最后评论排序";
    private static final String ORDER_PUBLISH_TIME = "发布时间排序";
    private static final String ORDER_HOT = "热门排序";
    private static final int ID_ORDER_REPLY_TIME = 51;
    private static final int ID_ORDER_PUBLISH_TIME = 49;
    private static final int ID_ORDER_HOT = 53;
    private static final String URI_TOPICS = Global.HOST_API + "/user/%s/project/%s/topics/mobile?type=%s&orderBy=%s";
    private static final String URI_TYPE_COUNTS = Global.HOST_API + "/user/%s/project/%s/topics/count";
    private static final String URI_ALL_LABELS = Global.HOST_API + "/user/%s/project/%s/topics/labels?withCount=true";
    private static final String URI_MY_LABELS = Global.HOST_API + "/user/%s/project/%s/topics/labels/my";
    // 刷新页面后如果当前标签不再存在，则自动变回【全部标签】再刷新
    private static final String URI_ALL_LABELS_THEN_RELOAD = "URI_ALL_LABELS_THEN_RELOAD";
    private static final String URI_MY_LABELS_THEN_RELOAD = "URI_MY_LABELS_THEN_RELOAD";
    @FragmentArg
    ProjectObject mProjectObject;
    @ViewById
    ListView listView;
    @ViewById
    View mask;
    @ViewById
    DropdownButton chooseType, chooseLabel, chooseOrder;
    @ViewById
    DropdownListView dropdownType, dropdownLabel, dropdownOrder;
    @ViewById
    View blankLayout;
    @AnimationRes
    Animation dropdown_in, dropdown_out, dropdown_mask_out;
    View.OnClickListener onClickUser = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String globaKey = (String) v.getTag();
            Intent intent = new Intent(getActivity(), UserDetailActivity_.class);
            intent.putExtra("globalKey", globaKey);
            startActivity(intent);
        }
    };
    private ArrayList<TopicObject> mData = new ArrayList<>();
    private String urlGet;
    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };
    BaseAdapter baseAdapter = new BaseAdapter() {
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
                convertView = mInflater.inflate(R.layout.fragment_project_topic_list_item, parent, false);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.icon.setOnClickListener(onClickUser);

                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.time.setFocusable(false);

                holder.refId = (TextView) convertView.findViewById(R.id.referenceId);

                holder.discuss = (TextView) convertView.findViewById(R.id.discuss);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.flowLayout = (FlowLabelLayout) convertView.findViewById(R.id.flowLayout);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            TopicObject data = (TopicObject) getItem(position);

            iconfromNetwork(holder.icon, data.owner.avatar);
            holder.icon.setTag(data.owner.global_key);

            holder.title.setText(Global.changeHyperlinkColor(data.title));

            holder.refId.setText(data.getRefId());

            holder.name.setText(data.owner.name);
            holder.time.setText(Global.changeHyperlinkColor(Global.dayToNow(data.created_at)));
            holder.discuss.setText(String.format("%d", data.child_count));

            int flowWidth = MyApp.sWidthPix - Global.dpToPx(66 + 12);
            holder.flowLayout.setLabels(data.labels, flowWidth);

            if (position == (getCount() - 1)) {
                loadMore();
            }

            return convertView;
        }
    };

    private DropdownButtonsController dropdownButtonsController = new DropdownButtonsController();

    @AfterViews
    protected void init() {
        dropdownButtonsController.init();
        initRefreshLayout();
        showDialogLoading();

        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(baseAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TopicListDetailActivity_.class);
                intent.putExtra("topicObject", ((TopicObject) baseAdapter.getItem((int)id)));
                getParentFragment().startActivityForResult(intent, RESULT_DETAIL);
            }
        });

        dropdownButtonsController.flushAll(true);
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(urlGet, urlGet);
    }

    @Override
    public void onRefresh() {
        urlGet = getLink();
        initSetting();
        loadMore();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (URI_TYPE_COUNTS.equals(tag)) {
            if (code == 0 && dropdownButtonsController != null && respanse != null)
                dropdownButtonsController.flushCounts(respanse.optJSONObject("data"));
            return;
        }
        if (URI_ALL_LABELS.equals(tag)) {
            if (code == 0 && dropdownButtonsController != null && respanse != null)
                dropdownButtonsController.flushAllLabels(respanse.optJSONArray("data"));
            return;
        }
        if (URI_MY_LABELS.equals(tag)) {
            if (code == 0 && dropdownButtonsController != null && respanse != null)
                dropdownButtonsController.flushMyLabels(respanse.optJSONArray("data"));
            return;
        }
        if (URI_MY_LABELS_THEN_RELOAD.equals(tag)) {
            if (code == 0 && dropdownButtonsController != null && respanse != null) {
                dropdownButtonsController.flushMyLabels(respanse.optJSONArray("data"));
                onRefresh();
            }
            return;
        }
        if (URI_ALL_LABELS_THEN_RELOAD.equals(tag)) {
            if (code == 0 && dropdownButtonsController != null && respanse != null) {
                dropdownButtonsController.flushAllLabels(respanse.optJSONArray("data"));
                onRefresh();
            }
            return;
        }
        setRefreshing(false);
        hideProgressDialog();
        if (code == 0) {
            if (isLoadingFirstPage(tag)) {
                mData.clear();
            }

            if (respanse.has("data")) {
                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    TopicObject topicObject = new TopicObject(json);
                    mData.add(topicObject);
                }
            }

            BlankViewDisplay.setBlank(mData.size(), TopicListFragment.this, true, blankLayout, onClickRetry);


            baseAdapter.notifyDataSetChanged();
        } else {
            showErrorMsg(code, respanse);
            BlankViewDisplay.setBlank(mData.size(), TopicListFragment.this, false, blankLayout, onClickRetry);
        }

        mFootUpdate.updateState(code, isLoadingLastPage(tag), mData.size());
    }

    @OptionsItem
    void action_add() {
        Intent intent = new Intent(getActivity(), TopicAddActivity_.class);
        intent.putExtra("projectObject", mProjectObject);
        getParentFragment().startActivityForResult(intent, RESULT_ADD);
    }

    @Override
    public void onResume() {
        super.onResume();
        dropdownButtonsController.reset();
        dropdownButtonsController.flushAll(false);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ADD:
                if (resultCode == Activity.RESULT_OK) {
//                    TopicObject topic = (TopicObject) data.getSerializableExtra("topic");
//                    mData.add(0, topic);
//                    baseAdapter.notifyDataSetChanged();
//                    dropdownButtonsController.flushAll();
                    dropdownButtonsController.flushAll(false);
                }
                break;

            case RESULT_DETAIL:
                if (resultCode == Activity.RESULT_OK) {
//                    int id = data.getIntExtra("id", -1);
//                    if (id != -1) {
//                        for (int i = 0; i < mData.size(); ++i) {
//                            if (mData.get(i).id == (id)) {
//                                mData.remove(i);
//                                baseAdapter.notifyDataSetChanged();
//                                break;
//                            }
//                        }
//                    } else {
//                        int topicId = data.getIntExtra("topic_id", 0);
//                        int childrenCount = data.getIntExtra("child_count", -1);
//                        for (int i = 0; i < mData.size(); ++i) {
//                            if (mData.get(i).id == topicId) {
//                                mData.get(i).child_count = childrenCount;
//                                baseAdapter.notifyDataSetChanged();
//                                break;
//                            }
//                        }
//                    }
//
//                    Serializable topicObject = data.getSerializableExtra("topic");
//                    if (topicObject instanceof TopicObject) {
//                        TopicObject topicData = (TopicObject) topicObject;
//
//                        for (int i = 0; i < mData.size(); ++i) {
//                            if (mData.get(i).id == topicData.id) {
//                                mData.set(i, topicData);
//                                baseAdapter.notifyDataSetChanged();
//                                break;
//                            }
//                        }
//
//                    }
                    dropdownButtonsController.flushAll(false);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected String getLink() {
        String topicType = dropdownType.current.value;
        String topicOrder = dropdownOrder.current.value;
        String url = String.format(URI_TOPICS, mProjectObject.owner_user_name, mProjectObject.name, topicType, topicOrder);
        if (dropdownLabel.current != null && !TextUtils.isEmpty(dropdownLabel.current.value)) {
            url += "&labelId=" + dropdownLabel.current.value;
        }
        return url;
    }

    @Click
    void mask() {
        dropdownButtonsController.hide();
    }

    public static class ViewHolder {
        public ImageView icon;
        public TextView title;
        public TextView discuss;
        public TextView time;
        public TextView name;
        public FlowLabelLayout flowLayout;
        public TextView refId;
    }

    private class DropdownButtonsController implements DropdownListView.Container {
        private DropdownListView currentDropdownList;
        private List<DropdownItemObject> datasetType = new ArrayList<>(2);
        private List<DropdownItemObject> datasetAllLabel = new ArrayList<>();
        private List<DropdownItemObject> datasetMyLabel = new ArrayList<>();
        private List<DropdownItemObject> datasetLabel = datasetAllLabel;
        private List<DropdownItemObject> datasetOrder = new ArrayList<>(3);

        @Override
        public void show(DropdownListView view) {
            if (currentDropdownList != null) {
                currentDropdownList.clearAnimation();
                currentDropdownList.startAnimation(dropdown_out);
                currentDropdownList.setVisibility(View.GONE);
                currentDropdownList.button.setChecked(false);
            }
            currentDropdownList = view;
            mask.clearAnimation();
            mask.setVisibility(View.VISIBLE);
            currentDropdownList.clearAnimation();
            currentDropdownList.startAnimation(dropdown_in);
            currentDropdownList.setVisibility(View.VISIBLE);
            currentDropdownList.button.setChecked(true);
        }

        @Override
        public void hide() {
            if (currentDropdownList != null) {
                currentDropdownList.clearAnimation();
                currentDropdownList.startAnimation(dropdown_out);
                currentDropdownList.button.setChecked(false);
                mask.clearAnimation();
                mask.startAnimation(dropdown_mask_out);
            }
            currentDropdownList = null;
        }

        @Override
        public void onSelectionChanged(DropdownListView view) {
            if (view == dropdownType) {
                updateLabels(getCurrentLabels());
            }
            onRefresh();
        }

        void reset() {
            chooseType.setChecked(false);
            chooseLabel.setChecked(false);
            chooseOrder.setChecked(false);

            dropdownType.setVisibility(View.GONE);
            dropdownLabel.setVisibility(View.GONE);
            dropdownOrder.setVisibility(View.GONE);
            mask.setVisibility(View.GONE);

            dropdownType.clearAnimation();
            dropdownLabel.clearAnimation();
            dropdownOrder.clearAnimation();
            mask.clearAnimation();
        }

        void init() {
            reset();
            datasetType.add(new DropdownItemObject(TYPE_ALL, ID_TYPE_ALL, "all"));
            datasetType.add(new DropdownItemObject(TYPE_MY, ID_TYPE_MY, "my"));
            dropdownType.bind(datasetType, chooseType, this, ID_TYPE_ALL);

            datasetAllLabel.add(new DropdownItemObject(LABEL_ALL, ID_LABEL_ALL, null) {
                @Override
                public String getSuffix() {
                    return dropdownType.current == null ? "" : dropdownType.current.getSuffix();
                }
            });
            datasetMyLabel.add(new DropdownItemObject(LABEL_ALL, ID_LABEL_ALL, null));
            datasetLabel = datasetAllLabel;
            dropdownLabel.bind(datasetLabel, chooseLabel, this, ID_LABEL_ALL);

            datasetOrder.add(new DropdownItemObject(ORDER_REPLY_TIME, ID_ORDER_REPLY_TIME, "51"));
            datasetOrder.add(new DropdownItemObject(ORDER_PUBLISH_TIME, ID_ORDER_PUBLISH_TIME, "49"));
            datasetOrder.add(new DropdownItemObject(ORDER_HOT, ID_ORDER_HOT, "53"));
            dropdownOrder.bind(datasetOrder, chooseOrder, this, ID_ORDER_REPLY_TIME);

            dropdown_mask_out.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (currentDropdownList == null) {
                        reset();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        private List<DropdownItemObject> getCurrentLabels() {
            return dropdownType.current != null && dropdownType.current.id == ID_TYPE_MY ? datasetMyLabel : datasetAllLabel;
        }

        void updateLabels(List<DropdownItemObject> targetList) {
            if (targetList == getCurrentLabels()) {
                datasetLabel = targetList;
                dropdownLabel.bind(datasetLabel, chooseLabel, this, dropdownLabel.current.id);
            }
        }

        void flushAll(boolean reloadImmedate) {
            getNetwork(String.format(URI_TYPE_COUNTS, mProjectObject.owner_user_name, mProjectObject.name), URI_TYPE_COUNTS);
            if (reloadImmedate) {
                getNetwork(String.format(URI_ALL_LABELS, mProjectObject.owner_user_name, mProjectObject.name), URI_ALL_LABELS);
                getNetwork(String.format(URI_MY_LABELS, mProjectObject.owner_user_name, mProjectObject.name), URI_MY_LABELS);
                onRefresh();
            } else {
                List<DropdownItemObject> currentList = getCurrentLabels();
                getNetwork(String.format(URI_ALL_LABELS, mProjectObject.owner_user_name, mProjectObject.name), currentList == datasetAllLabel ? URI_ALL_LABELS_THEN_RELOAD : URI_ALL_LABELS);
                getNetwork(String.format(URI_MY_LABELS, mProjectObject.owner_user_name, mProjectObject.name), currentList == datasetMyLabel ? URI_MY_LABELS_THEN_RELOAD : URI_MY_LABELS);
            }
        }

        public void flushCounts(JSONObject json) {
            if (json == null) return;
            datasetType.get(ID_TYPE_ALL).setSuffix(" (" + json.optInt("all", 0) + ")");
            datasetType.get(ID_TYPE_MY).setSuffix(" (" + json.optInt("my", 0) + ")");
            dropdownType.flush();
            dropdownLabel.flush();
        }

        void flushAllLabels(JSONArray array) {
            flushLabels(array, datasetAllLabel);
        }

        void flushMyLabels(JSONArray array) {
            flushLabels(array, datasetMyLabel);
        }

        private void flushLabels(JSONArray array, List<DropdownItemObject> targetList) {
            if (array == null) return;
            while (targetList.size() > 1) targetList.remove(targetList.size() - 1);
            for (int i = 0, n = array.length(); i < n; i++) {
                final JSONObject data = array.optJSONObject(i);
                int id = data.optInt("id");
                String name = data.optString("name");
                if (TextUtils.isEmpty(name)) continue;
                int topicsCount = data.optInt("count", 0);
                // 只有all才做0数量过滤，因为my的返回数据总是0
                if (topicsCount == 0 && targetList == datasetAllLabel) continue;
                DropdownItemObject item = new DropdownItemObject(name, id, String.valueOf(id));
                if (targetList == datasetAllLabel)
                    item.setSuffix(String.format(" (%d)", data.optInt("count", 0)));
                targetList.add(item);
            }
            updateLabels(targetList);
        }
    }
}
