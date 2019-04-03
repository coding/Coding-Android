package net.coding.program;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.os.Process;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.liulishuo.filedownloader.FileDownloader;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.PhoneType;
import net.coding.program.common.RedPointTip;
import net.coding.program.common.Unread;
import net.coding.program.common.activity.WebviewDetailActivity_;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.GitFileInfoObject;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.push.PushUrl;
import net.coding.program.common.ui.GlobalUnit;
import net.coding.program.common.util.FileUtil;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.login.auth.AuthListActivity;
import net.coding.program.login.auth.Login2FATipActivity;
import net.coding.program.maopao.MaopaoDetailActivity;
import net.coding.program.maopao.MaopaoDetailActivity_;
import net.coding.program.maopao.MaopaoListBaseFragment;
import net.coding.program.message.MessageListActivity;
import net.coding.program.message.MessageListActivity_;
import net.coding.program.message.NotifyListActivity;
import net.coding.program.message.UsersListFragment;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.pickphoto.detail.ImagePagerActivity_;
import net.coding.program.pickphoto.detail.ImagePagerFragment;
import net.coding.program.project.ProjectHomeActivity_;
import net.coding.program.project.ProjectListFragment;
import net.coding.program.project.detail.AttachmentsDownloadDetailActivity;
import net.coding.program.project.detail.AttachmentsHtmlDetailActivity;
import net.coding.program.project.detail.AttachmentsTextDetailActivity;
import net.coding.program.project.detail.GitViewActivity_;
import net.coding.program.project.detail.ProjectActivity_;
import net.coding.program.project.detail.ProjectDynamicFragment;
import net.coding.program.project.detail.ProjectFunction;
import net.coding.program.project.detail.ProjectGitFragment;
import net.coding.program.project.detail.TaskListFragment;
import net.coding.program.project.detail.TopicListFragment;
import net.coding.program.project.detail.file.v2.ProjectFileMainActivity;
import net.coding.program.project.detail.merge.CommitFileListActivity_;
import net.coding.program.project.detail.merge.MergeDetailActivity_;
import net.coding.program.project.detail.merge.MergeListFragment;
import net.coding.program.project.detail.merge.MergeReviewerListFragment;
import net.coding.program.project.detail.merge.ReleaseDetailActivity;
import net.coding.program.project.detail.merge.ReleaseDetailActivity_;
import net.coding.program.project.detail.topic.TopicListDetailActivity;
import net.coding.program.project.detail.topic.TopicListDetailActivity_;
import net.coding.program.project.detail.wiki.WikiMainActivity;
import net.coding.program.project.detail.wiki.WikiMainActivity_;
import net.coding.program.project.git.BranchMainActivity_;
import net.coding.program.project.maopao.ProjectMaopaoActivity;
import net.coding.program.route.BlankViewDisplay;
import net.coding.program.route.URLSpanNoUnderline;
import net.coding.program.subject.SubjectDetailActivity_;
import net.coding.program.subject.SubjectListFragment;
import net.coding.program.task.AllTasksActivity_;
import net.coding.program.task.add.TaskAddActivity_;
import net.coding.program.task.add.TaskJumpParams;
import net.coding.program.third.MyImageDownloader;
import net.coding.program.user.UserProjectListFragment;
import net.coding.program.user.team.TeamListActivity;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.coding.program.common.Global.makeLogTag;

/**
 * Created by cc191954 on 14-8-9.
 * 用来做一些初始化工作，比如设置 host，
 * 初始化图片库配置
 */
public class MyApp extends MultiDexApplication {

    private static final String TAG = makeLogTag(MyApp.class);

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .diskCacheFileCount(300)
                .imageDownloader(new MyImageDownloader(context))
                .tasksProcessingOrder(QueueProcessingType.LIFO)
//                .writeDebugLogs() // Remove for release app
                .diskCacheExtraOptions(GlobalData.sWidthPix / 3, GlobalData.sWidthPix / 3, null)
                .build();

