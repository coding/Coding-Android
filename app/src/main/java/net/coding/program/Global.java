package net.coding.program;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Browser;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import net.coding.program.common.HtmlContent;
import net.coding.program.common.MyImageGetter;
import net.coding.program.maopao.MaopaoDetailActivity;
import net.coding.program.maopao.MaopaoDetailActivity_;
import net.coding.program.message.MessageListActivity_;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.AttachmentFolderObject;
import net.coding.program.project.detail.AttachmentsActivity_;
import net.coding.program.project.detail.AttachmentsDownloadDetailActivity_;
import net.coding.program.project.detail.AttachmentsHtmlDetailActivity_;
import net.coding.program.project.detail.AttachmentsPicDetailActivity_;
import net.coding.program.project.detail.AttachmentsTextDetailActivity_;
import net.coding.program.project.detail.ProjectActivity;
import net.coding.program.project.detail.ProjectActivity_;
import net.coding.program.project.detail.TaskAddActivity;
import net.coding.program.project.detail.TaskAddActivity_;
import net.coding.program.project.detail.TopicListDetailActivity;
import net.coding.program.project.detail.TopicListDetailActivity_;
import net.coding.program.user.UserDetailActivity_;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.XMLReader;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cc191954 on 14-8-233.
 */
public class Global {
    public static final String HOST_CODING = "https://coding.net";

    public static String HOST = HOST_CODING;

    public static SimpleDateFormat DateFormatTime = new SimpleDateFormat("HH:mm");

    private static SimpleDateFormat DayFormatTime = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat MonthDayFormatTime = new SimpleDateFormat("MMMdd日");

    public static SimpleDateFormat WeekFormatTime = new SimpleDateFormat("EEE");
    public static SimpleDateFormat NextWeekFormatTime = new SimpleDateFormat("下EEE");
    public static SimpleDateFormat LastWeekFormatTime = new SimpleDateFormat("上EEE");

    public static String dayFromTime(long time) {
        return DayFormatTime.format(time);
    }

    public static long longFromDay(String day) throws ParseException {
        final String format = "yyyy-MM-dd";
        final SimpleDateFormat sd = new SimpleDateFormat(format);
        return sd.parse(day).getTime();
    }

    public static String dayCount(long time) {
        return DayFormatTime.format(time);
    }

    public static void errorLog(Exception e) {
        e.printStackTrace();
        Log.e("", "" + e);
    }

    public static String sha1(String s) throws Exception {
        return SimpleSHA1.SHA1(s);
    }

