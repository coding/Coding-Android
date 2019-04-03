package net.coding.program.message;


import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.LoadMore;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.StartActivity;
import net.coding.program.common.Unread;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.Message;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.route.BlankViewDisplay;
import net.coding.program.user.UsersListActivity;
import net.coding.program.util.TextWatcherAt;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

@EFragment(R.layout.fragment_users_list)
public class UsersListFragment extends RefreshBaseFragment implements LoadMore, StartActivity {

    static WeakReference<UsersListFragment> mInstance = new WeakReference<>(null);
    public final String HOST_MARK_MESSAGE = getHostMarkMessage();
    final String HOST_MESSAGE_USERS = Global.HOST_API + "/message/conversations?pageSize=10";

    final String HOST_UNREAD_SYSTEM = Global.HOST_API + "/notification/unread-count";


    final String TAG_DELETE_MESSAGE = "TAG_DELETE_MESSAGE";

    private final int RESULT_SELECT_USER = 2001;
    private static final int RESULT_NOTIFY = 2002;

    @ViewById
    ListView listView;

    @ViewById
    Toolbar usersListToolbar;
    @ViewById
    View blankLayout;

    ArrayList<Message.MessageObject> mData = new ArrayList<>();
    BadgeView badgeSystem;
    boolean mUpdateAll = false;
    MyImageGetter myImageGetter;
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
                convertView = mInflater.inflate(R.layout.fragment_message_user_list_item, parent, false);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.icon.setFocusable(false);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.content = (TextView) convertView.findViewById(R.id.comment);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.badge = (BadgeView) convertView.findViewById(R.id.badge);
                holder.badge.setFocusable(false);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Message.MessageObject user = (Message.MessageObject) getItem(position);
            iconfromNetwork(holder.icon, user.friend.avatar);
            holder.title.setText(user.friend.name);
            boolean isUnPlayedVoiceMessage = !user.sender.isMe() && user.played == 0 && user.file != null && user.file.endsWith(".amr");

            CharSequence contentString;
            if (isUnPlayedVoiceMessage) {
                contentString = Global.createGreenHtml("", user.content, "");
            } else {
                contentString = GlobalCommon.recentMessage(user.content, myImageGetter, Global.tagHandler);
            }
            holder.content.setText(contentString);
            holder.time.setText(Global.dayToNow(user.created_at, false));

            if (user.unreadCount > 0) {
                UnreadNotify.displayNotify(holder.badge, Unread.countToString(user.unreadCount));
                holder.badge.setVisibility(View.VISIBLE);
            } else {
                holder.badge.setVisibility(View.INVISIBLE);
            }

            if (position == (mData.size() - 1)) {
                loadMore();
            }

