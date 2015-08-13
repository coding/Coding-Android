package net.coding.program.common;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaochen on 14/12/31.
 */
public abstract class SaveFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private List<WeakReference<Fragment>> mList = new ArrayList<>();

    public List<WeakReference<Fragment>> getFragments() {
        for (int i = mList.size() - 1; i >= 0; --i) {
            if (mList.get(i).get() == null) {
                mList.remove(i);
            }
        }

        return mList;
    }

    protected void saveFragment(Fragment fragment) {
        if (fragment == null) {
            return;
        }

        for (WeakReference<Fragment> item : mList) {
            if (item.get() == fragment) {
                return;
            }
        }

        mList.add(new WeakReference(fragment));
    }

    protected SaveFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }


}
