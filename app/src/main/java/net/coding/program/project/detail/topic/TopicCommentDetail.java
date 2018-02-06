package net.coding.program.project.detail.topic;

import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.enter.EnterLayout;
import net.coding.program.common.enter.ImageCommentLayout;
import net.coding.program.common.model.TopicObject;
import net.coding.program.common.model.topic.TopicComment;
import net.coding.program.common.model.topic.TopicCommentChild;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.maopao.item.ImageCommentHolder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EActivity(R.layout.activity_topic_comment_detail)
public class TopicCommentDetail extends BaseTopicListDetailActivity {

    private static final String TAG_ALL_COMMENTS = "TAG_ALL_COMMENTS";
    @Extra
    TopicComment topicComment;
    ImageCommentLayout mEnterComment;
    ArrayList<TopicCommentChild> listData;

    ViewHolder holder;
    BaseAdapter baseAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageCommentHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.topic_comment_child, parent, false);
                holder = new ViewHolder(convertView, onClickComment, myImageGetter, getImageLoad(), GlobalCommon.mOnClickUser, onClickImage);
                convertView.setTag(R.id.layout, holder);
            } else {
                holder = (ImageCommentHolder) convertView.getTag(R.id.layout);
            }

            TopicCommentChild data = (TopicCommentChild) getItem(position);
            holder.setContent(data);


            return convertView;
        }


        class ViewHolder extends ImageCommentHolder {

            View rootLayout;

            public ViewHolder(View convertView, View.OnClickListener onClickComment, Html.ImageGetter imageGetter, ImageLoadTool imageLoadTool, View.OnClickListener clickUser, View.OnClickListener clickImage) {
                super(convertView, onClickComment, imageGetter, imageLoadTool, clickUser, clickImage);
                rootLayout = convertView;
            }

            @Override
            public void setContent(Object data) {
                show(true);
                super.setContent(data);
            }

            void show(boolean show) {
                rootLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            }

        }
    };
    int resultCode = RESULT_OK;

    @AfterViews
    void initTopicCommentDetail() {
        listViewAddHeaderSection(listView);

        urlCommentSend = String.format(Global.HOST_API + "/project/%s/topic/%s/comment", topicObject.project_id, topicObject.id);

        listData = topicComment.childcomments;
        mEnterComment = new ImageCommentLayout(this, mOnClickSend, getImageLoad());
        prepareComment();
        initList();
        initInput();

        loadAll();
    }

    private void initList() {
        View convertView = getLayoutInflater().inflate(R.layout.activity_task_comment_much_image_divide, listView, false);
        convertView.findViewById(R.id.moreChildComment).setVisibility(View.GONE);
        convertView.findViewById(R.id.bottomDivideLine).setVisibility(View.GONE);
        swipeRefreshLayout.setEnabled(false);

        holder = new ViewHolder(convertView, onClickComment, myImageGetter, getImageLoad(), GlobalCommon.mOnClickUser, onClickImage) {

            @Override
            public void setContent(Object data) {
                super.setContent(data);
                hideAllChildren();
                moreChildComment.setVisibility(View.GONE);
            }
        };
        holder.moreChildComment.setVisibility(View.GONE);
        convertView.setTag(R.id.layout, holder);
        updateHeader();

        listView.addHeaderView(convertView, null, false);
        listView.setAdapter(baseAdapter);

    }

    private void initInput() {
        EnterLayout enterLayout = mEnterComment.getEnterLayout();
        EditText message = enterLayout.content;
        message.setHint("回复 " + topicComment.owner.name);
        message.setTag(topicComment);
    }

    private void updateHeader() {
        holder.setContent(topicComment);
    }

    private void loadAll() {
        String url = topicComment.getUrlAllComment(topicObject.project_id);
        getNetwork(url, TAG_ALL_COMMENTS);
    }

    @OnActivityResult(RESULT_AT)
    void onResultAt(int requestCode, Intent data) {
        if (requestCode == Activity.RESULT_OK) {
            String name = data.getStringExtra("name");
            mEnterComment.getEnterLayout().insertText(name);
            mEnterComment.getEnterLayout().popKeyboard();
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_ALL_COMMENTS)) {
            ArrayList<TopicCommentChild> children = topicComment.childcomments;
            children.clear();
            JSONArray jsonArray = respanse.optJSONObject("data").optJSONArray("list");
            for (int i = 0; i < jsonArray.length(); ++i) {
                children.add(new TopicCommentChild(jsonArray.optJSONObject(i)));
            }
            topicComment.childcount = children.size();
            baseAdapter.notifyDataSetChanged();

        } else if (TAG_TOPIC_COMMENT_VOTE.equals(tag)) {
            if (code == 0) {
                showMiddleToast("+1 成功");
                TopicComment comment = topicComment;
                TopicComment newItem = new TopicComment(respanse.optJSONObject("data"));
                comment.upVoteUsers = newItem.upVoteUsers;
                comment.upvotecounts = newItem.upvotecounts;
                baseAdapter.notifyDataSetChanged();
                updateHeader();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (TAG_DELETE_TOPIC_COMMENT_VOTE.equals(tag)) {
            if (code == 0) {
                showMiddleToast("取消 +1 成功");
                VoteParam param = (VoteParam) data;
                param.topicComment.upVoteUsers.remove(param.topicCommentChild);
                param.topicComment.upvotecounts--;
                updateHeader();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (TAG_DELETE_TOPIC_COMMENT.equals(tag)) {
            if (code == 0) {
                umengEvent(UmengEvent.TOPIC, "删除讨论评论");

                if (data instanceof CommentParam) {
                    CommentParam commentParam = (CommentParam) data;
                    TopicComment topicComment = commentParam.topicComment;
                    topicComment.childcomments.remove(commentParam.child);
                    topicComment.childcount--;
                    baseAdapter.notifyDataSetChanged();
                } else if (data instanceof TopicComment) {
                    resultCode = RESULT_CANCELED;
                    finish();
                }
            } else {
                showButtomToast(R.string.delete_fail);
            }
        } else if (tag.equals(urlCommentSend)) {
            showProgressBar(false);
            if (code == 0) {
                umengEvent(UmengEvent.TOPIC, "新建讨论评论");

                if (data == null || data instanceof TopicObject) {
                    // 不可能
                } else if (data instanceof TopicComment) {
                    TopicComment comment = (TopicComment) data;
                    TopicCommentChild newItem = new TopicCommentChild(respanse.optJSONObject("data"));
                    comment.childcomments.add(newItem);
                    comment.childcount++;
                } else if (data instanceof TopicCommentChild) {
                    TopicComment comment = topicComment;
                    TopicCommentChild newItem = new TopicCommentChild(respanse.optJSONObject("data"));
                    comment.childcomments.add(newItem);
                    comment.childcount++;
                }

                EnterLayout enterLayout = mEnterComment.getEnterLayout();
                enterLayout.restoreDelete(data);
                mEnterComment.clearContent();
                baseAdapter.notifyDataSetChanged();

                showButtomToast("发表成功");
                baseAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
                baseAdapter.notifyDataSetChanged();
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("topicComment", topicComment);
        setResult(resultCode, intent);
        super.finish();
    }
}
