package net.coding.program.maopao.item;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.maopao.ClickImageParam;
import net.coding.program.common.maopao.ContentAreaMushImageOption;
import net.coding.program.common.param.MessageParse;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;

/**
 * Created by chenchao on 15/3/31.
 * 有多张图片的控件，比如任务的评论
 */
public class ContentAreaMuchImages extends ContentAreaBase {

    protected ImageLoadTool imageLoad;
    private View.OnClickListener mOnclickImage;

    private FlowLayout mFlowLayout;

    public ContentAreaMuchImages(View convertView, View.OnClickListener onClickContent, View.OnClickListener onclickImage, Html.ImageGetter imageGetterParamer, ImageLoadTool loadParams) {
        super(convertView, onClickContent, imageGetterParamer);

        imageLoad = loadParams;
//        contentMarginBottom = convertView.getResources().getDimensionPixelSize(R.dimen.message_text_margin_bottom);

        mFlowLayout = (FlowLayout) convertView.findViewById(R.id.flowLayout);
        mOnclickImage = onclickImage;
    }

    public void setDataContent(String s, Object contentObject) {
        String data = s;

        MessageParse maopaoData = HtmlContent.parseMessage(data);

        if (maopaoData.text.isEmpty()) {
            content.setVisibility(View.GONE);
        } else {
            content.setVisibility(View.VISIBLE);
            content.setText(GlobalCommon.changeHyperlinkColor(maopaoData.text, imageGetter, Global.tagHandler));
            content.setTag(contentObject);
        }

        setImageUrl(maopaoData.uris);
    }

    protected void setImageUrl(ArrayList<String> uris) {
        if (uris.size() == 0) {
            mFlowLayout.setVisibility(View.GONE);
            mFlowLayout.removeAllViews();
            return;
        }

        int count = mFlowLayout.getChildCount();
        mFlowLayout.setVisibility(View.VISIBLE);

        if (uris.size() > count) {
            int need = uris.size() - count;
            LayoutInflater inflater = LayoutInflater.from(mFlowLayout.getContext());
            for (int i = 0; i < need; ++i) {
                inflater.inflate(R.layout.comment_image, mFlowLayout);
                mFlowLayout.getChildAt(i).setOnClickListener(mOnclickImage);
            }
        } else if (uris.size() < count) {
            int release = count - uris.size();
            for (int i = 0; i < release; ++i) {
                mFlowLayout.removeViewAt(count - 1 - i);
            }
        }

        for (int i = 0; i < uris.size(); ++i) {
            ImageView image = (ImageView) mFlowLayout.getChildAt(i);
            image.setTag(new ClickImageParam(uris, i, false));
            imageLoad.loadImage(image, uris.get(i), ContentAreaMushImageOption.imageOptions);
        }
    }

}
