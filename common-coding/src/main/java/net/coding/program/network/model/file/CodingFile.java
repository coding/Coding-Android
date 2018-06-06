package net.coding.program.network.model.file;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.model.AttachmentFileObject;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.RequestData;
import net.coding.program.common.model.UserObject;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Created by chenchao on 2017/5/15.
 */
public class CodingFile implements Serializable {

    private static final long serialVersionUID = 101080554918581348L;

    public static final int ROLE_TYPE_OWNER = 100;
    public static final int SHARE_FOLDER = -1;

    @SerializedName("preview")
    @Expose
    public String preview = "";
    @SerializedName("fileType")
    @Expose
    public String fileType = "";
    @SerializedName("current_user_role_id")
    @Expose
    public int currentUserRoleId;
    @SerializedName("owner_preview")
    @Expose
    public String ownerPreview = "";
    @SerializedName("number")
    @Expose
    public int number;
    @SerializedName("comments")
    @Expose
    public int comments;
    @SerializedName("image_info_url")
    @Expose
    public String imageInfoUrl = "";
    @SerializedName("version")
    @Expose
    public int version;
    @SerializedName("count")
    @Expose
    public int count;
    @SerializedName("owner_id")
    @Expose
    public int ownerId;
    @SerializedName("parent_id")
    @Expose
    public int parentId;
    @SerializedName("created_at")
    @Expose
    public long createdAt;
    @SerializedName("updated_at")
    @Expose
    public long updatedAt;
    @SerializedName("deleted_at")
    @Expose
    public String deletedAt = "";
    @SerializedName("type")
    @Expose
    public int type; // 0 文件夹 2 png 文件
    @SerializedName("size")
    @Expose
    public long size;
    @SerializedName("name")
    @Expose
    public String name = "";
    @SerializedName("storage_type")
    @Expose
    public String storageType = "";
    @SerializedName("storage_key")
    @Expose
    public String storageKey = "";
    @SerializedName("history_id")
    @Expose
    public int historyId;
    @SerializedName(value = "id", alternate = "file_id")
    @Expose
    public int id;
    @SerializedName("owner")
    @Expose
    public UserObject owner;
    @SerializedName("url")
    @Expose
    public String url = "";
    @SerializedName("share")
    @Expose
    public Share share;

    public static final int MAX_PROGRESS = 100;

    public int downloadProgress = 0; // max 1000
    public BaseDownloadTask task;

    public static CodingFile craeteShareFolder() {
        CodingFile codingFile = new CodingFile();
        codingFile.id = SHARE_FOLDER;
        codingFile.name = "分享中";
        return codingFile;
    }

    public boolean isShareFolder() {
        return id == SHARE_FOLDER;
    }

    private static final String SAVE_NAME_SPLIT = "|||";
    public static String imagePatternStr = "(gif|png|jpeg|jpg|GIF|PNG|JPEG|JPG)"; // /\.(gif|png|jpeg|jpg)$/
    static Pattern imagePattern = Pattern.compile(imagePatternStr);
    static String docPatternStr = "(doc|docx)";
    static Pattern docPattern = Pattern.compile(docPatternStr);
    static String pptPatternStr = "(ppt|pptx)";
    static Pattern pptPattern = Pattern.compile(pptPatternStr);
    static String pdfPatternStr = "(pdf)";
    static Pattern pdfPattern = Pattern.compile(pdfPatternStr);
    static String xlsPatternStr = "(xls|xlsx)";
    static Pattern xlsPattern = Pattern.compile(xlsPatternStr);
    static String txtPatternStr = "(sh|txt)";
    static Pattern txtPattern = Pattern.compile(txtPatternStr);
    static String zipPatternStr = "(rar|zip|7z)";
    static Pattern zipPattern = Pattern.compile(zipPatternStr);
    static String htmlPatternStr = "(html|htm)";
    static Pattern htmlPattern = Pattern.compile(htmlPatternStr);
    static String mdPatternStr = "(markd|markdown|md|mdown)";
    static Pattern mdPattern = Pattern.compile(mdPatternStr);
    static String aiPatternStr = "(ai)";
    static Pattern aiPattern = Pattern.compile(aiPatternStr);
    static String apkPatternStr = "(apk)";
    static Pattern apkPattern = Pattern.compile(apkPatternStr);
    static String psdPatternStr = "(psd)";
    static Pattern psdPattern = Pattern.compile(psdPatternStr);
    static String soundPatternStr = "(mp3|aac|m4a|wma|flac|ape|wav|ogg)";
    static Pattern soundPattern = Pattern.compile(soundPatternStr);
    static String videoPatternStr = "(3gp|mp4|rmvb|avi|wmv|flv|rm|mkv)";
    static Pattern videoPattern = Pattern.compile(videoPatternStr);

