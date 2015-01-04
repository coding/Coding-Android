package net.coding.program.project.detail;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import net.coding.program.R;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.model.ProjectObject;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;


@EFragment(R.layout.fragment_project_dynamic_parent)
public class ProjectDynamicParentFragment extends BaseFragment {

    @FragmentArg
    ProjectObject mProjectObject;

    @ViewById
    WechatTab tabs;

    @ViewById(R.id.pagerFragmentProgram)
    ViewPager pager;

    @StringArrayRes
    String[] dynamic_type;

    @StringArrayRes
    String[] dynamic_type_params;

    @StringArrayRes
    String[] dynamic_type_public;

    @StringArrayRes
    String[] dynamic_type_public_params;

    @AfterViews
    protected void init() {
        String[] title = dynamic_type;
        String[] titleParams = dynamic_type_params;
        if (mProjectObject.is_public) {
            title = dynamic_type_public;
            titleParams = dynamic_type_public_params;
        }

        DynamicPagerAdapter adapter = new DynamicPagerAdapter(getChildFragmentManager(), title, titleParams);
        pager.setAdapter(adapter);

        int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs.setViewPager(pager);
        setTabsValue();
    }

    private void setTabsValue() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        // 设置Tab是自动填充满屏幕的
        tabs.setShouldExpand(true);
        // 设置Tab的分割线是透明的
        tabs.setDividerColor(Color.TRANSPARENT);
        // 设置Tab底部线的高度
        tabs.setUnderlineHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, dm));
        // 设置Tab Indicator的高度
        tabs.setIndicatorHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 3, dm));
        // 设置Tab标题文字的大小
        tabs.setTextSize((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 16, dm));
        // 设置Tab Indicator的颜色
        tabs.setIndicatorColor(Color.parseColor("#3bbd79"));
        // 设置选中Tab文字的颜色 (这是我自定义的一个方法)
        tabs.setSelectedTextColor(Color.parseColor("#3bbd79"));
        // 取消点击Tab时的背景色
//        tabs.setTabBackground(0);
//        tabs.setTabPaddingLeftRight(0);
//        tabs.setDividerPadding(0);

    }

    class DynamicPagerAdapter extends FragmentStatePagerAdapter {

        String[] mTitles;
        String[] mParams;

        DynamicPagerAdapter(FragmentManager fm, String[] titles, String[] params) {
            super(fm);
            mTitles = titles;
            mParams = params;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return ProjectDynamicFragment_.builder()
                        .mProjectObject(mProjectObject)
                        .mType(mParams[position])
                        .build();
        }

        @Override
        public int getCount() {
            return mParams.length;
        }
    }

    ;
}
