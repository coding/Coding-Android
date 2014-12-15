package net.coding.program.model;

import android.graphics.Color;

import net.coding.program.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Created by yangzhen on 2014/10/25.
 */
public class AttachmentFileObject implements Serializable {

    public static final int ROLE_TYPE_OWNER = 100;

    // .file-icon.doc,.file-icon.docx{background-color:#4a83dc}
    // .file-icon.ppt,.file-icon.pptx{background-color:#fcba17}
    // .file-icon.pdf{background-color:#ff0034}
    // .file-icon.xls,.file-icon.xlsx{background-color:#00c075}
    // .file-icon.txt{background-color:#b5bbc4}
    // .file-icon.rar,.file-icon.zip{background-color:#8e6dd2}
    // .file-icon.html,.file-icon.markd,.file-icon.markdown,.file-icon.md,.file-icon.mdown{background-color:#c5f0e9}

    public static String imagePatternStr = "(gif|png|jpeg|jpg)"; // /\.(gif|png|jpeg|jpg)$/
    static Pattern imagePattern = Pattern.compile(imagePatternStr);

    static String docPatternStr = "(doc|docx)";
    static Pattern docPattern = Pattern.compile(docPatternStr);
    static int docIconBgColor = Color.parseColor("#4a83dc");

    static String pptPatternStr = "(ppt|pptx)";
    static Pattern pptPattern = Pattern.compile(pptPatternStr);
    static int pptIconBgColor = Color.parseColor("#fcba17");

    static String pdfPatternStr = "(pdf)";
    static Pattern pdfPattern = Pattern.compile(pdfPatternStr);
    static int pdfIconBgColor = Color.parseColor("#ff0034");

    static String xlsPatternStr = "(xls|xlsx)";
    static Pattern xlsPattern = Pattern.compile(xlsPatternStr);
    static int xlsIconBgColor = Color.parseColor("#00c075");

    static String txtPatternStr = "(txt)";
    static Pattern txtPattern = Pattern.compile(txtPatternStr);
    static int txtIconBgColor = Color.parseColor("#b5bbc4");

    static String zipPatternStr = "(rar|zip|7z)";
    static Pattern zipPattern = Pattern.compile(zipPatternStr);
    static int zipIconBgColor = Color.parseColor("#8e6dd2");

    static String htmlPatternStr = "(html|htm)";
    static Pattern htmlPattern = Pattern.compile(htmlPatternStr);
    static int htmlIconBgColor = Color.parseColor("#c5f0e9");

    static String mdPatternStr = "(markd|markdown|md|mdown)";
    static Pattern mdPattern = Pattern.compile(mdPatternStr);
    static int mdIconBgColor = Color.parseColor("#c5f0e9");

    static String aiPatternStr = "(ai)";
    static Pattern aiPattern = Pattern.compile(aiPatternStr);
    static int aiIconBgColor = Color.parseColor("#000000");

    static String apkPatternStr = "(apk)";
    static Pattern apkPattern = Pattern.compile(apkPatternStr);
    static int apkIconBgColor = Color.parseColor("#000000");

    static String psdPatternStr = "(psd)";
    static Pattern psdPattern = Pattern.compile(psdPatternStr);
    static int psdIconBgColor = Color.parseColor("#000000");

    static String soundPatternStr = "(mp3|aac|m4a|wma|flac|ape|wav|ogg)";
    static Pattern soundPattern = Pattern.compile(soundPatternStr);
    static int soundIconBgColor = Color.parseColor("#000000");

    static String videoPatternStr = "(3gp|mp4|rmvb|avi|wmv|flv|rm|mkv)";
    static Pattern videoPattern = Pattern.compile(videoPatternStr);
    static int videoIconBgColor = Color.parseColor("#000000");

    static int defaultIconBgColor = Color.parseColor("#000000");

    public long created_at;
    public int current_user_role_id;
    public String fileType = "";// "xlsx"
    public String file_id = "";
    public String name = "";
    public UserObject owner = new UserObject();
    public String owner_id = "";
    public String owner_preview = "";
    public String parent_id = "";
    public String preview = "";
    public int size = 0;
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

    public AttachmentFileObject() {
    }

    public AttachmentFileObject(JSONObject json) throws JSONException {
        created_at = json.optLong("created_at");
        current_user_role_id = json.optInt("current_user_role_id");
        fileType = json.optString("fileType");
        file_id = json.optString("file_id");
        name = json.optString("name");
        if (json.has("owner")) {
            owner = new UserObject(json.getJSONObject("owner"));
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
    }

    public boolean isImage() {
        return imagePattern.matcher(this.fileType).find();
    }

    public boolean isDoc() {
        return docPattern.matcher(this.fileType).find();
    }

    public boolean isPpt() {
        return pptPattern.matcher(this.fileType).find();
    }

    public boolean isPdf() {
        return pdfPattern.matcher(this.fileType).find();
    }

    public boolean isXls() {
        return xlsPattern.matcher(this.fileType).find();
    }

    public boolean isTxt() {
        return txtPattern.matcher(this.fileType).find();
    }

    public boolean isZip() {
        return zipPattern.matcher(this.fileType).find();
    }

    public boolean isHtml() {
        return htmlPattern.matcher(this.fileType).find();
    }

    public boolean isMd() {
        return mdPattern.matcher(this.fileType).find();
    }

    public boolean isAi() {
        return aiPattern.matcher(this.fileType).find();
    }

    public boolean isApk() {
        return apkPattern.matcher(this.fileType).find();
    }

    public boolean isPsd() {
        return psdPattern.matcher(this.fileType).find();
    }

    public boolean isSound() {
        return soundPattern.matcher(this.fileType).find();
    }

    public boolean isVideo() {
        return videoPattern.matcher(this.fileType).find();
    }

    public int getBackgroundColor() {
        if (this.isDoc()) {
            return docIconBgColor;
        } else if (this.isPpt()) {
            return pptIconBgColor;
        } else if (this.isPdf()) {
            return pdfIconBgColor;
        } else if (this.isXls()) {
            return xlsIconBgColor;
        } else if (this.isTxt()) {
            return txtIconBgColor;
        } else if (this.isZip()) {
            return zipIconBgColor;
        } else if (this.isHtml()) {
            return htmlIconBgColor;
        } else if (this.isMd()) {
            return mdIconBgColor;
        } else {
            return defaultIconBgColor;
        }
    }

    public int getIconResourceId() {
        if (this.isAi()) {
            return R.drawable.ic_file_ai;
        } else if (this.isApk()) {
            return R.drawable.ic_file_apk;
        } else if (this.isDoc()) {
            return R.drawable.ic_file_docx;
        } else if (this.isHtml()) {
            return R.drawable.ic_file_html;
        } else if (this.isMd()) {
            return R.drawable.ic_file_md;
        } else if (this.isPdf()) {
            return R.drawable.ic_file_pdf;
        } else if (this.isPpt()) {
            return R.drawable.ic_file_ppt;
        } else if (this.isPsd()) {
            return R.drawable.ic_file_psd;
        } else if (this.isSound()) {
            return R.drawable.ic_file_sound;
        } else if (this.isTxt()) {
            return R.drawable.ic_file_txt;
        } else if (this.isVideo()) {
            return R.drawable.ic_file_video;
        } else if (this.isXls()) {
            return R.drawable.ic_file_x;
        } else if (this.isZip()) {
            return R.drawable.ic_file_zip;
        } else {
            return R.drawable.ic_file_unknown;
        }
    }

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

    public boolean isOwner() {
        return current_user_role_id == ROLE_TYPE_OWNER;
    }

}
