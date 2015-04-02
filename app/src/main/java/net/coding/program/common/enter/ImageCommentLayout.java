package net.coding.program.common.enter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.photopick.PhotoPickActivity;
import net.coding.program.common.photopick.PhotoPickDetailActivity;
import net.coding.program.maopao.MaopaoAddActivity;

import java.util.ArrayList;

/**
 * Created by chenchao on 15/3/30.
 */
public class ImageCommentLayout {

    public static final int RESULT_REQUEST_COMMENT_IMAGE = 1100;
    public static final int RESULT_REQUEST_COMMENT_IMAGE_DETAIL = 1101;

    private EnterLayout mEnterLayout;
    private View mRootLayout;

    private LinearLayout imagesLayout;
    private ImageView mImageViews[];
    private Activity mActivity;
    private ImageLoadTool mImageLoader;
    private ArrayList<PhotoPickActivity.ImageInfo> mArrayImages = new ArrayList();

    public ImageCommentLayout(Activity activity, View.OnClickListener onClickSend, ImageLoadTool imageLoader) {
        mEnterLayout = new EnterLayout(activity, onClickSend, EnterLayout.Type.TextOnly) {
            @Override
            protected boolean sendButtonEnable() {
                return getContent().length() > 0 ||
                        !mArrayImages.isEmpty();
            }
        };

        mActivity = activity;
        mImageLoader = imageLoader;

        View v = activity.findViewById(R.id.commonEnterRoot);
        mRootLayout = v;
        v.findViewById(R.id.commentImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, PhotoPickActivity.class);
                mActivity.startActivityForResult(intent, RESULT_REQUEST_COMMENT_IMAGE);
            }
        });

        imagesLayout = (LinearLayout) v.findViewById(R.id.imageLayout);
        LayoutInflater inflater = LayoutInflater.from(v.getContext());
        mImageViews = new ImageView[MaopaoAddActivity.PHOTO_MAX_COUNT];
        for (int i = 0; i < MaopaoAddActivity.PHOTO_MAX_COUNT; ++i) {
            mImageViews[i] = (ImageView) inflater.inflate(R.layout.common_enter_image_imageitem, imagesLayout, false);
            mImageViews[i].setVisibility(View.INVISIBLE);
            imagesLayout.addView(mImageViews[i]);
            mImageViews[i].setTag(R.id.image, i);
            mImageViews[i].setOnClickListener(mClickImage);
        }

        imagesLayout.setVisibility(View.GONE);
    }

    private View.OnClickListener mClickImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = (int) v.getTag(R.id.image);
            Intent intent = new Intent(mActivity, PhotoPickDetailActivity.class);
            intent.putExtra(PhotoPickDetailActivity.PHOTO_BEGIN, pos);
            intent.putExtra(PhotoPickDetailActivity.PICK_DATA, mArrayImages);
            intent.putExtra(PhotoPickDetailActivity.ALL_DATA, mArrayImages);
            mActivity.startActivityForResult(intent, RESULT_REQUEST_COMMENT_IMAGE_DETAIL);
        }
    };

    public EnterLayout getEnterLayout() {
        return mEnterLayout;
    }

    public void hide() {
        mRootLayout.setVisibility(View.GONE);
    }

    public void onActivityResult(int RESULT_TYPE, Intent data) {
        if (data == null) {
            return;
        }

        if (RESULT_TYPE == RESULT_REQUEST_COMMENT_IMAGE) {
            ArrayList<PhotoPickActivity.ImageInfo> images = (ArrayList) data.getSerializableExtra("data");
            mArrayImages.addAll(images);
            updateCommentImage();

        } else if (RESULT_TYPE == RESULT_REQUEST_COMMENT_IMAGE_DETAIL) {
            ArrayList<PhotoPickActivity.ImageInfo> images = (ArrayList) data.getSerializableExtra("data");
            mArrayImages = images;
            updateCommentImage();
        }
    }

    public void clearContent() {
        mEnterLayout.hideKeyboard();
        mEnterLayout.clearContent();
        mArrayImages.clear();
        updateCommentImage();
    }

    private void updateCommentImage() {
        String s = "";
        int i = 0;
        if (mArrayImages.isEmpty()) {
            imagesLayout.setVisibility(View.GONE);
        } else {
            imagesLayout.setVisibility(View.VISIBLE);
        }

        for (; i < mArrayImages.size(); ++i) {
            mImageViews[i].setVisibility(View.VISIBLE);
            mImageLoader.loadImage(mImageViews[i], mArrayImages.get(i).path);
        }

        for (; i < mImageViews.length; ++i) {
            mImageViews[i].setVisibility(View.INVISIBLE);
        }

        mEnterLayout.updateSendButtonStyle();
    }

    public ArrayList<PhotoPickActivity.ImageInfo> getPickPhotos() {
        return mArrayImages;
    }
}
