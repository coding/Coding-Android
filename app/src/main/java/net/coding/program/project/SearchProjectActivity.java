package net.coding.program.project;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.view.View;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Iterator;

@EActivity(R.layout.activity_search_project)
public class SearchProjectActivity extends BackActivity {

    @ViewById
    View emptyView, container;
    SearchView editText;
    @Extra
    ProjectFragment.Type type = ProjectFragment.Type.Main;
    private ArrayList<ProjectObject> mData = new ArrayList<>();
    private ArrayList<ProjectObject> mSearchData = new ArrayList<>();
    private ProjectListFragment searchFragment;

    @AfterViews
    void init() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.setCustomView(R.layout.activity_search_project_actionbar);

        editText = (SearchView) findViewById(R.id.editText);
        editText.onActionViewExpanded();
        editText.setIconified(false);

        editText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mSearchData.clear();
                if (s.length() > 0) {
                    String enter = s.toLowerCase();
                    for (ProjectObject item : mData) {
                        if (item.name.toLowerCase().contains(enter) ||
                                item.owner_user_name.toLowerCase().contains(enter) ||
                                Html.fromHtml(item.owner_user_home).toString().toLowerCase().contains(enter)) {
                            mSearchData.add(item);
                        }
                    }
                }

                if (mSearchData.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    container.setVisibility(View.INVISIBLE);
                } else {
                    emptyView.setVisibility(View.INVISIBLE);
                    container.setVisibility(View.VISIBLE);
                    updateSearchResult();
                }

                getSupportActionBar().setTitle(R.string.title_activity_search_project);
                return true;
            }
        });

        mData = AccountInfo.loadProjects(this);
        if (type == ProjectFragment.Type.Pick) {
            for (Iterator<ProjectObject> iterator = mData.iterator(); iterator.hasNext(); ) {
                if (iterator.next().isPublic()) {
                    iterator.remove();
                }
            }
        }

        searchFragment = ProjectListFragment_.builder().type(type).mData(mSearchData).build();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, searchFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    private void updateSearchResult() {
        searchFragment.setDataAndUpdate(mSearchData);
    }
}
