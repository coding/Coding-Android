package net.coding.program.project.detail.topic;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Spanned;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.coding.program.CodingGlobal;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.LoadMore;
import net.coding.program.common.StartActivity;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.enter.EnterLayout;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.TopicLabelObject;
import net.coding.program.common.model.TopicObject;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.model.request.Project;
import net.coding.program.common.model.topic.TopicComment;
import net.coding.program.common.model.topic.TopicCommentChild;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.maopao.BaseUsersArea;
import net.coding.program.maopao.item.ImageCommentHolder;
import net.coding.program.network.HttpObserverRaw;
import net.coding.program.network.Network;
import net.coding.program.network.model.HttpResult;
import net.coding.program.project.detail.TopicAddActivity_;
import net.coding.program.project.detail.TopicLabelActivity;
import net.coding.program.project.detail.TopicLabelActivity_;
import net.coding.program.project.detail.TopicLabelBar;
import net.coding.program.util.TextWatcherAt;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_topic_list_detail)
public class TopicListDetailActivity extends BaseTopicListDetailActivity implements StartActivity, SwipeRefreshLayout.OnRefreshListener, LoadMore {

    static final int RESULT_EDIT = 2;
    static final int RESULT_LABEL = 3;
    static final int RESULT_MODIFY_WATCHER = 4;

    static final String TAG_DELETE_TOPIC = "TAG_DELETE_TOPIC";
    static final String TAG_TOPIC_COMMENTS = "TAG_TOPIC_COMMENTS";

    @InstanceState
    protected boolean saveTopicWhenLoaded;

