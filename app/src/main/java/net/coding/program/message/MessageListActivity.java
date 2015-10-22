package net.coding.program.message;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.FootUpdate;
import net.coding.program.MyApp;
import net.coding.program.MyPushReceiver;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalSetting;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.MyMediaPlayer;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.StartActivity;
import net.coding.program.common.TextWatcherAt;
import net.coding.program.common.WeakRefHander;
import net.coding.program.common.enter.EnterLayout;
import net.coding.program.common.enter.EnterVoiceLayout;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.common.photopick.ImageInfo;
import net.coding.program.common.photopick.PhotoPickActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.widget.EnterLayoutAnimSupportContainer;
import net.coding.program.maopao.ContentArea;
import net.coding.program.maopao.item.ContentAreaImages;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.Message;
import net.coding.program.model.UserObject;
import net.coding.program.third.EmojiFilter;
import net.coding.program.user.UsersListActivity;
import net.coding.program.user.UsersListActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

@EActivity(R.layout.activity_message_list)
//@OptionsMenu(R.menu.message_list)
public class MessageListActivity extends BackActivity implements SwipeRefreshLayout.OnRefreshListener, FootUpdate.LoadMore,
        StartActivity, EnterLayout.CameraAndPhoto, Handler.Callback,EnterVoiceLayout.VoiceRecordCompleteCallback,
        ContentAreaImages.VoicePlayCallBack, EnterLayoutAnimSupportContainer.OnEnterLayoutBottomMarginChanagedCallBack,
        EmojiFragment.EnterEmojiLayout {

    private static final int RESULT_REQUEST_FOLLOW = 1002;
    private static final int RESULT_REQUEST_PICK_PHOTO = 1003;
    private static final int RESULT_REQUEST_PHOTO = 1005;
    private static final int PAGESIZE = 20;
    public static final String HOST_MESSAGE_SEND = Global.HOST_API + "/message/send?";
    final String hostDeleteMessage = Global.HOST_API + "/message/%s";
    final String TAG_SEND_IMAGE = "TAG_SEND_IMAGE";
    final String TAG_SEND_VOICE = "TAG_SEND_VOICE";
    final String TAG_MARK_VOICE_PLAYED = "TAG_MARK_VOICE_PLAYED";
    final String HOST_MESSAGE_LAST = Global.HOST_API + "/message/conversations/%s/last?id=%s";
    final String HOST_USER_INFO = Global.HOST_API + "/user/key/";
    final String HOST_MARK_VOICE_PLAYED = Global.HOST_API + "/message/conversations/%s/play";
    private final int REFRUSH_TIME = 3 * 1000;
    @Extra
    UserObject mUserObject;
    // 从push条转过来只会带有这个参数
    @Extra
    String mGlobalKey;
    ArrayList<Message.MessageObject> mData = new ArrayList<>();
    String url = "";
    ClickSmallImage clickImage = new ClickSmallImage(this);
    @ViewById
    ListView listView;
    @ViewById
    View blankLayout;
    EnterVoiceLayout mEnterLayout;

    WeakRefHander mWeakRefHandler;

    int mLastId = 0;
    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };
    String HOST_INSERT_IMAGE = Global.HOST_API + "/tweet/insert_image";
    String HOST_SEND_VOICE = Global.HOST_API + "/message/send_voice";
    MyImageGetter myImageGetter = new MyImageGetter(this);
    View.OnClickListener mOnClickSendText = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String s = mEnterLayout.getContent();
            if (EmojiFilter.containsEmptyEmoji(v.getContext(), s)) {
                return;
            }

            RequestParams params = new RequestParams();
            params.put("content", s);
            params.put("receiver_global_key", mUserObject.global_key);

            MyMessage temp = new MyMessage(MyMessage.REQUEST_TEXT, params, mUserObject);
            temp.content = s;
            mData.add(temp);
            adapter.notifyDataSetChanged();

            postNetwork(HOST_MESSAGE_SEND, params, HOST_MESSAGE_SEND + temp.getCreateTime(), -1, temp.getCreateTime());

            mEnterLayout.clearContent();
        }
    };
    private PhotoOperate photoOperate = new PhotoOperate(this);
    private Uri fileUri;
    private int mPxImageWidth = 0;
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
            if (item.sender.id == (item.friend.id)) {
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
            boolean isLeft = getItemViewType(position) == 0;
            if (convertView == null) {
                int res = isLeft ? R.layout.message_list_list_item_left : R.layout.message_list_list_item_right;
                convertView = mInflater.inflate(res, parent, false);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.icon.setOnClickListener(mOnClickUser);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.contentArea = new ContentArea(convertView, null, clickImage, myImageGetter, getImageLoad(), mPxImageWidth);
                holder.contentArea.clearConentLongClick();
                holder.resend = convertView.findViewById(R.id.resend);
                holder.sending = convertView.findViewById(R.id.sending);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            iconfromNetwork(holder.icon, item.sender.avatar);
            holder.icon.setTag(item.sender.global_key);

            // 本条与上一条时间间隔不超过0.5小时就不显示本条时间
            long lastTime = 0;
            if (position > 0) {
                lastTime = ((Message.MessageObject) getItem(position - 1)).created_at;
            }

            long selfTime = item.created_at;
            if (lessThanStandard(selfTime, lastTime)) {
                holder.time.setVisibility(View.GONE);
            } else {
                holder.time.setVisibility(View.VISIBLE);
                holder.time.setText(Global.getTimeDetail(selfTime));
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
                                postNetwork(HOST_MESSAGE_SEND, myMessage.requestParams, HOST_MESSAGE_SEND + myMessage.getCreateTime(), -1, myMessage.getCreateTime());
                            } else if(myMessage.myRequestType == MyMessage.REQUEST_IMAGE){
                                postNetwork(HOST_INSERT_IMAGE, myMessage.requestParams, TAG_SEND_IMAGE + myMessage.getCreateTime(), -1, myMessage.getCreateTime());
                            } else if(myMessage.myRequestType == MyMessage.REQUEST_VOICE){
                                Global.MessageParse mp = ContentArea.parseVoice(myMessage.extra);
                                postNetwork(HOST_SEND_VOICE, myMessage.requestParams, TAG_SEND_VOICE + mp.voiceUrl, -1, myMessage.getCreateTime());
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
            boolean isVoice = item.extra!=null && item.extra.startsWith("[voice]{") && item.extra.endsWith("}[voice]");
            String data = isVoice?item.extra:item.content;
            holder.contentArea.setData(data);
            if(holder.contentArea.getVocicePath()!=null){
                holder.contentArea.setVoicePlayCallBack(MessageListActivity.this);
                //与最新的消息相差3天的超过一个页面的语音消息不下载
                if(mData.size()>PAGESIZE && position <=mData.size()-1-PAGESIZE && lastTime!=0 && lastTime - selfTime >=3*24*60*60*1000){
                    holder.contentArea.setVoiceNeedDownload(false);
                }else{
                    holder.contentArea.setVoiceNeedDownload(true);
                }
            }
            if(isLeft && isVoice){
                if(item.played==0){
                    holder.resend.setVisibility(View.VISIBLE);
                    holder.sending.setVisibility(View.INVISIBLE);
                }
            }
            return convertView;
        }

        private boolean lessThanStandard(long selfTime, long lastTime) {
            return (selfTime - lastTime) < (30 * 60 * 1000);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();

            BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry);
        }
    };
    private int mPxImageDivide = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWeakRefHandler = new WeakRefHander(this, REFRUSH_TIME);
    }

    @Override
    protected void onDestroy() {
        if (mWeakRefHandler != null) {
            mWeakRefHandler.removeMessages(0);
            mWeakRefHandler = null;
        }

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWeakRefHandler.start();
    }

    @AfterViews
    protected final void initMessageListActivity() {
        mEnterLayout = new EnterVoiceLayout(this, mOnClickSendText);

        // 图片显示，单位为 dp
        // 72 photo 3 photo 3 photo 72
        final int divide = 3;
        mPxImageWidth = Global.dpToPx(MyApp.sWidthDp - 72 * 2 - divide * 2) / 3;
        mPxImageDivide = Global.dpToPx(divide);

        if (mUserObject == null) {
            getNetwork(HOST_USER_INFO + mGlobalKey, HOST_USER_INFO);
        } else {
            mGlobalKey = mUserObject.global_key;
            initControl();
        }

        MyPushReceiver.closeNotify(this, URLSpanNoUnderline.createMessageUrl(mGlobalKey));

        GlobalSetting.getInstance().setMessageNoNotify(mGlobalKey);

        String lastInput = AccountInfo.loadMessageDraft(this, mGlobalKey);
        mEnterLayout.setText(lastInput);

    }

    void initControl() {
        mData = AccountInfo.loadMessages(this, mUserObject.global_key);
        if (mData.isEmpty()) {
            showDialogLoading();
        }

        mEnterLayout.content.addTextChangedListener(new TextWatcherAt(this, this, RESULT_REQUEST_FOLLOW));

        url = String.format(Global.HOST_API + "/message/conversations/%s?pageSize="+PAGESIZE, mUserObject.global_key);

        getSupportActionBar().setTitle(mUserObject.name);

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
                final Global.MessageParse msgParse = HtmlContent.parseMessage(msg.content);

                AlertDialog.Builder builder = new AlertDialog.Builder(MessageListActivity.this);
                if (msgParse.text.isEmpty()) {
                    builder.setItems(R.array.message_action_image, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                relayMessage(msg);
                            } else if (which == 1) {
                                deleteMessage(msg);
                            }
                        }
                    });

                } else {
                    builder.setItems(R.array.message_action_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                Global.copy(MessageListActivity.this, msg.content);
                                showButtomToast("已复制");
                            } else if (which == 1) {
                                relayMessage(msg);
                            } else if (which == 2) {
                                deleteMessage(msg);
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

        listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int last;

            @Override
            public void onGlobalLayout() {
                int current = listView.getHeight();
                if (last > current) {
                    listView.setSelection(mData.size());
                }

                last = current;
            }
        });
    }

    private void relayMessage(Message.MessageObject message) {
        UsersListActivity_.intent(this)
                .type(UsersListActivity.Friend.Follow)
                .hideFollowButton(true)
                .relayString(message.content)
                .start();
    }

    private void deleteMessage(Message.MessageObject msg) {
        if (msg instanceof MyMessage) {
            mData.remove(msg);
            adapter.notifyDataSetChanged();
        } else {
            String url = String.format(hostDeleteMessage, msg.getId());
            deleteNetwork(url, hostDeleteMessage, msg.getId());
        }
        if(msg.extra!=null && msg.extra.startsWith("[voice]{") && msg.extra.endsWith("}[voice]")){
            Global.MessageParse mp = ContentArea.parseVoice(msg.extra);
            if(mp.voiceUrl!=null){
                File f = new File(Global.sVoiceDir + File.separator + mp.voiceUrl.substring(mp.voiceUrl.lastIndexOf('/')+1));
                f.delete();
            }
        }
    }

    public void photo() {
        Intent intent = new Intent(this, PhotoPickActivity.class);
        startActivityForResult(intent, RESULT_REQUEST_PICK_PHOTO);
    }

    @OptionsItem
    void action_refresh() {
        showProgressBar(true);
        initSetting();
        loadMore();
    }

    @Override
    public void loadMore() {
        onRefresh();
    }

    void deleteItem(int itemId) {
        for (int i = 0; i < mData.size(); ++i) {
            if (mData.get(i).getId() == itemId) {
                mData.remove(i);
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onRefresh() {
        if (url == null || url.isEmpty()) {
            getNetwork(HOST_USER_INFO + mGlobalKey, HOST_USER_INFO);
            return;
        }

        getNextPageNetwork(url, url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_PICK_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {

                try {
                    @SuppressWarnings("unchecked")
                    ArrayList<ImageInfo> pickPhots = (ArrayList<ImageInfo>) data.getSerializableExtra("data");
                    for (ImageInfo item : pickPhots) {
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

        MyMessage myMessage = new MyMessage(MyMessage.REQUEST_IMAGE, params, mUserObject);
        String imageLink = "<div class='message-image-box'><a href='%s' target='_blank'><img class='message-image' src='%s'/?></a></div>";
        myMessage.content = String.format(imageLink, uri.toString(), uri.toString());
        mData.add(myMessage);

        postNetwork(HOST_INSERT_IMAGE, params, TAG_SEND_IMAGE + myMessage.getCreateTime(), -1, myMessage.getCreateTime());
    }

    @Override
    public void onBackPressed() {
        if (mEnterLayout != null && mEnterLayout.isEnterPanelShowing()) {
            mEnterLayout.closeEnterPanel();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onPause() {
        mEnterLayout.removeTempWindow();
        super.onPause();
        if(mMyMediaPlayer!=null){
            onStopPlay();
            mMyMediaPlayer.release();
            mMyMediaPlayer = null;
        }
        Message.MessageObject item = null;
        if (mData.size() > 0) {
            item = mData.get(mData.size() - 1);
        }
        UsersListFragment.ReadedUserId.setReadedUser(mGlobalKey,item);
    }

    @Override
    protected void onStop() {

        if(mMyMediaPlayer!=null){
            onStopPlay();
            mMyMediaPlayer.release();
            mMyMediaPlayer = null;
        }

        String input = mEnterLayout.getContent();
        AccountInfo.saveMessageDraft(this, input, mGlobalKey);

        super.onStop();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_USER_INFO)) {
            if (code == 0) {
                mUserObject = new UserObject(respanse.getJSONObject("data"));
                initControl();
            } else {
                hideProgressDialog();
                showProgressBar(false);
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(url)) {
            hideProgressDialog();
            showProgressBar(false);

            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mData.clear();

                    // 标记信息已读
                    String url = String.format(UsersListFragment.HOST_MARK_MESSAGE, mGlobalKey);
                    postNetwork(url, new RequestParams(), UsersListFragment.HOST_MARK_MESSAGE);
                }

                JSONArray array = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < array.length(); ++i) {
                    Message.MessageObject item = new Message.MessageObject(array.getJSONObject(i));
                    handleVoiceMessage(item);
                    mData.add(0, item);
                }

                AccountInfo.saveMessages(this, mUserObject.global_key, mData);

                adapter.notifyDataSetChanged();

                if (mData.size() == array.length()) {
                    listView.setSelection(mData.size() - 1);
                } else {
                    final int index = array.length();
                    listView.setSelection(index + 1);
                }

                BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry);

            } else {
                BlankViewDisplay.setBlank(mData.size(), this, false, blankLayout, onClickRetry);
                showErrorMsg(code, respanse);
            }
            mFootUpdate.updateState(code, isLoadingLastPage(tag), mData.size());

        } else if (tag.equals(HOST_MESSAGE_LAST)) {
            if (code == 0) {
                int oldSize = mData.size();

                JSONArray array = respanse.getJSONArray("data");
                for (int i = 0; i < array.length(); ++i) {
                    Message.MessageObject item = new Message.MessageObject(array.getJSONObject(i));
                    handleVoiceMessage(item);
                    boolean inserted = false;
                    for (int j = mData.size() - 1; j >= 0; --j) {
                        Message.MessageObject msg = mData.get(j);

                        if (msg instanceof MyMessage) {
                            continue;
                        }

                        if (msg.getId() < item.getId()) {
                            inserted = true;
                            mData.add(j + 1, item);
                            break;
                        } else if (msg.getId() == item.getId()) {
                            inserted = true;
                            mData.remove(j);
                            mData.add(j, item);
                            break;
                        }
                    }

                    if (!inserted) {
                        mData.add(0, item);
                    }

                    if (i == 0) {
                        mLastId = item.getId();
                    }
                }

                adapter.notifyDataSetChanged();

                if (array.length() > 0) {
                    if (oldSize == listView.getLastVisiblePosition()) {
                        listView.smoothScrollToPosition(mData.size());
                    } else {
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(300);
                    }
                }
                BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry);
            } else {
                BlankViewDisplay.setBlank(mData.size(), this, false, blankLayout, onClickRetry);
            }

        } else if (tag.indexOf(HOST_MESSAGE_SEND) == 0) {
            long sendId = (long) data;
            if (code == 0) {
                umengEvent(UmengEvent.NOTIFY, "发私信");

                Message.MessageObject item = new Message.MessageObject(respanse.getJSONObject("data"));

                for (int i = mData.size() - 1; i >= 0; --i) {
                    Message.MessageObject temp = mData.get(i);
                    if (temp.getId() < item.getId()) {
                        mData.add(i, item);
                        break;
                    }
                }

                for (int i = mData.size() - 1; i >= 0; --i) {
                    Object singleItem = mData.get(i);
                    if (singleItem instanceof MyMessage) {
                        MyMessage tempMsg = (MyMessage) singleItem;
                        if (tempMsg.getCreateTime() == sendId) {
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
                        if (tempMsg.getCreateTime() == sendId) {
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
            long sendId = (Long) data;
            if (code == 0) {
                String imageUrl = respanse.getString("data");
                RequestParams params = new RequestParams();
                params.put("content", String.format(" ![图片](%s) ", imageUrl));
                params.put("receiver_global_key", mUserObject.global_key);
                postNetwork(HOST_MESSAGE_SEND, params, HOST_MESSAGE_SEND + sendId, -1, sendId);

            } else {
                for (int i = mData.size() - 1; i >= 0; --i) {
                    Object singleItem = mData.get(i);
                    if (singleItem instanceof MyMessage) {
                        MyMessage tempMsg = (MyMessage) singleItem;
                        if (tempMsg.getCreateTime() == sendId) {
                            tempMsg.myStyle = MyMessage.STYLE_RESEND;
                            break;
                        }
                    }
                }
                adapter.notifyDataSetChanged();

                showErrorMsg(code, respanse);
            }
        } else if (tag.indexOf(TAG_SEND_VOICE) == 0) {
            long sendId = (Long) data;
            if(code == 0){
                umengEvent(UmengEvent.NOTIFY, "发语音私信");
                Message.MessageObject item = new Message.MessageObject(respanse.getJSONObject("data"));
                String voiceUrl = item.file;
                //将发送到服务器的本地录音文件的名字改成与其在服务器的文件名一致
                File oldfile = new File(tag.replace(TAG_SEND_VOICE,""));
                File newFile = new File(String.format(Global.sVoiceDir + File.separator + voiceUrl.substring(voiceUrl.lastIndexOf('/')+1)));
                oldfile.renameTo(newFile);
                //设置content内容,方便气泡获取录音时长与链接
                item.extra = "[voice]{'voiceUrl':'"+voiceUrl+"',voiceDuration:"+item.duration/1000+"}[voice]";
                item.content = "[语音]";
                //替换本地消息为获取到的远程消息
                for (int i = mData.size() - 1; i >= 0; --i) {
                    Message.MessageObject temp = mData.get(i);
                    if (temp.getId() < item.getId()) {
                        mData.add(i, item);
                        break;
                    }
                }

                for (int i = mData.size() - 1; i >= 0; --i) {
                    Object singleItem = mData.get(i);
                    if (singleItem instanceof MyMessage) {
                        MyMessage tempMsg = (MyMessage) singleItem;
                        if (tempMsg.getCreateTime() == sendId) {
                            mData.remove(i);
                            break;
                        }
                    }
                }
                listView.setSelection(mData.size());
                AccountInfo.saveMessages(MessageListActivity.this, mUserObject.global_key, mData);
            }else{
                for (int i = mData.size() - 1; i >= 0; --i) {
                    Object singleItem = mData.get(i);
                    if (singleItem instanceof MyMessage) {
                        MyMessage tempMsg = (MyMessage) singleItem;
                        if (tempMsg.getCreateTime() == sendId) {
                            tempMsg.myStyle = MyMessage.STYLE_RESEND;
                            break;
                        }
                    }
                }
                showErrorMsg(code, respanse);
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(mData.size());
        }else if (tag.equals(hostDeleteMessage)) {
            if (code == 0) {
                umengEvent(UmengEvent.NOTIFY, "删除私信");
                deleteItem((int) data);
                AccountInfo.saveMessages(MessageListActivity.this, mUserObject.global_key, mData);
            } else {
                showErrorMsg(code, respanse);
            }
        }else if(tag.startsWith(TAG_MARK_VOICE_PLAYED)){
            if(code == 0){
                int id = Integer.valueOf(tag.replace(TAG_MARK_VOICE_PLAYED,""));
                
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

    public void refrushData() {
        int lastId = mLastId;
        if (lastId == 0) {
            if (mData.size() > 0) {
                for (int i = mData.size() - 1; i >= 0; --i) {
                    Message.MessageObject item = mData.get(i);
                    if (!(item instanceof MyMessage)) {
                        lastId = item.getId();
                        break;
                    }
                }
            }
        }

        if (lastId != 0) {
            String url = String.format(HOST_MESSAGE_LAST, mGlobalKey, lastId);
            getNetwork(url, HOST_MESSAGE_LAST);
        }
    }

    @Override
    public boolean handleMessage(android.os.Message msg) {
        refrushData();
        return true;
    }

    @Override
    public void recordFinished(long duration, String voicePath) {
        try {
            //开始发送语音文件
            RequestParams params = new RequestParams();
            params.put("receiver_global_key", mUserObject.global_key);
            params.put("file",new File(voicePath));
            MyMessage myMessage = new MyMessage(MyMessage.REQUEST_VOICE, params, mUserObject);
            //[voice]{'voiceUrl':'/sd/voice/a.amr',voiceDuration:10}[voice]
            myMessage.extra = "[voice]{'voiceUrl':'"+voicePath+"',voiceDuration:"+duration/1000+"}[voice]";
            mData.add(myMessage);
            adapter.notifyDataSetChanged();
            listView.setSelection(mData.size());
            postNetwork(HOST_SEND_VOICE, params, TAG_SEND_VOICE + voicePath, -1, myMessage.getCreateTime());
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }



    private MyMediaPlayer mMyMediaPlayer;


    @Override
    public void onStartPlay(String path,int id,MediaPlayer.OnPreparedListener mOnPreparedListener, MediaPlayer.OnCompletionListener mOnCompletionListener) {
        try {
            if(mMyMediaPlayer == null){
                mMyMediaPlayer = new MyMediaPlayer();
            }else{
                mMyMediaPlayer.reset();
            }
            mMyMediaPlayer.setVoiceId(id);
            mMyMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mMyMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMyMediaPlayer.setDataSource(path);
//            mMyMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//                @Override
//                public boolean onError(MediaPlayer mp, int what, int extra) {
//                    return false;
//                }
//            });
            mMyMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMyMediaPlayer.prepare();
            mMyMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPlayingVoicePath() {
        try{
            if(mMyMediaPlayer!=null && mMyMediaPlayer.isPlaying()){
                return mMyMediaPlayer.getDataSource();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onStopPlay() {
        try{
            if(mMyMediaPlayer!=null && mMyMediaPlayer.isPlaying()){
                mMyMediaPlayer.stop();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void markVoicePlayed(int id) {
        Log.w("VoiceMessage", "markVoicePlayed:id=" + id);
        postNetwork(String.format(HOST_MARK_VOICE_PLAYED, id), null, TAG_MARK_VOICE_PLAYED + id, -1, id);
        for(int i = 0;i<mData.size();i++){
                    Message.MessageObject item = mData.get(i);
                    if(item.getId() == id){
                        item.played = 1;
                        handleVoiceMessage(item);
                        break;
                    }
                }
        adapter.notifyDataSetChanged();
        AccountInfo.saveMessages(MessageListActivity.this, mUserObject.global_key, mData);
    }

    @Override
    public int getPlayingVoiceId() {
        return mMyMediaPlayer==null?-1:mMyMediaPlayer.getVoiceId();
    }

    private int minBottom = Global.dpToPx(200);
    @Override
    public void onChanage(int lastBottomMargin,int newBottomMargin) {
//        if(!mEnterLayout.isKeyboardOpen){
//            listView.smoothScrollBy(-(newBottomMargin - lastBottomMargin) ,0);
//        }
        EnterLayoutAnimSupportContainer.SoftKeyBordState  mSoftKeyBordState = mEnterLayout.getEnterLayoutAnimSupportContainer().getSoftKeyBordState();
        if(mSoftKeyBordState == EnterLayoutAnimSupportContainer.SoftKeyBordState.Hide){
            listView.smoothScrollBy(-(newBottomMargin - lastBottomMargin) ,0);
        }
        if(newBottomMargin==minBottom || newBottomMargin == 0){
            listView.setSelection(mData.size());
        }
    }

    @Override
    public EnterLayout getEnterLayout() {
        return mEnterLayout;
    }

    public static class MyMessage extends Message.MessageObject implements Serializable {

        public static final int STYLE_SENDING = 0;
        public static final int STYLE_RESEND = 1;

        public static final int REQUEST_TEXT = 0;
        public static final int REQUEST_IMAGE = 1;
        public static final int REQUEST_VOICE = 2;

        public RequestParams requestParams;
        public int myStyle = 0;
        public int myRequestType = 0;


        public MyMessage(int requestType, RequestParams params, UserObject friendUser) {
            myStyle = STYLE_SENDING;

            myRequestType = requestType;
            requestParams = params;

            friend = friendUser;
            sender = MyApp.sUserObject;

            created_at = Calendar.getInstance().getTimeInMillis();
        }

        public long getCreateTime() {
            return created_at;
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
