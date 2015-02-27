package net.coding.program.message;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.BaseActivity;
import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.LongClickLinkMovementMethod;
import net.coding.program.model.NotifyObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

@EActivity(R.layout.fragment_notify_list)
public class NotifyListActivity extends BaseActivity implements FootUpdate.LoadMore {

    private final String HOST_MARK_READ = Global.HOST + "/api/notification/mark-read";

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
            getSupportActionBar().setTitle("@我的");
            defaultIcon = R.drawable.ic_notify_at;
        } else if (type == 1) {
            getSupportActionBar().setTitle("评论");
            defaultIcon = R.drawable.ic_notify_comment;
        } else {
            getSupportActionBar().setTitle("系统通知");
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
        } else if (tag.equals(HOST_MARK_READ)) {
            int id = (int) data;
            for (NotifyObject item : mData) {
                if (item.id == id) {
                    item.setRead();
                    baseAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    View.OnClickListener onClickItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NotifyObject notifyObject = (NotifyObject) v.getTag(R.id.tag);

            RequestParams params = new RequestParams();
            params.put("id", notifyObject.id);
            postNetwork(HOST_MARK_READ, params, HOST_MARK_READ, 0, notifyObject.id);
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
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.fragment_notify_list_item_at, parent, false);
                convertView.setTag(holder);
                convertView.setOnClickListener(onClickItem);

                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.title.setMovementMethod(LongClickLinkMovementMethod.getInstance());
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.root = convertView;


            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            NotifyObject data = mData.get(position);

            holder.root.setTag(R.id.tag, data);

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

            } else if (itemType.equals("TweetComment")) {
                holder.icon.setImageResource(R.drawable.ic_notify_tweetcomment);

            } else if (itemType.equals("TweetLike")) {
                holder.icon.setImageResource(R.drawable.ic_notify_tweetlike);

            } else if (itemType.equals("MergeRequestBean")) {
                holder.icon.setImageResource(R.drawable.ic_notify_merge_request);

            } else if (itemType.equals("UserFollow")) {
                holder.icon.setImageResource(R.drawable.ic_notify_follow);

            } else if (itemType.equals("TaskComment")) {
                holder.icon.setImageResource(R.drawable.ic_notify_tasts);

            } else {
                holder.icon.setImageResource(R.drawable.ic_notify_at);
            }

            holder.title.setText(Global.changeHyperlinkColor(title));
            holder.title.setTextColor(data.isUnRead() ? 0xff222222 : 0xff999999);

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
        public View root;
    }

}
