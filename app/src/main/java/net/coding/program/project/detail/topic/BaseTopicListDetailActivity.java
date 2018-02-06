package net.coding.program.project.detail.topic;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ImageInfo;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.enter.EnterLayout;
import net.coding.program.common.enter.ImageCommentLayout;
import net.coding.program.common.model.BaseComment;
import net.coding.program.common.model.DynamicObject;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.TopicObject;
import net.coding.program.common.model.topic.TopicComment;
import net.coding.program.common.model.topic.TopicCommentChild;
import net.coding.program.common.ui.CodingToolbarBackActivity;
import net.coding.program.maopao.item.ImageCommentHolder;
import net.coding.program.pickphoto.ClickSmallImage;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chenchao on 16/9/9.
 * TopicListDetailActivity 和 TopicCommentDetail
 * 公用一些代码
 */

@EActivity(R.layout.activity_topic_list_detail)
public abstract class BaseTopicListDetailActivity extends CodingToolbarBackActivity {

    static final int RESULT_AT = 1;
    static final int RESULT_COMMENT = 5;

    static final String TAG_DELETE_TOPIC_COMMENT = "TAG_DELETE_TOPIC_COMMENT";
    static final String TAG_DELETE_TOPIC_COMMENT_VOTE = "TAG_DELETE_TOPIC_COMMENT_VOTE";
    static final String TAG_TOPIC_COMMENT_VOTE = "TAG_TOPIC_COMMENT_VOTE";

    protected ClickSmallImage onClickImage = new ClickSmallImage(this);
    MyImageGetter myImageGetter = new MyImageGetter(this);
    HashMap<String, String> mSendedImages = new HashMap<>();
    @Extra
    TopicObject topicObject;

    @Extra
    ProjectObject projectObject;

    @ViewById
    ListView listView;
    @ViewById
    SwipeRefreshLayout swipeRefreshLayout;
    ImageCommentLayout mEnterComment;
    View.OnClickListener onClickComment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            BaseComment comment = (BaseComment) tag;

