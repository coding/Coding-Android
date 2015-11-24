package net.coding.program.message;


import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.LongClickLinkMovementMethod;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.model.NotifyObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EActivity(R.layout.activity_notify_list)
@OptionsMenu(R.menu.notify_list_activity)
public class NotifyListActivity extends BackActivity implements FootUpdate.LoadMore {

    final String HOST_MARK_AT = Global.HOST_API + "/notification/mark-read?all=1&type=0";
    final String HOST_MARK_COMMENT = Global.HOST_API + "/notification/mark-read?all=1&type=1&type=2";
    final String HOST_MARK_SYSTEM = Global.HOST_API + "/notification/mark-read?all=1&type=4";
    private final String HOST_MARK_READ = Global.HOST_API + "/notification/mark-read";
    @Extra
    int type;

    @ViewById
    ListView listView;

    private static HashMap<String, Pair<Integer, Integer>> sHashMap = new HashMap<>();


    static {
        final int DEFAULT_BG = 0xFF14A9DA;

//        sHashMap.put("ProjectStar", new Pair<>(R.drawable.ic_notify_project_star, 0xff112233));
//        sHashMap.put("ProjectFile", new Pair<>(R.drawable.ic_notify_project_file, 0xff112233));
//        sHashMap.put("ProjectWatcher", new Pair<>(R.drawable.ic_notify_project_watcher, 0xff112233));
        sHashMap.put("ProjectMember", new Pair<>(R.drawable.ic_notify_project_member, 0xFF1AB6D9));
        sHashMap.put("BranchMember", new Pair<>(R.drawable.ic_notify_branch_member, 0xFF1AB6D9));
        sHashMap.put("Depot", new Pair<>(R.drawable.ic_notify_depot, DEFAULT_BG));
        sHashMap.put("Task", new Pair<>(R.drawable.ic_notify_task, 0xFF379FD3));
        sHashMap.put("PullRequestComment", new Pair<>(R.drawable.ic_notify_pull_request_comment, 0xFF49C9A7));
        sHashMap.put("QcTask", new Pair<>(R.drawable.ic_notify_qc_task, 0xFF3C8CEA));
        sHashMap.put("ProjectTopic", new Pair<>(R.drawable.ic_notify_project_topic, 0xFF2FAEEA));
        sHashMap.put("Project", new Pair<>(R.drawable.ic_notify_project, 0xFFF8BE46));
        sHashMap.put("PullRequestBean", new Pair<>(R.drawable.ic_notify_pull_request_bean, 0xFF49C9A7));
        sHashMap.put("Tweet", new Pair<>(R.drawable.ic_notify_tweet, 0xFFFB8638));
        sHashMap.put("TweetComment", new Pair<>(R.drawable.ic_notify_tweet_comment, 0xFFFB8638));
        sHashMap.put("TweetLike", new Pair<>(R.drawable.ic_notify_tweet_like, 0xFFFF5847));
        sHashMap.put("MergeRequestBean", new Pair<>(R.drawable.ic_notify_merge_request_bean, 0xFF4E74B7));
        sHashMap.put("UserFollow", new Pair<>(R.drawable.ic_notify_user_follow, 0xFF3BBD79));
        sHashMap.put("User", new Pair<>(R.drawable.ic_notify_user, 0xFF496AB3));
        sHashMap.put("TaskComment", new Pair<>(R.drawable.ic_notify_task_comment, 0xFF379FD3));
        sHashMap.put("CommitLineNote", new Pair<>(R.drawable.ic_notify_commit_line_note, DEFAULT_BG));
        sHashMap.put("MergeRequestComment", new Pair<>(R.drawable.ic_notify_merge_request_comment, 0xFF4E74B7));
        sHashMap.put("ProjectFileComment", new Pair<>(R.drawable.ic_notify_project_file_comment, DEFAULT_BG));
        sHashMap.put("ProjectPayment", new Pair<>(R.drawable.ic_notify_project_payment, DEFAULT_BG));
        sHashMap.put("ProjectTweet", new Pair<>(R.drawable.ic_notify_project_tweet, 0xFFFB8638));
        sHashMap.put("ProjectTweetComment", new Pair<>(R.drawable.ic_notify_project_tweet_comment, 0xFFFB8638));
    }

    int defaultIcon = R.drawable.ic_notify_at;

    ArrayList<NotifyObject> mData = new ArrayList<>();
    String URI_NOTIFY;
    View.OnClickListener onClickItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NotifyObject notifyObject = (NotifyObject) v.getTag(R.id.mainLayout);

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
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.name.setMovementMethod(LongClickLinkMovementMethod.getInstance());
                holder.detailLayout = convertView.findViewById(R.id.detailLayout);
                holder.detail = (TextView) convertView.findViewById(R.id.detail);
                holder.detail.setMovementMethod(LongClickLinkMovementMethod.getInstance());
                holder.badge = convertView.findViewById(R.id.badge);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            NotifyObject data = mData.get(position);

