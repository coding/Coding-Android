package net.coding.program;

import net.coding.program.common.model.AttachmentFileObject;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.network.Network;
import net.coding.program.network.model.HttpResult;
import net.coding.program.network.model.file.CodingFile;
import net.coding.program.project.detail.AttachmentsDownloadDetailActivity_;
import net.coding.program.project.detail.AttachmentsHtmlDetailActivity_;
import net.coding.program.project.detail.AttachmentsPhotoDetailActivity_;
import net.coding.program.project.detail.AttachmentsTextDetailActivity_;
import net.coding.program.project.detail.file.v2.ProjectFileMainActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.io.Serializable;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_file_url)
public class FileUrlActivity extends BaseActivity {

    @Extra
    Param param;

    ProjectObject projectResult;
    CodingFile codingDirResult;
    CodingFile codingFileResult;

    @AfterViews
    public void parseUrl() {
        if (param.isDir()) {
            jumpDir();
        } else {
            jumpToFileDetail();
        }
    }

    public void jumpDir() {
        String user = param.user;
        String project = param.project;
        int dirId = param.id;

        Observable<HttpResult<ProjectObject>> p = Network.getRetrofit(this)
                .getProject(user, project)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Observable<HttpResult<CodingFile>> d = Network.getRetrofit(this)
                .getDirDetail(user, project, dirId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        Observable.zip(p, d, (rp, rd) -> {
            if (rp.code == 0 && rd.code == 0) {
                projectResult = rp.data;
                codingDirResult = rd.data;
                return true;
            }
            return false;
        }).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {
                ProjectFileMainActivity_.intent(FileUrlActivity.this)
                        .parentFolder(codingDirResult)
                        .project(projectResult)
                        .start();
                overridePendingTransition(0, 0);
                finish();
            }

            @Override
            public void onError(Throwable e) {
                finish();
            }

            @Override
            public void onNext(Boolean aBoolean) {
            }
        });
    }

    // TODO: 2018/6/5 没写完
    private void jumpToFileDetail() {
        AttachmentFileObject folderFile = new AttachmentFileObject(codingFileResult);
        int projectId = projectResult.id;

        if (codingFileResult.isImage() || codingFileResult.isGif()) {
            AttachmentsPhotoDetailActivity_.intent(this)
                    .mProjectObjectId(projectId)
                    .mAttachmentFileObject(folderFile)
                    .start();
        } else if (AttachmentFileObject.isMd(folderFile.fileType)) {
            AttachmentsHtmlDetailActivity_.intent(this)
                    .mProjectObjectId(projectId)
                    .mAttachmentFileObject(folderFile)
                    .start();
        } else if (AttachmentFileObject.isTxt(folderFile.fileType)) {
            AttachmentsTextDetailActivity_.intent(this)
                    .mProjectObjectId(projectId)
                    .mAttachmentFileObject(folderFile)
                    .start();
        } else {
            AttachmentsDownloadDetailActivity_.intent(this)
                    .mProjectObjectId(projectId)
                    .mAttachmentFileObject(folderFile)
                    .start();
        }

        overridePendingTransition(0, 0);
        finish();
    }

    public static final class Param implements Serializable {

        private static final long serialVersionUID = -7005258403758123690L;

        public String user;
        public String project;
        public int id;
        boolean isFile;

        public boolean isDir() {
            return isFile;
        }

        public Param(String user, String project, int file, boolean isFile) {
            this.user = user;
            this.project = project;
            this.id = file;
            this.isFile = isFile;
        }
    }
}