    public static boolean isImage(String suffix) {
        return imagePattern.matcher(suffix.toLowerCase()).find();
    }

    public static boolean isDoc(String fileName) {
        return docPattern.matcher(fileName.toLowerCase()).find();
    }

    public static boolean isPpt(String fileName) {
        return pptPattern.matcher(fileName.toLowerCase()).find();
    }

    public static boolean isPdf(String fileName) {
        return pdfPattern.matcher(fileName.toLowerCase()).find();
    }

    public static boolean isXls(String fileName) {
        return xlsPattern.matcher(fileName.toLowerCase()).find();
    }

    public static boolean isTxt(String fileName) {
        return txtPattern.matcher(fileName.toLowerCase()).find();
    }

    public static boolean isZip(String fileName) {
        return zipPattern.matcher(fileName.toLowerCase()).find();
    }

    public static boolean isHtml(String fileName) {
        return htmlPattern.matcher(fileName.toLowerCase()).find();
    }

    public static boolean isMd(String fileName) {
        return mdPattern.matcher(fileName.toLowerCase()).find();
    }

    public static boolean isAi(String fileName) {
        return aiPattern.matcher(fileName.toLowerCase()).find();
    }

    public static boolean isApk(String fileName) {
        return apkPattern.matcher(fileName.toLowerCase()).find();
    }

    public static boolean isPsd(String fileName) {
        return psdPattern.matcher(fileName.toLowerCase()).find();
    }

    public static boolean isSound(String fileName) {
        return soundPattern.matcher(fileName.toLowerCase()).find();
    }

    public static boolean isVideo(String fileName) {
        return videoPattern.matcher(fileName.toLowerCase()).find();
    }

    public static int getIconResourceId(String fileName) {
        if (AttachmentFileObject.isAi(fileName)) {
            return R.drawable.ic_file_ai;
        } else if (AttachmentFileObject.isApk(fileName)) {
            return R.drawable.ic_file_apk;
        } else if (AttachmentFileObject.isDoc(fileName)) {
            return R.drawable.ic_file_docx;
        } else if (AttachmentFileObject.isHtml(fileName)) {
            return R.drawable.ic_file_html;
        } else if (AttachmentFileObject.isMd(fileName)) {
            return R.drawable.ic_file_md;
        } else if (AttachmentFileObject.isPdf(fileName)) {
            return R.drawable.ic_file_pdf;
        } else if (AttachmentFileObject.isPpt(fileName)) {
            return R.drawable.ic_file_ppt;
        } else if (AttachmentFileObject.isPsd(fileName)) {
            return R.drawable.ic_file_psd;
        } else if (AttachmentFileObject.isSound(fileName)) {
            return R.drawable.ic_file_sound;
        } else if (AttachmentFileObject.isTxt(fileName)) {
            return R.drawable.ic_file_txt;
        } else if (AttachmentFileObject.isVideo(fileName)) {
            return R.drawable.ic_file_video;
        } else if (AttachmentFileObject.isXls(fileName)) {
            return R.drawable.ic_file_x;
        } else if (AttachmentFileObject.isZip(fileName)) {
            return R.drawable.ic_file_zip;
        } else {
            return R.drawable.ic_file_unknown;
        }
    }

    public boolean isShared() {
        return share != null;
    }


    public String getSaveName(int projectId) {
//        修改字段要记得修改 INFO_COUNT
        return projectId + SAVE_NAME_SPLIT +
                id + SAVE_NAME_SPLIT +
                historyId + SAVE_NAME_SPLIT +
                name;
    }

    public RequestData getHttpShareLinkOn(ProjectObject projectObject) {
        String url = Global.HOST_API + "/share/create";
        RequestParams params = new RequestParams();
        params.put("resourceId", id);
        params.put("resourceType", 0);
        params.put("projectId", projectObject.getId());
        params.put("accessType", 0);
        return new RequestData(url, params);
    }


    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public long getSize() {
        return size;
    }

    public boolean isImage() {
        return isImage(fileType);
    }

    public boolean isGif() {
        return this.fileType.toLowerCase().equals("gif");
    }

    public boolean needJump() {
        return isTxt(fileType) || isMd(fileType) || isImage();
    }

    public int getIconResourceId() {
        return getIconResourceId(fileType);
    }

    public boolean isOwner() {
        return currentUserRoleId == ROLE_TYPE_OWNER;
    }

    public boolean isFolder() {
        return type == 0;
    }

    // 已下载
    public boolean isDownloaded() {
        return downloadProgress == MAX_PROGRESS;
    }

    // 下载中
    public boolean isDownloading() {
        return 0 < downloadProgress && downloadProgress < MAX_PROGRESS;
    }

    public boolean isDeleteable() {
        return count == 0;
    }
}