        ImageLoader.getInstance().init(config);
    }

    private static String getProcessName(Context context) {
        ActivityManager actMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appList = actMgr.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : appList) {
            if (info.pid == android.os.Process.myPid()) {
                return info.processName;
            }
        }
        return "";
    }

    public static void openNewActivityFromMain(Context context, String url) {
        if (TextUtils.isEmpty(url)) return;

        if (GlobalData.getMainActivityState()) {
            URLSpanNoUnderline.openActivityByUri(context, url, true);
        } else {
            Intent mainIntent = new Intent(context, CodingCompat.instance().getMainActivity());
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainIntent);
            URLSpanNoUnderline.openActivityByUri(context, url, true);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            ApplicationInfo info = getApplicationInfo();
            isDebug = (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {

        }

//        if (isInMainProcess(this)) {
        GlobalData.app = this;
//        }

        CodingColor.init(this);

        AccountInfo.CustomHost customHost = AccountInfo.getCustomHost(this);
        String host = customHost.getHost();
        if (host.isEmpty()) {
            host = Global.DEFAULT_HOST;
        } else if (host.equalsIgnoreCase("s")) {
            host = Global.STAGING_HOST;
        } else if (host.equalsIgnoreCase("t")) {
            host = Global.TESTING_HOST;
        }
        Global.HOST = host;
        Global.HOST_API = Global.HOST + "/api";

        try {
            Global.sVoiceDir = FileUtil.getDestinationInExternalFilesDir(this, Environment.DIRECTORY_MUSIC, FileUtil.getDownloadFolder()).getAbsolutePath();
            Log.w("VoiceDir", Global.sVoiceDir);
        } catch (Exception e) {
            Global.errorLog(e);
        }

        MyAsyncHttpClient.init(this);

        initImageLoader(this);

        loadBaiduMap();

        initGlobaData();
        initBlankView();

        RedPointTip.init(this);
        GlobalUnit.init(this);

        FileDownloader.init(this);

        initURLCallback();
    }

    public void initGlobaData() {
        GlobalData.sScale = getResources().getDisplayMetrics().density;
        GlobalData.sWidthPix = getResources().getDisplayMetrics().widthPixels;
        GlobalData.sHeightPix = getResources().getDisplayMetrics().heightPixels;
        GlobalData.sWidthDp = (int) (GlobalData.sWidthPix / GlobalData.sScale);
        GlobalCommon.clickJumpWebView = v -> {
            Object object = v.getTag();
            if (object instanceof String) {
                WebviewDetailActivity_.intent(v.getContext())
                        .comment((String) object)
                        .start();
            }
        };
        GlobalCommon.mOnClickUser = v -> {
            Object tag = v.getTag();
            if (tag instanceof String) {
                String globalKey = (String) tag;
                CodingCompat.instance().launchUserDetailActivity(v.getContext(), globalKey);
            } else if (tag instanceof UserObject) {
                String globalKey = ((UserObject) tag).global_key;
                CodingCompat.instance().launchUserDetailActivity(v.getContext(), globalKey);
            }
        };

        GlobalData.sEmojiNormal = getResources().getDimensionPixelSize(R.dimen.emoji_normal);
        GlobalData.sEmojiMonkey = getResources().getDimensionPixelSize(R.dimen.emoji_monkey);

        GlobalData.sUserObject = AccountInfo.loadAccount(this);
        GlobalData.sUnread = new Unread();

        // todo  路由要想办法，否则后面没法做
        GlobalCommon.rounterMap.put(GlobalCommon.ROUNTER_2FA, Login2FATipActivity.class);
        GlobalCommon.rounterMap.put(GlobalCommon.ROUNTER_AUTH_LIST, AuthListActivity.class);

        GlobalData.compatCallback = new GlobalData.CompatCallback() {
            @Override
            public void lunchSetGKActivity(Context context) {
                CodingCompat.instance().launchSetGKActivity(context);
            }

            @Override
            public void lunchLoginActivity(Context context) {
                context.startActivity(new Intent(context, CodingCompat.instance().getLoginActivity()));
            }
        };
    }

    private void initBlankView() {
        BlankViewDisplay.callback = new BlankViewDisplay.BlankCallback() {
            @Override
            public void setBlank(int itemSize, Object fragment, boolean request, View v, String tipString, int iconId) {
                boolean show = (itemSize == 0);
                if (!show) {
                    v.setVisibility(View.GONE);
                    return;
                }
                v.setVisibility(View.VISIBLE);

                View loading = v.findViewById(R.id.loadingLayout);
                if (loading != null) {
                    loading.setVisibility(View.GONE);
                }

                String text = "";

                if (tipString.isEmpty()) {
                    if (request) {
                        if (fragment instanceof ProjectListFragment) {
                            iconId = R.drawable.ic_exception_blank_task;
                            text = BlankViewDisplay.MY_PROJECT_BLANK;
                        } else if (fragment instanceof TaskListFragment) {
                            iconId = R.drawable.ic_exception_blank_task_my;
                            text = "您还没有任务\n赶快为团队做点贡献吧~";
                        } else if (fragment instanceof NotifyListActivity) {
                            iconId = R.drawable.ic_exception_blank_task_my;
                        } else if (fragment instanceof TopicListFragment) {
                            iconId = R.drawable.ic_exception_blank_topic;
                            text = "还没有讨论\n创建一个讨论发表对项目的看法吧";
                        } else if (fragment instanceof MaopaoListBaseFragment) {
                            iconId = R.drawable.ic_exception_blank_maopao;
                            text = "还没有发表过冒泡呢～";
                        } else if (fragment instanceof SubjectListFragment) {
                            iconId = R.drawable.ic_exception_blank_maopao;
                            text = "还没有参与过话题呢~";
                        } else if (fragment instanceof UsersListFragment) {
                            iconId = R.drawable.ic_exception_blank_message;
                            text = "还没有新消息~";
                        } else if (fragment instanceof MergeReviewerListFragment) {
                            iconId = R.drawable.ic_exception_blank_task;
                            text = "这里还什么都没有\n赶快起来弄出一点动静吧";
                        } else if (fragment instanceof ProjectGitFragment) {
                            iconId = R.drawable.ic_exception_blank_task;
                            text = "此项目的 Git 仓库为空";
                        } else if (fragment instanceof ProjectFileMainActivity) {
                            iconId = R.drawable.ic_exception_blank_dir;
                            text = "这里还没有任何文件~";
                        } else if (fragment instanceof UserProjectListFragment) {
                            iconId = R.drawable.ic_exception_blank_task;
                            text = BlankViewDisplay.OTHER_PROJECT_BLANK;
                        } else if (fragment instanceof MessageListActivity) {
                            iconId = R.drawable.ic_exception_blank_message;
                            text = "无私信\n打个招呼吧~";
                        } else if (fragment instanceof MergeListFragment) {
                            iconId = R.drawable.ic_exception_blank_task;
                            text = "这里还什么都没有\n赶快起来弄出一点动静吧~";
                        } else if (fragment instanceof ImagePagerFragment
                                || fragment instanceof AttachmentsDownloadDetailActivity
                                || fragment instanceof AttachmentsHtmlDetailActivity
                                || fragment instanceof AttachmentsTextDetailActivity) {
                            iconId = R.drawable.ic_exception_no_network;
                            text = "晚了一步\n文件已经被人删除了~";
                        } else if (fragment instanceof ProjectMaopaoActivity) {
                            iconId = R.drawable.ic_exception_blank_announcement;
                            text = v.getContext().getString(R.string.project_maopao_list_empty);
                        } else if (fragment instanceof ProjectDynamicFragment) {
                            iconId = R.drawable.ic_exception_blank_dynamic;
                            text = "当前项目暂无相关动态~";
                        } else if (fragment instanceof ReleaseDetailActivity) {
                            iconId = R.drawable.ic_exception_blank_task;
                            text = "Release 不存在";
                        } else if (fragment instanceof TeamListActivity) {
                            iconId = R.drawable.ic_exception_blank_team;
                            text = "还没有创建团队~";
                        } else {
                            iconId = R.drawable.ic_exception_blank_task;
                            text = "还什么都没有~";
                        }
                    } else {
                        iconId = R.drawable.ic_exception_no_network;
                        text = "获取数据失败\n请检查下网络是否通畅";
                    }
                } else {
                    if (request) {
                        if (iconId == 0) {
                            iconId = R.drawable.ic_exception_blank_task;
                        }
                    } else {
                        iconId = R.drawable.ic_exception_no_network;
                    }

                    if (TextUtils.isEmpty(tipString)) {
                        if (request) {
                            text = "还什么都没有~";
                        } else {
                            text = "获取数据失败";
                        }
                    } else {
                        text = tipString;
                    }
                }

                v.findViewById(R.id.icon).setBackgroundResource(iconId);
                TextView textView = (TextView) v.findViewById(R.id.message);
                textView.setText(text);
                textView.setLineSpacing(3.0f, 1.2f);

            }
        };
    }

    private void initURLCallback() {
        URLSpanNoUnderline.urlCallback = new URLSpanNoUnderline.URLCallback() {
            @Override
            public boolean openActivityByUri(Context context, String uri, boolean newTask, boolean defaultIntent, boolean share) {
                uri = GlobalData.transformEnterpriseUri(uri);

                final String ProjectPath = "/u/([\\w.-]+)/p/([\\w\\.-]+)";
                final String Host = Global.HOST;

                Intent intent = new Intent();
                if (newTask) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }

                final String uriString = uri.replace("/team/", "/user/").replace("/t/", "/u/");  // 添加 team 后导致的 api 失效问题

                final String NAME = "([\\w.-]+)";

                final String uriPath = uriString.replace(Global.HOST, "");

                Log.d(TAG, uri);

                final String projectPattern = String.format("^/u/%s/p/%s(.*)", NAME, NAME);
                Pattern pattern = Pattern.compile(projectPattern);
                Matcher matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    String user = matcher.group(1);
                    String project = matcher.group(2);
                    String simplePath = matcher.group(3); // 去除了 /u/*/p/* 的路径
                    final String projectPath = String.format("/user/%s/project/%s", user, project);

                    // 代码中的文件 https://coding.net/u/8206503/p/TestPrivate/git/blob/master/jumpto
                    final String gitFile = String.format("^/git/blob/%s/(.*)$", NAME);
                    pattern = Pattern.compile(gitFile);
                    matcher = pattern.matcher(simplePath);
                    if (matcher.find()) {
                        String version = matcher.group(1);
                        String path = matcher.group(2);

                        intent.setClass(context, GitViewActivity_.class);
                        intent.putExtra("mProjectPath", projectPath);
                        intent.putExtra("mVersion", version);
                        intent.putExtra("showClickTitleTip", true);
                        intent.putExtra("mGitFileInfoObject", new GitFileInfoObject(path));
                        context.startActivity(intent);
                        return true;
                    }
                }

                // 用户名
                final String atSomeOne = "^/u/([\\w.-]+)$";
                pattern = Pattern.compile(atSomeOne);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    String global = matcher.group(1);
                    CodingCompat.instance().launchUserDetailActivity(context, global);
                    return true;
                }

                // 项目讨论列表
                // https://coding.net/u/8206503/p/TestIt2/topic/mine
                final String topicList = "^/u/([\\w.-]+)/p/([\\w.-]+)/topic/(mine|all)$";
                pattern = Pattern.compile(topicList);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    intent.setClass(context, ProjectActivity_.class);
                    ProjectJumpParam param = new ProjectJumpParam(
                            matcher.group(1), matcher.group(2)
                    );
                    intent.putExtra("mJumpParam", param);
                    intent.putExtra("mJumpType", ProjectFunction.topic);
                    context.startActivity(intent);
                    return true;
                }

                // 单个项目讨论
                // https://coding.net/u/8206503/p/AndroidCoding/topic/9638?page=1
                final String topic = "^/[ut]/([\\w.-]+)/p/([\\w.-]+)/topic/([\\w.-]+)(?:\\?[\\w=&-]*)?(#comment-\\w*)?$";
                pattern = Pattern.compile(topic);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    intent.setClass(context, TopicListDetailActivity_.class);
                    TopicListDetailActivity.TopicDetailParam param =
                            new TopicListDetailActivity.TopicDetailParam(matcher.group(1),
                                    matcher.group(2), matcher.group(3));
                    intent.putExtra("mJumpParam", param);
                    intent.putExtra("showClickTitleTip", true);
                    context.startActivity(intent);
                    return true;
                }

                // 项目
                // https://coding.net/u/8206503/p/AndroidCoding
                // https://coding.net/u/8206503/p/FireEye/git
                //
                final String project = "^/u/([\\w.-]+)/p/([\\w.-]+)(/git)?$";
                pattern = Pattern.compile(project);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    intent.setClass(context, ProjectHomeActivity_.class);
                    ProjectJumpParam param = new ProjectJumpParam(
                            matcher.group(1), matcher.group(2)
                    );
                    intent.putExtra("mJumpParam", param);
                    context.startActivity(intent);
                    return true;
                }

                // 冒泡
                // https://coding.net/u/8206503/pp/9275
                final String maopao = "^/u/([\\w.-]+)/pp/([\\w.-]+)$";
                pattern = Pattern.compile(maopao);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    intent.setClass(context, MaopaoDetailActivity_.class);
                    MaopaoDetailActivity.ClickParam param = new MaopaoDetailActivity.ClickParam(
                            matcher.group(1), matcher.group(2));
                    intent.putExtra("mClickParam", param);
                    context.startActivity(intent);
                    return true;
                }

                // 项目公告
                // https://coding.net/t/superrocket/p/TestPrivate?pp=2417
                final String projectMaopao = String.format("^%s\\?pp=([\\d]+)", ProjectPath);
                pattern = Pattern.compile(projectMaopao);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    intent.setClass(context, MaopaoDetailActivity_.class);
                    MaopaoDetailActivity.ClickParam param = new MaopaoDetailActivity.ClickParam(
                            matcher.group(1), matcher.group(2), matcher.group(3));
                    intent.putExtra("mClickParam", param);
                    context.startActivity(intent);
                    return true;
                }

                // 项目公告
                // /u/codingcorp/p/0320/setting/notice/32
                final String projectMaopaoDetail = String.format("^%s/setting/notice/([\\d]+)", ProjectPath);
                pattern = Pattern.compile(projectMaopaoDetail);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    intent.setClass(context, MaopaoDetailActivity_.class);
                    MaopaoDetailActivity.ClickParam param = new MaopaoDetailActivity.ClickParam(
                            matcher.group(1), matcher.group(2), matcher.group(3));
                    intent.putExtra("mClickParam", param);
                    context.startActivity(intent);
                    return true;
                }

                // 冒泡列表
                // https://codingcorp.coding.net/p/Coding/setting/notice
                final String projectMaopaoList = String.format("^%s/setting/notice", ProjectPath);
                pattern = Pattern.compile(projectMaopaoList);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    ProjectJumpParam param = new ProjectJumpParam(matcher.group(1), matcher.group(2));
                    CodingCompat.instance().launchProjectMaopoaList(context, param);
                    return true;
                }

                // 冒泡话题
                // https://coding.net/u/8206503/pp/9275
                final String maopaoTopic = "^(?:/u/(?:[\\w.-]+))?/pp/topic/([\\w.-]+)$";
                pattern = Pattern.compile(maopaoTopic);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    intent.setClass(context, SubjectDetailActivity_.class);
                    intent.putExtra("topicId", Integer.valueOf(matcher.group(1)));
                    context.startActivity(intent);
                    return true;
                }

                // 还是冒泡话题 https://coding.net/pp/topic/551
                final String maopao2 = "^/pp/topic/([\\w.-]+)$";
                pattern = Pattern.compile(maopao2);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    intent.setClass(context, SubjectDetailActivity_.class);
                    intent.putExtra("topicId", Integer.valueOf(matcher.group(1)));
                    context.startActivity(intent);
                    return true;
                }

                // 任务详情
                // https://coding.net/u/wzw/p/coding/task/9220
                final String task = "^/u/([\\w.-]+)/p/([\\w.-]+)/task/(\\w+)(#comment-\\w*)?$";
                pattern = Pattern.compile(task);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    Log.d("", "gg " + matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3));
                    intent.setClass(context, TaskAddActivity_.class);
                    intent.putExtra("mJumpParams", new TaskJumpParams(matcher.group(1),
                            matcher.group(2), matcher.group(3)));
                    intent.putExtra("showClickTitleTip", true);
                    context.startActivity(intent);
                    return true;
                }

                // release 详情
                // https://coding.net/u/wzw/p/coding/task/9220
                final String release = "^/u/([\\w.-]+)/p/([\\w.-]+)/git/releases/([\\w.-]+)$";
                pattern = Pattern.compile(release);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    intent.setClass(context, ReleaseDetailActivity_.class);
                    intent.putExtra("param", new ReleaseDetailActivity.JumpParam(matcher.group(1),
                            matcher.group(2), matcher.group(3)));
                    context.startActivity(intent);
                    return true;
                }

