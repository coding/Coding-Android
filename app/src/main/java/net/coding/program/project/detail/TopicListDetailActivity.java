package net.coding.program.project.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.BaseActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.DialogUtil;
import net.coding.program.common.Global;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.StartActivity;
import net.coding.program.common.TextWatcherAt;
import net.coding.program.common.comment.HtmlCommentHolder;
import net.coding.program.common.enter.EnterLayout;
import net.coding.program.maopao.MaopaoDetailActivity;
import net.coding.program.model.TopicObject;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

@EActivity(R.layout.activity_topic_list_detail)
public class TopicListDetailActivity extends BaseActivity implements StartActivity, SwipeRefreshLayout.OnRefreshListener {

    @Extra
    TopicObject topicObject;

    @Extra
    TopicDetailParam mJumpParam;

    private WebView webView;
    private TextView topicTitleTextView;

    public static class TopicDetailParam implements Serializable {
        public String mUser;
        public String mProject;
        public String mTopic;

        public TopicDetailParam(String mUser, String mProject, String mTopic) {
            this.mUser = mUser;
            this.mProject = mProject;
            this.mTopic = mTopic;
        }
    }

    @ViewById
    ListView listView;

    @ViewById
    SwipeRefreshLayout swipeRefreshLayout;

    EnterLayout mEnterLayout;

    String owerGlobar = "";

    String urlCommentList = Global.HOST + "/api/topic/%s/comments?pageSize=200";

    String urlCommentSend = Global.HOST + "/api/project/%s/topic?parent=%s";

    String urlTopic = "";

    ArrayList<TopicObject> mData = new ArrayList();

    Intent mResultData = new Intent();

