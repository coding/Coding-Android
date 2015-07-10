package net.coding.program.maopao;

import android.text.Html;
import android.view.View;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.widget.GifMarkImageView;
import net.coding.program.maopao.item.ContentAreaImages;

import java.util.ArrayList;

/**
 * Created by chaochen on 14-9-19.
 * 添加了当图片只有一张时，显示为一张大图的功能
 */
public class ContentArea extends ContentAreaImages {

    private GifMarkImageView imageSingle;

    public ContentArea(View convertView, View.OnClickListener onClickContent, View.OnClickListener onclickImage, Html.ImageGetter imageGetterParamer, ImageLoadTool loadParams, int pxImageWidth) {
        super(convertView, onClickContent, onclickImage, imageGetterParamer, loadParams, pxImageWidth);

        imageSingle = (GifMarkImageView) convertView.findViewById(R.id.imageSingle);
        imageSingle.setOnClickListener(onclickImage);
        imageSingle.setFocusable(false);
        imageSingle.setLongClickable(true);

    }

    @Override
    protected void setImageUrl(ArrayList<String> uris) {
        if (uris.size() == 0) {
            imageSingle.setVisibility(View.GONE);
            imageLayout0.setVisibility(View.GONE);
            imageLayout1.setVisibility(View.GONE);
        } else if (uris.size() == 1) {
            imageSingle.setVisibility(View.VISIBLE);

            imageLayout0.setVisibility(View.GONE);
            imageLayout1.setVisibility(View.GONE);
        } else if (uris.size() < 3) {
            imageLayout0.setVisibility(View.VISIBLE);
            imageSingle.setVisibility(View.GONE);
            imageLayout1.setVisibility(View.GONE);
        } else {
            imageSingle.setVisibility(View.GONE);
            imageLayout0.setVisibility(View.VISIBLE);
            imageLayout1.setVisibility(View.VISIBLE);
        }

        if (uris.size() == 1) {
            imageLoad.loadImage(imageSingle, uris.get(0), imageOptions);
            imageSingle.showGifFlag(uris.get(0));
            imageSingle.setTag(new MaopaoListFragment.ClickImageParam(uris, 0, false));
        } else {
            super.setImageUrl(uris);
        }
    }
}
