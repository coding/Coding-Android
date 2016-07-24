package net.coding.program.model;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Created by yangzhen on 2014/10/25.
 * 项目文档中的文件
 */
public class AttachmentFileObject implements Serializable {

    public static final int ROLE_TYPE_OWNER = 100;

    public static final String RESULT = "AttachmentFileObject";
    public static final String ACTION_EDIT = "ACTION_EDIT";
    public static final String ACTION_DELETE = "ACTION_DELETE";

    // .file-icon.doc,.file-icon.docx{background-color:#4a83dc}
    // .file-icon.ppt,.file-icon.pptx{background-color:#fcba17}
    // .file-icon.pdf{background-color:#ff0034}
    // .file-icon.xls,.file-icon.xlsx{background-color:#00c075}
    // .file-icon.txt{background-color:#b5bbc4}
    // .file-icon.rar,.file-icon.zip{background-color:#8e6dd2}
    // .file-icon.html,.file-icon.markd,.file-icon.markdown,.file-icon.md,.file-icon.mdown{background-color:#c5f0e9}
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
    public long created_at;
    public int current_user_role_id;
    public String fileType = "";// "xlsx"
    public String file_id = "";
    public UserObject owner = new UserObject();
    public String owner_id = "";

    /* https://coding.net/api/project/1/files/70699/imagePreview
     * 可以看到图片的原图，如果是其它类型的文件
     * 就需要去掉 imagePreview,推荐的用法是拼接url出来，
     * https://coding.net/api/project/1/files/70699/download
     */
    public String owner_preview = "";
    public String parent_id = "";
    public String preview = "";
    public String storage_key = "";
    public String storage_type = "";
    public int type;
    public long updated_at;
    public boolean isSelected = false;
    public boolean isFolder = false;
    public boolean isDownload = false;
    public long downloadId = 0L;
    public int[] bytesAndStatus;
    public AttachmentFolderObject folderObject;
    private String name = "";
    private int size = 0;
    private int history_id;
    //    private Share share = new Share();
    private String share_url = "";

    public AttachmentFileObject() {
    }

    public AttachmentFileObject(JSONObject json) {
        created_at = json.optLong("created_at");
        current_user_role_id = json.optInt("current_user_role_id");
        fileType = json.optString("fileType");
        file_id = json.optString("file_id");
        name = json.optString("name");
        if (json.has("owner")) {
            owner = new UserObject(json.optJSONObject("owner"));
        }
        owner_id = json.optString("owner_id");
        owner_preview = json.optString("owner_preview");
        parent_id = json.optString("parent_id");
        preview = json.optString("preview");
        size = json.optInt("size");
        storage_key = json.optString("storage_key");
        storage_type = json.optString("storage_type");
        type = json.optInt("type");
        updated_at = json.optLong("updated_at");

        history_id = json.optInt("history_id");

        share_url = json.optString("share_url");
    }

//    public void setShare(Share shareParam) {
//        share = shareParam;
//    }

    public static AttachmentFileObject parseFileObject(AttachmentFolderObject folderObject) {
        AttachmentFileObject returnFileObject = new AttachmentFileObject();

        returnFileObject.created_at = folderObject.created_at;
        //current_user_role_id = json.optString("current_user_role_id");
        returnFileObject.fileType = "dir";
        returnFileObject.isFolder = true;
        returnFileObject.file_id = folderObject.file_id;
        returnFileObject.name = folderObject.getNameCount();
        /*if (json.has("owner")) {
            owner = new UserObject(json.getJSONObject("owner"));
        }*/
        returnFileObject.owner_id = folderObject.owner_id;
        //owner_preview = json.optString("owner_preview");
        returnFileObject.parent_id = folderObject.parent_id;
        //preview = json.optString("preview");
        //size = json.optInt("size");
        //storage_key = json.optString("storage_key");
        //storage_type = json.optString("storage_type");
        //type = json.optInt("type");
        returnFileObject.updated_at = folderObject.updated_at;
        returnFileObject.folderObject = folderObject;
        return returnFileObject;
    }

    public boolean isShared() {
        return !share_url.isEmpty();
    }

    public String getShareLink() {
//        return share.url;
        return share_url;
    }

    public void setShereLink(String link) {
        if (link == null) {
            link = "";
        }
        share_url = link;
    }

    public int getHistory_id() {
        return history_id;
    }

    // 保存在本地的名字
    public static final int INFO_COUNT = 4;

    public String getSaveName(int projectId) {
//        修改字段要记得修改 INFO_COUNT
        return projectId + SAVE_NAME_SPLIT +
                file_id + SAVE_NAME_SPLIT +
                history_id + SAVE_NAME_SPLIT +
                name;
    }

    public RequestData getHttpShareLinkOn(ProjectObject projectObject) {
        String url = Global.HOST_API + "/share/create";
        RequestParams params = new RequestParams();
        params.put("resourceId", file_id);
        params.put("resourceType", 0);
        params.put("projectId", projectObject.getId());
        params.put("accessType", 0);
        return new RequestData(url, params);
    }

    public String getHttpShareLinkOff() {
        int pos = share_url.lastIndexOf("/");
        if (pos == -1) {
            return "";
        }

        String hash = share_url.substring(pos + 1);
        return Global.HOST_API + "/share/" + hash;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public int getSize() {
        return size;
    }

    public boolean isImage() {
        return isImage(fileType);
    }

    public static boolean isImage(String suffix) {
        return imagePattern.matcher(suffix.toLowerCase()).find();
    }

    public boolean isGif() {
        return this.fileType.toLowerCase().equals("gif");
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

    public boolean needJump() {
        return isTxt(fileType) || isMd(fileType) || isImage();
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

    public int getIconResourceId() {
       return getIconResourceId(fileType);
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

    public boolean isOwner() {
        return current_user_role_id == ROLE_TYPE_OWNER;
    }

    public static class Share implements Serializable {

        /**
         * resource_type : 0
         * resource_id : 308811
         * user_id : 7074
         * access_type : 0
         * project_id : 126848
         * overdue : 0
         * created_at : 1440992709000
         * hash : 572e7ead-8b42-4e2f-9d18-b74b48d2195a
         * url : https://coding.net/s/572e7ead-8b42-4e2f-9d18-b74b48d2195a
         */

        int resource_type;
        int resource_id;
        int user_id;
        int access_type;
        int project_id;
        int overdue;
        long created_at;
        String hash = "";
        String url = "";

        public Share() {
        }

        public Share(JSONObject json) {
            resource_type = json.optInt("resource_type");
            resource_id = json.optInt("resource_id");
            user_id = json.optInt("user_id");
            access_type = json.optInt("access_type");
            project_id = json.optInt("project_id");
            overdue = json.optInt("overdue");
            created_at = json.optLong("created_at");
            hash = json.optString("hash");
            url = json.optString("url");
        }

        public String getUrl() {
            return url;
        }
    }
}
