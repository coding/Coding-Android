package net.coding.program.message;


import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.BaseActivity;
import net.coding.program.FootUpdate;
import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.model.NotifyObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.fragment_notify_list)
public class NotifyListActivity extends BaseActivity implements FootUpdate.LoadMore {

    @Extra
    int type;

    @ViewById
    ListView listView;

    int defaultIcon = R.drawable.ic_notify_at;

    ArrayList<NotifyObject> mData = new ArrayList<NotifyObject>();

    @AfterViews
    void init() {
        showDialogLoading();
        URI_NOTIFY = Global.HOST + "/api/notification?type=" + type;
        if (type == 1) {
            URI_NOTIFY += "&type=2";
        }

        setDefaultByType();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(baseAdapter);
        loadMore();
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(URI_NOTIFY, URI_NOTIFY);
    }

    private void setDefaultByType() {
        if (type == 0) {
            getActionBar().setTitle("@我的");
            defaultIcon = R.drawable.ic_notify_at;
        } else if (type == 1) {
            getActionBar().setTitle("评论");
            defaultIcon = R.drawable.ic_notify_comment;
        } else {
            getActionBar().setTitle("系统通知");
            defaultIcon = R.drawable.ic_notify_comment;
        }
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    String URI_NOTIFY;

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(URI_NOTIFY)) {
            hideProgressDialog();
            if (code == 0) {
                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    NotifyObject notifyObject = new NotifyObject(jsonArray.getJSONObject(i));
                    mData.add(notifyObject);
                }

                baseAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

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
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.fragment_notify_list_item_at, parent, false);
                convertView.setTag(holder);

                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.title.setMovementMethod(LinkMovementMethod.getInstance());
                holder.time = (TextView) convertView.findViewById(R.id.time);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            NotifyObject data = mData.get(position);

            String title = data.content;
            holder.time.setText(Global.dayToNow(data.created_at));

            String itemType = data.target_type;

            if (itemType.equals("ProjectMember")) {
                holder.icon.setImageResource(R.drawable.ic_notify_user);

            } else if (itemType.equals("Depot")) {
                holder.icon.setImageResource(R.drawable.ic_notify_code);

            } else if (itemType.equals("Task")) {
                holder.icon.setImageResource(R.drawable.ic_notify_tasts);

            } else if (itemType.equals("ProjectFile")) {
                holder.icon.setImageResource(R.drawable.ic_notify_file);

            } else if (itemType.equals("QcTask")) {
                holder.icon.setImageResource(R.drawable.ic_notify_tasts);

            } else if (itemType.equals("ProjectTopic")) {
                holder.icon.setImageResource(R.drawable.ic_notify_projecttopic);

            } else if (itemType.equals("Project")) {
                holder.icon.setImageResource(R.drawable.ic_notify_project);

            } else if (itemType.equals("ProjectStar")) {
                holder.icon.setImageResource(R.drawable.ic_notify_unfollow);

            } else if (itemType.equals("ProjectWatcher")) {
                holder.icon.setImageResource(R.drawable.ic_notify_unfollow);

            } else if (itemType.equals("PullRequestComment")) {
                holder.icon.setImageResource(R.drawable.ic_notify_merge_request);

            } else if (itemType.equals("PullRequestBean")) {
                holder.icon.setImageResource(R.drawable.ic_notify_pull_request);

            } else if (itemType.equals("Tweet")) {
                holder.icon.setImageResource(R.drawable.ic_notify_tweet);
//                title = ParseLink.parseMaopao(title);

            } else if (itemType.equals("TweetComment")) {
                holder.icon.setImageResource(R.drawable.ic_notify_tweetcomment);
//                title = ParseLink.parseMaopao(title);

            } else if (itemType.equals("TweetLike")) {
                holder.icon.setImageResource(R.drawable.ic_notify_tweetlike);
//                        title = ParseLink.parseMaopao(title);

            } else if (itemType.equals("MergeRequestBean")) {
                holder.icon.setImageResource(R.drawable.ic_notify_merge_request);
//                title = ParseLink.parseMergeRequestBean(title);

            } else if (itemType.equals("UserFollow")) {
                holder.icon.setImageResource(R.drawable.ic_notify_follow);
//                title = ParseLink.parsePerson(title);

            } else if (itemType.equals("TaskComment")) {
                holder.icon.setImageResource(R.drawable.ic_notify_tasts);
//                title = ParseLink.parsePerson(title);

            } else {
                holder.icon.setImageResource(R.drawable.ic_notify_at);
            }

            holder.title.setText(Global.changeHyperlinkColor(title));

            if (position == (mData.size() - 1)) {
                loadMore();
            }

            return convertView;
        }

    };

    private static class ViewHolder {
        public ImageView icon;
        public TextView title;
        public TextView time;
    }

}
