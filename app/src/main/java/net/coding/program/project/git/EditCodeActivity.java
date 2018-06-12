package net.coding.program.project.git;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.GitFileBlobObject;
import net.coding.program.common.model.GitFileInfoObject;
import net.coding.program.common.model.GitFileObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.url.UrlCreate;
import net.coding.program.project.detail.ProjectGitFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.json.JSONObject;


@EActivity(R.layout.activity_edit_code)
public class EditCodeActivity extends BackActivity {

    @Extra
    String mProjectPath;

    @Extra
    GitFileInfoObject mGitFileInfoObject;

    @Extra
    GitFileBlobObject mGitAll;

    GitFileObject file;

    @Extra
    String mVersion = ProjectGitFragment.MASTER;

    private PreviewCodeFragment previewFragment;
    private EditCodeFragment editFragment;

    private MenuItem editMenu;
    private MenuItem previewMenu;

    @AfterViews
    protected final void initGitViewActivity() {
        setActionBarTitle(mGitFileInfoObject.name);
        file = mGitAll.getGitFileObject();

        previewFragment = PreviewCodeFragment_.builder().projectPath(mProjectPath).build();
        editFragment = EditCodeFragment_.builder().build();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, previewFragment)
                .add(R.id.container, editFragment)
                .commit();
    }

    public GitFileBlobObject getFile() {
        return mGitAll;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_edit_preview, menu);
        editMenu = menu.findItem(R.id.action_edit);
        editMenu.setVisible(false);
        previewMenu = menu.findItem(R.id.action_preview);

        return super.onCreateOptionsMenu(menu);
    }

    @OptionsItem
    void action_preview() {
        file.data = editFragment.getInput();
        previewFragment.loadData();
        switchFragment(previewFragment, editFragment);

        previewMenu.setVisible(false);
        editMenu.setVisible(true);
    }

    @OptionsItem
    void action_edit() {
        switchFragment(editFragment, previewFragment);

        previewMenu.setVisible(true);
        editMenu.setVisible(false);
    }

    @OptionsItem
    void action_save() {
        String url = Global.HOST_API + mProjectPath + "/git/edit/" + mVersion + UrlCreate.pathEncode2(file.path);
        RequestParams params = new RequestParams();
        params.put("content", editFragment.getInput());
        mGitAll.getGitFileObject().data = editFragment.getInput();
        params.put("message", "update " + mGitFileInfoObject.name);
        params.put("lastCommitSha", mGitAll.getCommitId());
        MyAsyncHttpClient.post(this, url, params, new MyJsonResponse(EditCodeActivity.this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                umengEvent(UmengEvent.E_GIT, "编辑代码_提交");
                closeAndSave();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                showProgressBar(false);
            }
        });
        showProgressBar(true);
    }

    private void switchFragment(Fragment show, Fragment hide) {
        getSupportFragmentManager().beginTransaction()
                .hide(hide)
                .show(show)
                .commit();
    }

    private void closeAndSave() {
        Intent intent = new Intent();
        intent.putExtra("resultData", mGitAll);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (editFragment.isModify()) {
            String msg = "如果不保存，更改将丢失，是否确认返回？";
            showDialog("", msg, (dialog, which) -> super.onBackPressed(), null, "确认返回", "取消");
        } else {
            super.onBackPressed();
        }
    }
}
