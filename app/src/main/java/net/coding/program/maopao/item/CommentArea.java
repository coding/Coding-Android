package net.coding.program.maopao.item;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.HtmlContent;
import net.coding.program.maopao.MaopaoListFragment;
import net.coding.program.model.Maopao;

import java.util.ArrayList;

/**
 * Created by chaochen on 14-9-19.
 */
public class CommentArea {

    private static final int[] commentIds = new int[]{
            R.id.comment0,
            R.id.comment1,
            R.id.comment2,
            R.id.comment3,
            R.id.comment4
    };

    private static final int commentMaxCount = commentIds.length;

    private static final int[] commentNames = new int[]{
            R.id.name0,
            R.id.name1,
            R.id.name2,
            R.id.name3,
            R.id.name4
    };

    private static final int[] commentTimes = new int[]{
            R.id.time0,
            R.id.time1,
            R.id.time2,
            R.id.time3,
            R.id.time4
    };

    private static final int[] commentLayouts = new int[]{
            R.id.commentLayout0,
            R.id.commentLayout1,
            R.id.commentLayout2,
            R.id.commentLayout3,
            R.id.commentLayout4,
    };

    public CommentArea(View convertView, View.OnClickListener onClickComment, Html.ImageGetter imageGetterParamer) {
        imageGetter = imageGetterParamer;
        commentLayout = convertView.findViewById(R.id.commentArea);
        comment = new CommentItem[commentIds.length];
        commentMore = convertView.findViewById(R.id.commentMore);
        commentMoreCount = (TextView) convertView.findViewById(R.id.commentMoreCount);

        for (int i = 0; i < commentIds.length; ++i) {
            comment[i] = new CommentItem(convertView, onClickComment, i);
        }
    }

    public void displayContentData(Maopao.MaopaoObject data) {
        ArrayList<Maopao.Comment> commentsData = data.comment_list;
        if (commentsData.isEmpty()) {
            commentLayout.setVisibility(View.GONE);
        } else {
            commentLayout.setVisibility(View.VISIBLE);

            int displayCount = Math.min(commentMaxCount, data.comment_list.size());
            int i = 0;
            for (; i < displayCount; ++i) {
                CommentItem item = comment[i];
                item.setVisibility(View.VISIBLE);
                Maopao.Comment comment = commentsData.get(i);
                item.setContent(comment.owner.name, comment.content, comment.created_at, imageGetter, Global.tagHandler);
                item.layout.setTag(MaopaoListFragment.TAG_COMMENT, comment);
                item.content.setTag(MaopaoListFragment.TAG_COMMENT, comment);
            }

            for (; i < commentMaxCount; ++i) {
                comment[i].setVisibility(View.GONE);
            }

            if ((data.comments > commentMaxCount) || (data.comments > data.comment_list.size())) {
                commentMore.setVisibility(View.VISIBLE);
                commentMoreCount.setText(String.format("查看全部%d条评论", data.comments));

            } else {
                commentMore.setVisibility(View.GONE);
            }
        }
    }

    View commentMore;
    TextView commentMoreCount;

    private View commentLayout;
    private CommentItem comment[];

    Html.ImageGetter imageGetter;

    public static class CommentItem {
        public CommentItem(View convertView, View.OnClickListener onClickComment, int i) {
            layout = convertView.findViewById(commentLayouts[i]);
            layout.setOnClickListener(onClickComment);
            name = (TextView) convertView.findViewById(commentNames[i]);
            time = (TextView) convertView.findViewById(commentTimes[i]);
            content = (TextView) convertView.findViewById(commentIds[i]);
            content.setMovementMethod(LinkMovementMethod.getInstance());
            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((View) (v.getParent())).callOnClick();
                }
            });
        }

        protected TextView content;
        protected TextView name;
        protected TextView time;
        protected View layout;

        public void setVisibility(int visibility) {
            layout.setVisibility(visibility);
        }

        public void setContent(String nameString, String contentString, long timeParam, Html.ImageGetter imageGetter, Html.TagHandler tagHandler) {
            name.setText(nameString);
            time.setText(Global.dayToNow(timeParam));
            Global.MessageParse parse = HtmlContent.parseMessage(contentString);
            content.setText(Global.changeHyperlinkColor(parse.text, imageGetter, tagHandler));
        }
    }
}
