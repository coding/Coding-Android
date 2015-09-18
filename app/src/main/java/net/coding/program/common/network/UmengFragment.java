package net.coding.program.common.network;

import android.support.v4.app.Fragment;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by chenchao on 15/7/15.
 */
public class UmengFragment extends Fragment {
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getSimpleName());
    }

    protected void umengEvent(String s, String param) {
        if (getActivity() != null) {
            MobclickAgent.onEvent(getActivity(), s, param);
        }
    }

}