//      我的已过期任务  "/user/tasks"
//      企业版我的已过期任务  "/user/tasks?owner=23"
                final String myExpireTask = "/user/tasks";
                pattern = Pattern.compile(myExpireTask);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    intent.setClass(context, AllTasksActivity_.class);
                    context.startActivity(intent);
                    return true;
                }

                // 私信推送
                // https://coding.net/user/messages/history/1984
                final String message = URLSpanNoUnderline.PATTERN_URL_MESSAGE;
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
                pattern = Pattern.compile("^/u/([\\w.-]+)/p/([\\w.-]+)/attachment/([\\w.-]+)$");
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    FileUrlActivity_.IntentBuilder_ build = FileUrlActivity_.intent(context);
                    if (newTask) build.flags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    build.url(uriString).start();
                    FileUrlActivity.Param param = new FileUrlActivity.Param(
                            matcher.group(1),
                            matcher.group(2),
                            Integer.valueOf(matcher.group(3)),
                            false);
                    build.param(param).start();
                    return true;
                }

                // 文件，文件评论
                // https://coding.net/u/8206503/p/TestIt2/attachment/65138/preview/66171
                pattern = Pattern.compile("^/u/([\\w.-]+)/p/([\\w.-]+)/attachment/([\\w.-]+)/preview/([\\d]+)$");
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    FileUrlActivity_.IntentBuilder_ builder = FileUrlActivity_.intent(context);
                    if (newTask) builder.flags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    builder.url(uriString)
