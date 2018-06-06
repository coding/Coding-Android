package net.coding.program;

import net.coding.program.common.model.AttachmentFileObject;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.network.Network;
import net.coding.program.network.model.HttpResult;
import net.coding.program.network.model.file.CodingFile;
import net.coding.program.network.model.file.CodingFileView;
import net.coding.program.project.detail.AttachmentsDownloadDetailActivity_;
import net.coding.program.project.detail.AttachmentsHtmlDetailActivity_;
import net.coding.program.project.detail.AttachmentsPhotoDetailActivity_;
import net.coding.program.project.detail.AttachmentsTextDetailActivity_;
import net.coding.program.project.detail.file.v2.ProjectFileMainActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.io.Serializable;
import java.util.ArrayList;

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

        Observable<HttpResult<ArrayList<CodingFile>>> d = Network.getRetrofit(this)
                .getDirDetail(user, project, dirId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        Observable.zip(p, d, (rp, rd) -> {
            if (rp.code == 0 && rd.code == 0) {
                projectResult = rp.data;

                if (rd.data == null || rd.data.isEmpty()) {
                    showButtomToast("未找到文件夹，可能已被删除");
                    return false;
                } else {
                    codingDirResult = rd.data.get(rd.data.size() - 1);
                }
                return true;
            }
            return false;
        }).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                finish();
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    ProjectFileMainActivity_.intent(FileUrlActivity.this)
                            .project(projectResult)
                            .parentFolder(codingDirResult)
                            .start();
                }

                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    private void jumpToFileDetail() {
        String user = param.user;
        String project = param.project;
        int fileId = param.id;

        Observable<HttpResult<ProjectObject>> p = Network.getRetrofit(this)
                .getProject(user, project)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Observable<HttpResult<CodingFileView>> f = Network.getRetrofit(this)
                .getFileDetail(user, project, fileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Observable.zip(p, f, (p1, f1) -> {
            if (p1.code == 0 && f1.code == 0) {
                codingFileResult = f1.data.file;
                projectResult = p1.data;

                if (codingFileResult == null) {
                    showButtomToast("未找到文件，可能已被删除");
                    return false;
                }
                return true;
            }

            return false;
        }).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                finish();
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    AttachmentFileObject folderFile = new AttachmentFileObject(codingFileResult);
                    int projectId = projectResult.id;

                    if (codingFileResult.isImage() || codingFileResult.isGif()) {
                        AttachmentsPhotoDetailActivity_.intent(FileUrlActivity.this)
                                .mProjectObjectId(projectId)
                                .mAttachmentFileObject(folderFile)
                                .start();
                    } else if (AttachmentFileObject.isMd(folderFile.fileType)) {
                        AttachmentsHtmlDetailActivity_.intent(FileUrlActivity.this)
                                .mProjectObjectId(projectId)
                                .mAttachmentFileObject(folderFile)
                                .start();
                    } else if (AttachmentFileObject.isTxt(folderFile.fileType)) {
                        AttachmentsTextDetailActivity_.intent(FileUrlActivity.this)
                                .mProjectObjectId(projectId)
                                .mAttachmentFileObject(folderFile)
                                .start();
                    } else {
                        AttachmentsDownloadDetailActivity_.intent(FileUrlActivity.this)
                                .mProjectObjectId(projectId)
                                .mAttachmentFileObject(folderFile)
                                .start();
                    }
                }

                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    public static final class Param implements Serializable {

        private static final long serialVersionUID = -7005258403758123690L;

        public String user;
        public String project;
        public int id;
        boolean isFile;

        public boolean isDir() {
            return !isFile;
        }

        public Param(String user, String project, int file, boolean isFile) {
            this.user = user;
            this.project = project;
            this.id = file;
            this.isFile = isFile;
        }
    }
}
