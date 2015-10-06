package net.coding.program.project.detail;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;

import net.coding.program.R;
import net.coding.program.common.ui.BaseFragment;
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
        if (mProjectObject.isPublic()) {
            title = dynamic_type_public;
            titleParams = dynamic_type_public_params;
        }

        DynamicPagerAdapter adapter = new DynamicPagerAdapter(getChildFragmentManager(), title, titleParams);
        pager.setAdapter(adapter);

        int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs.setViewPager(pager);
//        tabs.setUnderlinePadding0();
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