//                            .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            .start();
                    FileUrlActivity.Param param = new FileUrlActivity.Param(
                            matcher.group(1),
                            matcher.group(2),
                            Integer.valueOf(matcher.group(4)),
                            true);
                    builder.param(param).start();
                    return true;
                }

                // 图片链接
                final String imageSting = "(http|https):.*?.[.]{1}(gif|jpg|png|bmp)";
                pattern = Pattern.compile(imageSting);
                matcher = pattern.matcher(uriString);
                if (matcher.find()) {
                    intent.setClass(context, ImagePagerActivity_.class);
                    intent.putExtra("mSingleUri", uriString);
                    context.startActivity(intent);
                    return true;
                }

                // 跳转图片链接
                // https://coding.net/api/project/78813/files/137849/imagePreview
                final String imageJumpString = Global.HOST_API + "/project/\\d+/files/\\d+/imagePreview";
                pattern = Pattern.compile(imageJumpString);
                matcher = pattern.matcher(uriString);
                if (matcher.find()) {
                    intent.setClass(context, ImagePagerActivity_.class);
                    intent.putExtra("mSingleUri", uriString);
                    context.startActivity(intent);
                    return true;
                }

                // 跳转到 wiki
                final String wikiUrl = "^/u/([\\w.-]+)/p/([\\w.-]+)/wiki/(\\d+)";
                pattern = Pattern.compile(wikiUrl);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    intent.setClass(context, WikiMainActivity_.class);
                    WikiMainActivity.JumpParam param = new WikiMainActivity.JumpParam(
                            matcher.group(1),
                            matcher.group(2),
                            Integer.valueOf(matcher.group(3)));
                    intent.putExtra("jumpParam", param);
                    context.startActivity(intent);
                    return true;
                }

                // 跳转到merge或pull
                final String mergeString = "^/u/([\\w.-]+)/p/([\\w.-]+)/git/(merge)?(pull)?/(\\d+)";
                pattern = Pattern.compile(mergeString);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    intent.setClass(context, MergeDetailActivity_.class);
                    intent.putExtra("mMergeUrl", uriString);
                    intent.putExtra("showClickTitleTip", true);
                    context.startActivity(intent);
                    return true;
                }

                // 跳转到commit
                final String commitString = "^/u/([\\w.-]+)/p/([\\w.-]+)/git/commit/.+$";
                pattern = Pattern.compile(commitString);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    intent.setClass(context, CommitFileListActivity_.class);
                    intent.putExtra("mCommitUrl", uriString);
                    intent.putExtra("showClickTitleTip", true);
                    context.startActivity(intent);
                    return true;
                }

                // 跳转到branch
                final String branchString = "^/u/([\\w.-]+)/p/([\\w.-]+)/git/tree/(.+)$";
                pattern = Pattern.compile(branchString);
                matcher = pattern.matcher(uriPath);
                if (matcher.find()) {
                    intent.setClass(context, BranchMainActivity_.class);
                    String userString = matcher.group(1);
                    String projectString = matcher.group(2);
                    String version = matcher.group(3);
                    String projectPath = String.format("/user/%s/project/%s", userString, projectString);
                    if (!TextUtils.isEmpty(version)) {
                        version = Global.decodeUtf8(version);
                    }

                    intent.putExtra("mProjectPath", projectPath);
                    intent.putExtra("mVersion", version);
                    intent.putExtra("showClickTitleTip", true);
                    context.startActivity(intent);
                    return true;
                }

                if (PushUrl.INSTANCE.is2faLink(uriString)) {
                    intent.setClass(context, AuthListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return true;
                }

                try {
                    if (defaultIntent) {
                        intent = new Intent(context, WebActivity_.class);

                        if (newTask) {
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        if (uri.startsWith("/u/")) {
                            uri = Global.HOST + uri;
                        }

                        if (share) {
                            intent.putExtra("share", true);
                        }

                        intent.putExtra("url", uri);
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "" + uri, Toast.LENGTH_LONG).show();
                    Global.errorLog(e);
                }

                return false;
            }
        };
    }

    public static boolean isInMainProcess(Context context) {
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        String mainProcessName = context.getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processes) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    private void loadBaiduMap() {
        if (!PhoneType.isX86or64()) {
            // x86的机器上会抛异常，因为百度没有提供x86的.so文件
            // 64 位的机器也不行
            // 只在主进程初始化lbs
            if (this.getPackageName().equals(getProcessName(this))) {
                SDKInitializer.initialize(this);
            }
        }
    }

    private static boolean isDebug = false;

    public static boolean isDebug() {
        return isDebug;
    }
}
