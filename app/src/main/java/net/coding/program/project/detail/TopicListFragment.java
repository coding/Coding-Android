package net.coding.program.project.detail;


import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.base.CustomMoreFragment;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TopicObject;
import net.coding.program.user.UserDetailActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_project_topic_list)
@OptionsMenu(R.menu.project_task)
public class TopicListFragment extends CustomMoreFragment implements FootUpdate.LoadMore {

    private static final int INDEX_DISPLAY_ALL = 0;
    private static final int INDEX_DISPLAY_MINE = 1;
    private static final String DISPLAY_ALL = "全部讨论";
    private static final String DISPLAY_MINE = "我的讨论";

    private static final String LABEL_ALL = "全部标签";

    private static final String ORDER_REPLY_TIME = "最后评论排序";
    private static final String ORDER_PUBLISH_TIME = "发布时间排序";
    private static final String ORDER_HOT = "热门排序";
    private static final int ID_ORDER_REPLY_TIME = 51;
    private static final int ID_ORDER_PUBLISH_TIME = 49;
    private static final int ID_ORDER_HOT = 53;

    private static final String URI_TOPICS = Global.HOST +"/api/user/%s/project/%s/topics/mobile?type=%s&orderBy=%s";
    private static final String URI_FLUSH_COUNT = Global.HOST + "/api/user/%s/project/%s/topics/count";
    private static final String URI_FLUSH_LABELS = Global.HOST + "/api/user/%s/project/%s/topics/labels?withCount=true";

    @FragmentArg
    ProjectObject mProjectObject;

    @ViewById
    ListView listView;
    @ViewById
    View mask;
    @ViewById
    DropdownTabButton chooseAllOrMine,chooseLabel,chooseOrder;
    @ViewById
    DropdownListView dropdownAllOrMine,dropdownLabel,dropdownOrder;
    @ViewById
    View blankLayout;