    @Extra
    TopicDetailParam mJumpParam;
    //    EnterLayout mEnterLayout;
    String owerGlobar = "";
    String URI_DELETE_TOPIC_LABEL = Global.HOST_API + "/topic/%s/label/%s";
    String urlTopic = "";
    ArrayList<TopicComment> mData = new ArrayList<>();
    View mListHead;
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
                holder = new ViewHolder(convertView, onClickComment, myImageGetter, getImageLoad(), GlobalCommon.mOnClickUser, onClickImage);
                convertView.setTag(R.id.layout, holder);
            } else {
                holder = (ImageCommentHolder) convertView.getTag(R.id.layout);
            }

            TopicComment data = (TopicComment) getItem(position);
            holder.setContent(data);

            loadMore();

            return convertView;
        }
    };
    private WatchHelp watchHelp;
    private CommentHelp commentHelp;
    private TopicLabelBar labelBar;
    private int currentLabelId;
    private TextView textViewCommentCount;
    private ArrayList<UserObject> watchers = new ArrayList<>(0);
    View.OnClickListener clickAddWatch = v -> {
        WatcherListActivity_.intent(v.getContext())
                .mProjectObjectId(topicObject.project_id)
                .topicId(topicObject.id)
                .watchers(watchers)
                .startForResult(RESULT_MODIFY_WATCHER);
    };

    @AfterViews
    protected final void initTopicListDetailActivity() {
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.font_green);
        mFootUpdate.init(listView, mInflater, this);

        showDialogLoading();
        loadData();
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
            Network.getRetrofit(this)
                    .getProject(mJumpParam.mUser, mJumpParam.mProject)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new HttpObserverRaw<HttpResult<ProjectObject>>(this) {
                        @Override
                        public void onSuccess(HttpResult<ProjectObject> data) {
                            super.onSuccess(data);

                            projectObject = data.data;
                            urlTopic = String.format(Global.HOST_API + "/topic/%s?", mJumpParam.mTopic);
                            getNetwork(urlTopic, urlTopic);
                        }

                        @Override
                        public void onFail(int errorCode, @NonNull String error) {
                            super.onFail(errorCode, error);
                        }
                    });
        } else if (topicObject != null) {
            owerGlobar = topicObject.owner.global_key;
            setActionBarTitle(projectObject.name);

            urlTopic = String.format(Global.HOST_API + "/topic/%s?", topicObject.id);
            getNetwork(urlTopic, urlTopic);
        } else {
            finish();
        }
    }

    private void initData() {
        setActionBarTitle(projectObject.name);
        updateHeadData();
        if (saveTopicWhenLoaded) {
            saveTopicWhenLoaded = false;
            mResultData.putExtra("topic", topicObject);
        }

        urlCommentSend = String.format(Global.HOST_API + "/project/%s/topic/%s/comment", topicObject.project_id, topicObject.id);

        loadMore();
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

    @OnActivityResult(RESULT_COMMENT)
    void onResultComment(int result, @OnActivityResult.Extra TopicComment topicComment) {
        if (topicComment != null) {
            if (result == RESULT_OK) {
                for (int i = 0; i < mData.size(); ++i) {
                    if (mData.get(i).id == topicComment.id) {
                        mData.set(i, topicComment);
                        break;
                    }
                }
            } else if (result == RESULT_CANCELED) {  // 表示删除
                for (int i = 0; i < mData.size(); ++i) {
                    if (mData.get(i).id == topicComment.id) {
                        mData.remove(i);
                        break;
                    }
                }
            }

            baseAdapter.notifyDataSetChanged();
        }
    }

    @OnActivityResult(RESULT_MODIFY_WATCHER)
    void onResultModifyWatcher(int code, @OnActivityResult.Extra ArrayList<UserObject> resultData) {
        if (code == RESULT_OK) {
            watchers = resultData;
            watchHelp.setData(this.watchers);
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
        TopicAddActivity_.intent(this).projectObject(projectObject).topicObject(topicObject).startForResult(RESULT_EDIT);
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
        showDialog("删除讨论?", (dialog, which) -> deleteNetwork(String.format(Global.HOST_API + "/topic/%s", topicObject.id), TAG_DELETE_TOPIC));
    }

    @OptionsItem
    protected final void action_copy() {
//        final String urlTemplate = Global.HOST + "/u/%s/p/%s/topic/%d";
//        String url = String.format(urlTemplate, topicObject.project.owner_user_name, topicObject.project.name, topicObject.id);
//        Global.copy(this, url);
//        showButtomToast("已复制 " + url);
    }

    void updateDisplayCommentCount() {
        String commentCount = String.format("%s 条回答", topicObject.child_count);
        textViewCommentCount.setText(commentCount);
    }

    private void updateHeadData() {
        mEnterComment.getEnterLayout().content.addTextChangedListener(new TextWatcherAt(this, this, RESULT_AT, projectObject));

        if (mListHead == null) {
            mListHead = mInflater.inflate(R.layout.activity_project_topic_comment_list_head, listView, false);
            listView.addHeaderView(mListHead);
            watchHelp = new WatchHelp(mListHead);
            commentHelp = new CommentHelp(mListHead);
        }

        ImageView icon = (ImageView) mListHead.findViewById(R.id.icon);
        iconfromNetwork(icon, topicObject.owner.avatar);
        icon.setTag(topicObject.owner.global_key);
        icon.setOnClickListener(GlobalCommon.mOnClickUser);

        TextView topicTitleTextView = ((TextView) mListHead.findViewById(R.id.title));
        topicTitleTextView.setText(topicObject.title);

        Spanned timeString = Global.createGreenHtml("", topicObject.owner.name, " 发布于" + Global.dayToNow(topicObject.updated_at));
        ((TextView) mListHead.findViewById(R.id.time)).setText(timeString);

        ((TextView) mListHead.findViewById(R.id.referenceId)).setText(topicObject.getRefId());

        updateLabels(topicObject.labels);

        WebView webView = (WebView) mListHead.findViewById(R.id.comment);
        CodingGlobal.setWebViewContent(webView, "topic-android.html", topicObject.content);

        textViewCommentCount = (TextView) mListHead.findViewById(R.id.commentCount);
        updateDisplayCommentCount();

        Spinner spinner = (Spinner) mListHead.findViewById(R.id.spinner);
        spinner.setAdapter(new TopicSortAdapter(this));
        spinner.setSelection(0, true); // 一定要写，否则会自动调用一次 onItemSelected
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int defaultPostion = TopicObject.SORT_DEFAULT;
                if (position == 0) {
                    defaultPostion = TopicObject.SORT_OLD;
                } else if (position == 1) {
                    defaultPostion = TopicObject.SORT_NEW;
                }
                topicObject.setSortOld(defaultPostion);

                initSetting();
                loadMore();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mListHead.setOnClickListener(v -> {
            prepareComment();
            mEnterComment.getEnterLayout().popKeyboard();
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
                        .projectPath(projectObject.getProjectPath())
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

    private void updateWatchFromNetwork() {
        if (topicObject == null) {
            return;
        }

        Project.topicWatchList(this, topicObject.project_id, topicObject.id, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                watchers.clear();

                JSONArray jsonArray = response.optJSONObject("data").optJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    UserObject user = new UserObject(jsonArray.optJSONObject(i));
                    watchers.add(user);
                }
                watchHelp.setData(watchers);
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
            }
        });
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_TOPIC_COMMENTS)) {
            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mData.clear();
                }

                JSONArray jsonArray = respanse.optJSONObject("data").optJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    TopicComment commnet = new TopicComment(jsonArray.optJSONObject(i));
                    mData.add(commnet);
                }

            } else {
                showErrorMsg(code, respanse);
            }
            commentHelp.update();
            baseAdapter.notifyDataSetChanged();
            mFootUpdate.updateState(code, isLoadingLastPage(tag), mData.size());

        } else if (tag.equals(TAG_DELETE_TOPIC)) {
            if (code == 0) {
                umengEvent(UmengEvent.TOPIC, "删除讨论");
                mResultData.putExtra("id", topicObject.id);
                setResult(RESULT_OK, mResultData);
                finish();
            } else {
                showButtomToast("删除讨论失败");
            }
        } else if (tag.equals(urlCommentSend)) {
            showProgressBar(false);
            if (code == 0) {
                umengEvent(UmengEvent.TOPIC, "新建讨论评论");

                JSONObject jsonObject = respanse.getJSONObject("data");

                if (data == null || data instanceof TopicObject) {
                    TopicComment newItem = new TopicComment(respanse.optJSONObject("data"));
                    mData.add(newItem);
                    ++topicObject.child_count;
                    mResultData.putExtra("child_count", topicObject.child_count);
                    mResultData.putExtra("topic_id", topicObject.id);
                    updateDisplayCommentCount();
                } else if (data instanceof TopicComment) {
                    TopicComment comment = (TopicComment) data;
                    TopicCommentChild newItem = new TopicCommentChild(respanse.optJSONObject("data"));
                    comment.childcomments.add(newItem);
                    comment.childcount++;
                } else if (data instanceof TopicCommentChild) {
                    TopicCommentChild child = (TopicCommentChild) data;
                    TopicComment comment = null;
                    for (TopicComment item : mData) {
                        if (child.parentid == item.id) {
                            comment = item;
                            break;
                        }
                    }
                    if (comment != null) {
                        TopicCommentChild newItem = new TopicCommentChild(respanse.optJSONObject("data"));
                        comment.childcomments.add(newItem);
                        comment.childcount++;
                    }
                }

                EnterLayout enterLayout = mEnterComment.getEnterLayout();
                enterLayout.restoreDelete(data);
                mEnterComment.clearContent();
                baseAdapter.notifyDataSetChanged();
                showButtomToast("发表成功");
                commentHelp.update();
            } else {
                showErrorMsg(code, respanse);
                baseAdapter.notifyDataSetChanged();
            }
        } else if (tag.equals(urlTopic)) {
            swipeRefreshLayout.setRefreshing(false);
            hideProgressDialog();
            if (code == 0) {
                topicObject = new TopicObject(respanse.getJSONObject("data"));
                initData();
                updateWatchFromNetwork();
                invalidateOptionsMenu();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_DELETE_TOPIC_COMMENT)) {
            if (code == 0) {
                umengEvent(UmengEvent.TOPIC, "删除讨论评论");
                if (data instanceof CommentParam) {
                    CommentParam commentParam = (CommentParam) data;
                    TopicComment topicComment = commentParam.topicComment;
                    topicComment.childcomments.remove(commentParam.child);
                    topicComment.childcount--;
                    baseAdapter.notifyDataSetChanged();
                } else if (data instanceof TopicComment) {
                    TopicComment comment = (TopicComment) data;
                    mData.remove(comment);
                    topicObject.child_count--;
                    updateDisplayCommentCount();
                    baseAdapter.notifyDataSetChanged();
                }
//                for (int i = 0; i < mData.size(); ++i) {
//                    if (itemId == mData.get(i).id) {
//                        mData.remove(i);
//                        --topicObject.child_count;
//                        mResultData.putExtra("child_count", topicObject.child_count);
//                        mResultData.putExtra("topic_id", topicObject.id);
//                        updateDisplayCommentCount();
//                        baseAdapter.notifyDataSetChanged();
//                        break;
//                    }
//                }

                commentHelp.update();
            } else {
                showButtomToast(R.string.delete_fail);
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
        } else if (TAG_TOPIC_COMMENT_VOTE.equals(tag)) {
            if (code == 0) {
                showMiddleToast("+1 成功");
                TopicComment comment = (TopicComment) data;
                TopicComment newItem = new TopicComment(respanse.optJSONObject("data"));
                comment.upVoteUsers = newItem.upVoteUsers;
                comment.upvotecounts = newItem.upvotecounts;
                baseAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (TAG_DELETE_TOPIC_COMMENT_VOTE.equals(tag)) {
            if (code == 0) {
                showMiddleToast("取消 +1 成功");
                VoteParam param = (VoteParam) data;
                param.topicComment.upVoteUsers.remove(param.topicCommentChild);
                param.topicComment.upvotecounts--;
                baseAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
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

    private class WatchHelp {
        View emptyWatchLayout;
        View watchUsersLayout;

        TextView watchCount;
        LinearLayout watchUsers;
        BaseUsersArea userArea;

        public WatchHelp(View headView) {
            emptyWatchLayout = (View) headView.findViewById(R.id.emptyWatchLayout);
            TextView emptyWatchAdd = (TextView) headView.findViewById(R.id.emptyWatchAdd);
            emptyWatchAdd.setText(Global.createGreenHtml("尚未添加任何关注者, ", "去添加", ""));

            emptyWatchAdd.setOnClickListener(clickAddWatch);

            watchUsersLayout = (View) headView.findViewById(R.id.watchUsersLayout);
            watchCount = (TextView) headView.findViewById(R.id.watchCount);
            View watchListAdd = (View) headView.findViewById(R.id.watchListAdd);
            watchListAdd.setOnClickListener(clickAddWatch);
            watchUsers = (LinearLayout) headView.findViewById(R.id.watchUsers);

            userArea = new BaseUsersArea(watchUsers, null, TopicListDetailActivity.this, GlobalCommon.mOnClickUser, getImageLoad());
        }

        public void setData(List<UserObject> watches) {
            if (watches.isEmpty()) {
                emptyWatchLayout.setVisibility(View.VISIBLE);
                watchUsersLayout.setVisibility(View.GONE);
            } else {
                emptyWatchLayout.setVisibility(View.GONE);
                watchUsersLayout.setVisibility(View.VISIBLE);

                watchCount.setText(String.format("%s 人关注", watches.size()));
                watchUsers.setTag(watches);
                userArea.displayLikeUser();
            }
        }
    }

    private class CommentHelp {
        View commentButton;
        View commentSort;

        public CommentHelp(View v) {
            commentButton = v.findViewById(R.id.topicCommentButton);
            commentSort = v.findViewById(R.id.spinner);
            commentButton.setOnClickListener(v1 -> {
                prepareComment();
                mEnterComment.getEnterLayout().popKeyboard();
            });
        }

        public void update() {
            if (mData.isEmpty()) {
                commentButton.setVisibility(View.VISIBLE);
                commentSort.setVisibility(View.GONE);
            } else {
                commentButton.setVisibility(View.GONE);
                commentSort.setVisibility(View.VISIBLE);
            }
        }
    }
}