    public static void copy(String content, Context context) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content);
    }

    public static String replaceAvatar(JSONObject json) throws JSONException {
        return replaceUrl(json, "avatar");
    }

    public static String getErrorMsg(JSONObject jsonObject) {
        String s = "";
        try {
            JSONObject jsonData = jsonObject.getJSONObject("msg");
            String key = (String) jsonData.keys().next();
            s = jsonData.getString(key);
        } catch (Exception e) {
        }

        return s;
    }

    public static String replaceUrl(JSONObject json, String name) throws JSONException {
        String s = json.optString(name);
        if (s.indexOf("/static") == 0) {
            return Global.HOST + s;
        }

        return s;
    }

    public static void popSoftkeyboard(Context ctx, View view, boolean wantPop) {
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (wantPop) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static Html.TagHandler tagHandler = new Html.TagHandler() {
        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
            if (tag.toLowerCase().equals("code") && !opening) {
                output.append("\n\n");
            }
        }
    };

    public static String makeSmallUrl(ImageView view, String url) {
        String realUrl = url.split("\\?")[0];

        if (url.indexOf("http") == 0) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            String width = intToString(lp.width);
            String height = intToString(lp.height);

            // 如果初始化的时候没有长宽，默认取高度为120dp缩略图
            if (width.isEmpty() && height.isEmpty()) {
                height = String.valueOf(Global.dpToPx(120));

            }
            String smallImageUrl = String.format("?imageMogr2/thumbnail/!%sx%s", width, height);
            return realUrl + smallImageUrl;
        } else {
            return realUrl;
        }

    }

    private static String intToString(int length) {
        String width;
        if (length > 0) {
            width = String.valueOf(length);
        } else {
            width = "";
        }

        return width;
    }

    public static int dpToPx(int dpValue) {
        return (int) (dpValue * MyApp.sScale + 0.5f);
    }

    public static int pxToDp(float pxValue) {
        return (int) (pxValue / MyApp.sScale + 0.5f);
    }

    public static Spannable changeHyperlinkColor(String content) {
        return Global.changeHyperlinkColor(content, null, null);
    }

    public static Spannable changeHyperlinkColor(String content, int color, MyImageGetter imageGetter) {
        return Global.changeHyperlinkColor(content, imageGetter, null);
    }

    public static Spannable changeHyperlinkColor(String content, Html.ImageGetter imageGetter, Html.TagHandler tagHandler) {
        return changeHyperlinkColor(content, imageGetter, tagHandler, 0xFF3BBD79);
    }

    public static Spannable changeHyperlinkColor(String content, Html.ImageGetter imageGetter, Html.TagHandler tagHandler, int color) {
        Spannable s = (Spannable) Html.fromHtml(content, imageGetter, tagHandler);
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL(), color);
            s.setSpan(span, start, end, 0);
        }
        return s;
    }

    public static Spannable recentMessage(String content, Html.ImageGetter imageGetter, Html.TagHandler tagHandler) {
        String parse = parseNoMonkeyImage(content);

        Spannable s = (Spannable) Html.fromHtml(parse, imageGetter, null);
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL(), 0xff999999);
            s.setSpan(span, start, end, 0);
        }
        return s;
    }

    private static String parseNoMonkeyImage(String s) {
        s = s.replaceAll("<img class=\"emotion monkey\".*?title=\"(.*?)\">", "[$1]");

        // 新的图片格式
        final String regx = "(?:<br>)? ?<a href=\".*?\" target=\"_blank\" class=\"bubble-markdown-image-link\".*?><img src=\"(.*?)\" alt=\"(.*?)\".*?></a>(?:<br>)? ?";
        s = s.replaceAll(regx, "[图片]");

        // 旧的图片格式
        final String photoOld = "<div class='message-image-box'><a href='.*?' target='_blank'><img class='message-image' src='(.*?)'/></a></div>";
        s = s.replaceAll(photoOld, "[图片]");

        final String code = "(<pre>)?<code .*(\\n)?</code>(</pre>)?";
        s = s.replaceAll(code, "[代码]");

        final String html = "<([A-Za-z][A-Za-z0-9]*)[^>]*>(.*?)</\\1>";
        s = s.replaceAll(html, "$2");

        return s;
    }

    public static class URLSpanNoUnderline extends URLSpan {

        private int color;

        public URLSpanNoUnderline(String url, int color) {
            super(url);
            this.color = color;

            Log.d("", "dduu " + url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
            ds.setColor(color);
        }

        @Override
        public void onClick(View widget) {
            openActivityByUri(widget.getContext(), getURL(), false);
        }
    }

    public static void openActivityByUri(Context context, String uriString, boolean newTask) {
        Intent intent = new Intent();
        if (newTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        // 用户名
        final String atSomeOne = "^(?:https://[\\w.-]+)?/u/([\\w.-]+)$";
        Pattern pattern = Pattern.compile(atSomeOne);
        Matcher matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            String global = matcher.group(1);
            intent.setClass(context, UserDetailActivity_.class);
            intent.putExtra("globalKey", global);
            context.startActivity(intent);
            return;
        }

        // 项目讨论
        // https://coding.net/u/8206503/p/AndroidCoding/topic/9638?page=1
        final String topic = "^https://[\\w.-]*/u/([\\w.-]+)/p/([\\w.-]+)/topic/(\\w+)(?:\\?[\\w=&-]*)?$";
        pattern = Pattern.compile(topic);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            intent.setClass(context, TopicListDetailActivity_.class);
            TopicListDetailActivity.TopicDetailParam param =
                    new TopicListDetailActivity.TopicDetailParam(matcher.group(1),
                            matcher.group(2), matcher.group(3));
            intent.putExtra("mJumpParam", param);
            context.startActivity(intent);
            return;
        }

        // 项目
        // https://coding.net/u/8206503/p/AndroidCoding
        final String project = "^https://[\\w.-]+/u/([\\w.-]+)/p/([\\w.-]+)$";
        pattern = Pattern.compile(project);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            intent.setClass(context, ProjectActivity_.class);
            ProjectActivity.ProjectJumpParam param = new ProjectActivity.ProjectJumpParam(
                    matcher.group(1), matcher.group(2)
            );
            intent.putExtra("mJumpParam", param);
            context.startActivity(intent);
            return;
        }

        // 冒泡
        // https://coding.net/u/8206503/pp/9275
        final String maopao = "^https://[\\w.-]+/u/([\\w.-]+)/pp/([\\w.-]+)$";
        pattern = Pattern.compile(maopao);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            intent.setClass(context, MaopaoDetailActivity_.class);
            MaopaoDetailActivity.ClickParam param = new MaopaoDetailActivity.ClickParam(
                    matcher.group(1), matcher.group(2));
            intent.putExtra("mClickParam", param);
            context.startActivity(intent);
            return;
        }

        // 任务详情
        // https://coding.net/u/wzw/p/coding/task/9220
        final String task = "^https://[\\w.-]*/u/(\\w+)/p/([\\w-]+)/task/(\\w+)$";
        pattern = Pattern.compile(task);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            Log.d("", "gg " + matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3));
            intent.setClass(context, TaskAddActivity_.class);
            intent.putExtra("mJumpParams", new TaskAddActivity.TaskJumpParams(matcher.group(1),
                    matcher.group(2), matcher.group(3)));
            context.startActivity(intent);
            return;
        }

        // 私信推送
        // https://coding.net/user/messages/history/1984
        final String message = "^https://[\\w.-]*/user/messages/history/([\\w-]+)$";
        pattern = Pattern.compile(message);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            Log.d("", "gg " + matcher.group(1));
            intent.setClass(context, MessageListActivity_.class);
            intent.putExtra("mGlobalKey", matcher.group(1));
            context.startActivity(intent);
            return;
        }

        // 文件夹，这个url后面的字段是添加上去的
        // https://coding.net/u/8206503/p/TestIt2/attachment/65138/projectid/5741/name/aa.jpg
        final String dir = "^https://[\\w.-]+/u/([\\w.-]+)/p/([\\w.-]+)/attachment/([\\w.-]+)/projectid/([\\d]+)/name/(.*+)$";
        pattern = Pattern.compile(dir);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            AttachmentFolderObject folder = new AttachmentFolderObject();
            folder.file_id = matcher.group(3);
            folder.name = matcher.group(5);
            AttachmentsActivity_.intent(context)
                    .mAttachmentFolderObject(folder)
                    .mProjectObjectId(matcher.group(4))
                    .start();
            return;
        }

        // 文件，这个url后面的字段是添加上去的
        // https://coding.net/u/8206503/p/TestIt2/attachment/65138/preview/66171/projectid/5741/name/aa.jpg
        final String dirFile = "^https://[\\w.-]+/u/([\\w.-]+)/p/([\\w.-]+)/attachment/([\\w.-]+)/preview/([\\d]+)/projectid/([\\d]+)/name/(.*+)$";
        pattern = Pattern.compile(dirFile);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            AttachmentFolderObject folder = new AttachmentFolderObject();
            folder.name = matcher.group(3);

            AttachmentFileObject folderFile = new AttachmentFileObject();
            folderFile.file_id = matcher.group(4);
            folderFile.name = matcher.group(6);

            String projectId = matcher.group(5);

            String extension = folderFile.name.toLowerCase();
            final String imageType = ".*\\.(gif|png|jpeg|jpg)$";
            final String htmlMdType = ".*\\.(html|htm|markd|markdown|md|mdown)$";
            final String txtType = ".*\\.(txt)$";
            if (extension.matches(imageType)) {
                AttachmentsPicDetailActivity_.intent(context)
                        .mProjectObjectId(projectId)
                        .mAttachmentFolderObject(folder)
                        .mAttachmentFileObject(folderFile)
                        .start();

            } else if (extension.matches(htmlMdType)) {
                AttachmentsHtmlDetailActivity_.intent(context)
                        .mProjectObjectId(projectId)
                        .mAttachmentFolderObject(folder)
                        .mAttachmentFileObject(folderFile)
                        .start();

            } else if (extension.matches(txtType)) {
                AttachmentsTextDetailActivity_.intent(context)
                        .mProjectObjectId(projectId)
                        .mAttachmentFolderObject(folder)
                        .mAttachmentFileObject(folderFile)
                        .start();
            } else {
                AttachmentsDownloadDetailActivity_.intent(context)
                        .mProjectObjectId(projectId)
                        .mAttachmentFolderObject(folder)
                        .mAttachmentFileObject(folderFile)
                        .start();
            }

            return;
        }


        final String imageSting = "(http|https):.*?.[.]{1}(gif|jpg|png|bmp)";
        pattern = Pattern.compile(imageSting);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            intent.setClass(context, ImagePagerActivity_.class);
            intent.putExtra("mSingleUri", uriString);
            context.startActivity(intent);
            return;
        }

        // 加了自定义图片前缀的链接
        if (uriString.indexOf(HtmlContent.TYPE_IMAGE_HEAD) == 0) {
//            String imageUrl = uriString.replaceFirst(HtmlContent.TYPE_IMAGE_HEAD, "");
//                intent.setClass(context, ImagePagerActivity_.class);
//                intent.putExtra("mSingleUri", imageUrl);
//                intent.putExtra("isPrivate", true);
//                context.startActivity(intent);

            return;
        }

        Uri uri = Uri.parse(uriString);
        intent = new Intent(Intent.ACTION_VIEW, uri);
        if (newTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        context.startActivity(intent);
    }

    public static class MessageParse {
        public String text = "";
        public ArrayList<String> uris = new ArrayList<String>();

        public String toString() {
            String s = "text " + text + "\n";
            for (int i = 0; i < uris.size(); ++i) {
                s += uris.get(i) + "\n";
            }
            return s;
        }
    }

    private static class SimpleSHA1 {
        private static String convertToHex(byte[] data) {
            StringBuilder buf = new StringBuilder();
            for (byte b : data) {
                int halfbyte = (b >>> 4) & 0x0F;
                int two_halfs = 0;
                do {
                    buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                    halfbyte = b & 0x0F;
                } while (two_halfs++ < 1);
            }
            return buf.toString();
        }

        public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            byte[] sha1hash = md.digest();
            return convertToHex(sha1hash);
        }
    }

    public static String dayToNow(long time) {
        Calendar now = Calendar.getInstance();

        long minute = (now.getTimeInMillis() - time) / 60000;
        if (minute < 60) {
            if (minute == 0) {
                return "刚刚";
            } else {
                return minute + "分钟前";
            }
        }

        long hour = minute / 60;
        if (hour < 24) {
            return hour + "小时前";
        }

        long day = hour / 24;
        if (day < 30) {
            return day + "天前";
        }

        long month = day / 30;
        if (month < 11) {
            return month + "个月前";
        }

        long year = month / 12;
        return year + "年前";
    }

    public static boolean isKK() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = isKK();

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static DecimalFormat df = new java.text.DecimalFormat("#.00");

    /**
     * 显示文件大小,保留两位
     *
     * @param size
     * @return
     */
    public static String HumanReadableFilesize(double size) {
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
        double mod = 1024.0;
        int i = 0;
        while (size >= mod) {
            size /= mod;
            i++;
        }
        //return Math.round(size) + units[i];
        return df.format(size) + " " + units[i];
    }
}
