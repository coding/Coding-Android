package net.coding.program.project.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
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
import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.Global;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.StartActivity;
import net.coding.program.common.TextWatcherAt;
import net.coding.program.common.enter.EnterLayout;
import net.coding.program.common.enter.ImageCommentLayout;
import net.coding.program.common.photopick.PhotoPickActivity;
import net.coding.program.maopao.MaopaoDetailActivity;
import net.coding.program.maopao.item.ImageCommentHolder;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.TopicLabelObject;
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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@EActivity(R.layout.activity_topic_list_detail)
public class TopicListDetailActivity extends BaseActivity implements StartActivity, SwipeRefreshLayout.OnRefreshListener, FootUpdate.LoadMore {

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

    //    EnterLayout mEnterLayout;
    ImageCommentLayout mEnterComment;
    private TopicLabelBar labelBar;

    String owerGlobar = "";

    String urlCommentList = Global.HOST + "/api/topic/%s/comments?pageSize=20";

    String urlCommentSend = Global.HOST + "/api/project/%s/topic?parent=%s";

    String URI_DELETE_TOPIC_LABEL = Global.HOST + "/api/topic/%s/label/%s";

    String urlTopic = "";

    ArrayList<TopicObject> mData = new ArrayList();
    private int currentLabelId;

    Intent mResultData = new Intent();

