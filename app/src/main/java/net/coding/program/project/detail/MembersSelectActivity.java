package net.coding.program.project.detail;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import net.coding.program.BaseActivity;
import net.coding.program.R;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;

@EActivity(R.layout.activity_members_select)
public class MembersSelectActivity extends BaseActivity {

    @Extra
    ProjectObject mProjectObject;

    MembersListFragment fragment;

    @AfterViews
    void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragment = new MembersListFragment_
                .FragmentBuilder_()
                .mProjectObject(mProjectObject)
                .mSelect(true)
                .build();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @OptionsItem(android.R.id.home)
    void back() {
        onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.users_fans, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setIcon(R.drawable.ic_menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        try { // 更改搜索按钮的icon
            int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
            ImageView v = (ImageView) searchView.findViewById(searchImgId);
            v.setImageResource(R.drawable.ic_menu_search);
        } catch (Exception e) {
        }

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
