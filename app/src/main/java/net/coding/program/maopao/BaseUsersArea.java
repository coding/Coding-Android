package net.coding.program.maopao;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.model.UserObject;

import java.util.List;

/**
 * Created by chenchao on 16/7/26.
 */
public class BaseUsersArea {
    public LinearLayout likeUsersLayout;
    Fragment fragment;
    Activity activity;
    ImageLoadTool imageLoadTool;
    View.OnClickListener mOnClickUser;
    View.OnClickListener onClickLikeUsrs = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), LikeUsersListActivity_.class);
            intent.putExtra("id", (int) v.getTag());
            startActivity(intent);
        }
    };

    public BaseUsersArea(LinearLayout likeUsersLayout, Fragment fragment, Activity activity, View.OnClickListener mOnClickUser, ImageLoadTool imageLoadTool) {
        this.fragment = fragment;
        this.activity = activity;
        this.likeUsersLayout = likeUsersLayout;
        this.mOnClickUser = mOnClickUser;
        this.imageLoadTool = imageLoadTool;

        likeUsersLayout.getViewTreeObserver().addOnPreDrawListener(new MyPreDraw(likeUsersLayout));
    }

    protected Activity getActivity() {
        if (activity != null) {
            return activity;
        } else {
            return fragment.getActivity();
        }
    }

    protected void startActivity(Intent intent) {
        if (activity != null) {
            activity.startActivity(intent);
        } else {
            fragment.startActivity(intent);
        }
    }

    public void displayLikeUser() {
//        Maopao.MaopaoObject maopaoData = (Maopao.MaopaoObject) likeUsersLayout.getTag(MaopaoListBaseFragment.TAG_MAOPAO);
//
//        if ((maopaoData.likes + maopaoData.rewards) == 0) {
//            likeUsersAllLayout.setVisibility(View.GONE);
//        } else {
//            likeUsersAllLayout.setVisibility(View.VISIBLE);
//        }
//
//        if (likeUsersLayout.getChildCount() == 0) {
//            likeUsersLayout.setTag(maopaoData);
//            return;
//        }
        if (likeUsersLayout.getChildCount() == 0) {
            return;
        }

        List<UserObject> users = (List<UserObject>) likeUsersLayout.getTag();
        final List<UserObject> displayUsers = users;
//        for (Maopao.Like_user like : maopaoData.like_users) {
//            boolean find = false;
//            for (Maopao.Like_user reward : displayUsers) {
//                if (like.global_key.equals(reward.global_key)) {
//                    find = true;
//                    break;
//                }
//            }
//
//            if (!find) {
//                displayUsers.add(like);
//            }
//        }

        int readUserCount = users.size();
        int imageCount = likeUsersLayout.getChildCount() - 1;

//        Log.d("", "ddd disgood " + imageCount + "," + displayUsers.size() + "," + readUserCount);

//        likeUsersLayout.getChildAt(imageCount).setTag();

        if (displayUsers.size() < imageCount) {
            if (readUserCount <= imageCount) {
                int i = 0;
                for (; i < displayUsers.size(); ++i) {
                    updateImageDisplay(displayUsers, i);
                }

                for (; i < imageCount; ++i) {
                    likeUsersLayout.getChildAt(i).setVisibility(View.GONE);
                }

                likeUsersLayout.getChildAt(i).setVisibility(View.GONE);

            } else {
                int i = 0;
                for (; i < displayUsers.size(); ++i) {
                    updateImageDisplay(displayUsers, i);
                }

                for (; i < imageCount; ++i) {
                    likeUsersLayout.getChildAt(i).setVisibility(View.GONE);
                }

                TextView textV = (TextView) likeUsersLayout.getChildAt(imageCount);
                textV.setVisibility(View.VISIBLE);
                textV.setText(readUserCount + "");
            }

        } else {
            --imageCount;
            for (int i = 0; i < imageCount; ++i) {
                updateImageDisplay(displayUsers, i);
            }

            likeUsersLayout.getChildAt(imageCount).setVisibility(View.GONE);
            TextView textView = (TextView) likeUsersLayout.getChildAt(imageCount + 1);
            textView.setVisibility(View.VISIBLE);
            textView.setText(readUserCount + "");
        }

        imageCount = likeUsersLayout.getChildCount() - 1;
        for (int i = 0; i < imageCount; ++i) {
            View v = likeUsersLayout.getChildAt(i);
            if (v.getVisibility() == View.VISIBLE) {
                v.setTag(displayUsers.get(i).global_key);
            } else {
                break;
            }
        }
    }


    protected void updateImageDisplay(List<UserObject> likeUsers, int i) {
        ImageView image = (ImageView) likeUsersLayout.getChildAt(i);
        image.setVisibility(View.VISIBLE);

        UserObject like_user = likeUsers.get(i);
        image.setTag(LikeUserImage.TAG, like_user);
        imageLoadTool.loadImage(image, like_user.avatar);
    }

    protected class MyPreDraw implements ViewTreeObserver.OnPreDrawListener {

        private LinearLayout layout;

        public MyPreDraw(LinearLayout linearLayout) {
            layout = linearLayout;
        }

        @Override
        public boolean onPreDraw() {
            int width = layout.getWidth();

            if (width <= 0) {
                return true;
            }

            if (layout.getChildCount() > 0) {
                layout.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }

            width -= (layout.getPaddingLeft() + layout.getPaddingRight());

            int imageWidth = GlobalCommon.dpToPx(30);
            int imageMargin = GlobalCommon.dpToPx(5);

            int shenxia = width % (imageWidth + imageMargin);
            int count = width / (imageWidth + imageMargin);
            imageMargin += shenxia / count;
            imageMargin /= 2;

            final int MAX_DISPLAY_USERS = 10;
            if (count > MAX_DISPLAY_USERS) {
                count = MAX_DISPLAY_USERS;
            }

            for (int i = 0; i < count; ++i) {
                LikeUserImage view = new LikeUserImage(getActivity());
                layout.addView(view);

                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                lp.width = imageWidth;
                lp.height = imageWidth;
                lp.leftMargin = imageMargin;
                lp.rightMargin = imageMargin;
                view.setLayoutParams(lp);
                view.setOnClickListener(mOnClickUser);
            }

            TextView textView = new TextView(getActivity());
            textView.setGravity(Gravity.CENTER);
            layout.addView(textView);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) textView.getLayoutParams();
            lp.width = imageWidth;
            lp.height = imageWidth;
            lp.leftMargin = imageMargin;
            lp.rightMargin = imageMargin;
            textView.setBackgroundResource(R.drawable.ic_bg_good_count);
            textView.setTextColor(CodingColor.fontWhite);
            textView.setVisibility(View.GONE);
            textView.setOnClickListener(onClickLikeUsrs);

            displayLikeUser();

            return true;
        }
    }
}
