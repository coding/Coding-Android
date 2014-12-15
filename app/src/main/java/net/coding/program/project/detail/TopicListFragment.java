package net.coding.program.project.detail;


import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.FootUpdate;
import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TopicObject;
import net.coding.program.user.UserDetailActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EFragment(R.layout.common_refresh_listview)
@OptionsMenu(R.menu.project_task)
public class TopicListFragment extends RefreshBaseFragment implements FootUpdate.LoadMore {

    @FragmentArg
    ProjectObject mProjectObject;

    @FragmentArg
    int type;

    @ViewById
    ListView listView;

    @ViewById
    View blankLayout;

    ArrayList<TopicObject> mData = new ArrayList<TopicObject>();

    String URL_DISCUSS_ALL = Global.HOST + "/api/project/%s/topics?pageSize=20&type=1";
    String URL_DISCUSS_ME = Global.HOST + "/api/project/%s/topics/me?pageSize=20&type=1";

    String urlGet;

    @AfterViews
    protected void init() {
        super.init();
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

        urlGet = String.format(type == 0 ? URL_DISCUSS_ALL : URL_DISCUSS_ME, mProjectObject.id);
        loadMore();
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(urlGet, urlGet);
    }

    @Override
    public void onRefresh() {
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

            if (isLoadingLastPage(tag)) {
                mFootUpdate.dismiss();
            } else {
                mFootUpdate.showLoading();
            }

            BlankViewDisplay.setBlank(mData.size(), TopicListFragment.this, true, blankLayout, onClickRetry);

            baseAdapter.notifyDataSetChanged();
        } else {
            showErrorMsg(code, respanse);

            if (mData.size() > 0) {
                mFootUpdate.showFail();
            } else {
                mFootUpdate.dismiss();
            }

            BlankViewDisplay.setBlank(mData.size(), TopicListFragment.this, false, blankLayout, onClickRetry);
        }
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
        Intent intent = new Intent(getActivity(), TopicCreateActivity_.class);
        intent.putExtra("projectObject", mProjectObject);
        getParentFragment().startActivityForResult(intent, RESULT_ADD);
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
                }
                break;

            case RESULT_DETAIL:
                if (resultCode == Activity.RESULT_OK) {

                    String id = data.getStringExtra("id");
                    if (id != null) {
                        for (int i = 0; i < mData.size(); ++i) {
                            if (mData.get(i).id.equals(id)) {
                                mData.remove(i);
                                baseAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    } else {
                        String topicId = data.getStringExtra("topic_id");
                        int childrenCount = data.getIntExtra("child_count", -1);
                        for (int i = 0; i < mData.size(); ++i) {
                            if (mData.get(i).id.equals(topicId)) {
                                mData.get(i).child_count = childrenCount;
                                baseAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
