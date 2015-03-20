package net.coding.program.common.htmltext;

import android.content.Context;
import android.content.Intent;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import net.coding.program.FileUrlActivity;
import net.coding.program.FileUrlActivity_;
import net.coding.program.ImagePagerActivity_;
import net.coding.program.TestActivity;
import net.coding.program.WebActivity_;
import net.coding.program.common.Global;
import net.coding.program.maopao.MaopaoDetailActivity;
import net.coding.program.maopao.MaopaoDetailActivity_;
import net.coding.program.message.MessageListActivity_;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.model.AttachmentFolderObject;
import net.coding.program.project.ProjectHomeActivity_;
import net.coding.program.project.detail.AttachmentsActivity_;
import net.coding.program.project.detail.AttachmentsDownloadDetailActivity_;
import net.coding.program.project.detail.AttachmentsHtmlDetailActivity_;
import net.coding.program.project.detail.AttachmentsPicDetailActivity_;
import net.coding.program.project.detail.AttachmentsTextDetailActivity_;
import net.coding.program.project.detail.ProjectActivity;
import net.coding.program.project.detail.ProjectActivity_;
import net.coding.program.project.detail.TopicListDetailActivity;
import net.coding.program.project.detail.TopicListDetailActivity_;
import net.coding.program.task.TaskAddActivity;
import net.coding.program.task.TaskAddActivity_;
import net.coding.program.user.UserDetailActivity_;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chaochen on 15/1/12.
 */
public class URLSpanNoUnderline extends URLSpan {

    private int color;

    public URLSpanNoUnderline(String url, int color) {
        super(url);
        this.color = color;
    }

    public static void openActivityByUri(Context context, String uriString, boolean newTask) {
        openActivityByUri(context, uriString, newTask, true);
    }

    public static boolean openActivityByUri(Context context, String uriString, boolean newTask, boolean defaultIntent) {
        Log.d("", "yes reload");
        Intent intent = new Intent();
        if (newTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        // 用户名
        final String atSomeOne = "^(?:https://[\\w.]*)?/u/([\\w.-]+)$";
        Pattern pattern = Pattern.compile(atSomeOne);
        Matcher matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            String global = matcher.group(1);
            intent.setClass(context, UserDetailActivity_.class);
            intent.putExtra("globalKey", global);
            context.startActivity(intent);
            return true;
        }

        // 项目讨论列表
        // https://coding.net/u/8206503/p/TestIt2/topic/mine
        final String topicList = "^(?:https://[\\w.]*)?/u/([\\w.-]+)/p/([\\w.-]+)/topic/(mine|all)$";
        pattern = Pattern.compile(topicList);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            intent.setClass(context, ProjectActivity_.class);
            ProjectActivity.ProjectJumpParam param = new ProjectActivity.ProjectJumpParam(
                    matcher.group(1), matcher.group(2)
            );
            intent.putExtra("mJumpParam", param);
            intent.putExtra("mJumpType", ProjectActivity.ProjectJumpParam.JumpType.typeTopic);
            context.startActivity(intent);
            return true;
        }