            holder.root.setTag(R.id.mainLayout, data);

            String title = data.content;
            holder.time.setText(Global.dayToNow(data.created_at));

            String itemType = data.target_type;

//            if (itemType.equals("ProjectMember")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_user);
//
//            } else if (itemType.equals("Depot")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_code);
//
//            } else if (itemType.equals("Task")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_tasts);
//
//            } else if (itemType.equals("ProjectFile")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_file);
//
//            } else if (itemType.equals("QcTask")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_tasts);
//
//            } else if (itemType.equals("ProjectTopic")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_projecttopic);
//
//            } else if (itemType.equals("Project")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_project);
//
//            } else if (itemType.equals("ProjectStar")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_unfollow);
//
//            } else if (itemType.equals("ProjectWatcher")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_unfollow);
//
//            } else if (itemType.equals("PullRequestComment")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_merge_request);
//
//            } else if (itemType.equals("PullRequestBean")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_pull_request);
//
//            } else if (itemType.equals("Tweet")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_tweet);
//
//            } else if (itemType.equals("TweetComment")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_tweetcomment);
//
//            } else if (itemType.equals("TweetLike")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_tweetlike);
//
//            } else if (itemType.equals("MergeRequestBean")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_merge_request);
//
//            } else if (itemType.equals("UserFollow")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_follow);
//
//            } else if (itemType.equals("TaskComment")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_tasts);
//
//            } else if (itemType.equals("CommitLineNote")) {
//                holder.icon.setImageResource(R.drawable.ic_notify_tweetcomment);
//
//            } else {
//                holder.icon.setImageResource(R.drawable.ic_notify_unknown);
//            }

            Pair<Integer, Integer> iconItem = sHashMap.get(itemType);
            if (iconItem == null) {
                iconItem = new Pair<>(R.drawable.ic_notify_unknown, 0xFF14A9DA);
            }

            holder.icon.setImageResource(iconItem.first);
            holder.icon.setBackgroundColor(iconItem.second);

            String hrefBegin = "<a href=";
            String hrefEnd = "</a>";
            int firstStart = title.indexOf(hrefBegin);
            int firstEnd = title.indexOf(hrefEnd, firstStart);

            String firstLink = "";
            if (firstStart != -1 && firstEnd != -1) {
                firstLink = title.substring(firstStart, firstEnd + hrefEnd.length());
                holder.name.setText(Global.changeHyperlinkColor(firstLink));

                title = title.replace(firstLink, "");
                int lastEnd = title.lastIndexOf(hrefEnd);
                int lastStart = title.lastIndexOf(hrefBegin, lastEnd);

                if (lastStart != -1 && lastEnd != -1) { // 至少两个链接
                    int last = lastEnd + hrefEnd.length();
                    String thirdLink = title.substring(lastStart, last);

                    holder.detailLayout.setVisibility(View.VISIBLE);
                    holder.detail.setText(Global.changeHyperlinkColor(thirdLink, 0xFF222222));

                    StringBuilder b = new StringBuilder(title);
                    b.replace(lastStart, last, "");
                    title = b.toString();

                } else {
                    holder.detailLayout.setVisibility(View.GONE);

                    if (title.trim().isEmpty()) {
                        title = firstLink;
                        holder.name.setText(" ");
                    }

                }

            } else {
                holder.detailLayout.setVisibility(View.GONE);
                holder.name.setText(" ");
            }


            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(Global.changeHyperlinkColor(title));



//            if (urlSpan.length >= 2) {
//                StringBuffer sb = new StringBuffer(ttt);
//
//                URLSpan spanLast = urlSpan[urlSpan.length - 1];
//                int spanLastStart = text.getSpanStart(spanLast);
//                int spanLastEnd = text.getSpanEnd(spanLast);
//                String nameString = title.substring(spanLastStart, spanLastEnd);
//                Spannable spanName = (Spannable) Html.fromHtml(nameString);
//                spanName.setSpan(spanLast, 0, spanName.length(), 0);
//
//                StringBuffer detalBuffer = sb.delete(spanLastStart, spanLastEnd);
//
//                URLSpan spanFirst = urlSpan[0];
//                int spanFirstStart = text.getSpanStart(spanFirst);
//                int spanFirstEnd = text.getSpanEnd(spanFirst);
//                StringBuffer nameBuffer = sb.delete(spanFirstStart, spanFirstEnd);
//
//                holder.title.setText(Global.changeHyperlinkColor(sb.toString()));
//                holder.detail.setText(Global.changeHyperlinkColor(detalBuffer.toString()));
//            } else if (urlSpan.length == 1) {
//
//            } else {
//
//            }
//
//            if (urlSpan.length >= 2) {
//
//
//                StringBuffer sb = new StringBuffer(ttt);
//
//                URLSpan spanLast = urlSpan[urlSpan.length - 1];
//                int spanLastStart = text.getSpanStart(spanLast);
//                int spanLastEnd = text.getSpanEnd(spanLast);
//
//
//                URLSpan spanFirst = urlSpan[0];
//                int spanFirstStart = text.getSpanStart(spanFirst);
//                int spanFirstEnd = text.getSpanEnd(spanFirst);
//                String nameString = tttString.substring(spanFirstStart, spanFirstEnd);
//                Spannable nameSpan = new SpannableString(nameString);
//                nameSpan.setSpan(spanFirst, 0, nameSpan.length(), 0);
//                holder.name.setText(nameSpan);
//
//                holder.title.setText(Global.changeHyperlinkColor(sb.toString()));
////                holder.detail.setText(Global.changeHyperlinkColor(detalBuffer.toString()));
//            } else if (urlSpan.length == 1) {
//
//            } else {
//
//            }

//
//
//            out.append("<a href=\"");
//            out.append(((URLSpan) style[j]).getURL());
//            out.append("\">");
//            title.


//            URLSpan[] urlSpans = text.getSpans(0, text.length(), URLSpan.class);
//            for (URLSpan item : urlSpans) {
//                text.removeSpan(item);
//            }
//            holder.title.setText(text);
//            holder.title.setTextColor(data.isUnRead() ? 0xff222222 : 0xff999999);

