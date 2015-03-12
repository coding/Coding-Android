package net.coding.program.maopao;

import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.maopao.item.ContentAreaBase;
import net.coding.program.model.Maopao;

import java.util.ArrayList;

/**
 * Created by chaochen on 14-9-19.
 */
public class ContentArea extends ContentAreaBase {

    private static final int[] itemImages = new int[]{
            R.id.image0,
            R.id.image1,
            R.id.image2,
            R.id.image3,
            R.id.image4,
            R.id.image5
    };

    private static final int itemImagesMaxCount = itemImages.length;

    private ImageView imageSingle;
    private View imageLayout0;
    private View imageLayout1;
    private ImageView images[] = new ImageView[itemImagesMaxCount];

    private int contentMarginBottom = 0;

    ImageLoadTool imageLoad;

    DisplayImageOptions imageOptions = new DisplayImageOptions
            .Builder()
            .showImageOnLoading(R.drawable.ic_default_image)
            .showImageForEmptyUri(R.drawable.ic_default_image)
            .showImageOnFail(R.drawable.ic_default_image)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .build();

    public ContentArea(View convertView, View.OnClickListener onClickContent, View.OnClickListener onclickImage, Html.ImageGetter imageGetterParamer, ImageLoadTool loadParams, int pxImageWidth) {
        super(convertView, onClickContent, imageGetterParamer);

        imageSingle = (ImageView) convertView.findViewById(R.id.imageSingle);
        imageSingle.setOnClickListener(onclickImage);
        imageSingle.setFocusable(false);
        imageSingle.setLongClickable(true);

        imageLayout0 = convertView.findViewById(R.id.imagesLayout0);
        imageLayout1 = convertView.findViewById(R.id.imagesLayout1);
        for (int i = 0; i < itemImagesMaxCount; ++i) {
            images[i] = (ImageView) convertView.findViewById(itemImages[i]);
            images[i].setOnClickListener(onclickImage);
            images[i].setFocusable(false);
            images[i].setLongClickable(true);
            ViewGroup.LayoutParams lp = images[i].getLayoutParams();
            lp.width = pxImageWidth;
            lp.height = pxImageWidth;
        }

        imageLoad = loadParams;

        contentMarginBottom = convertView.getResources().getDimensionPixelSize(R.dimen.message_text_margin_bottom);
    }

    // 用来设置冒泡的
    public void setData(Maopao.MaopaoObject maopaoObject) {
        String data = maopaoObject.content;

        Global.MessageParse maopaoData;
        maopaoData = HtmlContent.parseMaopao(data);

        if (maopaoData.text.isEmpty()) {
            content.setVisibility(View.GONE);
        } else {
            content.setVisibility(View.VISIBLE);
            content.setText(Global.changeHyperlinkColor(maopaoData.text, imageGetter, Global.tagHandler));
            content.setTag(maopaoObject);
        }

        setImageUrl(maopaoData.uris);
    }

    // 用来设置message的
    public void setData(String data) {
        Global.MessageParse maopaoData;
        maopaoData = HtmlContent.parseMessage(data);

        if (maopaoData.text.isEmpty()) {
            content.setVisibility(View.GONE);
        } else {
            content.setVisibility(View.VISIBLE);
            content.setText(Global.changeHyperlinkColor(maopaoData.text, imageGetter, Global.tagHandler));

            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) content.getLayoutParams();
            if (maopaoData.uris.size() > 0) {
                lp.bottomMargin = contentMarginBottom;
            } else {
                lp.bottomMargin = 0;
            }
            content.setLayoutParams(lp);
        }

        setImageUrl(maopaoData.uris);
    }

    private void setImageUrl(ArrayList<String> uris) {
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

        int i = 0;
        if (uris.size() == 1) {
            imageLoad.loadImage(imageSingle, uris.get(i), imageOptions);
            imageSingle.setTag(new MaopaoListFragment.ClickImageParam(uris, 0, false));

        } else {
            int min = Math.min(uris.size(), images.length);
            for (; i < min; ++i) {
                images[i].setVisibility(View.VISIBLE);
                images[i].setTag(new MaopaoListFragment.ClickImageParam(uris, i, false));
                imageLoad.loadImage(images[i], uris.get(i), imageOptions);
            }
        }

        for (; i < itemImagesMaxCount; ++i) {
            images[i].setVisibility(View.GONE);
        }
    }
}
