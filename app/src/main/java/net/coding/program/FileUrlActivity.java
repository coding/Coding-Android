package net.coding.program;

import net.coding.program.common.Global;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.AttachmentFolderObject;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.detail.AttachmentsActivity_;
import net.coding.program.project.detail.AttachmentsDownloadDetailActivity_;
import net.coding.program.project.detail.AttachmentsHtmlDetailActivity_;
import net.coding.program.project.detail.AttachmentsPicDetailActivity_;
import net.coding.program.project.detail.AttachmentsTextDetailActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EActivity(R.layout.activity_file_url)
public class FileUrlActivity extends BaseActivity {

    public static final String HOST_PROJECT = Global.HOST_API + "/user/%s/project/%s";
    public static final String PATTERN_DIR = "^(?:https://[\\w.]*)?/u/([\\w.-]+)/p/([\\w.-]+)/attachment/([\\w.-]+)$";
    public static final String PATTERN_DIR_FILE = "^(?:https://[\\w.]*)?/u/([\\w.-]+)/p/([\\w.-]+)/attachment/([\\w.-]+)/preview/([\\d]+)$";
    final String HOST_FILE = Global.HOST_API + "/project/%s/files/%s/view";
    @Extra
    String url;

    private String dirId;
    private String fileId;
    private int projectId;

    @AfterViews
    public void parseUrl() {
        Pattern pattern = Pattern.compile(PATTERN_DIR);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String user = matcher.group(1);
            String project = matcher.group(2);
            dirId = matcher.group(3);


            String projectUrl = String.format(HOST_PROJECT, user, project);
            getNetwork(projectUrl, HOST_PROJECT);
            return;
        }

        pattern = Pattern.compile(PATTERN_DIR_FILE);
        matcher = pattern.matcher(url);
        if (matcher.find()) {
            String user = matcher.group(1);
            String project = matcher.group(2);
            dirId = matcher.group(3);
            fileId = matcher.group(4);

            String projectUrl = String.format(HOST_PROJECT, user, project);
            getNetwork(projectUrl, PATTERN_DIR_FILE);
            return;
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_PROJECT)) {
            if (code == 0) {
                ProjectObject projectObject = new ProjectObject(respanse.optJSONObject("data"));
                projectId = projectObject.getId();

                AttachmentFolderObject folder = new AttachmentFolderObject();
                folder.file_id = dirId;
                folder.name = "";
                AttachmentsActivity_.intent(this)
                        .mAttachmentFolderObject(folder)
                        .mProjectObjectId(projectId)
                        .start();
                overridePendingTransition(0, 0);
            } else {
                showErrorMsg(code, respanse);
            }
            finish();
        } else if (tag.equals(PATTERN_DIR_FILE)) {
            if (code == 0) {
                ProjectObject projectObject = new ProjectObject(respanse.optJSONObject("data"));
                projectId = projectObject.getId();

                String fileUrl = String.format(HOST_FILE, projectId, fileId);
                getNetwork(fileUrl, HOST_FILE);
            } else {
                showErrorMsg(code, respanse);
                finish();
            }
        } else if (tag.equals(HOST_FILE)) {
            if (code == 0) {
                AttachmentFileObject fileObject = new AttachmentFileObject(respanse.optJSONObject("data").optJSONObject("file"));

                AttachmentFolderObject folder = new AttachmentFolderObject();
                AttachmentFileObject folderFile = fileObject;

                if (fileObject.isImage() || fileObject.isGif()) {
                    AttachmentsPicDetailActivity_.intent(this)
                            .mProjectObjectId(projectId)
                            .mAttachmentFolderObject(folder)
                            .mAttachmentFileObject(folderFile)
                            .start();

                } else if (fileObject.isMd()) {
                    AttachmentsHtmlDetailActivity_.intent(this)
                            .mProjectObjectId(projectId)
                            .mAttachmentFolderObject(folder)
                            .mAttachmentFileObject(folderFile)
                            .start();
                } else if (fileObject.isTxt()) {
                    AttachmentsTextDetailActivity_.intent(this)
                            .mProjectObjectId(projectId)
                            .mAttachmentFolderObject(folder)
                            .mAttachmentFileObject(folderFile)
                            .start();
                } else {
                    AttachmentsDownloadDetailActivity_.intent(this)
                            .mProjectObjectId(projectId)
                            .mAttachmentFolderObject(folder)
                            .mAttachmentFileObject(folderFile)
                            .start();
                }

                overridePendingTransition(0, 0);
            } else {
                showErrorMsg(code, respanse);
            }
            finish();
        }
    }
}
