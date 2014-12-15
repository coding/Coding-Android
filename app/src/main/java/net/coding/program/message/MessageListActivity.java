package net.coding.program.message;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.BaseFragmentActivity;
import net.coding.program.FootUpdate;
import net.coding.program.Global;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.StartActivity;
import net.coding.program.common.TextWatcherAt;
import net.coding.program.common.enter.EnterEmojiLayout;
import net.coding.program.common.enter.EnterLayout;
import net.coding.program.common.photopick.PhotoPickActivity;
import net.coding.program.maopao.item.ContentArea;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.Message;
import net.coding.program.model.UserObject;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

@EActivity(R.layout.activity_message_list)
public class MessageListActivity extends BaseFragmentActivity implements SwipeRefreshLayout.OnRefreshListener, FootUpdate.LoadMore, StartActivity, EnterLayout.CameraAndPhoto {

    @Extra
    UserObject mUserObject;

    // 从push条转过来只会带有这个参数
    @Extra
    String mGlobalKey;

    private PhotoOperate photoOperate = new PhotoOperate(this);

    ArrayList<Message.MessageObject> mData = new ArrayList<Message.MessageObject>();

    final String HOST_MESSAGE_SEND = Global.HOST + "/api/message/send?";
    final String hostDeleteMessage = Global.HOST + "/api/message/%s";
    final String TAG_SEND_IMAGE = "TAG_SEND_IMAGE";
    String url;

    ClickSmallImage clickImage = new ClickSmallImage(this);

    @ViewById
    ListView listView;

    @ViewById
    View blankLayout;

    EnterEmojiLayout mEnterLayout;

    final String HOST_USER_INFO = Global.HOST + "/api/user/key/";

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (mUserObject == null) {
            getNetwork(HOST_USER_INFO + mGlobalKey, HOST_USER_INFO);
        } else {
            initControl();
        }

    }

    void initControl() {
        mData = AccountInfo.loadMessages(this, mUserObject.global_key);
        if (mData.isEmpty()) {
            showDialogLoading();
        }

        mEnterLayout = new EnterEmojiLayout(this, mOnClickSendText);
        mEnterLayout.content.addTextChangedListener(new TextWatcherAt(this, this, RESULT_REQUEST_FOLLOW));

        url = String.format(Global.HOST + "/api/message/conversations/%s?pageSize=10", mUserObject.global_key);

        getActionBar().setTitle(mUserObject.name);

        mFootUpdate.initToHead(listView, mInflater, this);
        listView.setAdapter(adapter);
        listView.setSelection(mData.size());
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mEnterLayout.hideKeyboard();
                }
                return false;
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int ppp, long id) {
                final Message.MessageObject msg = mData.get((int) id);
                Global.MessageParse msgParse = HtmlContent.parseMessage(msg.content);

                AlertDialog.Builder builder = new AlertDialog.Builder(MessageListActivity.this);
                if (msgParse.text.isEmpty()) {
                    builder.setItems(R.array.message_action_image, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String url = String.format(hostDeleteMessage, msg.id);
                            deleteNetwork(url, hostDeleteMessage, msg.id);
                        }
                    });

                } else {
                    builder.setItems(R.array.message_action_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                Global.copy(msg.content, MessageListActivity.this);
                                showButtomToast("已复制");
                            } else if (which == 1) {
                                String url = String.format(hostDeleteMessage, msg.id);
                                deleteNetwork(url, hostDeleteMessage, msg.id);
                            }
                        }
                    });
                }

                AlertDialog dialog = builder.show();
                CustomDialog.dialogTitleLineColor(MessageListActivity.this, dialog);

                return true;
            }
        });

        getNextPageNetwork(url, url);
    }

    public void photo() {
        Intent intent = new Intent(this, PhotoPickActivity.class);
        startActivityForResult(intent, RESULT_REQUEST_PICK_PHOTO);
    }

    @Override
    public void loadMore() {
        onRefresh();
    }

    void deleteItem(String itemId) {
        for (int i = 0; i < mData.size(); ++i) {
            if (mData.get(i).id.equals(itemId)) {
                mData.remove(i);
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onRefresh() {
        getNextPageNetwork(url, url);
    }


    public static final int RESULT_REQUEST_FOLLOW = 1002;
    public static final int RESULT_REQUEST_PICK_PHOTO = 1003;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_PICK_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {

                try {
                    ArrayList<PhotoPickActivity.ImageInfo> pickPhots = (ArrayList<PhotoPickActivity.ImageInfo>) data.getSerializableExtra("data");
                    for (PhotoPickActivity.ImageInfo item : pickPhots) {
                        Uri uri = Uri.parse(item.path);
                        sendPhotoPre(uri);
                    }
                } catch (Exception e) {
                    showMiddleToast("缩放图片失败");
                    Global.errorLog(e);
                }
                adapter.notifyDataSetChanged();
            }
        } else if (requestCode == RESULT_REQUEST_PHOTO) {
            if (resultCode == RESULT_OK) {
                try {
                    sendPhotoPre(fileUri);
                } catch (Exception e) {
                    showMiddleToast("缩放图片失败");
                    Global.errorLog(e);
                }

                adapter.notifyDataSetChanged();
            }
        } else if (requestCode == RESULT_REQUEST_FOLLOW) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra("name");
                mEnterLayout.insertText(name);
            }

        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendPhotoPre(Uri uri) throws Exception {
        RequestParams params = new RequestParams();
        params.put("tweetImg", photoOperate.scal(uri));

        int myId = MyMessage.createId();
        postNetwork(HOST_INSERT_IMAGE, params, TAG_SEND_IMAGE + myId, myId, null);

        MyMessage myMessage = new MyMessage(myId, MyMessage.REQUEST_IMAGE, params, mUserObject);
        String imageLink = "<div class='message-image-box'><a href='%s' target='_blank'><img class='message-image' src='%s'/></a></div>";
        myMessage.content = String.format(imageLink, uri.toString(), uri.toString());
        mData.add(myMessage);
    }

    @OptionsItem(android.R.id.home)
    void back() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (mEnterLayout != null && mEnterLayout.isEmojiKeyboardShowing()) {
            mEnterLayout.closeEmojiKeyboard();
            return;
        }

        super.onBackPressed();
    }

    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_USER_INFO)) {
            if (code == 0) {
                if (code == 0) {
                    mUserObject = new UserObject(respanse.getJSONObject("data"));
                    initControl();
                } else {
                    showErrorMsg(code, respanse);
                }

            } else {
                hideProgressDialog();
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(url)) {
            hideProgressDialog();

            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mData.clear();
                }

                JSONArray array = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < array.length(); ++i) {
                    Message.MessageObject item = new Message.MessageObject(array.getJSONObject(i));
                    mData.add(0, item);
                }

                AccountInfo.saveMessages(this, mUserObject.global_key, mData);

                if (isLoadingLastPage(tag)) {
                    mFootUpdate.dismiss();
                } else {
                    mFootUpdate.showLoading();
                }
                // 为空就为空，不显示猴子
