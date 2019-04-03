package net.coding.program.project.detail;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.coding.program.R;
import net.coding.program.common.base.CustomMoreFragment;
import net.coding.program.common.model.Merge;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.project.detail.merge.MergeReviewerListFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_members_select)
public class MembersSelectActivity extends BackActivity {

    @Extra
    ProjectObject mProjectObject;

    @Extra
    String mMergeUrl;

    @Extra
    String actionBarTitle;

    @Extra
    String userListUrl;

    @Extra
    Merge mMerge;  // 写 Reviewer 功能加上

    @Extra
    boolean mSelect = false;

    CustomMoreFragment fragment;

    @AfterViews
    void init() {
        if (mProjectObject != null && mMerge == null) {
            fragment = new MembersListFragment_
                    .FragmentBuilder_()
                    .mProjectObject(mProjectObject)
                    .type(MembersListFragment.Type.Pick)
                    .build();
        } else if (userListUrl != null) {
            String title = actionBarTitle != null ? actionBarTitle : "";
            setActionBarTitle(title);
            fragment = new MembersListFragment_
                    .FragmentBuilder_()
                    .mMergeUrl(userListUrl)
                    .type(MembersListFragment.Type.Member)
                    .dataType(MembersListFragment.DataType.User)
                    .build();

        } else if (mMergeUrl != null) {
            setActionBarTitle("选择@对象");
            fragment = new MembersListFragment_
                    .FragmentBuilder_()
                    .mMergeUrl(mMergeUrl)
                    .type(MembersListFragment.Type.Pick)
                    .build();
        } else if (mMerge != null) {
            fragment = new MergeReviewerListFragment_
                    .FragmentBuilder_()
                    .mMerge(mMerge)
                    .mSelect(mSelect)
                    .build();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.users_fans, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                fragment.search(s);
                return true;
            }
        });


        return true;
    }
}
