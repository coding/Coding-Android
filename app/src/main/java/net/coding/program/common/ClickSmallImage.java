package net.coding.program.common;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;

import net.coding.program.ImagePagerActivity_;
import net.coding.program.maopao.MaopaoListFragment;

/**
 * Created by chaochen on 14-9-22.
 */
public class ClickSmallImage implements View.OnClickListener {

    private Activity mActivity;
    private Fragment mFragment;

    public ClickSmallImage(Activity activity) {
        this.mActivity = activity;
    }

    public ClickSmallImage(Fragment fragment) {
        this.mFragment = fragment;
    }

    @Override
    public void onClick(View v) {
        Activity activity = mActivity != null ? mActivity : mFragment.getActivity();

        Intent intent = new Intent(activity, ImagePagerActivity_.class);
        MaopaoListFragment.ClickImageParam param = (MaopaoListFragment.ClickImageParam) v.getTag();

        intent.putExtra("mArrayUri", param.urls);
        intent.putExtra("mPagerPosition", param.pos);
        intent.putExtra("needEdit", param.needEdit);

        if (mActivity != null) {
            mActivity.startActivity(intent);
        } else {
            mFragment.startActivity(intent);
        }
    }
}