            return convertView;
        }
    };
    private View listHeader;

    public static String getHostMarkMessage() {
        return Global.HOST_API + "/message/conversations/%s/read";
    }

    public static void receiverMessagePush(String globalKey, String content) {
        if (mInstance != null) {
            UsersListFragment fragment = mInstance.get();
            if (fragment != null) {
                fragment.messagePlus1(globalKey, content);
            }
        }
    }

    private void postMarkReaded(String globalKey) {
        String url = String.format(HOST_MARK_MESSAGE, globalKey);
        postNetwork(url, new RequestParams(), HOST_MARK_MESSAGE, -1, globalKey);
    }

    @AfterViews
    protected void initUsersListFragment() {
        usersListToolbar.inflateMenu(R.menu.message_users_list);
        usersListToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_message_add) {
                TextWatcherAt.startActivityAt(getActivity(), this, RESULT_SELECT_USER);
            }
            return true;
        });

        initRefreshLayout();

        myImageGetter = new MyImageGetter(getActivity());
        mData = AccountInfo.loadMessageUsers(getActivity());
        initHead();

        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Message.MessageObject user = mData.get((int) id);
            Intent intent = new Intent(getActivity(), MessageListActivity_.class);
            intent.putExtra("mUserObject", user.friend);
            startActivity(intent);

            postMarkReaded(user.friend.global_key);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            final Message.MessageObject msg = mData.get((int) id);
            final String format = "删除你和%s之间的所有私信?";
            String title = String.format(format, msg.friend.name);

            showDialog("私信", title, (dialog, which) -> {
                final String hostDeleteAll = Global.HOST_API + "/message/conversations/%s";
                String url = String.format(hostDeleteAll, msg.friend.id);
                deleteNetwork(url, TAG_DELETE_MESSAGE, msg);
            });

            return true;
        });

        initData();
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(HOST_MESSAGE_USERS, HOST_MESSAGE_USERS);
    }

    void initData() {
        initSetting();

        mUpdateAll = true;
        loadMore();
    }

    @Override
    public void onStart() {
        super.onStart();

        getNetwork(HOST_UNREAD_SYSTEM, HOST_UNREAD_SYSTEM);

        mInstance = new WeakReference<>(this);

        onRefresh();
    }

    @Override
    public void onStop() {
        mInstance = new WeakReference<>(null);
        super.onStop();
    }

    void deleteItem(Message.MessageObject msg) {
        for (int i = 0; i < mData.size(); ++i) {
            if (msg.getId() == (mData.get(i).getId())) {
                mData.remove(i);
                adapter.notifyDataSetChanged();
                return;
            }
        }
    }

    @Override
    public void onRefresh() {
        initData();
    }

    private void initHead() {
        listHeader = mInflater.inflate(R.layout.fragment_message_user_list_head, listView, false);
        listHeader.findViewById(R.id.systemLayout).setOnClickListener(v1 -> startNotifyListActivity());
        badgeSystem = listHeader.findViewById(R.id.badgeSystem);
        badgeSystem.setVisibility(View.INVISIBLE);
        listView.addHeaderView(listHeader, null, false);

        View footer = mInflater.inflate(R.layout.divide_bottom, listView, false);
        listView.addFooterView(footer, null, false);

        updateHeader();
    }

    private void startNotifyListActivity() {
        NotifyListActivity_.intent(UsersListFragment.this).startForResult(RESULT_NOTIFY);
    }

    @OnActivityResult(RESULT_SELECT_USER)
    void onSelectUser(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            UserObject user = (UserObject) data.getSerializableExtra(UsersListActivity.RESULT_EXTRA_USESR);
            if (user != null) {
                MessageListActivity_.intent(this).mUserObject(user).start();
            }
        }
    }

    private void handleVoiceMessage(Message.MessageObject item) {
        //语音消息重新设置extra
        if (item.file != null && item.file.endsWith(".amr") && item.duration > 0) {
            Log.w("test", "recordDuration1=" + item.duration);
            int dur = item.duration / 1000;
            item.content = "[语音]";
            item.extra = "[voice]{'id':" + item.getId() + ",'voiceUrl':'" + item.file + "','voiceDuration':" + dur + ",'played':" + item.played + "}[voice]";
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_MESSAGE_USERS)) {
            setRefreshing(false);
            if (code == 0) {
                if (mUpdateAll) {
                    mUpdateAll = false;
                    mData.clear();
                }

                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    Message.MessageObject messageObject = new Message.MessageObject(jsonArray.getJSONObject(i));
                    handleVoiceMessage(messageObject);
                    mData.add(messageObject);
                }
                AccountInfo.saveMessageUsers(getActivity(), mData);

                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }

            updateHeader();

            BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, v -> loadMore());

            mFootUpdate.updateState(code, isLoadingLastPage(tag), mData.size());

            mUpdateAll = false;

        } else if (tag.equals(HOST_UNREAD_SYSTEM)) {
            if (code == 0) {
                int count = respanse.getInt("data");
                UnreadNotify.displayNotify(badgeSystem, Unread.countToString(count));
            }

        } else if (tag.equals(HOST_MARK_MESSAGE)) {
            if (code == 0) {
                String globalKey = (String) data;
                markUserReaded(globalKey);
            }
        } else if (tag.equals(TAG_DELETE_MESSAGE)) {
            Message.MessageObject msg = (Message.MessageObject) data;
            if (code == 0) {
                deleteItem(msg);
            } else {
                showButtomToast("删除失败");
            }
        }
    }

    public void updateHeader() {
        if (mData.isEmpty()) {
            FrameLayout.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) blankLayout.getLayoutParams();
            lp.topMargin = listHeader.getHeight();
            blankLayout.setLayoutParams(lp);
            listHeader.findViewById(R.id.headerBottomLine).setVisibility(View.GONE);
            listHeader.findViewById(R.id.headerBottomLineEmpty).setVisibility(View.VISIBLE);
        } else {
            listHeader.findViewById(R.id.headerBottomLine).setVisibility(View.VISIBLE);
            listHeader.findViewById(R.id.headerBottomLineEmpty).setVisibility(View.GONE);
        }
    }

    private void markUserReaded(String globalKey, Message.MessageObject message) {
        for (int i = 0; i < mData.size(); ++i) {
            Message.MessageObject item = mData.get(i);
            handleVoiceMessage(item);
            if (item.friend.global_key.equals(globalKey)) {
                item.unreadCount = 0;
                if (message != null) {
                    item.content = message.content;
                    item.played = message.played;
                }
                adapter.notifyDataSetChanged();
                return;
            }
        }
    }

    private void markUserReaded(String globalKey) {
        markUserReaded(globalKey, null);
    }

    private void messagePlus1(String globalKey, String message) {
        for (int i = 0; i < mData.size(); ++i) {
            Message.MessageObject messageObject = mData.get(i);
            handleVoiceMessage(messageObject);
            if (messageObject.friend.global_key.equals(globalKey)) {
                messageObject.content = message;
                messageObject.unreadCount += 1;
                adapter.notifyDataSetChanged();
                return;
            }
        }
    }

    @OnActivityResult(RESULT_NOTIFY)
    void onResultNotify() {
        getNetwork(HOST_UNREAD_SYSTEM, HOST_UNREAD_SYSTEM);
    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView content;
        TextView time;
        BadgeView badge;
    }
}