        // 单个项目讨论
        // https://coding.net/u/8206503/p/AndroidCoding/topic/9638?page=1
        final String topic = "^(?:https://[\\w.]*)?/u/([\\w.-]+)/p/([\\w.-]+)/topic/([\\w.-]+)(?:\\?[\\w=&-]*)?$";
        pattern = Pattern.compile(topic);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            intent.setClass(context, TopicListDetailActivity_.class);
            TopicListDetailActivity.TopicDetailParam param =
                    new TopicListDetailActivity.TopicDetailParam(matcher.group(1),
                            matcher.group(2), matcher.group(3));
            intent.putExtra("mJumpParam", param);
            context.startActivity(intent);
            return true;
        }

        // 项目
        // https://coding.net/u/8206503/p/AndroidCoding
        //
        final String project = "^(?:https://[\\w.]*)?/u/([\\w.-]+)/p/([\\w.-]+)$";
        pattern = Pattern.compile(project);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            intent.setClass(context, ProjectHomeActivity_.class);
            ProjectActivity.ProjectJumpParam param = new ProjectActivity.ProjectJumpParam(
                    matcher.group(1), matcher.group(2)
            );
            intent.putExtra("mJumpParam", param);
            context.startActivity(intent);
            return true;
        }

        // 冒泡
        // https://coding.net/u/8206503/pp/9275
        final String maopao = "^(?:https://[\\w.]*)?/u/([\\w.-]+)/pp/([\\w.-]+)$";
        pattern = Pattern.compile(maopao);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            intent.setClass(context, MaopaoDetailActivity_.class);
            MaopaoDetailActivity.ClickParam param = new MaopaoDetailActivity.ClickParam(
                    matcher.group(1), matcher.group(2));
            intent.putExtra("mClickParam", param);
            context.startActivity(intent);
            return true;
        }

        // 任务详情
        // https://coding.net/u/wzw/p/coding/task/9220
        final String task = "^(?:https://[\\w.]*)?/u/([\\w.-]+)/p/([\\w-]+)/task/(\\w+)$";
        pattern = Pattern.compile(task);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            Log.d("", "gg " + matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3));
            intent.setClass(context, TaskAddActivity_.class);
            intent.putExtra("mJumpParams", new TaskAddActivity.TaskJumpParams(matcher.group(1),
                    matcher.group(2), matcher.group(3)));
            context.startActivity(intent);
            return true;
        }

        // 私信推送
        // https://coding.net/user/messages/history/1984
        final String message = "^(?:https://[\\w.]*)?/user/messages/history/([\\w-]+)$";
        pattern = Pattern.compile(message);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            Log.d("", "gg " + matcher.group(1));
            intent.setClass(context, MessageListActivity_.class);
            intent.putExtra("mGlobalKey", matcher.group(1));
            context.startActivity(intent);
            return true;
        }

        // 跳转到文件夹，与服务器相同
        pattern = Pattern.compile(FileUrlActivity.PATTERN_DIR);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            FileUrlActivity_.intent(context)
                    .url(uriString)
                    .start();
            return true;
        }

        // 文件夹，这个url后面的字段是添加上去的
        // https://coding.net/u/8206503/p/TestIt2/attachment/65138/projectid/5741/name/aa.jpg
        final String dir = "^(?:https://[\\w.]*)?/u/([\\w.-]+)/p/([\\w.-]+)/attachment/([\\w.-]+)/projectid/([\\d]+)/name/(.*+)$";
        pattern = Pattern.compile(dir);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            AttachmentFolderObject folder = new AttachmentFolderObject();
            folder.file_id = matcher.group(3);
            folder.name = matcher.group(5);
            AttachmentsActivity_.intent(context)
                    .mAttachmentFolderObject(folder)
                    .mProjectObjectId(Integer.valueOf(matcher.group(4)))
                    .start();
            return true;
        }

        pattern = Pattern.compile(FileUrlActivity.PATTERN_DIR_FILE);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            FileUrlActivity_.intent(context)
                    .url(uriString)
                    .start();
            return true;
        }

        // 文件，这个url后面的字段是添加上去的
        // https://coding.net/u/8206503/p/TestIt2/attachment/65138/preview/66171/projectid/5741/name/aa.jpg
        final String dirFile = "^(?:https://[\\w.]*)?/u/([\\w.-]+)/p/([\\w.-]+)/attachment/([\\w.-]+)/preview/([\\d]+)/projectid/([\\d]+)/name/(.*+)$";
        pattern = Pattern.compile(dirFile);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            AttachmentFolderObject folder = new AttachmentFolderObject();
            folder.name = matcher.group(3);

            AttachmentFileObject folderFile = new AttachmentFileObject();
            folderFile.file_id = matcher.group(4);
            folderFile.name = matcher.group(6);

            int projectId = Integer.valueOf(matcher.group(5));

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

            return true;
        }

        final String imageSting = "(http|https):.*?.[.]{1}(gif|jpg|png|bmp)";
        pattern = Pattern.compile(imageSting);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            intent.setClass(context, ImagePagerActivity_.class);
            intent.putExtra("mSingleUri", uriString);
            context.startActivity(intent);
            return true;
        }

//        // 加了自定义图片前缀的链接
//        if (uriString.indexOf(HtmlContent.TYPE_IMAGE_HEAD) == 0) {
////            String imageUrl = uriString.replaceFirst(HtmlContent.TYPE_IMAGE_HEAD, "");
////                intent.setClass(context, ImagePagerActivity_.class);
////                intent.putExtra("mSingleUri", imageUrl);
////                intent.putExtra("isPrivate", true);
////                context.startActivity(intent);
//
//            return true;
//        }

        try {
            if (defaultIntent) {
                intent = new Intent(context, WebActivity_.class);

                if (newTask) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                if (uriString.startsWith("/u/")) {
                    uriString = Global.HOST + uriString;
                }

                intent.putExtra("url", uriString);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(context, "" + uriString.toString(), Toast.LENGTH_LONG).show();
            Global.errorLog(e);
        }

        return false;
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