            if (comment.isMy()) {
                String item1 = "回复" + comment.owner.name;
                new AlertDialog.Builder(v.getContext(), R.style.MyAlertDialogStyle)
                        .setItems(new String[]{item1, "删除"}, (dialog, which) -> {
                            if (which == 1) {
                                showOptionDialog(comment, tag, v);
                            } else {
                                replyComment(v, tag, comment);
                            }
                        })
                        .show();
            } else {
                replyComment(v, tag, comment);
            }
        }

        void showOptionDialog(final BaseComment comment, final Object tag, final View v) {
            new AlertDialog.Builder(v.getContext(), R.style.MyAlertDialogStyle)
                    .setTitle("删除评论")
                    .setPositiveButton("确定", (dialog, which) -> {
                        String url = String.format(Global.HOST_API + "/project/%s/topic/%s/comment/%s", topicObject.project_id, topicObject.id, comment.id);
                        if (tag instanceof TopicComment) {
                            deleteNetwork(url, TAG_DELETE_TOPIC_COMMENT, tag);
                        } else if (tag instanceof TopicCommentChild) {
                            TopicComment topicComment = (TopicComment) v.getTag(R.layout.topic_comment_child);
                            CommentParam param = new CommentParam(topicComment, (TopicCommentChild) tag);
                            deleteNetwork(url, TAG_DELETE_TOPIC_COMMENT, param);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }

        private void replyComment(View v, Object tag, BaseComment comment) {
            EnterLayout enterLayout = mEnterComment.getEnterLayout();
            EditText message = enterLayout.content;
            message.setHint("回复 " + comment.owner.name);
            enterLayout.popKeyboard();

            if (tag instanceof TopicComment) {
                message.setTag(comment);
            } else if (tag instanceof TopicCommentChild) {
                message.setTag(comment);
                TopicComment topicComment = (TopicComment) v.getTag(R.layout.topic_comment_child);
                message.setTag(R.layout.topic_comment_child, topicComment);
                enterLayout.restoreLoad(comment);
            }
        }
    };
    String tagUrlCommentPhoto = "";
    Intent mResultData = new Intent();
    String urlCommentSend = "";
    View.OnClickListener mOnClickSend = v -> sendCommentAll();
    View.OnClickListener clickVoteButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TopicComment comment = (TopicComment) v.getTag();
            ArrayList<DynamicObject.Owner> users = comment.upVoteUsers;
            DynamicObject.Owner voteOwner = findVoteOwnerWithMe(users);

            String host = String.format("%s/project/%s/topic/%s/comment/%s/upvote", Global.HOST_API,
                    topicObject.project_id, topicObject.id, comment.id);
            if (voteOwner == null) {
                postNetwork(host, new RequestParams(), TAG_TOPIC_COMMENT_VOTE, -1, comment);
            } else {
                deleteNetwork(host, TAG_DELETE_TOPIC_COMMENT_VOTE, new VoteParam(comment, voteOwner));
            }
        }
    };

    @AfterViews
    void initBaseTopicListDetailActivity() {
        mEnterComment = new ImageCommentLayout(this, mOnClickSend, getImageLoad());
        prepareComment();
    }

    @Nullable
    @Override
    protected ProjectObject getProject() {
        return projectObject;
    }

    protected void prepareComment() {
        EditText message = mEnterComment.getEnterLayout().content;
        message.setHint("发表看法");
        message.setTag(topicObject);

        mEnterComment.getEnterLayout().restoreLoad(topicObject);
    }

    @OnActivityResult(RESULT_AT)
    void onResultAt(int requestCode, Intent data) {
        if (requestCode == Activity.RESULT_OK) {
            String name = data.getStringExtra("name");
            mEnterComment.getEnterLayout().insertText(name);
            mEnterComment.getEnterLayout().popKeyboard();
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

    private void sendCommentAll() {
        showProgressBar(true);

        ArrayList<ImageInfo> photos = mEnterComment.getPickPhotos();
        for (ImageInfo item : photos) {
            String imagePath = item.path;
            if (!mSendedImages.containsKey(imagePath)) {
                try {
                    String url = ProjectObject.getPublicTopicUploadPhoto(topicObject.project_id);
                    RequestParams params = new RequestParams();
                    params.put("dir", 0);
                    File fileImage = new File(imagePath);
                    if (!Global.isGifByFile(fileImage)) {
                        Uri uri = Uri.parse(imagePath);
                        fileImage = new PhotoOperate(this).getFile(uri);
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

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(tagUrlCommentPhoto)) {
            if (code == 0) {
                String fileUri;
//                if (topicObject.project.isPublic()) {
                // 现在只有公开项目可以看 topic
                fileUri = respanse.optString("data", "");
//                } else {
//                    AttachmentFileObject fileObject = new AttachmentFileObject(respanse.optJSONObject("data"));
//                    fileUri = fileObject.owner_preview;
//                }
                String mdPhotoUri = String.format("\n![图片](%s)", fileUri);
                mSendedImages.put((String) data, mdPhotoUri);
                sendCommentAll();
            } else {
                showErrorMsg(code, respanse);
                showProgressBar(false);
            }
        }
    }

    private void sendComment(String send) {
        if (TextUtils.isEmpty(urlCommentSend)) {
            return;
        }

        String input = send;
        if (EmojiFilter.containsEmptyEmoji(this, input)) {
            showProgressBar(false);
            return;
        }

        RequestParams params = new RequestParams();
        Object object = mEnterComment.getEnterLayout().content.getTag();

        int type = 0;
        int parentId = 0;
        if (object instanceof TopicComment) {
            type = 1;
            parentId = ((TopicComment) object).id;
        } else if (object instanceof TopicCommentChild) {
            type = 1;
            TopicCommentChild child = (TopicCommentChild) object;
            parentId = child.parentid;
            input = Global.encodeInput(child.owner.name, input);
        } else {

        }

        params.put("content", input);
        params.put("type", type);
        if (parentId != 0) {
            params.put("parent_id", parentId);
        }

        postNetwork(urlCommentSend, params, urlCommentSend, 0, object);

        showProgressBar(R.string.sending_comment);
    }

    @Nullable
    public DynamicObject.Owner findVoteOwnerWithMe(ArrayList<DynamicObject.Owner> users) {
        DynamicObject.Owner voteOwner = null;
        for (DynamicObject.Owner item : users) {
            if (item.global_key.equals(GlobalData.sUserObject.global_key)) {
                voteOwner = item;
                break;
            }
        }
        return voteOwner;
    }

    protected static class CommentParam {
        TopicComment topicComment;
        TopicCommentChild child;

        public CommentParam(TopicComment topicComment, TopicCommentChild topicCommentChild) {
            this.topicComment = topicComment;
            this.child = topicCommentChild;
        }
    }

    protected static class VoteParam {
        TopicComment topicComment;
        DynamicObject.Owner topicCommentChild;

        public VoteParam(TopicComment topicComment, DynamicObject.Owner topicCommentChild) {
            this.topicComment = topicComment;
            this.topicCommentChild = topicCommentChild;
        }
    }

    class ViewHolder extends ImageCommentHolder {
        View recommendView;
        TextView voteView;

        //        View childCommentTopLine;


        ChildHolder[] childHolders;


        TextView moreChildComment;

        public ViewHolder(View convertView, View.OnClickListener onClickComment, Html.ImageGetter imageGetter, ImageLoadTool imageLoadTool, View.OnClickListener clickUser, View.OnClickListener clickImage) {
            super(convertView, onClickComment, imageGetter, imageLoadTool, clickUser, clickImage);

            recommendView = convertView.findViewById(R.id.recommendLayout);
            voteView = (TextView) convertView.findViewById(R.id.vote);
            voteView.setOnClickListener(clickVoteButton);

//            childCommentTopLine = convertView.findViewById(R.id.childCommentTopLine);
            ChildHolder childHolder0 = new ChildHolder(convertView.findViewById(R.id.child0), R.id.child0, onClickComment, imageGetter, imageLoadTool, clickUser, clickImage);
            ChildHolder childHolder1 = new ChildHolder(convertView.findViewById(R.id.child1), R.id.child1, onClickComment, imageGetter, imageLoadTool, clickUser, clickImage);
            ChildHolder childHolder2 = new ChildHolder(convertView.findViewById(R.id.child2), R.id.child2, onClickComment, imageGetter, imageLoadTool, clickUser, clickImage);
            ChildHolder childHolder3 = new ChildHolder(convertView.findViewById(R.id.child3), R.id.child3, onClickComment, imageGetter, imageLoadTool, clickUser, clickImage);
            ChildHolder childHolder4 = new ChildHolder(convertView.findViewById(R.id.child4), R.id.child4, onClickComment, imageGetter, imageLoadTool, clickUser, clickImage);
            ChildHolder childHolder5 = new ChildHolder(convertView.findViewById(R.id.child5), R.id.child5, onClickComment, imageGetter, imageLoadTool, clickUser, clickImage);
            childHolders = new ChildHolder[]{
                    childHolder0,
                    childHolder1,
                    childHolder2,
                    childHolder3,
                    childHolder4,
                    childHolder5,
            };

            moreChildComment = (TextView) convertView.findViewById(R.id.moreChildComment);
        }

        @Override
        public void setContent(Object data) {
            super.setContent(data);

            TopicComment comment = (TopicComment) data;
            recommendView.setVisibility(comment.isRecommend() ? View.VISIBLE : View.GONE);
            voteView.setText(String.format("+%s", comment.upvotecounts));
            if (findVoteOwnerWithMe(comment.upVoteUsers) == null) {
                voteView.setBackgroundResource(R.drawable.shape_vote_no);
                voteView.setTextColor(CodingColor.font3);
            } else {
                voteView.setBackgroundResource(R.drawable.shape_vote);
                voteView.setTextColor(CodingColor.fontWhite);
            }
            voteView.setTag(comment);

            ArrayList<TopicCommentChild> childcomments = ((TopicComment) data).childcomments;
            if (childcomments.size() > 0) {
                int i = 0;
                for (; i < childHolders.length && i < childcomments.size(); ++i) {
                    childHolders[i].setContent(childcomments.get(i), comment);
                    childHolders[i].show(true);
                }

                for (; i < childHolders.length; ++i) {
                    childHolders[i].show(false);
                }
            } else {
                hideAllChildren();
            }

            if (comment.childcount > comment.childcomments.size()
                    || comment.childcomments.size() > childHolders.length) {
                moreChildComment.setVisibility(View.VISIBLE);
                moreChildComment.setText(String.format("查看全部%s条评论", comment.childcount));
                moreChildComment.setOnClickListener(v -> {
                    TopicCommentDetail_.intent(v.getContext())
                            .topicObject(topicObject)
                            .projectObject(projectObject)
                            .topicComment(comment)
                            .startForResult(RESULT_COMMENT);
                });
            } else {
                moreChildComment.setVisibility(View.GONE);
            }
        }

        protected void hideAllChildren() {
            for (ChildHolder item : childHolders) {
                item.show(false);
            }
        }
    }

    class ChildHolder extends ImageCommentHolder {

        //        TextView moreChildComment;
        View rootLayout;

        public ChildHolder(View convertView, int rootLayoutId, View.OnClickListener onClickComment, Html.ImageGetter imageGetter, ImageLoadTool imageLoadTool, View.OnClickListener clickUser, View.OnClickListener clickImage) {
            super(convertView, rootLayoutId, onClickComment, imageGetter, imageLoadTool, clickUser, clickImage);
//            moreChildComment = (TextView) convertView.findViewById(moreChildComment);
            rootLayout = convertView;

//            if (rootLayoutId == R.id.child0) { // 第一个子评论
//                moreChildComment.setVisibility(View.GONE);
//            } else if (rootLayoutId == R.id.child1) { // 最后一个子评论
//                rootLayout.findViewById(R.id.bottomLine).setVisibility(View.INVISIBLE);
//            }
        }

        @Override
        public void setContent(Object data) {
            show(true);
            super.setContent(data);
        }

        public void setContent(TopicCommentChild child, TopicComment comment) {
            setContent(child);
            rootLayout.setTag(R.layout.topic_comment_child, comment);
        }

        void show(boolean show) {
            rootLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        }

    }
}
