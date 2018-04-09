package net.coding.program.project.git.local;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.orhanobut.logger.Logger;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.event.EventDownloadError;
import net.coding.program.common.event.EventDownloadProgress;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.common.ui.shadow.CodingRecyclerViewSpace;
import net.coding.program.git.GitCodeReadActivity;
import net.coding.program.pickphoto.detail.ImagePagerActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.eclipse.jgit.diff.RawText;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

@EActivity(R.layout.activity_git_main)
@OptionsMenu(R.menu.git_main)
public class GitMainActivity extends BaseActivity {

    @Extra
    ProjectObject project;

    @ViewById
    View codeListLayout, codeEmptyLayout;

    @ViewById(R.id.codingRecyclerView)
    RecyclerView codingRecyclerView;

    LoadMoreAdapter codingAdapter;

    List<File> listData = new ArrayList<>();
    File currentDir;
    File rootDir;
    Stack<File> stackDir = new Stack<>();

    @AfterViews
    void initGitMainActivity() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        CloneCodeService.Param param = new CloneCodeService.Param(project, GlobalData.sUserObject.global_key, "");
        rootDir = param.getFile(this);
        currentDir = rootDir;

        if (!rootDir.exists() || rootDir.list().length == 0) {
            showCodeEmpty();
        } else {
            showCodeList();
        }

        codingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        codingAdapter = new LoadMoreAdapter(listData);
        codingAdapter.setOnItemClickListener((adapter, view, position) -> {
            File file = (File) adapter.getItem(position);
            if (file == null) return;

            if (file.isDirectory()) {
                stackDir.push(currentDir);
                currentDir = file;
                onRefrush();
            } else {
                if (ImageUtils.isImage(file)) {
                    Intent intent = new Intent(GitMainActivity.this, ImagePagerActivity_.class);
                    intent.putExtra("mSingleUri", Uri.fromFile(file).toString());
                    startActivity(intent);
                } else {
                    try {
                        boolean isBinary = isBinaryFile(file);
                        if (isBinary) {
                            showMiddleToast("暂不支持打开此文件");
                        } else {
                            Intent intent = new Intent(GitMainActivity.this, GitCodeReadActivity.class);
                            intent.putExtra(GitCodeReadActivity.PARAM, file);
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        Logger.d(e);
                    }
                }
            }
        });

        codingRecyclerView.setAdapter(codingAdapter);
        codingRecyclerView.addItemDecoration(new CodingRecyclerViewSpace(this));

//        codingAdapter.setEmptyView(R.layout.loading_view, codingRecyclerView);

        onRefrush();
    }

    @Override
    protected boolean isProgressCannCancel() {
        return true;
    }

    private void showCodeList() {
        codeListLayout.setVisibility(View.VISIBLE);
        codeEmptyLayout.setVisibility(View.INVISIBLE);
    }

    private void showCodeEmpty() {
        codeListLayout.setVisibility(View.INVISIBLE);
        codeEmptyLayout.setVisibility(View.VISIBLE);
    }

    private boolean isTextFile(File f) throws Exception {
        FileInputStream in = new FileInputStream(f);
        int size = in.available();
        if (size > 1000)
            size = 1000;
        byte[] data = new byte[size];
        in.read(data);
        in.close();
        String s = new String(data, "ISO-8859-1");
        String s2 = s.replaceAll(
                "[a-zA-Z0-9ßöäü\\.\\*!\"§\\$\\%&/()=\\?@~'#:,;\\" +
                        "+><\\|\\[\\]\\{\\}\\^°²³\\\\ \\n\\r\\t_\\-`´âêîô" +
                        "ÂÊÔÎáéíóàèìòÁÉÍÓÀÈÌÒ©‰¢£¥€±¿»«¼½¾™ª]", "");
        // will delete all text signs

        double d = (double) (s.length() - s2.length()) / (double) (s.length());
        // percentage of text signs in the text
        return d > 0.95;
    }
//
//    public static boolean isBinaryFile(File f) throws FileNotFoundException, IOException {
//        FileInputStream in = new FileInputStream(f);
//        int size = in.available();
//        if (size > 1024) size = 1024;
//        byte[] data = new byte[size];
//        in.read(data);
//        in.close();
//
//        int ascii = 0;
//        int other = 0;
//
//        for (int i = 0; i < data.length; i++) {
//            byte b = data[i];
//            if (b < 0x09) return true;
//
//            if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D) ascii++;
//            else if (b >= 0x20 && b <= 0x7E) ascii++;
//            else other++;
//        }
//
//        if (other == 0) return false;
//
//        return 100 * other / (ascii + other) > 95;
//    }

