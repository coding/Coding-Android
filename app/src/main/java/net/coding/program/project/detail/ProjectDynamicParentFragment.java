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

    @AfterViews
    protected void init() {
        DynamicPagerAdapter adapter = new DynamicPagerAdapter(getChildFragmentManager());
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

        final String[] type = new String[]{
                "all",
                "task",
                "topic",
                "file",
                "code",
                "other"
        };

        DynamicPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return dynamic_type[position];
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            ProjectDynamicFragment_ fragment = new ProjectDynamicFragment_();

            Bundle bundle = new Bundle();
            bundle.putSerializable("mProjectObject", mProjectObject);
            bundle.putString("mType", type[position]);
            fragment.setArguments(bundle);

            return fragment;
        }

        @Override
        public int getCount() {
            return type.length;
        }
    }

    ;

}
