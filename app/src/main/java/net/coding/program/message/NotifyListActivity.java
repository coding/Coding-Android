package net.coding.program.message;


import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.LoadMore;
import net.coding.program.common.LongClickLinkMovementMethod;
import net.coding.program.common.model.NotifyObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.route.BlankViewDisplay;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EActivity(R.layout.activity_notify_list1)
public class NotifyListActivity extends BackActivity implements LoadMore {

    private static HashMap<String, Pair<Integer, Integer>> sHashMap = new HashMap<>();

    static {
        final int DEFAULT_BG = 0xFF14A9DA;

//        sHashMap.put("ProjectStar", new Pair<>(R.drawable.ic_notify_project_star, 0xff112233));
//        sHashMap.put("ProjectFile", new Pair<>(R.drawable.ic_notify_project_file, 0xff112233));
//        sHashMap.put("ProjectWatcher", new Pair<>(R.drawable.ic_notify_project_watcher, 0xff112233));
        sHashMap.put("ProjectMember", new Pair<>(R.drawable.ic_notify_project_member, 0xFF1AB6D9));
        sHashMap.put("BranchMember", new Pair<>(R.drawable.ic_notify_project_member, 0xFF1AB6D9));

        sHashMap.put("Task", new Pair<>(R.drawable.ic_notify_task, 0xFF379FD3));

        sHashMap.put("QcTask", new Pair<>(R.drawable.ic_notify_qc_task, 0xFF3C8CEA));

        sHashMap.put("ProjectTopic", new Pair<>(R.drawable.ic_notify_project_topic, CodingColor.fontBlue));

        sHashMap.put("Project", new Pair<>(R.drawable.ic_notify_project, 0xFFF8BE46));

        sHashMap.put("PullRequestComment", new Pair<>(R.drawable.ic_notify_pull_request_comment, 0xFF49C9A7));
        sHashMap.put("PullRequestBean", new Pair<>(R.drawable.ic_notify_pull_request_comment, 0xFF49C9A7));

        sHashMap.put("MergeRequestComment", new Pair<>(R.drawable.ic_notify_merge_request_comment, 0xFF4E74B7));
        sHashMap.put("MergeRequestBean", new Pair<>(R.drawable.ic_notify_merge_request_comment, 0xFF4E74B7));

        sHashMap.put("TweetLike", new Pair<>(R.drawable.ic_notify_tweet_like, 0xFFFF5847));

        sHashMap.put("UserFollow", new Pair<>(R.drawable.ic_notify_user_follow, CodingColor.fontGreen));

        sHashMap.put("User", new Pair<>(R.drawable.ic_notify_user, 0xFF496AB3));

        sHashMap.put("TaskComment", new Pair<>(R.drawable.ic_notify_task_comment, 0xFF379FD3));

        sHashMap.put("ProjectFileComment", new Pair<>(R.drawable.ic_notify_project_file_comment, DEFAULT_BG));

        sHashMap.put("ProjectPayment", new Pair<>(R.drawable.ic_notify_project_payment, DEFAULT_BG));

        sHashMap.put("CommitLineNote", new Pair<>(R.drawable.ic_notify_commit_line_note, DEFAULT_BG));

        sHashMap.put("Tweet", new Pair<>(R.drawable.ic_notify_tweet, 0xFFFB8638));
        sHashMap.put("TweetComment", new Pair<>(R.drawable.ic_notify_tweet, 0xFFFB8638));
        sHashMap.put("ProjectTweet", new Pair<>(R.drawable.ic_notify_tweet, 0xFFFB8638));
        sHashMap.put("ProjectTweetComment", new Pair<>(R.drawable.ic_notify_tweet, 0xFFFB8638));

        sHashMap.put("Depot", new Pair<>(R.drawable.ic_notify_project_payment, DEFAULT_BG));

    }

    final String HOST_MARK_SYSTEM = Global.HOST_API + "/notification/mark-read";
    final String TAG_NOTIFY = "TAG_NOTIFY";
    private final String HOST_MARK_READ = Global.HOST_API + "/notification/mark-read";

    private static final String TAG_MARK_READ = "TAG_MARK_READ";
    private static final String TAG_MARK_READ_ALL = "TAG_MARK_READ_ALL";

    @ViewById
    ListView listView;
    @ViewById
    View blankLayout;
    int defaultIcon = R.drawable.ic_notify_at;
    ArrayList<NotifyObject> mData = new ArrayList<>();
    String URI_NOTIFY;
    View.OnClickListener onClickItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NotifyObject notifyObject = (NotifyObject) v.getTag(R.id.mainLayout);

            RequestParams params = new RequestParams();
            params.put("id", notifyObject.id);
            postNetwork(HOST_MARK_READ, params, TAG_MARK_READ, 0, notifyObject.id);
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

