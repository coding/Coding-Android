package net.coding.program.common;

import android.app.Activity;
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
        mActivity = activity;
    }

    public ClickSmallImage(Fragment fragment) {
        mFragment = fragment;
    }

    @Override
    public void onClick(View v) {
        MaopaoListFragment.ClickImageParam param = (MaopaoListFragment.ClickImageParam) v.getTag();

        ImagePagerActivity_.IntentBuilder_ builder;
        if (mActivity != null) {
            builder = ImagePagerActivity_.intent(mActivity);
        } else {
            builder = ImagePagerActivity_.intent(mFragment);
        }
        builder.mArrayUri(param.urls)
                .mPagerPosition(param.pos)
                .needEdit(param.needEdit)
                .start();
    }
}
