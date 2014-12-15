package net.coding.program.message;


import android.content.DialogInterface;
import android.content.Intent;
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
import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.Unread;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.Message;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EFragment(R.layout.fragment_users_list)
@OptionsMenu(R.menu.message_users_list)
public class UsersListFragment extends RefreshBaseFragment implements FootUpdate.LoadMore {

    @ViewById
    ListView listView;

    ArrayList<Message.MessageObject> mData = new ArrayList<Message.MessageObject>();

    final String HOST_MESSAGE_USERS = Global.HOST + "/api/message/conversations?pageSize=10";

    final String HOST_UNREAD_AT = Global.HOST + "/api/notification/unread-count?type=0";
    final String HOST_UNREAD_COMMENT = Global.HOST + "/api/notification/unread-count?type=1&type=2";
    final String HOST_UNREAD_SYSTEM = Global.HOST + "/api/notification/unread-count?type=4";

    final String HOST_MARK_AT = Global.HOST + "/api/notification/mark-read?all=1&type=0";
    final String HOST_MARK_COMMENT = Global.HOST + "/api/notification/mark-read?all=1&type=1&type=2";
    final String HOST_MARK_SYSTEM = Global.HOST + "/api/notification/mark-read?all=1&type=4";

    final String HOST_MARK_MESSAGE = Global.HOST + "/api/message/conversations/%s/read";

    BadgeView badgeAt;
    BadgeView badgeComment;
    BadgeView badgeSystem;

//    @ViewById
//    View blankLayout;

    @AfterViews
    protected void init() {
        super.init();

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

                String url = String.format(HOST_MARK_MESSAGE, user.friend.global_key);
                postNetwork(url, new RequestParams(), HOST_MARK_MESSAGE, (int) id, null);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Message.MessageObject msg = mData.get((int) id);
                final String format = "删除你和%s之间的所有私信?";
                String title = String.format(format, msg.friend.name);

                showDialog(title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String hostDeleteAll = Global.HOST + "/api/message/conversations/%s";
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

    boolean mUpdateAll = false;

    void initData() {
        initSetting();

        mUpdateAll = true;
        loadMore();

        getNetwork(HOST_UNREAD_AT, HOST_UNREAD_AT);
        getNetwork(HOST_UNREAD_COMMENT, HOST_UNREAD_COMMENT);
        getNetwork(HOST_UNREAD_SYSTEM, HOST_UNREAD_SYSTEM);
    }

    void deleteItem(Message.MessageObject msg) {
        for (int i = 0; i < mData.size(); ++i) {
            if (msg.id.equals(mData.get(i).id)) {
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
                Intent intent = new Intent(getActivity(), NotifyListActivity_.class);
                intent.putExtra("type", 0);
                startActivity(intent);

                postNetwork(HOST_MARK_AT, new RequestParams(), HOST_MARK_AT);
            }
        });

        v.findViewById(R.id.commentLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NotifyListActivity_.class);
                intent.putExtra("type", 1);
                startActivity(intent);

                postNetwork(HOST_MARK_COMMENT, new RequestParams(), HOST_MARK_COMMENT);
            }
        });

        v.findViewById(R.id.systemLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NotifyListActivity_.class);
                intent.putExtra("type", 4);
                startActivity(intent);

                postNetwork(HOST_MARK_SYSTEM, new RequestParams(), HOST_MARK_SYSTEM);
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

    @OptionsItem
    void action_add() {
        Intent intent = new Intent(getActivity(), FollowUsersActivity_.class);
        startActivity(intent);
    }

    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    final String TAG_DELETE_MESSAGE = "TAG_DELETE_MESSAGE";

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
                    mData.add(messageObject);
                }
                AccountInfo.saveMessageUsers(getActivity(), mData);

                adapter.notifyDataSetChanged();

                if (isLoadingFirstPage(tag)) {
                    mFootUpdate.dismiss();
                } else {
                    mFootUpdate.showLoading();
                }
//                BlankViewDisplay.setBlank(mData.size(), UsersListFragment.this, true, blankLayout);

            } else {
                showErrorMsg(code, respanse);
                if (mData.size() > 0) {
                    mFootUpdate.showFail();
                } else {
                    mFootUpdate.dismiss(); // 显示猴子照片
                }
//                BlankViewDisplay.setBlank(mData.size(), UsersListFragment.this, false, blankLayout, onClickRetry);
            }

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

        } else if (tag.equals(HOST_MARK_AT)) {
            if (code == 0) {
                UnreadNotify.displayNotify(badgeAt, Unread.countToString(0));
                UnreadNotify.update(getActivity());
            }

        } else if (tag.equals(HOST_MARK_COMMENT)) {
            if (code == 0) {
                UnreadNotify.displayNotify(badgeComment, Unread.countToString(0));
                UnreadNotify.update(getActivity());
            }

        } else if (tag.equals(HOST_MARK_SYSTEM)) {
            if (code == 0) {
                UnreadNotify.displayNotify(badgeSystem, Unread.countToString(0));
                UnreadNotify.update(getActivity());
            }

        } else if (tag.equals(HOST_MARK_MESSAGE)) {
            if (code == 0) {
                mData.get(pos).unreadCount = 0;
                adapter.notifyDataSetChanged();
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
            holder.content.setText(Global.recentMessage(user.content, myImageGetter, Global.tagHandler));
            holder.time.setText(Global.dayToNow(user.created_at));

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

    static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView content;
        TextView time;
        BadgeView badge;
    }
}