//                BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry);

                adapter.notifyDataSetChanged();

                if (mData.size() == array.length()) {
                    listView.setSelection(mData.size() - 1);
                } else {
                    final int index = array.length();
                    listView.setSelection(index + 1);
                }

            } else {
                if (mData.size() > 0) {
                    mFootUpdate.showFail();
                } else {
                    mFootUpdate.dismiss();
                }

                BlankViewDisplay.setBlank(mData.size(), this, false, blankLayout, onClickRetry);
                showErrorMsg(code, respanse);
            }

        } else if (tag.indexOf(HOST_MESSAGE_SEND) == 0) {
            if (code == 0) {
                Message.MessageObject item = new Message.MessageObject(respanse.getJSONObject("data"));
                mData.add(item);

                for (int i = mData.size() - 1; i >= 0; --i) {
                    Object singleItem = mData.get(i);
                    if (singleItem instanceof MyMessage) {
                        MyMessage tempMsg = (MyMessage) singleItem;
                        if (tempMsg.myId == pos) {
                            mData.remove(i);
                            break;
                        }
                    }
                }
                mEnterLayout.clearContent();
                AccountInfo.saveMessages(MessageListActivity.this, mUserObject.global_key, mData);

            } else {
                for (int i = mData.size() - 1; i >= 0; --i) {
                    Object singleItem = mData.get(i);
                    if (singleItem instanceof MyMessage) {
                        MyMessage tempMsg = (MyMessage) singleItem;
                        if (tempMsg.myId == pos) {
                            tempMsg.myStyle = MyMessage.STYLE_RESEND;
                            break;
                        }
                    }
                }
                showErrorMsg(code, respanse);
            }
            adapter.notifyDataSetChanged();

            listView.setSelection(mData.size());

        } else if (tag.indexOf(TAG_SEND_IMAGE) == 0) {
            if (code == 0) {
                String imageUrl = respanse.getString("data");
                Log.d("", "image " + imageUrl);
                RequestParams params = new RequestParams();
                params.put("content", "");
                params.put("extra", imageUrl);
                params.put("receiver_global_key", mUserObject.global_key);
                postNetwork(HOST_MESSAGE_SEND, params, HOST_MESSAGE_SEND + pos, pos, null);

            } else {
                for (int i = mData.size() - 1; i >= 0; --i) {
                    Object singleItem = mData.get(i);
                    if (singleItem instanceof MyMessage) {
                        MyMessage tempMsg = (MyMessage) singleItem;
                        if (tempMsg.myId == pos) {
                            tempMsg.myStyle = MyMessage.STYLE_RESEND;
                            break;
                        }
                    }
                }
                adapter.notifyDataSetChanged();

                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(hostDeleteMessage)) {
            if (code == 0) {
                deleteItem((String) data);
                AccountInfo.saveMessages(MessageListActivity.this, mUserObject.global_key, mData);
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    View.OnClickListener mOnClickSendText = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String s = mEnterLayout.getContent();

            if (EmojiFilter.containsEmoji(s)) {
                showMiddleToast("暂不支持发表情");
                return;
            }

            RequestParams params = new RequestParams();
            params.put("content", s);
            params.put("extra", "");
            params.put("receiver_global_key", mUserObject.global_key);

            int msgId = MyMessage.createId();

            postNetwork(HOST_MESSAGE_SEND, params, HOST_MESSAGE_SEND, msgId, null);

            MyMessage temp = new MyMessage(msgId, MyMessage.REQUEST_TEXT, params, mUserObject);
            temp.content = s;

            mData.add(temp);
            adapter.notifyDataSetChanged();

            mEnterLayout.clearContent();
        }
    };

    String HOST_INSERT_IMAGE = Global.HOST + "/api/tweet/insert_image";

    MyImageGetter myImageGetter = new MyImageGetter(this);

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
        public int getItemViewType(int position) {
            Message.MessageObject item = (Message.MessageObject) getItem(position);
            if (item.sender.id.equals(item.friend.id)) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Message.MessageObject item = (Message.MessageObject) getItem(position);
            ViewHolder holder;
            if (convertView == null) {
                int res = getItemViewType(position) == 0 ? R.layout.message_list_list_item_left : R.layout.message_list_list_item_right;
                convertView = mInflater.inflate(res, parent, false);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.icon.setOnClickListener(mOnClickUser);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.contentArea = new ContentArea(convertView, null, clickImage, myImageGetter, getImageLoad());
                holder.resend = convertView.findViewById(R.id.resend);
                holder.sending = convertView.findViewById(R.id.sending);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            iconfromNetwork(holder.icon, item.sender.avatar);
            holder.icon.setTag(item.sender.global_key);

            String lastTimeString = "";
            if (position > 0) {
                long lastTime = ((Message.MessageObject) getItem(position - 1)).created_at;
                lastTimeString = Global.dayToNow(lastTime);
            }

            String timeString = Global.dayToNow(item.created_at);
            if (timeString.equals(lastTimeString)) {
                holder.time.setVisibility(View.GONE);
            } else {
                holder.time.setVisibility(View.VISIBLE);
                holder.time.setText(timeString);
            }

            if (position == 0) {
                if (!isLoadingLastPage(url)) {
                    onRefresh();
                }
            }

            if (item instanceof MyMessage) {
                final MyMessage myMessage = (MyMessage) item;
                if (myMessage.myStyle == MyMessage.STYLE_RESEND) {
                    holder.resend.setVisibility(View.VISIBLE);
                    holder.sending.setVisibility(View.INVISIBLE);
                    holder.resend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (myMessage.myRequestType == MyMessage.REQUEST_TEXT) {
                                postNetwork(HOST_MESSAGE_SEND, myMessage.requestParams, HOST_MESSAGE_SEND, myMessage.myId, null);
                            } else {
                                postNetwork(HOST_INSERT_IMAGE, myMessage.requestParams, TAG_SEND_IMAGE + myMessage.myId, myMessage.myId, null);
                            }
                            myMessage.myStyle = MyMessage.STYLE_SENDING;
                            adapter.notifyDataSetChanged();
                        }
                    });

                } else {
                    holder.resend.setVisibility(View.INVISIBLE);
                    holder.sending.setVisibility(View.VISIBLE);
                }

            } else {
                holder.resend.setVisibility(View.INVISIBLE);
                holder.sending.setVisibility(View.INVISIBLE);
            }

            holder.contentArea.setData(item.content, ContentArea.Type.Message);

            return convertView;
        }
    };

    public static class MyMessage extends Message.MessageObject {
        public static int sId = 0;

        public static final int STYLE_SENDING = 0;
        public static final int STYLE_RESEND = 1;

        public static final int REQUEST_TEXT = 0;
        public static final int REQUEST_IMAGE = 1;

        public RequestParams requestParams;
        public int myStyle = 0;
        public int myId = 0;
        public int myRequestType = 0;

        public static int createId() {
            return sId++;
        }

        public MyMessage(int id, int requestType, RequestParams params, UserObject friendUser) {
            myId = id;
            myStyle = STYLE_SENDING;

            myRequestType = requestType;
            requestParams = params;

            friend = friendUser;
            sender = MyApp.sUserObject;

            created_at = Calendar.getInstance().getTimeInMillis();
        }
    }

    static class ViewHolder {
        TextView time;
        ImageView icon;
        ContentArea contentArea;

        View resend;
        View sending;
    }
}
