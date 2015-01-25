package net.coding.program.maopao.item;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
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

    private View commentMore;
    private Html.ImageGetter imageGetter;
    private TextView commentMoreCount;
    private View commentLayout;

    private CommentItem comment[];
    private static final int commentMaxCount = commentIds.length;

    public CommentArea(View convertView, View.OnClickListener onClickComment, Html.ImageGetter imageGetterParamer) {
        imageGetter = imageGetterParamer;

        commentLayout = convertView.findViewById(R.id.commentArea);
        commentMore = convertView.findViewById(R.id.commentMore);
        commentMoreCount = (TextView) convertView.findViewById(R.id.commentMoreCount);

        comment = new CommentItem[commentIds.length];
        for (int i = 0; i < commentIds.length; ++i) {
            comment[i] = new CommentItem(convertView.findViewById(commentIds[i]), onClickComment, i);
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
                item.setContent(comment, imageGetter, Global.tagHandler);
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
}
