package net.coding.program.project.git.local;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.orhanobut.logger.Logger;

import net.coding.program.R;
import net.coding.program.common.GlobalData;
import net.coding.program.common.event.EventDownloadProgress;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BackActivity;
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
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EActivity(R.layout.activity_git_main)
@OptionsMenu(R.menu.git_main)
public class GitMainActivity extends BackActivity {

    @Extra
    ProjectObject project;

    @ViewById
    ProgressBar progressBar;

    @ViewById
    View codeListLayout, codeContentLayout, codeEmptyLayout;

    @ViewById
    TextView progressText;

    @ViewById(R.id.codingRecyclerView)
    RecyclerView codingRecyclerView;

    LoadMoreAdapter codingAdapter;

    List<File> listData = new ArrayList<>();
    File currentDir;
    File rootDir;

    @AfterViews
    void initGitMainActivity() {
        CloneCodeService.Param param = new CloneCodeService.Param(project, GlobalData.sUserObject.global_key, "222222");
        rootDir = param.getFile(this);
        currentDir = rootDir;

        codeContentLayout.setVisibility(View.INVISIBLE);
        if (!rootDir.exists() || rootDir.list().length == 0) {
            showCodeNoClone();
        } else {
            codeListLayout.setVisibility(View.VISIBLE);
            codeEmptyLayout.setVisibility(View.INVISIBLE);
        }

        codingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        codingAdapter = new LoadMoreAdapter(listData);
        codingAdapter.setOnItemClickListener((adapter, view, position) -> {
            File file = (File) adapter.getItem(position);
            if (file == null) return;

            if (file.isDirectory()) {
                currentDir = file;
                onRefrush();
            } else {
                if (ImageUtils.isImage(file)) {
                    Intent intent = new Intent(GitMainActivity.this, ImagePagerActivity_.class);
                    intent.putExtra("mSingleUri", Uri.fromFile(file));
                    startActivity(intent);
                } else {
                    try {
                        String fileType = file.toURI().toURL().openConnection().getContentType();
                        boolean isBinary = isTextFile(file);
//                        Logger.d("file is text " + isBinary);
                        Intent intent = new Intent(GitMainActivity.this, GitCodeReadActivity.class);
                        intent.putExtra(GitCodeReadActivity.PARAM, file);
                        startActivity(intent);
                    } catch (Exception e) {
                        Logger.d(e);
                    }
                }
            }
        });

        codingRecyclerView.setAdapter(codingAdapter);
        codingRecyclerView.addItemDecoration(new CodingRecyclerViewSpace(this));

        codingAdapter.setEmptyView(R.layout.loading_view, codingRecyclerView);

        onRefrush();
    }

    private void showCodeNoClone() {
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

    public static boolean isBinaryFile(File f) throws FileNotFoundException, IOException {
        FileInputStream in = new FileInputStream(f);
        int size = in.available();
        if (size > 1024) size = 1024;
        byte[] data = new byte[size];
        in.read(data);
        in.close();

        int ascii = 0;
        int other = 0;

        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            if (b < 0x09) return true;

            if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D) ascii++;
            else if (b >= 0x20 && b <= 0x7E) ascii++;
            else other++;
        }

        if (other == 0) return false;

        return 100 * other / (ascii + other) > 95;
    }

    public void onRefrush() {
        if (currentDir == null) {
            return;
        }

        listData.clear();

        File[] files = currentDir.listFiles((dir, name) -> !name.equals(".git"));
        if (files != null) {
            Collections.addAll(listData, files);
        }

        codingAdapter.notifyDataSetChanged();
    }

    @Click
    void cloneButton() {
        showDialog("确定 clone 代码到本地？大的代码库可能需要较长时间", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cloneCode();
            }
        });
    }

    @OptionsItem
    void actionPull() {
        cloneButton();
    }

    private void cloneCode() {
        CloneCodeService.Param param = new CloneCodeService.Param(project, GlobalData.sUserObject.global_key, "222222");
        CloneCodeService.startActionGit(this, param);
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
        progressText.setText(event.progress);
        progressBar.setVisibility(View.VISIBLE);
        if (event.progress.startsWith("Updating references:    100%")) {
        }
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
}
