package net.coding.program.project.detail;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.coding.program.R;
import net.coding.program.common.base.CustomMoreFragment;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_members_select)
public class ProjectMembersActivity extends BackActivity {

    @Extra
    ProjectObject projectObject;

    CustomMoreFragment fragment;

    @AfterViews
    void init() {
        fragment = new MembersListFragment_
                .FragmentBuilder_()
                .mProjectObject(projectObject)
                .build();

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