    public void onRefrush() {
        if (currentDir == null) {
            return;
        }

        File[] files = currentDir.listFiles((dir, name) -> !name.equals(".git"));
        if (files != null) {
            listData.clear();
            Collections.addAll(listData, files);
            Collections.sort(listData, (o1, o2) -> o1.getName().compareTo(o2.getName()));
            codingAdapter.notifyDataSetChanged();
        }
    }

    @Click
    void cloneButton() {
        showDialog("确定 clone 代码到本地？", (dialog, which) -> {
            cloneCode();
        });
    }

    @Override
    public void onBackPressed() {
        if (stackDir.isEmpty()) {
            finish();
        } else {
            currentDir = stackDir.pop();
            onRefrush();
        }
    }

    @OptionsItem(android.R.id.home)
    protected final void annotaionClose() {
        finish();
    }

    @OptionsItem
    void actionPull() {
        cloneCode();
    }

    private void cloneCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        View content = getLayoutInflater().inflate(R.layout.input_password, null);
        final EditText editText = content.findViewById(R.id.password);
        builder.setView(content)
                .setPositiveButton(R.string.action_ok, (dialog, which) -> {
                    Global.hideSoftKeyboard(this);
                    CloneCodeService.Param param = new CloneCodeService.Param(project, GlobalData.sUserObject.global_key, editText.getText().toString());
                    CloneCodeService.startActionGit(this, param);
                    showProgressBar(true);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @UiThread(delay = 2000)
    void delyRefush() {
        showCodeList();
        showProgressBar(false);
        showButtomToast("已下载最新代码");
        onRefrush();
    }

    @OptionsItem
    void actionDelete() {
        showDialog("确定删除本地代码?", (dialog, which) -> {
            showProgressBar(true);
            deleteLocalCode();
            finish();
        });
    }

    @Background
    void deleteLocalCode() {
        FileUtils.deleteDir(rootDir);
    }

    @Override
    protected boolean userEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventDownloadProgress event) {
        showProgressBar(true, event.progress);
        if (event.progress.contains("Updating references:    100%")) {

//            CloneCodeService.Param param = new CloneCodeService.Param(project, GlobalData.sUserObject.global_key, "");
//            rootDir = param.getFile(this);
//            currentDir = rootDir;
//            onRefrush();
            delyRefush();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventError(EventDownloadError event) {
        if (event.error.contains("not authorized")) {
            showMiddleToast("没有获取代码的权限或者密码错误");
        } else {
            showMiddleToast(event.error);
        }

        showProgressBar(false);
    }

    static class LoadMoreAdapter extends BaseQuickAdapter<File, BaseViewHolder> {

        public LoadMoreAdapter(@Nullable List<File> data) {
            super(R.layout.git_list_item, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, File item) {
            helper.setText(R.id.name, item.getName());
            ImageView icon = helper.getView(R.id.icon);
            if (item.isDirectory()) {
                icon.setImageResource(R.drawable.ic_project_code_folder);
            } else if (ImageUtils.isImage(item)) {
                icon.setImageResource(R.drawable.ic_git_img);
            } else {
                icon.setImageResource(R.drawable.ic_project_code_file);
            }
        }
    }

    public static boolean isBinaryFile(File file) {
        try {
            InputStream in = new FileInputStream(file);
            boolean result = RawText.isBinary(in);
            in.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }

}
