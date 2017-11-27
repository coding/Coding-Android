package net.coding.program.maopao;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.model.Maopao;

import java.util.ArrayList;

public class LikeUsersArea extends BaseUsersArea {

    View likeUsersAllLayout;


    public LikeUsersArea(View convertView, Fragment fragment, ImageLoadTool imageLoadTool, View.OnClickListener mOnClickUser) {
        this(convertView, fragment, null, imageLoadTool, mOnClickUser);
    }

    public LikeUsersArea(View convertView, Activity activity, ImageLoadTool imageLoadTool, View.OnClickListener mOnClickUser) {
        this(convertView, null, activity, imageLoadTool, mOnClickUser);
    }

    private LikeUsersArea(View convertView, Fragment fragment, Activity activity, ImageLoadTool imageLoadTool, View.OnClickListener mOnClickUser) {
        super((LinearLayout) convertView.findViewById(R.id.likeUsersLayout), fragment, activity, mOnClickUser, imageLoadTool);

        likeUsersAllLayout = convertView.findViewById(R.id.likesAllLayout);

    }

    public void displayLikeUser() {
        Maopao.MaopaoObject maopaoData = (Maopao.MaopaoObject) likeUsersLayout.getTag(MaopaoListBaseFragment.TAG_MAOPAO);

        if ((maopaoData.likes + maopaoData.rewards) == 0) {
            likeUsersAllLayout.setVisibility(View.GONE);
        } else {
            likeUsersAllLayout.setVisibility(View.VISIBLE);
        }

        if (likeUsersLayout.getChildCount() == 0) {
            likeUsersLayout.setTag(maopaoData);
            return;
        }

        int readUserCount = maopaoData.likes + maopaoData.rewards;
        final ArrayList<Maopao.Like_user> displayUsers = new ArrayList<>(maopaoData.reward_users);
        for (Maopao.Like_user like : maopaoData.like_users) {
            boolean find = false;
            for (Maopao.Like_user reward : displayUsers) {
                if (like.global_key.equals(reward.global_key)) {
                    find = true;
                    break;
                }
            }

            if (!find) {
                displayUsers.add(like);
            }
        }

        int imageCount = likeUsersLayout.getChildCount() - 1;

        Log.d("", "ddd disgood " + imageCount + "," + displayUsers.size() + "," + readUserCount);

        likeUsersLayout.getChildAt(imageCount).setTag(maopaoData.id);

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
                textV.setText(String.valueOf(readUserCount));
            }

        } else {
            --imageCount;
            for (int i = 0; i < imageCount; ++i) {
                updateImageDisplay(displayUsers, i);
            }

            likeUsersLayout.getChildAt(imageCount).setVisibility(View.GONE);
            TextView textView = (TextView) likeUsersLayout.getChildAt(imageCount + 1);
            textView.setVisibility(View.VISIBLE);
            textView.setText(String.valueOf(readUserCount));
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


    protected void updateImageDisplay(ArrayList<Maopao.Like_user> likeUsers, int i) {
        ImageView image = (ImageView) likeUsersLayout.getChildAt(i);
        image.setVisibility(View.VISIBLE);

        Maopao.Like_user like_user = likeUsers.get(i);
        image.setTag(LikeUserImage.TAG, like_user);
        imageLoadTool.loadImage(image, like_user.avatar);
    }

}