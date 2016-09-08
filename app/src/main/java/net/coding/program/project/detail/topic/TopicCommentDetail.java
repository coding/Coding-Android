package net.coding.program.project.detail.topic;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.enter.EnterLayout;
import net.coding.program.common.enter.ImageCommentLayout;
import net.coding.program.common.photopick.ImageInfo;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.maopao.item.ImageCommentHolder;
import net.coding.program.model.TopicObject;
import net.coding.program.model.topic.TopicCommentChild;
import net.coding.program.third.EmojiFilter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

@EActivity(R.layout.activity_topic_comment_detail)
public class TopicCommentDetail extends BackActivity {

    @Extra
    TopicObject topicObject;

    @ViewById
    ListView listView;

    final int RESULT_AT = 1;

    final String TAG_DELETE_TOPIC_COMMENT = "TAG_DELETE_TOPIC_COMMENT";

    HashMap<String, String> mSendedImages = new HashMap<>();
    ImageCommentLayout mEnterComment;
    ArrayList<TopicCommentChild> data = new ArrayList<>();

    View.OnClickListener mOnClickSend = v -> sendCommentAll();

    private final ClickSmallImage onClickImage = new ClickSmallImage(this);
    @AfterViews
    void initTopicCommentDetail() {

        mEnterComment = new ImageCommentLayout(this, mOnClickSend, getImageLoad());


        prepareComment();
    }

    private void prepareComment() {
        EditText message = mEnterComment.getEnterLayout().content;
        message.setHint("发表评论");
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

    View.OnClickListener onClickComment = v -> {
        final TopicObject comment = (TopicObject) v.getTag();

        if (comment.isMy()) {
            new AlertDialog.Builder(TopicCommentDetail.this)
                    .setTitle("删除评论")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // // TODO: 16/9/8 删除评论
//                            String url = String.format(Global.HOST_API + "/project/%s/topic/%s/comment/%s", topicObject.project.getId(), topicObject.id, comment.id);
//                            deleteNetwork(url, TAG_DELETE_TOPIC_COMMENT, comment.id);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();

        } else {
            EnterLayout enterLayout = mEnterComment.getEnterLayout();
            EditText message = enterLayout.content;
            message.setHint("回复 " + comment.owner.name);

            message.setTag(comment);
            enterLayout.popKeyboard();

            enterLayout.restoreLoad(comment);
        }
    };
    MyImageGetter myImageGetter = new MyImageGetter(this);

    BaseAdapter baseAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);

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
                holder = new ViewHolder(convertView, onClickComment, myImageGetter, getImageLoad(), mOnClickUser, onClickImage);
                convertView.setTag(R.id.layout, holder);
            } else {
                holder = (ImageCommentHolder) convertView.getTag(R.id.layout);
            }

            TopicCommentChild data = (TopicCommentChild) getItem(position);
            holder.setContent(data);


            return convertView;
        }


        class ViewHolder extends ImageCommentHolder {

            TextView moreChildComment;
            View rootLayout;

            public ViewHolder(View convertView, View.OnClickListener onClickComment, Html.ImageGetter imageGetter, ImageLoadTool imageLoadTool, View.OnClickListener clickUser, View.OnClickListener clickImage) {
                super(convertView, onClickComment, imageGetter, imageLoadTool, clickUser, clickImage);
                moreChildComment = (TextView) convertView.findViewById(R.id.moreChildComment);
                rootLayout = convertView;

//                if (rootLayoutId == R.id.child0) { // 第一个子评论
//                    moreChildComment.setVisibility(View.GONE);
//                } else if (rootLayoutId == R.id.child1) { // 最后一个子评论
//                    rootLayout.findViewById(R.id.bottomLine).setVisibility(View.INVISIBLE);
//                }
            }

            @Override
            public void setContent(Object data) {
                show(true);
                super.setContent(data);
            }

            void show(boolean show) {
                rootLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            }

//            void showMoreChildButton(int count) {
//                if (count > 2) {
//                    moreChildComment.setVisibility(View.VISIBLE);
//                    moreChildComment.setText(String.format("查看全部%s条评论", count));
//                    moreChildComment.setOnClickListener(v -> {
//                        // dd
//                    });
//                } else {
//                    moreChildComment.setVisibility(View.GONE);
//                }
//            }
        }
    };
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

    String tagUrlCommentPhoto = "";
    private final String HOST_COMMENT_SEND = Global.HOST_API + "/project/%s/topic?parent=%s";

    String urlCommentSend = HOST_COMMENT_SEND;

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
}
