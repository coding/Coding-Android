package net.coding.program.project.detail;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;

import net.coding.program.R;
import net.coding.program.common.SaveFragmentPagerAdapter;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.model.ProjectObject;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

import java.lang.ref.WeakReference;
import java.util.List;


@EFragment(R.layout.fragment_topic)
public class TopicFragment extends BaseFragment {

    @FragmentArg
    ProjectObject mProjectObject;

    @ViewById
    WechatTab tabs;

    @ViewById(R.id.pagerTopicFragment)
    ViewPager pager;

    @StringArrayRes
    String topic_type[];

    private MyPagerAdapter adapter;

    @AfterViews
    void init() {
        adapter = new MyPagerAdapter(getChildFragmentManager());

        pager.setAdapter(adapter);

        int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs.setViewPager(pager);
    }

    class MyPagerAdapter extends SaveFragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return topic_type[position];
        }

        @Override
        public int getCount() {
            return topic_type.length;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new TopicListFragment_();
            Bundle bundle = new Bundle();
            bundle.putSerializable("mProjectObject", mProjectObject);
            bundle.putInt("type", position);
            fragment.setArguments(bundle);

            saveFragment(fragment);

            return fragment;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        List<WeakReference<Fragment>> fragments = adapter.getFragments();
        for (WeakReference<Fragment> fragment : fragments) {
            fragment.get().onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