    @AfterViews
    void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.green);

        loadData();

        mEnterLayout = new EnterLayout(this, mOnClickSend, EnterLayout.Type.TextOnly);

        prepareComment();
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    private void loadData() {
        if (topicObject == null) {
            urlTopic = String.format(Global.HOST + "/api/topic/%s?", mJumpParam.mTopic);
            getNetwork(urlTopic, urlTopic);

        } else {
            owerGlobar = topicObject.owner.global_key;
            getSupportActionBar().setTitle(topicObject.project.name);

            urlTopic = String.format(Global.HOST + "/api/topic/%s?", topicObject.id);
            getNetwork(urlTopic, urlTopic);
        }
    }

    private void initData() {
        getSupportActionBar().setTitle(topicObject.project.name);
        updateHeadData();
        urlCommentSend = String.format(urlCommentSend, topicObject.project_id, topicObject.id);
        urlCommentList = String.format(urlCommentList, topicObject.id);

        getNetwork(urlCommentList, urlCommentList);
    }

    final int RESULT_AT = 1;
    final int RESULT_EDIT = 2;

    @OnActivityResult(RESULT_AT)
    void onResultAt(int requestCode, Intent data) {
        if (requestCode == Activity.RESULT_OK) {
            String name = data.getStringExtra("name");
            mEnterLayout.insertText(name);
            mEnterLayout.popKeyboard();
        }
    }

    @OnActivityResult(RESULT_EDIT)
    void onResultEdit(int requestCode, Intent data) {
        if (requestCode == Activity.RESULT_OK) {
            topicObject = (TopicObject) data.getSerializableExtra("topic");
            topicTitleTextView.setText(topicObject.title);
            setTopicWebView(this, webView, bubble, topicObject.content);
            mResultData.putExtra("topic", topicObject);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (topicObject != null) {
            int menuRes;
            if (topicObject.isMy()) {
                menuRes = R.menu.topic_detail_modify;
            } else {
                menuRes = R.menu.common_more;
            }
            getMenuInflater().inflate(menuRes, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @OptionsItem
    void action_more() {
        showRightTopPop();
    }

    @OptionsItem
    void action_edit() {
        TopicAddActivity_.intent(this).projectObject(topicObject.project).topicObject(topicObject).startForResult(RESULT_EDIT);
    }

    private DialogUtil.RightTopPopupWindow mRightTopPopupWindow = null;

    private void initRightTopPop() {
        if (mRightTopPopupWindow == null) {
            ArrayList<DialogUtil.RightTopPopupItem> popupItemArrayList = new ArrayList();
            DialogUtil.RightTopPopupItem downloadItem = new DialogUtil.RightTopPopupItem(getString(R.string.copy_link), R.drawable.ic_menu_link);
            popupItemArrayList.add(downloadItem);
            if (owerGlobar.equals(MyApp.sUserObject.global_key)) {
                DialogUtil.RightTopPopupItem deleteItem = new DialogUtil.RightTopPopupItem(getString(R.string.delete_topic), R.drawable.ic_menu_delete_selector);
                popupItemArrayList.add(deleteItem);
            }
            mRightTopPopupWindow = DialogUtil.initRightTopPopupWindow(this, popupItemArrayList, onRightTopPopupItemClickListener);
        }
    }

    private void showRightTopPop() {
        initRightTopPop();

        mRightTopPopupWindow.adapter.notifyDataSetChanged();

        Rect rectgle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int StatusBarHeight = rectgle.top;
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        //int TitleBarHeight= contentViewTop - StatusBarHeight;
        mRightTopPopupWindow.adapter.notifyDataSetChanged();
        mRightTopPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        mRightTopPopupWindow.showAtLocation(listView, Gravity.TOP | Gravity.RIGHT, 0, contentViewTop);
    }

    private AdapterView.OnItemClickListener onRightTopPopupItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 0:
                    action_copy();
                    break;
                case 1:
                    action_delete();
                    break;
            }
            mRightTopPopupWindow.dismiss();
        }
    };

    @Override
    public void onBackPressed() {
        if (mResultData.getExtras() == null) {
            setResult(Activity.RESULT_CANCELED);
        } else {
            setResult(Activity.RESULT_OK, mResultData);
        }

        super.onBackPressed();
    }

    final String HOST_MAOPAO_DELETE = Global.HOST + "/api/topic/%s";

    @OptionsItem
    void action_delete() {
        showDialog("讨论", "删除讨论?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNetwork(String.format(HOST_MAOPAO_DELETE, topicObject.id), TAG_DELETE_TOPIC);
            }
        });
    }

    void action_copy() {
        final String urlTemplate = Global.HOST + "/u/%s/p/%s/topic/%d";
        String url = String.format(urlTemplate, topicObject.project.owner_user_name, topicObject.project.name, topicObject.id);
        Global.copy(this, url);
        showButtomToast("已复制 " + url);
    }

    private TextView textViewCommentCount;

    void updateDisplayCommentCount() {
        String commentCount = String.format("%d条评论", topicObject.child_count);
        textViewCommentCount.setText(commentCount);
    }

    String bubble;

    static public void setTopicWebView(Context context, WebView webView, String bubble, String content) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(0);
        webView.getBackground().setAlpha(0);
        webView.setWebViewClient(new MaopaoDetailActivity.CustomWebViewClient(context));

        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.loadDataWithBaseURL(Global.HOST, bubble.replace("${webview_content}", content), "text/html", "UTF-8", null);
    }

    View mListHead;

    private void updateHeadData() {
        mEnterLayout.content.addTextChangedListener(new TextWatcherAt(this, this, RESULT_AT, topicObject.project));

        if (mListHead == null) {
            mListHead = mInflater.inflate(R.layout.activity_project_topic_comment_list_head, listView, false);
            listView.addHeaderView(mListHead);
        }

        try {
            if (bubble == null) {
                bubble = Global.readTextFile(getAssets().open("topic-android"));
            }
        } catch (Exception e) {
            Global.errorLog(e);
        }

        ImageView icon = (ImageView) mListHead.findViewById(R.id.icon);
        iconfromNetwork(icon, topicObject.owner.avatar);
        icon.setTag(topicObject.owner.global_key);
        icon.setOnClickListener(mOnClickUser);

        topicTitleTextView = ((TextView) mListHead.findViewById(R.id.title));
        topicTitleTextView.setText(topicObject.title);

        final String format = "<font color='#3bbd79'>%s</font> 发布于%s";
        String timeString = String.format(format, topicObject.owner.name, Global.dayToNow(topicObject.updated_at));
        ((TextView) mListHead.findViewById(R.id.time)).setText(Html.fromHtml(timeString));

        webView = (WebView) mListHead.findViewById(R.id.comment);
        setTopicWebView(this, webView, bubble, topicObject.content);

        textViewCommentCount = (TextView) mListHead.findViewById(R.id.commentCount);
        updateDisplayCommentCount();

        mListHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareComment();
                mEnterLayout.popKeyboard();
            }
        });

        listView.setAdapter(baseAdapter);
    }

    private void prepareComment() {
        EditText message = mEnterLayout.content;
        message.setHint("发表评论");
        message.setTag(topicObject);

        mEnterLayout.restoreLoad(topicObject);
    }

    View.OnClickListener mOnClickSend = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String input = mEnterLayout.getContent();

            if (EmojiFilter.containsEmptyEmoji(v.getContext(), input)) {
                return;
            }

            RequestParams params = new RequestParams();
            EditText message = mEnterLayout.content;

            TopicObject comment = (TopicObject) message.getTag();
            if (comment != null && comment.parent_id != 0) {
                input = Global.encodeInput(comment.owner.name, input);
            } else {
                input = Global.encodeInput("", input);
            }
            params.put("content", input);

            postNetwork(urlCommentSend, params, urlCommentSend, 0, comment);

            showProgressBar(true, R.string.sending_comment);
        }
    };

    @OptionsItem(android.R.id.home)
    void back() {
        onBackPressed();
    }

    final String TAG_DELETE_TOPIC_COMMENT = "TAG_DELETE_TOPIC_COMMENT";
    final String TAG_DELETE_TOPIC = "TAG_DELETE_TOPIC";

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(urlCommentList)) {
            if (code == 0) {
                mData.clear();

                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    TopicObject commnet = new TopicObject(jsonArray.getJSONObject(i));
                    mData.add(commnet);
                }
                baseAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
                baseAdapter.notifyDataSetChanged();
            }

        } else if (tag.equals(urlCommentSend)) {
            showProgressBar(false);
            if (code == 0) {
                JSONObject jsonObject = respanse.getJSONObject("data");

                ++topicObject.child_count;
                mResultData.putExtra("child_count", topicObject.child_count);
                mResultData.putExtra("topic_id", topicObject.id);
                updateDisplayCommentCount();

                mData.add(new TopicObject(jsonObject));

                mEnterLayout.restoreDelete(data);

                mEnterLayout.clearContent();
                mEnterLayout.hideKeyboard();
                baseAdapter.notifyDataSetChanged();
                showButtomToast("发送评论成功");
            } else {
                showErrorMsg(code, respanse);
                baseAdapter.notifyDataSetChanged();
            }
        } else if (tag.equals(urlTopic)) {
            swipeRefreshLayout.setRefreshing(false);
            if (code == 0) {
                topicObject = new TopicObject(respanse.getJSONObject("data"));
                initData();
                invalidateOptionsMenu();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_DELETE_TOPIC_COMMENT)) {
            int itemId = (int) data;
            if (code == 0) {
                for (int i = 0; i < mData.size(); ++i) {
                    if (itemId == mData.get(i).id) {
                        mData.remove(i);
                        --topicObject.child_count;
                        mResultData.putExtra("child_count", topicObject.child_count);
                        mResultData.putExtra("topic_id", topicObject.id);
                        updateDisplayCommentCount();
                        baseAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            } else {
                showButtomToast(R.string.delete_fail);
            }
        } else if (tag.equals(TAG_DELETE_TOPIC)) {
            if (code == 0) {
                mResultData.putExtra("id", topicObject.id);
                setResult(RESULT_OK, mResultData);
                finish();

            } else {
                showButtomToast(R.string.delete_fail);
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
            HtmlCommentHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_maopao_detail_item, parent, false);
                holder = new HtmlCommentHolder(convertView, onClickComment, myImageGetter, getImageLoad(), mOnClickUser);
                convertView.setTag(R.id.layout, holder);

            } else {
                holder = (HtmlCommentHolder) convertView.getTag(R.id.layout);
            }

            TopicObject data = (TopicObject) getItem(position);
            holder.setContent(data);

            return convertView;
        }
    };

    View.OnClickListener onClickComment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final TopicObject comment = (TopicObject) v.getTag();

            if (comment.isMy()) {
                AlertDialog dialog = new AlertDialog.Builder(TopicListDetailActivity.this).setTitle("删除评论")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String HOST_MAOPAO_DELETE = Global.HOST + "/api/topic/%s";
                                deleteNetwork(String.format(HOST_MAOPAO_DELETE, comment.id), TAG_DELETE_TOPIC_COMMENT, comment.id);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                CustomDialog.dialogTitleLineColor(TopicListDetailActivity.this, dialog);

            } else {
                EditText message = mEnterLayout.content;
                message.setHint("回复 " + comment.owner.name);

                message.setTag(comment);
                mEnterLayout.popKeyboard();

                mEnterLayout.restoreLoad(comment);
            }
        }
    };

    MyImageGetter myImageGetter = new MyImageGetter(this);
}