            holder.badge.setVisibility(data.isUnRead() ? View.VISIBLE : View.INVISIBLE);



//          这种情况做特殊处理。  早上好，今天您有3个任务已超期
            String titleString = holder.title.getText().toString();
            if (data.target_type.equals("Task") && data.type == 4) {
                Pattern pattern = Pattern.compile("早上好，今天您有.*");
                Matcher matcher = pattern.matcher(titleString);
                if (matcher.find()) {
                    holder.name.setVisibility(View.VISIBLE);
                    holder.name.setText("任务提醒");
                    holder.title.setVisibility(View.GONE);
                    holder.detailLayout.setVisibility(View.VISIBLE);
                    Pair<Integer, Integer> pair = sHashMap.get(data.target_type);
                    if (pair != null) {
                        holder.icon.setImageResource(pair.first);
                        holder.icon.setBackgroundColor(pair.second);
                        holder.detail.setText(Global.changeHyperlinkColor(firstLink, 0xFF222222));
                    }
                }
            } else if (data.target_type.equals("Tweet") && data.type == 1) {
                if (titleString.endsWith("推荐到冒泡广场")) {
                    holder.name.setVisibility(View.VISIBLE);
                    holder.name.setText("冒泡提醒");
                    holder.title.setVisibility(View.VISIBLE);
                    holder.detailLayout.setVisibility(View.VISIBLE);
                    Pair<Integer, Integer> pair = sHashMap.get(data.target_type);
                    if (pair != null) {
                        holder.icon.setImageResource(pair.first);
                        holder.icon.setBackgroundColor(pair.second);
                        holder.detail.setText(Global.changeHyperlinkColor(firstLink, 0xFF222222));
                    }
                }
            }

            if (position == (mData.size() - 1)) {
                loadMore();
            }

            return convertView;
        }
    };


    @AfterViews
    protected final void initNotifyListActivity() {
        showDialogLoading();
        URI_NOTIFY = Global.HOST_API + "/notification?type=" + type;
        if (type == 1) {
            URI_NOTIFY += "&type=2";
        }

        setDefaultByType();

        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(baseAdapter);
        loadMore();
    }

    @OptionsItem
    protected void markRead() {
        if (type == 0) {
            postNetwork(HOST_MARK_AT, HOST_MARK_AT);
        } else if (type == 1) {
            postNetwork(HOST_MARK_COMMENT, HOST_MARK_COMMENT);
        } else {
            postNetwork(HOST_MARK_SYSTEM, HOST_MARK_SYSTEM);
        }
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
            umengEvent(UmengEvent.NOTIFY, "标记已读");
            int id = (int) data;
            for (NotifyObject item : mData) {
                if (item.id == id) {
                    item.setRead();
                    baseAdapter.notifyDataSetChanged();
                    break;
                }
            }
        } else if (tag.equals(HOST_MARK_AT)
                || tag.equals(HOST_MARK_COMMENT)
                || tag.equals(HOST_MARK_SYSTEM)) {
            if (code == 0) {
                umengEvent(UmengEvent.NOTIFY, "标记全部已读");

                markAllRead();
            } else {
                showErrorMsg(code, respanse);
            }

        }
    }

    private void markAllRead() {
        for (NotifyObject item : mData) {
            item.setRead();
        }
        baseAdapter.notifyDataSetChanged();
    }

    private static class ViewHolder {
        public ImageView icon;
        public TextView title;
        public TextView time;
        public View root;
        public View badge;
        public TextView name;
        public TextView detail;
        public View detailLayout;
    }

}
