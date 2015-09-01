package net.coding.program.message;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.StartActivity;
import net.coding.program.common.Unread;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.Message;
import net.coding.program.model.UserObject;
import net.coding.program.user.UsersListActivity;
import net.coding.program.user.UsersListActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

@EFragment(R.layout.fragment_users_list)
@OptionsMenu(R.menu.message_users_list)
public class UsersListFragment extends RefreshBaseFragment implements FootUpdate.LoadMore, StartActivity {

    public static final String HOST_MARK_MESSAGE = Global.HOST_API + "/message/conversations/%s/read";
    static WeakReference<UsersListFragment> mInstance = new WeakReference<>(null);
    final String HOST_MESSAGE_USERS = Global.HOST_API + "/message/conversations?pageSize=10";
    final String HOST_UNREAD_AT = Global.HOST_API + "/notification/unread-count?type=0";
    final String HOST_UNREAD_COMMENT = Global.HOST_API + "/notification/unread-count?type=1&type=2";
    final String HOST_UNREAD_SYSTEM = Global.HOST_API + "/notification/unread-count?type=4";
    final String TAG_DELETE_MESSAGE = "TAG_DELETE_MESSAGE";
    private final int RESULT_SELECT_USER = 2001;
    @ViewById
    ListView listView;
    ArrayList<Message.MessageObject> mData = new ArrayList<>();
    BadgeView badgeAt;
    BadgeView badgeComment;

    //    private void postMarkReaded(String globalKey) {
//        String url = String.format(HOST_MARK_MESSAGE, globalKey);
//        postNetwork(url, new RequestParams(), HOST_MARK_MESSAGE, -1, globalKey);
//    }
    BadgeView badgeSystem;
    boolean mUpdateAll = false;
    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };
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
                holder.icon =
                        (ImageView) convertView.findViewById(R.id.icon);
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
            boolean isUnPlayedVoiceMessage = !user.sender.isMe() && user.played == 0 && user.file!=null && user.file.endsWith(".amr");
            holder.content.setText(isUnPlayedVoiceMessage? Html.fromHtml("<font color='#3bbd79'>"+user.content+"</font>"):Global.recentMessage(user.content, myImageGetter, Global.tagHandler));
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
    protected void init() {
        initRefreshLayout();

        myImageGetter = new MyImageGetter(getActivity());
        mData = AccountInfo.loadMessageUsers(getActivity());
        initHead();

        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message.MessageObject user = mData.get((int) id);
                Intent intent = new Intent(getActivity(), MessageListActivity_.class);
                intent.putExtra("mUserObject", user.friend);
                startActivity(intent);

                postMarkReaded(user.friend.global_key);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Message.MessageObject msg = mData.get((int) id);
                final String format = "删除你和%s之间的所有私信?";
                String title = String.format(format, msg.friend.name);

                showDialog("私信", title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String hostDeleteAll = Global.HOST_API + "/message/conversations/%s";
                        String url = String.format(hostDeleteAll, msg.friend.id);
                        deleteNetwork(url, TAG_DELETE_MESSAGE, msg);
                    }
                });

                return true;
            }
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

        getNetwork(HOST_UNREAD_AT, HOST_UNREAD_AT);
        getNetwork(HOST_UNREAD_COMMENT, HOST_UNREAD_COMMENT);
        getNetwork(HOST_UNREAD_SYSTEM, HOST_UNREAD_SYSTEM);

        mInstance = new WeakReference<>(this);
    }

    @Override
    public void onStop() {
        mInstance = new WeakReference<>(null);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

        String userGlobal = ReadedUserId.getReadedUser();
        if (!userGlobal.isEmpty()) {
            markUserReaded(userGlobal, ReadedUserId.getUserLastMessage());

            postMarkReaded(userGlobal);
            ReadedUserId.remove();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

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
        View v = mInflater.inflate(R.layout.fragment_message_user_list_head, null, false);

        v.findViewById(R.id.atLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNotifyListActivity(0);
            }
        });

        v.findViewById(R.id.commentLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNotifyListActivity(1);
            }
        });

        v.findViewById(R.id.systemLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNotifyListActivity(4);
            }
        });

        badgeAt = (BadgeView) v.findViewById(R.id.badgeAt);
        badgeAt.setVisibility(View.INVISIBLE);
        badgeComment = (BadgeView) v.findViewById(R.id.badgeComment);
        badgeComment.setVisibility(View.INVISIBLE);
        badgeSystem = (BadgeView) v.findViewById(R.id.badgeSystem);
        badgeSystem.setVisibility(View.INVISIBLE);

        listView.addHeaderView(v);
    }

    private void startNotifyListActivity(int type) {
        NotifyListActivity_.intent(UsersListFragment.this)
                .type(type)
                .startForResult(type);
    }

    @OptionsItem
    void action_add() {
        UsersListActivity_.intent(this)
                .type(UsersListActivity.Friend.Follow)
                .select(true)
                .hideFollowButton(true)
                .startForResult(RESULT_SELECT_USER);
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
        if(item.file!=null && item.file.endsWith(".amr") && item.duration>0){
            Log.w("test", "recordDuration1=" + item.duration);
            int dur = item.duration/1000;
            item.content = "[语音]";
            item.extra = "[voice]{'id':"+item.getId()+",'voiceUrl':'"+item.file+"','voiceDuration':"+dur+",'played':"+item.played+"}[voice]";
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

//                if (isLoadingLastPage(tag)) {
//                    mFootUpdate.dismiss();
//                } else {
//                    mFootUpdate.showLoading();
//                }
//                BlankViewDisplay.setBlank(mData.size(), UsersListFragment.this, true, blankLayout);


            } else {
                showErrorMsg(code, respanse);
//                if (mData.size() > 0) {
//                    mFootUpdate.showFail();
//                } else {
//                    mFootUpdate.dismiss(); // 显示猴子照片
//                }
//                BlankViewDisplay.setBlank(mData.size(), UsersListFragment.this, false, blankLayout, onClickRetry);
            }
            mFootUpdate.updateState(code, isLoadingLastPage(tag), mData.size());

            mUpdateAll = false;

        } else if (tag.equals(HOST_UNREAD_AT)) {
            if (code == 0) {
                int count = respanse.getInt("data");
                UnreadNotify.displayNotify(badgeAt, Unread.countToString(count));
            }

        } else if (tag.equals(HOST_UNREAD_COMMENT)) {
            if (code == 0) {
                int count = respanse.getInt("data");
                UnreadNotify.displayNotify(badgeComment, Unread.countToString(count));
            }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                getNetwork(HOST_UNREAD_AT, HOST_UNREAD_AT);
                break;
            case 1:
                getNetwork(HOST_UNREAD_COMMENT, HOST_UNREAD_COMMENT);
                break;
            case 4:
                getNetwork(HOST_UNREAD_SYSTEM, HOST_UNREAD_SYSTEM);
                break;
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView content;
        TextView time;
        BadgeView badge;
    }

    public static class ReadedUserId {

        private static String sReadedUser = "";

        private static Message.MessageObject mData = null;

        public static void setReadedUser(String id, Message.MessageObject data) {
            if (id == null) {
                id = "";
                mData = null;
            }

            sReadedUser = id;
            mData = data;
        }

        public static String getReadedUser() {
            return sReadedUser;
        }

        public static Message.MessageObject getUserLastMessage() {
            return mData;
        }

        public static void remove() {
            sReadedUser = "";
            mData = null;
        }
    }
}