            Pair<Integer, Integer> iconItem = sHashMap.get(itemType);
            if (iconItem == null) {
                iconItem = new Pair<>(R.drawable.ic_notify_project_payment, 0xFF14A9DA);
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
                holder.name.setText(GlobalCommon.changeHyperlinkColor(firstLink, CodingColor.select1));

                title = title.replace(firstLink, "");
                int lastEnd = title.lastIndexOf(hrefEnd);
                int lastStart = title.lastIndexOf(hrefBegin, lastEnd);

                if (lastStart != -1 && lastEnd != -1) { // 至少两个链接
                    int last = lastEnd + hrefEnd.length();
                    String thirdLink = title.substring(lastStart, last);

                    holder.detailLayout.setVisibility(View.VISIBLE);
                    holder.detail.setText(GlobalCommon.changeHyperlinkColor(thirdLink, CodingColor.font1));

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
            holder.title.setText(GlobalCommon.changeHyperlinkColor(title, CodingColor.select2));

            holder.badge.setVisibility(data.isUnRead() ? View.VISIBLE : View.INVISIBLE);

//          这种情况做特殊处理。  早上好，今天您有3个任务已超期
            String titleString = holder.title.getText().toString();
            if (data.target_type.equals("Task")) {
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
                        holder.detail.setText(GlobalCommon.changeHyperlinkColor(firstLink, CodingColor.font1));
                    }
                }
            } else if (data.target_type.equals("Tweet")) {
                if (titleString.endsWith("推荐到冒泡广场")) {
                    holder.name.setVisibility(View.VISIBLE);
                    holder.name.setText("冒泡提醒");
                    holder.title.setVisibility(View.VISIBLE);
                    holder.detailLayout.setVisibility(View.VISIBLE);
                    Pair<Integer, Integer> pair = sHashMap.get(data.target_type);
                    if (pair != null) {
                        holder.icon.setImageResource(pair.first);
                        holder.icon.setBackgroundColor(pair.second);
                        holder.detail.setText(GlobalCommon.changeHyperlinkColor(firstLink, CodingColor.font1));
                    }
                }
            } else if (data.target_type.equals("User") && titleString.endsWith("重置了你的账号密码。")) {
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText("账号提醒");
            }

            if (position == (mData.size() - 1)) {
                loadMore();
            }

            return convertView;
        }
    };
    View.OnClickListener onClickRetry = v -> loadMore();
    private boolean isShowNoRead = false;
    private MenuItem menuItemShowNoRead;
    private MenuItem menuItemShowAll;

    private void setUrl(boolean showNoRead) {
        isShowNoRead = showNoRead;

        String type = "/notification?";
        if (showNoRead) {
            type = "/notification/unread-list?";
        }

        URI_NOTIFY = Global.HOST_API + type;
    }

    @AfterViews
    protected final void initNotifyListActivity() {
        showDialogLoading();

        setUrl(false);

        setDefaultByType();

        listViewAddHeaderSection(listView);
        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(baseAdapter);

        loadMore();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(net.coding.program.R.menu.notify_list_activity, menu);
        menuItemShowNoRead = menu.findItem(R.id.showNoRead);
        menuItemShowAll = menu.findItem(R.id.showAll);
        menuItemShowAll.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @OptionsItem
    void showNoRead() {
        menuItemShowAll.setVisible(true);
        menuItemShowNoRead.setVisible(false);

        setUrl(true);
        initSetting();
        loadMore();

        showDialogLoading();
    }

    @OptionsItem
    void showAll() {
        menuItemShowAll.setVisible(false);
        menuItemShowNoRead.setVisible(true);

        setUrl(false);
        initSetting();
        loadMore();
        showDialogLoading();
    }

    @OptionsItem
    protected void markRead() {
        postNetwork(HOST_MARK_SYSTEM, new RequestParams("all", true), TAG_MARK_READ_ALL);
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(URI_NOTIFY, TAG_NOTIFY);
    }

    private void setDefaultByType() {
        setActionBarTitle("通知");
        defaultIcon = R.drawable.ic_notify_comment;
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_NOTIFY)) {
            hideProgressDialog();
            if (code == 0) {
                if (isLoadingFirstPage(TAG_NOTIFY)) {
                    mData.clear();
                }

                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    NotifyObject notifyObject = new NotifyObject(jsonArray.getJSONObject(i));
                    mData.add(notifyObject);
                }

                if (mData.isEmpty()) {
                    loadMoreDelay();
                }

                baseAdapter.notifyDataSetChanged();

                if (!mData.isEmpty() || isLoadingLastPage(tag)) {
                    String blankTip;
                    if (isShowNoRead) {
                        blankTip = "没有未读的消息~";
                    } else {
                        blankTip = "您没有收到通知噢~";
                    }
                    BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry, blankTip, R.drawable.ic_exception_blank_notify);
                }
            } else {
                showErrorMsg(code, respanse);
                BlankViewDisplay.setBlank(mData.size(), this, false, blankLayout, onClickRetry);
            }
        } else if (tag.equals(TAG_MARK_READ)) {
            umengEvent(UmengEvent.NOTIFY, "标记已读");
            int id = (int) data;
            for (NotifyObject item : mData) {
                if (item.id == id) {
                    item.setRead();
                    baseAdapter.notifyDataSetChanged();
                    break;
                }
            }
        } else if (tag.equals(TAG_MARK_READ_ALL)) {
            if (code == 0) {
                umengEvent(UmengEvent.NOTIFY, "标记全部已读");
                markAllRead();
            } else {
                showErrorMsg(code, respanse);
            }

        }
    }

    @UiThread(delay = 1)
    void loadMoreDelay() {
        loadMore();
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