    @AfterViews
    void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.green);

        mFootUpdate.init(listView, mInflater, this);

        loadData();

        mEnterComment = new ImageCommentLayout(this, mOnClickSend, getImageLoad());

        prepareComment();
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(urlCommentList, urlCommentList);
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

        loadMore();
    }

    final int RESULT_AT = 1;
    final int RESULT_EDIT = 2;
    final int RESULT_LABEL = 3;

    @OnActivityResult(RESULT_AT)
    void onResultAt(int requestCode, Intent data) {
        if (requestCode == Activity.RESULT_OK) {
            String name = data.getStringExtra("name");
            mEnterComment.getEnterLayout().insertText(name);
            mEnterComment.getEnterLayout().popKeyboard();
        }
    }

    @OnActivityResult(RESULT_EDIT)
    void onResultEdit(int requestCode, Intent data) {
        if (requestCode == Activity.RESULT_OK) {
            topicObject = (TopicObject) data.getSerializableExtra("topic");
            topicTitleTextView.setText(topicObject.title);
            updateLabels(topicObject.labels);
            setTopicWebView(this, webView, bubble, topicObject.content);
            mResultData.putExtra("topic", topicObject);
        }
    }

    @OnActivityResult(RESULT_LABEL)
    void onResultLabel(int code, @OnActivityResult.Extra ArrayList<TopicLabelObject> labels){
        if(code == RESULT_OK){
            topicObject.labels = labels;
            updateLabels(topicObject.labels);
        }
    }

    @OnActivityResult(ImageCommentLayout.RESULT_REQUEST_COMMENT_IMAGE)
    final void commentImage(int result, Intent data) {
        if (result == RESULT_OK) {
            mEnterComment.onActivityResult(
                    ImageCommentLayout.RESULT_REQUEST_COMMENT_IMAGE,
                    data);
        }
    }

    @OnActivityResult(ImageCommentLayout.RESULT_REQUEST_COMMENT_IMAGE_DETAIL)
    final void commentImageDetail(int result, Intent data) {
        if (result == RESULT_OK) {
            mEnterComment.onActivityResult(
                    ImageCommentLayout.RESULT_REQUEST_COMMENT_IMAGE_DETAIL,
                    data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (topicObject != null) {
            int menuRes;
            if (topicObject.isMy()) {
                menuRes = R.menu.topic_detail_modify;
            } else {
                menuRes = R.menu.common_more_copy_link;
            }
            getMenuInflater().inflate(menuRes, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }


    @OptionsItem
    void action_edit() {
        TopicAddActivity_.intent(this).projectObject(topicObject.project).topicObject(topicObject).startForResult(RESULT_EDIT);
    }

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

    @OptionsItem
    protected final void action_copy() {
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
        Global.initWebView(webView);
        webView.setWebViewClient(new MaopaoDetailActivity.CustomWebViewClient(context));
        webView.loadDataWithBaseURL(Global.HOST, bubble.replace("${webview_content}", content), "text/html", "UTF-8", null);
    }

    View mListHead;

    private void updateHeadData() {
        mEnterComment.getEnterLayout().content.addTextChangedListener(new TextWatcherAt(this, this, RESULT_AT, topicObject.project));

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

        updateLabels(topicObject.labels);

        webView = (WebView) mListHead.findViewById(R.id.comment);
        setTopicWebView(this, webView, bubble, topicObject.content);

        textViewCommentCount = (TextView) mListHead.findViewById(R.id.commentCount);
        updateDisplayCommentCount();

        mListHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareComment();
                mEnterComment.getEnterLayout().popKeyboard();
            }
        });

        listView.setAdapter(baseAdapter);
    }

    private void updateLabels(List<TopicLabelObject> labels){
        if(labelBar == null) labelBar = (TopicLabelBar) mListHead.findViewById(R.id.labelBar);
        labelBar.bind(labels, new TopicLabelBar.Controller() {
            @Override
            public boolean canShowLabels() {
                return true;
            }

            @Override
            public boolean canEditLabels() {
                return true;
            }

            @Override
            public void onEditLabels(TopicLabelBar view) {
                TopicLabelActivity_.intent(TopicListDetailActivity.this)
                        .ownerUser(topicObject.project.owner_user_name)
                        .projectName(topicObject.project.name)
                        .topicId(topicObject.id)
                        .checkedLabels(topicObject.labels)
                        .startForResult(RESULT_LABEL);
            }

            @Override
            public void onRemoveLabel(TopicLabelBar view, int labelId) {
                currentLabelId = labelId;
                String url = String.format(URI_DELETE_TOPIC_LABEL,topicObject.id, labelId);
                deleteNetwork(url, URI_DELETE_TOPIC_LABEL);
            }
        });
    }

    private void prepareComment() {
        EditText message = mEnterComment.getEnterLayout().content;
        message.setHint("发表评论");
        message.setTag(topicObject);

        mEnterComment.getEnterLayout().restoreLoad(topicObject);
    }

    View.OnClickListener mOnClickSend = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendCommentAll();
        }
    };

    String tagUrlCommentPhoto = "";

    HashMap<String, String> mSendedImages = new HashMap<>();

    private void sendCommentAll() {
        showProgressBar(true);

        ArrayList<PhotoPickActivity.ImageInfo> photos = mEnterComment.getPickPhotos();
        for (PhotoPickActivity.ImageInfo item : photos) {
            String imagePath = item.path;
            if (!mSendedImages.containsKey(imagePath)) {
                try {
                    String url = topicObject.project.getHttpUploadPhoto();
                    RequestParams params = new RequestParams();
                    params.put("dir", 0);
                    Uri uri = Uri.parse(imagePath);
                    File file = new PhotoOperate(this).scal(uri);
                    params.put("file", file);
                    tagUrlCommentPhoto = imagePath; // tag必须不同，否则无法调用下一次
                    postNetwork(url, params, tagUrlCommentPhoto, 0, imagePath);
                    showProgressBar(true);
                } catch (Exception e) {
                    showProgressBar(false);
                }

                return;
            }
        }

        String send = mEnterComment.getEnterLayout().getContent();
        for (PhotoPickActivity.ImageInfo item : photos) {
            send += mSendedImages.get(item.path);
        }
        sendComment(send);
    }

    private void sendComment(String send) {
        String input = send;
        if (EmojiFilter.containsEmptyEmoji(this, input)) {
            showProgressBar(false);
            return;
        }

        RequestParams params = new RequestParams();
        TopicObject comment = (TopicObject) mEnterComment.getEnterLayout().content.getTag();
        if (comment != null && comment.parent_id != 0) {
            input = Global.encodeInput(comment.owner.name, input);
        } else {
            input = Global.encodeInput("", input);
        }
        params.put("content", input);

        postNetwork(urlCommentSend, params, urlCommentSend, 0, comment);

        showProgressBar(R.string.sending_comment);
    }

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
                if (isLoadingFirstPage(tag)) {
                    mData.clear();
                }

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
            mFootUpdate.updateState(code, isLoadingLastPage(tag), mData.size());

        } else if (tag.equals(urlCommentSend)) {
            showProgressBar(false);
            if (code == 0) {
                JSONObject jsonObject = respanse.getJSONObject("data");

                ++topicObject.child_count;
                mResultData.putExtra("child_count", topicObject.child_count);
                mResultData.putExtra("topic_id", topicObject.id);
                updateDisplayCommentCount();

                mData.add(new TopicObject(jsonObject));

                EnterLayout enterLayout = mEnterComment.getEnterLayout();
                enterLayout.restoreDelete(data);
                mEnterComment.clearContent();
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
        } else if (tag.equals(tagUrlCommentPhoto)) {
            if (code == 0) {
                String fileUri;
                if (topicObject.project.isPublic()) {
                    fileUri = respanse.optString("data", "");
                } else {
                    AttachmentFileObject fileObject = new AttachmentFileObject(respanse.optJSONObject("data"));
                    fileUri = fileObject.owner_preview;
                }
                String mdPhotoUri = String.format("\n![图片](%s)", fileUri);
                mSendedImages.put((String) data, mdPhotoUri);
                sendCommentAll();
            } else {
                showErrorMsg(code, respanse);
                showProgressBar(false);
            }
        } else if (URI_DELETE_TOPIC_LABEL.equals(tag)){
            if (code == 0) {
                labelBar.removeLabel(currentLabelId);
            }else{
                currentLabelId = -1;
                showErrorMsg(code, respanse);
                showProgressBar(false);
            }
        }
    }

    private final ClickSmallImage onClickImage = new ClickSmallImage(this);

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
            ImageCommentHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_task_comment_much_image, parent, false);
                holder = new ImageCommentHolder(convertView, onClickComment, myImageGetter, getImageLoad(), mOnClickUser, onClickImage);
                convertView.setTag(R.id.layout, holder);
            } else {
                holder = (ImageCommentHolder) convertView.getTag(R.id.layout);
            }

            TopicObject data = (TopicObject) getItem(position);
            holder.setTaskCommentContent(data);

            loadMore();

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
                EnterLayout enterLayout = mEnterComment.getEnterLayout();
                EditText message = enterLayout.content;
                message.setHint("回复 " + comment.owner.name);

                message.setTag(comment);
                enterLayout.popKeyboard();

                enterLayout.restoreLoad(comment);
            }
        }
    };

    MyImageGetter myImageGetter = new MyImageGetter(this);
}
