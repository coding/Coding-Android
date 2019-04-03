package net.coding.program.common.network;

import android.view.View;

import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

/**
 * Created by chenchao on 16/9/20.
 */
@EFragment
public class LoadingFragment extends RefreshBaseFragment {

    View baseLoadinggView;

    public void showLoading(boolean show) {
        if (baseLoadinggView == null) {
            return;
        }

        baseLoadinggView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRefresh() {
    }

    @AfterViews
    protected void initLoadingFragment() {
        View view = getView();
        if (view != null) {
            baseLoadinggView = view.findViewById(R.id.baseLoadingView);
        }
    }
}
