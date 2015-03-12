package net.coding.program.project;

import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.View;
import android.widget.EditText;

import net.coding.program.BaseActivity;
import net.coding.program.R;
import net.coding.program.common.enter.SimpleTextWatcher;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

@EActivity(R.layout.activity_search_project)
public class SearchProjectActivity extends BaseActivity {

    private ArrayList<ProjectObject> mData = new ArrayList();
    private ArrayList<ProjectObject> mSearchData = new ArrayList();

    @ViewById
    View emptyView, container;

    EditText editText;
    private ProjectListFragment searchFragment;

    @AfterViews
    void init() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.setCustomView(R.layout.activity_search_project_actionbar);
//        actionBar.setTitle(R.string.title_activity_search_project);

        editText = (EditText) findViewById(R.id.editText);
        editText.addTextChangedListener(new SimpleTextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchData.clear();
                if (s.length() > 0) {
                    String enter = s.toString().toLowerCase();
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
            }
        });

        mData = AccountInfo.loadProjects(this);
        searchFragment = ProjectListFragment_.builder().mData(mSearchData).build();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, searchFragment)
                .commit();
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    private void updateSearchResult() {
        searchFragment.setDataAndUpdate(mSearchData);
    }
}
