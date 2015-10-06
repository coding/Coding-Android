package net.coding.program.project.detail.topic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.ui.BackActivity;
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
import net.coding.program.common.photopick.ImageInfo;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.maopao.item.ImageCommentHolder;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.TopicLabelObject;
import net.coding.program.model.TopicObject;
import net.coding.program.project.detail.TopicAddActivity_;
import net.coding.program.project.detail.TopicLabelActivity;
import net.coding.program.project.detail.TopicLabelActivity_;
import net.coding.program.project.detail.TopicLabelBar;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
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
public class TopicListDetailActivity extends BackActivity implements StartActivity, SwipeRefreshLayout.OnRefreshListener, FootUpdate.LoadMore {

    private static final String TAG_TOPIC_COMMENTS = "TAG_TOPIC_COMMENTS";
    final int RESULT_AT = 1;
    final int RESULT_EDIT = 2;
    final int RESULT_LABEL = 3;
    final String HOST_MAOPAO_DELETE = Global.HOST_API + "/topic/%s";
    final String TAG_DELETE_TOPIC_COMMENT = "TAG_DELETE_TOPIC_COMMENT";
    final String TAG_DELETE_TOPIC = "TAG_DELETE_TOPIC";
    private final String HOST_COMMENT_SEND = Global.HOST_API + "/project/%s/topic?parent=%s";
    private final ClickSmallImage onClickImage = new ClickSmallImage(this);
    @InstanceState
    protected boolean saveTopicWhenLoaded;
    @Extra
    TopicObject topicObject;
    @Extra
    TopicDetailParam mJumpParam;
    @ViewById
    ListView listView;
    @ViewById
    SwipeRefreshLayout swipeRefreshLayout;
    //    EnterLayout mEnterLayout;
    ImageCommentLayout mEnterComment;
    String owerGlobar = "";
    String urlCommentSend = HOST_COMMENT_SEND;
    String URI_DELETE_TOPIC_LABEL = Global.HOST_API + "/topic/%s/label/%s";
    String urlTopic = "";
    ArrayList<TopicObject> mData = new ArrayList<>();
    Intent mResultData = new Intent();
    View mListHead;
    String tagUrlCommentPhoto = "";
    HashMap<String, String> mSendedImages = new HashMap<>();
    View.OnClickListener mOnClickSend = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendCommentAll();
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
                                final String HOST_MAOPAO_DELETE = Global.HOST_API + "/topic/%s";
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
                convertView = mInflater.inflate(R.layout.activity_task_comment_much_image_divide, parent, false);
                holder = new ImageCommentHolder(convertView, onClickComment, myImageGetter, getImageLoad(), mOnClickUser, onClickImage);
                convertView.setTag(R.id.layout, holder);
            } else {
                holder = (ImageCommentHolder) convertView.getTag(R.id.layout);
            }

            TopicObject data = (TopicObject) getItem(position);
            holder.setContent(data);

