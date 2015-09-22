package net.coding.program.maopao;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.model.Maopao;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class LikeUsersArea {
    Fragment fragment;
    Activity activity;
    View likeUsersAllLayout;
    public LinearLayout likeUsersLayout;

    ImageLoadTool imageLoadTool;
    View.OnClickListener mOnClickUser;

    public LikeUsersArea(View convertView, Fragment fragment, ImageLoadTool imageLoadTool, View.OnClickListener mOnClickUser) {
        this(convertView, fragment, null, imageLoadTool, mOnClickUser);
    }

    public LikeUsersArea(View convertView, Activity activity, ImageLoadTool imageLoadTool, View.OnClickListener mOnClickUser) {
        this(convertView, null, activity, imageLoadTool, mOnClickUser);
    }

    private LikeUsersArea(View convertView, Fragment fragment, Activity activity, ImageLoadTool imageLoadTool, View.OnClickListener mOnClickUser) {
        this.fragment = fragment;
        this.activity = activity;
        this.imageLoadTool = imageLoadTool;
        this.mOnClickUser = mOnClickUser;
        likeUsersAllLayout = convertView.findViewById(R.id.likesAllLayout);
        likeUsersLayout = (LinearLayout) convertView.findViewById(R.id.likeUsersLayout);
        likeUsersLayout.getViewTreeObserver().addOnPreDrawListener(new MyPreDraw(likeUsersAllLayout, likeUsersLayout));
    }

    private Activity getActivity() {
        if (activity != null) {
            return activity;
        } else {
            return fragment.getActivity();
        }
    }

    private void startActivity(Intent intent) {
        if (activity != null) {
            activity.startActivity(intent);
        } else {
            fragment.startActivity(intent);
        }
    }

    private class MyPreDraw implements ViewTreeObserver.OnPreDrawListener {

        private LinearLayout layout;
        private View allLayout;

        public MyPreDraw(View allLayout, LinearLayout linearLayout) {
            layout = linearLayout;
            this.allLayout = allLayout;
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

            int imageWidth = Global.dpToPx(30);
            int imageMargin = Global.dpToPx(5);

            int shenxia = width % (imageWidth + imageMargin);
            int count = width / (imageWidth + imageMargin);
            imageMargin += shenxia / count;
            imageMargin /= 2;

            final int MAX_DISPLAY_USERS = 10;
            if (count > MAX_DISPLAY_USERS) {
                count = MAX_DISPLAY_USERS;
            }

            for (int i = 0; i < count; ++i) {
                CircleImageView view = new CircleImageView(getActivity());
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
            textView.setTextColor(0xffffffff);
            textView.setVisibility(View.GONE);
            textView.setOnClickListener(onClickLikeUsrs);

            displayLikeUser();

            return true;
        }
    }

    public void displayLikeUser() {
        Maopao.MaopaoObject data = (Maopao.MaopaoObject) likeUsersLayout.getTag(MaopaoListBaseFragment.TAG_MAOPAO);

        if (data.likes == 0) {
            likeUsersAllLayout.setVisibility(View.GONE);
        } else {
            likeUsersAllLayout.setVisibility(View.VISIBLE);
        }

        if (likeUsersLayout.getChildCount() == 0) {
            likeUsersLayout.setTag(data);
            return;
        }

        int likes = data.likes;
        final ArrayList<Maopao.Like_user> likeUsers = data.like_users;

        int imageCount = likeUsersLayout.getChildCount() - 1;

        Log.d("", "ddd disgood " + imageCount + "," + likeUsers.size() + "," + likes);

        likeUsersLayout.getChildAt(imageCount).setTag(data.id);

        if (likeUsers.size() < imageCount) {
            if (likes <= imageCount) {
                int i = 0;
                for (; i < likeUsers.size(); ++i) {
                    ImageView image = (ImageView) likeUsersLayout.getChildAt(i);
                    image.setVisibility(View.VISIBLE);

                    imageLoadTool.loadImage(image, likeUsers.get(i).avatar);
                }

                for (; i < imageCount; ++i) {
                    likeUsersLayout.getChildAt(i).setVisibility(View.GONE);
                }

                likeUsersLayout.getChildAt(i).setVisibility(View.GONE);

            } else {
                int i = 0;
                for (; i < likeUsers.size(); ++i) {
                    ImageView image = (ImageView) likeUsersLayout.getChildAt(i);
                    image.setVisibility(View.VISIBLE);

                    imageLoadTool.loadImage(image, likeUsers.get(i).avatar);
                }

                for (; i < imageCount; ++i) {
                    likeUsersLayout.getChildAt(i).setVisibility(View.GONE);
                }

                TextView textV = (TextView) likeUsersLayout.getChildAt(imageCount);
                textV.setVisibility(View.VISIBLE);
                textV.setText(likes + "");
            }

        } else {
            --imageCount;
            for (int i = 0; i < imageCount; ++i) {
                ImageView image = (ImageView) likeUsersLayout.getChildAt(i);
                image.setVisibility(View.VISIBLE);
                imageLoadTool.loadImage(image, likeUsers.get(i).avatar);
            }

            likeUsersLayout.getChildAt(imageCount).setVisibility(View.GONE);
            TextView textView = (TextView) likeUsersLayout.getChildAt(imageCount + 1);
            textView.setVisibility(View.VISIBLE);
            textView.setText(likes + "");
        }

        imageCount = likeUsersLayout.getChildCount() - 1;
        for (int i = 0; i < imageCount; ++i) {
            View v = likeUsersLayout.getChildAt(i);
            if (v.getVisibility() == View.VISIBLE) {
                v.setTag(likeUsers.get(i).global_key);
            } else {
                break;
            }
        }
    }

    View.OnClickListener onClickLikeUsrs = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), LikeUsersListActivity_.class);
            intent.putExtra("id", (int) v.getTag());
            startActivity(intent);
        }
    };

}