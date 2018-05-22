package net.coding.program.project.maopao;


import android.net.Uri;
import android.support.annotation.NonNull;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.util.FileUtil;
import net.coding.program.common.widget.UploadHelp;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.file.CodingFile;
import net.coding.program.task.TaskDespEditFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

import java.io.File;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EFragment(R.layout.fragment_topic_edit)
public class EnterpriseProjectMaopaoEditFragment extends TaskDespEditFragment {

    @AfterViews
    void initProjectMaopaoEditFragment() {
        edit.setHint(R.string.input_project_maopao_content);
    }

    @Override
    protected String getCustomUploadPhoto() {
        return Global.HOST_API + "/tweet/insert_image";
    }

    @Override
    protected void updateImage(Uri updateFileUri) {
        String path = getProjectPath();
        if (path.startsWith("/")) {
            String[] split = path.toLowerCase().replace("/user/", "").replace("/project", "").split("/");
            Network.getRetrofit(getContext())
                    .getProject(split[0], split[1])
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new HttpObserver<ProjectObject>(getContext()) {
                        @Override
                        public void onSuccess(ProjectObject data) {
                            super.onSuccess(data);

                            realUpdateImage(updateFileUri, data.id);
                        }

                        @Override
                        public void onFail(int errorCode, @NonNull String error) {
                            super.onFail(errorCode, error);
                            showProgressBar(false);
                        }
                    });
        } else {
            try {
                int projectId = Integer.valueOf(path);
                realUpdateImage(updateFileUri, projectId);
            } catch (Exception e) {
                showProgressBar(false);
                showButtomToast("项目路径错误 " + path);
            }
        }
    }

    private void realUpdateImage(Uri updateFileUri, int projectId) {
        UploadHelp help = new UploadHelp(getContext(), projectId, 0,
                new File(FileUtil.getPath(getContext(), updateFileUri)),
                new UploadHelp.NetworkRequest() {
                    @Override
                    public void onProgress(int progress) {
                    }

                    @Override
                    public void onSuccess(CodingFile codingFile) {
                        uploadImageSuccess(codingFile.ownerPreview);
                        showProgressBar(false);
                    }

                    @Override
                    public void onFail() {
                        showProgressBar(false);
                    }
                }, false);
        help.upload();
    }
}