    private ArrayList<TopicObject> mData = new ArrayList();
    private String urlGet;
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
                intent.putExtra("topicObject", ((TopicObject) baseAdapter.getItem(position)));
                getParentFragment().startActivityForResult(intent, RESULT_DETAIL);
            }
        });
        urlGet = getLink();
        loadMore();
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

    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if(URI_FLUSH_COUNT.equals(tag)){
            if(code == 0 && dropdownButtonsController!=null && respanse !=null)
                dropdownButtonsController.flushCount(respanse.optJSONObject("data"));
            return;
        }
        if(URI_FLUSH_LABELS.equals(tag)){
            if(code == 0 && dropdownButtonsController!=null && respanse !=null)
                dropdownButtonsController.flushLabels(respanse.optJSONArray("data"));
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

    View.OnClickListener onClickUser = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String globaKey = (String) v.getTag();
            Intent intent = new Intent(getActivity(), UserDetailActivity_.class);
            intent.putExtra("globalKey", globaKey);
            startActivity(intent);
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

                holder.discuss = (TextView) convertView.findViewById(R.id.discuss);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            TopicObject data = (TopicObject) getItem(position);

            iconfromNetwork(holder.icon, data.owner.avatar);
            holder.icon.setTag(data.owner.global_key);

            holder.title.setText(Global.changeHyperlinkColor(data.title));

            final String timeFormat = "%s 发布于%s";
//            String user = HtmlContent.createUserHtml(data.owner.global_key, data.owner.name);
            String time = Global.dayToNow(data.created_at);
            String timeContent = String.format(timeFormat, data.owner.name, time);
            holder.time.setText(Global.changeHyperlinkColor(timeContent));

            holder.discuss.setText(String.format("%d", data.child_count));

            if (position == (getCount() - 1)) {
                loadMore();
            }

            return convertView;
        }
    };

    static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView time;
        TextView discuss;
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
        dropdownButtonsController.flushCount();
        dropdownButtonsController.flushLabels();
    }

    @Override
    public void onPause() {
        dropdownButtonsController.hide();
        super.onPause();
    }

    static final int RESULT_ADD = 1;
    static final int RESULT_DETAIL = 2;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ADD:
                if (resultCode == Activity.RESULT_OK) {
                    TopicObject topic = (TopicObject) data.getSerializableExtra("topic");
                    mData.add(0, topic);
                    baseAdapter.notifyDataSetChanged();
                    dropdownButtonsController.flushCount();
                    dropdownButtonsController.flushLabels();
                }
                break;

            case RESULT_DETAIL:
                if (resultCode == Activity.RESULT_OK) {
                    int id = data.getIntExtra("id", -1);
                    if (id != -1) {
                        for (int i = 0; i < mData.size(); ++i) {
                            if (mData.get(i).id == (id)) {
                                mData.remove(i);
                                baseAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    } else {
                        int topicId = data.getIntExtra("topic_id", 0);
                        int childrenCount = data.getIntExtra("child_count", -1);
                        for (int i = 0; i < mData.size(); ++i) {
                            if (mData.get(i).id == topicId) {
                                mData.get(i).child_count = childrenCount;
                                baseAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }

                    Serializable topicObject = data.getSerializableExtra("topic");
                    if (topicObject instanceof TopicObject) {
                        TopicObject topicData = (TopicObject) topicObject;

                        for (int i = 0; i < mData.size(); ++i) {
                            if (mData.get(i).id == topicData.id) {
                                mData.set(i, topicData);
                                baseAdapter.notifyDataSetChanged();
                                break;
                            }
                        }

                    }
                    dropdownButtonsController.flushCount();
                    dropdownButtonsController.flushLabels();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected String getLink() {
        String topicType = dropdownAllOrMine.current.filter;
        String topicOrder= dropdownOrder.current.filter;
        String url = String.format(URI_TOPICS, mProjectObject.owner_user_name, mProjectObject.name,topicType, topicOrder);
        if(!TextUtils.isEmpty(dropdownLabel.current.filter)) {
            url += "&labelId=" + dropdownLabel.current.filter;
        }
        return url.toString();
    }

    @Click
    void mask(){
        dropdownButtonsController.hide();
    }

    private class DropdownButtonsController implements DropdownListView.Container{
        DropdownListView currentDropdownList;
        private List<DropdownItemObject> allOrMyDataset;
        private List<DropdownItemObject> labelDataset;
        private List<DropdownItemObject> orderDataset;

        @Override
        public void show(DropdownListView listView) {
            if(currentDropdownList != null){
                currentDropdownList.setVisibility(View.GONE);
                currentDropdownList.button.setChecked(false);
            }
            currentDropdownList = listView;
            mask.setVisibility(View.VISIBLE);
            currentDropdownList.setVisibility(View.VISIBLE);
            currentDropdownList.button.setChecked(true);
        }

        @Override
        public void hide() {
            if(currentDropdownList != null){
                currentDropdownList.setVisibility(View.GONE);
                currentDropdownList.button.setChecked(false);
            }
            currentDropdownList = null;
            listView.setVisibility(View.GONE);
            mask.setVisibility(View.GONE);
        }

        @Override
        public void flushTopics() {
            onRefresh();
        }

        void init(){
            dropdownAllOrMine.setVisibility(View.GONE);
            dropdownLabel.setVisibility(View.GONE);
            dropdownOrder.setVisibility(View.GONE);
            mask.setVisibility(View.GONE);
            initType();
            initLabel();
            initOrder();
        }

        private void initType(){
            allOrMyDataset = new ArrayList<>(2);
            allOrMyDataset.add(new DropdownItemObject(DISPLAY_ALL, INDEX_DISPLAY_ALL, "all"));
            allOrMyDataset.add(new DropdownItemObject(DISPLAY_MINE, INDEX_DISPLAY_MINE, "my"));
            dropdownAllOrMine.bind(allOrMyDataset, chooseAllOrMine, this, null);
            flushCount();
        }

        private void initLabel() {
            labelDataset = new ArrayList<>();
            labelDataset.add(new DropdownItemObject(LABEL_ALL, -1, null));
            dropdownLabel.bind(labelDataset, chooseLabel, this, null);
            flushLabels();
        }

        private void initOrder() {
            orderDataset = new ArrayList<>(3);
            orderDataset.add(new DropdownItemObject(ORDER_REPLY_TIME, ID_ORDER_REPLY_TIME, "51"));
            orderDataset.add(new DropdownItemObject(ORDER_PUBLISH_TIME, ID_ORDER_PUBLISH_TIME, "49"));
            orderDataset.add(new DropdownItemObject(ORDER_HOT, ID_ORDER_HOT, "53"));
            dropdownOrder.bind(orderDataset, chooseOrder,this, null);
        }


        void flushCount(){
            String url = String.format(URI_FLUSH_COUNT, mProjectObject.owner_user_name, mProjectObject.name);
            getNetwork(url, URI_FLUSH_COUNT);
        }

        void flushCount(JSONObject json) {
            if(json == null) return;
            allOrMyDataset.get(INDEX_DISPLAY_ALL).suffix = String.format(" (%d)" , json.optInt("all", 0));
            allOrMyDataset.get(INDEX_DISPLAY_MINE).suffix = String.format(" (%d)", json.optInt("my", 0));
            dropdownAllOrMine.flush();
        }

        void flushLabels(){
            String url = String.format(URI_FLUSH_LABELS, mProjectObject.owner_user_name, mProjectObject.name);
            getNetwork(url, URI_FLUSH_LABELS);
        }

        void flushLabels(JSONArray array) {
            if(array == null) return;
            // 标签可能被删除，改名等，这时要更新全部标签
            while(labelDataset.size()>1) labelDataset.remove(labelDataset.size()-1);
            int oldSelectedId = dropdownLabel.current.id;
            dropdownLabel.current = null;
            for(int i=0,n=array.length();i<n;i++){
                JSONObject data = array.optJSONObject(i);
                int id = data.optInt("id");
                String name = data.optString("name");
                if(TextUtils.isEmpty(name)) continue;
                DropdownItemObject item = new DropdownItemObject(name,id,String.valueOf(id));
                item.suffix = String.format(" (%d)", data.optInt("count", 0));
                labelDataset.add(item);
                if(oldSelectedId == id && dropdownLabel.current == null) dropdownLabel.current = item;
            }
            dropdownLabel.bind(labelDataset, chooseLabel, this, dropdownLabel.current);
        }
    }
}
