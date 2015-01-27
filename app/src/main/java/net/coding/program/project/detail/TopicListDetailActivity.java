package net.coding.program.project.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
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
public class TopicListDetailActivity extends BaseActivity implements StartActivity {

    @Extra
    TopicObject topicObject;

    @Extra
    TopicDetailParam mJumpParam;

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

    EnterLayout mEnterLayout;

    String owerGlobar = "";

    String urlCommentList = Global.HOST + "/api/topic/%s/comments?pageSize=200";

    String urlCommentSend = Global.HOST + "/api/project/%s/topic?parent=%s";

    String urlTopic = "";

    ArrayList<TopicObject> mData = new ArrayList<TopicObject>();

    Intent mResultData = new Intent();

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (topicObject == null) {
            urlTopic = String.format(Global.HOST + "/api/topic/%s?", mJumpParam.mTopic);
            getNetwork(urlTopic, urlTopic);

        } else {
            owerGlobar = topicObject.owner.global_key;

            urlTopic = String.format(Global.HOST + "/api/topic/%s?", topicObject.id);
            getNetwork(urlTopic, urlTopic);
        }

        mEnterLayout = new EnterLayout(this, mOnClickSend, EnterLayout.Type.TextOnly);

        prepareComment();
    }

    private void initData() {
        updateHeadData();
        urlCommentSend = String.format(urlCommentSend, topicObject.project_id, topicObject.id);
        urlCommentList = String.format(urlCommentList, topicObject.id);

        getNetwork(urlCommentList, urlCommentList);
    }

    final int RESULT_AT = 1;

    @OnActivityResult(RESULT_AT)
    void onResultAt(int requestCode, Intent data) {
        if (requestCode == Activity.RESULT_OK) {
            String name = data.getStringExtra("name");
            mEnterLayout.insertText(name);
            mEnterLayout.popKeyboard();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (owerGlobar.equals(MyApp.sUserObject.global_key)) {
            getMenuInflater().inflate(R.menu.topic_detail, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (mResultData.getIntExtra("child_count", -1) == -1) {
            setResult(Activity.RESULT_CANCELED);
        } else {
            setResult(Activity.RESULT_OK, mResultData);
        }

        super.onBackPressed();
    }

    final String HOST_MAOPAO_DELETE = Global.HOST + "/api/topic/%s";

    @OptionsItem(R.id.action_delete)
    void menuDeleteTopic() {
        showDialog("讨论", "删除讨论?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNetwork(String.format(HOST_MAOPAO_DELETE, topicObject.id), TAG_DELETE_TOPIC);
            }
        });
    }

    private TextView textViewCommentCount;

    void updateDisplayCommentCount() {
        String commentCount = String.format("%d条评论", topicObject.child_count);
        textViewCommentCount.setText(commentCount);
    }

    String bubble;

    private void updateHeadData() {
        mEnterLayout.content.addTextChangedListener(new TextWatcherAt(this, this, RESULT_AT, topicObject.project));

        View head = mInflater.inflate(R.layout.activity_project_topic_comment_list_head, listView, false);
        try {
            bubble = Global.readTextFile(getAssets().open("topic-android"));
        } catch (Exception e) {
            Global.errorLog(e);
        }

        ImageView icon = (ImageView) head.findViewById(R.id.icon);
        iconfromNetwork(icon, topicObject.owner.avatar);
        icon.setTag(topicObject.owner.global_key);
        icon.setOnClickListener(mOnClickUser);

        ((TextView) head.findViewById(R.id.title)).setText(topicObject.title);

        final String format = "<font color='#3bbd79'>%s</font> 发布于%s";
        String timeString = String.format(format, topicObject.owner.name, Global.dayToNow(topicObject.updated_at));
        ((TextView) head.findViewById(R.id.time)).setText(Html.fromHtml(timeString));

        WebView webView = (WebView) head.findViewById(R.id.comment);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(0);
        webView.getBackground().setAlpha(0);
        webView.setWebViewClient(new MaopaoDetailActivity.CustomWebViewClient(this));

        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.loadDataWithBaseURL(null, bubble.replace("${webview_content}", topicObject.content), "text/html", "UTF-8", null);

        textViewCommentCount = (TextView) head.findViewById(R.id.commentCount);
        updateDisplayCommentCount();

        head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareComment();
                mEnterLayout.popKeyboard();
            }
        });

        listView.addHeaderView(head);
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

            String content = mEnterLayout.getContent();
            if (content.isEmpty()) {
                showMiddleToast("你还什么都没有写");
                return;
            }

            if (EmojiFilter.containsEmoji(content)) {
                showMiddleToast("暂不支持发表情");
                return;
            }

            RequestParams params = new RequestParams();
            EditText message = mEnterLayout.content;
            TopicObject comment = (TopicObject) message.getTag();

            if (!comment.parent_id.isEmpty()) {
                content = String.format("@%s : ", comment.owner.name) + content;
            }
            params.put("content", content);
            postNetwork(urlCommentSend, params, urlCommentSend, 0, comment);
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
            if (code == 0) {
                JSONObject jsonObject = respanse.getJSONObject("data");

                ++topicObject.child_count;
                mResultData.putExtra("child_count", topicObject.child_count);
                mResultData.putExtra("topic_id", topicObject.id);
                updateDisplayCommentCount();

                mData.add(0, new TopicObject(jsonObject));

                mEnterLayout.restoreDelete(data);

                mEnterLayout.clearContent();
                mEnterLayout.hideKeyboard();
                baseAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
                baseAdapter.notifyDataSetChanged();
            }
        } else if (tag.equals(urlTopic)) {
            if (code == 0) {
                topicObject = new TopicObject(respanse.getJSONObject("data"));
                initData();

            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_DELETE_TOPIC_COMMENT)) {
            String itemId = (String) data;
            if (code == 0) {
                for (int i = 0; i < mData.size(); ++i) {
                    if (itemId.equals(mData.get(i).id)) {
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
                showButtomToast("删除失败");
            }
        } else if (tag.equals(TAG_DELETE_TOPIC)) {
            if (code == 0) {
                mResultData.putExtra("id", topicObject.id);
                setResult(RESULT_OK, mResultData);
                finish();

            } else {
                showButtomToast("删除失败");
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
