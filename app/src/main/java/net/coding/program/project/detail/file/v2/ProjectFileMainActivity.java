package net.coding.program.project.detail.file.v2;

import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.ui.shadow.RecyclerViewSpace;
import net.coding.program.common.widget.CommonListView;
import net.coding.program.model.ProjectObject;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.Pager;
import net.coding.program.network.model.file.CodingFile;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by chenchao on 2017/5/15.
 */

@EActivity(R.layout.project_file_listview)
@OptionsMenu(R.menu.project_file_listview)
public class ProjectFileMainActivity extends BackActivity {

    @Extra
    ProjectObject project;

    @ViewById
    CommonListView listView;

    @ViewById(R.id.folder_actions_layout)
    View foldeBottomBar;

    @ViewById(R.id.files_actions_layout)
    View fileBottomBar;

    Set<CodingFile> selectFiles = new HashSet<>();

    List<CodingFile> listData = new ArrayList<>();
    private ProjectFileAdapter listAdapter;

    ActionMode actionMode = null;

    @AfterViews
    void initProjectFileMainActivity() {
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.addItemDecoration(new RecyclerViewSpace(this));
        listView.setEmptyView(R.layout.fragment_enterprise_project_empty, R.layout.fragment_enterprise_project_empty);

        listAdapter = new ProjectFileAdapter(listData, selectFiles);
        listView.setAdapter(listAdapter);

        Network.getRetrofit(this)
                .getFileList(project.owner_user_name, project.name, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Pager<CodingFile>>(this) {
                    @Override
                    public void onSuccess(Pager<CodingFile> data) {
                        super.onSuccess(data);

                        listData.clear();
                        listData.addAll(data.list);
                        listAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                    }
                });
    }

    @OptionsItem
    void actionEdit() {
        setEditMode(true);
    }

    private void setEditMode(boolean editMode) {
        listAdapter.setEditMode(editMode);
        if (editMode) {
            if (actionMode == null) {
                actionMode = startSupportActionMode(actionModeCallback);
            }
        } else {
            actionMode = null;
        }
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.project_attachment_file_edit, menu);

            fileBottomBar.setVisibility(View.VISIBLE);
            foldeBottomBar.setVisibility(View.GONE);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_all:
                    actionAll();
                    return true;
                case R.id.action_inverse:
                    actionInverse();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            fileBottomBar.setVisibility(View.GONE);
            foldeBottomBar.setVisibility(View.VISIBLE);
            setEditMode(false);
        }
    };

    private void actionAll() {
        for (CodingFile item : listData) {
            selectFiles.add(item);
        }
        listAdapter.notifyDataSetChanged();
    }

    private void actionInverse() {
        for (CodingFile item : listData) {
            if (selectFiles.contains(item)) {
                selectFiles.remove(item);
            } else {
                selectFiles.add(item);
            }
        }
        listAdapter.notifyDataSetChanged();
    }

}