//            convertView.findViewById(R.id.customDivide)

            loadMore();

            return convertView;
        }
    };
    private TopicLabelBar labelBar;
    private int currentLabelId;
    private TextView textViewCommentCount;

    @AfterViews
    protected final void initTopicListDetailActivity() {
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
        getNextPageNetwork(topicObject.getHttpComments(), TAG_TOPIC_COMMENTS);
    }

    private void loadData() {
        if (mJumpParam != null) {
            urlTopic = String.format(Global.HOST_API + "/topic/%s?", mJumpParam.mTopic);
            getNetwork(urlTopic, urlTopic);

        } else if (topicObject != null) {
            owerGlobar = topicObject.owner.global_key;
            getSupportActionBar().setTitle(topicObject.project.name);

            urlTopic = String.format(Global.HOST_API + "/topic/%s?", topicObject.id);
            getNetwork(urlTopic, urlTopic);
        } else {
            finish();
        }
    }

    private void initData() {
        getSupportActionBar().setTitle(topicObject.project.name);
        updateHeadData();
        if (saveTopicWhenLoaded) {
            saveTopicWhenLoaded = false;
            mResultData.putExtra("topic", topicObject);
        }
        urlCommentSend = String.format(urlCommentSend, topicObject.project_id, topicObject.id);

        loadMore();
    }

    @OnActivityResult(RESULT_AT)
    void onResultAt(int requestCode, Intent data) {
        if (requestCode == Activity.RESULT_OK) {
            String name = data.getStringExtra("name");
            mEnterComment.getEnterLayout().insertText(name);
            mEnterComment.getEnterLayout().popKeyboard();
        }
    }

    @OnActivityResult(RESULT_EDIT)
    void onResultEdit() {
        // 分支情况太多，如编辑状态下可进入标签管理删掉目前用的标签，
        // 回到编辑后又重复进入修改名字或者继续添加删除，最后还可以不保存返回
        // 除非一直把全局labels的所有状态通过intents传递，否则原状态难以维持，这里只好直接重新刷新了，
        // 会慢一些但状态肯定是对的，可能影响回复列表页数
        saveTopicWhenLoaded = true;
        onRefresh();
    }

    @OnActivityResult(RESULT_LABEL)
    void onResultLabel(int code, @OnActivityResult.Extra ArrayList<TopicLabelObject> labels) {
        if (code == RESULT_OK) {
            topicObject.labels = labels;
            updateLabels(topicObject.labels);
            mResultData.putExtra("topic", topicObject);
        }
    }

    @OnActivityResult(ImageCommentLayout.RESULT_REQUEST_COMMENT_IMAGE)
    protected final void commentImage(int result, Intent data) {
        if (result == RESULT_OK) {
            mEnterComment.onActivityResult(
                    ImageCommentLayout.RESULT_REQUEST_COMMENT_IMAGE,
                    data);
        }
    }

    @OnActivityResult(ImageCommentLayout.RESULT_REQUEST_COMMENT_IMAGE_DETAIL)
    protected final void commentImageDetail(int result, Intent data) {
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

    void updateDisplayCommentCount() {
        String commentCount = String.format("评论(%d)", topicObject.child_count);
        textViewCommentCount.setText(commentCount);
    }

    private void updateHeadData() {
        mEnterComment.getEnterLayout().content.addTextChangedListener(new TextWatcherAt(this, this, RESULT_AT, topicObject.project));

        if (mListHead == null) {
            mListHead = mInflater.inflate(R.layout.activity_project_topic_comment_list_head, listView, false);
            listView.addHeaderView(mListHead);
        }

        ImageView icon = (ImageView) mListHead.findViewById(R.id.icon);
        iconfromNetwork(icon, topicObject.owner.avatar);
        icon.setTag(topicObject.owner.global_key);
        icon.setOnClickListener(mOnClickUser);

        TextView topicTitleTextView = ((TextView) mListHead.findViewById(R.id.title));
        topicTitleTextView.setText(topicObject.title);

        final String format = "<font color='#3bbd79'>%s</font> 发布于%s";
        String timeString = String.format(format, topicObject.owner.name, Global.dayToNow(topicObject.updated_at));
        ((TextView) mListHead.findViewById(R.id.time)).setText(Html.fromHtml(timeString));

        ((TextView) mListHead.findViewById(R.id.referenceId)).setText(topicObject.getRefId());

        updateLabels(topicObject.labels);

        WebView webView = (WebView) mListHead.findViewById(R.id.comment);
        Global.setWebViewContent(webView, "topic-android", topicObject.content);

        textViewCommentCount = (TextView) mListHead.findViewById(R.id.commentCount);
        updateDisplayCommentCount();

        Spinner spinner = (Spinner) mListHead.findViewById(R.id.spinner);
        spinner.setAdapter(new TopicSortAdapter(this));
        spinner.setSelection(0, true); // 一定要写，否则会自动调用一次 onItemSelected
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    topicObject.setSortOld(TopicObject.SORT_OLD);
                } else {
                    topicObject.setSortOld(TopicObject.SORT_NEW);
                }
                initSetting();
                loadMore();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mListHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareComment();
                mEnterComment.getEnterLayout().popKeyboard();
            }
        });

        listView.setAdapter(baseAdapter);
    }

    private void updateLabels(List<TopicLabelObject> labels) {
        if (labelBar == null) {
            labelBar = (TopicLabelBar) mListHead.findViewById(R.id.labelBar);
        }

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
                        .labelType(TopicLabelActivity.LabelType.Topic)
                        .projectPath(topicObject.project.getProjectPath())
                        .id(topicObject.id)
                        .checkedLabels(topicObject.labels)
                        .startForResult(RESULT_LABEL);
            }

            @Override
            public void onRemoveLabel(TopicLabelBar view, int labelId) {
                currentLabelId = labelId;
                String url = String.format(URI_DELETE_TOPIC_LABEL, topicObject.id, labelId);
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

    private void sendCommentAll() {
        showProgressBar(true);

        ArrayList<ImageInfo> photos = mEnterComment.getPickPhotos();
        for (ImageInfo item : photos) {
            String imagePath = item.path;
            if (!mSendedImages.containsKey(imagePath)) {
                try {
                    String url = topicObject.project.getHttpUploadPhoto();
                    RequestParams params = new RequestParams();
                    params.put("dir", 0);
                    File fileImage = new File(imagePath);
                    if (!Global.isGifByFile(fileImage)) {
                        Uri uri = Uri.parse(imagePath);
                        fileImage = new PhotoOperate(this).scal(uri);
                    }

                    params.put("file", fileImage);
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
        for (ImageInfo item : photos) {
            send += mSendedImages.get(item.path);
        }
        sendComment(send);
    }

    private void sendComment(String send) {
        if (urlCommentSend.equals(HOST_COMMENT_SEND)) {
            return;
        }

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

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_TOPIC_COMMENTS)) {
            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mData.clear();
                }

                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    TopicObject commnet = new TopicObject(jsonArray.getJSONObject(i));
                    mData.add(commnet);
                }
            } else {
                showErrorMsg(code, respanse);
            }
            baseAdapter.notifyDataSetChanged();
            mFootUpdate.updateState(code, isLoadingLastPage(tag), mData.size());

        } else if (tag.equals(urlCommentSend)) {
            showProgressBar(false);
            if (code == 0) {
                umengEvent(UmengEvent.TOPIC, "新建讨论评论");

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
                umengEvent(UmengEvent.TOPIC, "删除讨论评论");
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
                umengEvent(UmengEvent.TOPIC, "删除讨论");
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
        } else if (URI_DELETE_TOPIC_LABEL.equals(tag)) {
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "删除标签");

                labelBar.removeLabel(currentLabelId);
                if (topicObject.labels != null) {
                    for (TopicLabelObject item : topicObject.labels) {
                        if (item.id == currentLabelId) {
                            topicObject.labels.remove(item);
                            break;
                        }
                    }
                }
                mResultData.putExtra("topic", topicObject);
            } else {
                currentLabelId = -1;
                showErrorMsg(code, respanse);
                showProgressBar(false);
            }
        }
    }

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
}
