package net.coding.program.project;


import android.content.DialogInterface;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.maopao.MaopaoDetailActivity;
import net.coding.program.model.DynamicObject;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.detail.ProjectActivity;
import net.coding.program.project.detail.ProjectActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@EFragment(R.layout.fragment_public_project_home)
public class PublicProjectHomeFragment extends BaseFragment {

    @FragmentArg
    ProjectObject mProjectObject;

    @ViewById
    View recommendIcon;

    @ViewById
    ImageView projectIcon;

    @ViewById
    TextView projectName;

    @ViewById
    TextView description;

    @ViewById
    TextView projectAuthor;

    @ViewById
    WebView webView;

    @ViewById
    View needReadme;

    @ViewById
    TextView readme;

    @ViewById
    View buttonStar, buttonWatch, buttonFork;

    ProjectMarkButton mButtonStar;
    ProjectMarkButton mButtonWatch;
    ProjectMarkButton mButtonFork;
    private String hostGitTree;

    private String httpProjectObject;
    private String forkUrl;

    @AfterViews
    final void init() {
        mUrlStar = mProjectObject.getHttpStar(true);
        mUrlUnstar = mProjectObject.getHttpStar(false);
        mUrlWatch = mProjectObject.getHttpWatch(true);
        mUrlUnwatch = mProjectObject.getHttpWatch(false);

        iconfromNetwork(projectIcon, mProjectObject.icon, ImageLoadTool.optionsRounded2);
        projectName.setText(mProjectObject.name);
        projectAuthor.setText(mProjectObject.owner_user_name);

        if (mProjectObject.description.isEmpty()) {
            description.setVisibility(View.GONE);
        } else {
            description.setText(mProjectObject.description);
        }

        View root = getView();
        initHead2();
        initHead3(root);

        hostGitTree = mProjectObject.getHttpGitTree("master");
        getNetwork(hostGitTree, hostGitTree);
        httpProjectObject = mProjectObject.getHttpProjectObject();
        getNetwork(httpProjectObject);
    }

    private void showEmptyReadme() {
        readme.setText("README.md");
        needReadme.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(hostGitTree)) {
            if (code == 0) {
                JSONObject readmeJson = respanse.optJSONObject("data").optJSONObject("readme");
                if (readmeJson == null) {
                    showEmptyReadme();

                } else {
                    String readmeHtml = readmeJson.optString("preview", "");
                    if (readmeHtml.isEmpty()) {
                        showEmptyReadme();

                    } else {
                        String readmeName = readmeJson.optString("name", "");
                        readme.setText(readmeName);

                        needReadme.setVisibility(View.GONE);
                        webView.setVisibility(View.VISIBLE);

                        webView.getSettings().setJavaScriptEnabled(true);
                        webView.setBackgroundColor(0);
                        webView.getBackground().setAlpha(0);

                        String bubble = "${webview_content}";
                        try {
                            bubble = readTextFile(getResources().getAssets().open("bubble"));
                        } catch (Exception e) {
                            Global.errorLog(e);
                        }

                        webView.getSettings().setDefaultTextEncodingName("UTF-8");
                        webView.loadDataWithBaseURL(null, bubble.replace("${webview_content}", readmeHtml), "text/html", "UTF-8", null);
                        webView.setWebViewClient(new MaopaoDetailActivity.CustomWebViewClient(getActivity()));
                    }
                }


            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(httpProjectObject)) {
            if (code == 0) {
                mProjectObject = new ProjectObject(respanse.getJSONObject("data"));
                initHead2();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(mUrlStar) || tag.equals(mUrlUnstar)) {
            if (code != 0) {
                mButtonStar.changeState();
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(mUrlWatch) || tag.equals(mUrlUnwatch)) {
            if (code != 0) {
                mButtonWatch.changeState();
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(forkUrl)) {
            showProgressBar(false);
            if (code == 0) {
                JSONObject jsonData = respanse.getJSONObject("data");
                String projectName = jsonData.optString("name");
                DynamicObject.Owner owner = new DynamicObject.Owner(jsonData.optJSONObject("owner"));
                ProjectActivity.ProjectJumpParam param = new ProjectActivity.ProjectJumpParam(owner.global_key,
                        projectName);
                ProjectHomeActivity_
                        .intent(this)
                        .mJumpParam(param)
                        .start();
                mButtonFork.changeState();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
        }
        return outputStream.toString();
    }

    private void initHead2() {
        String[][] titles = new String[][]{
                new String[]{"收藏", "已收藏"},
                new String[]{"关注", "已关注"},
                new String[]{"Fork", "Fork"},
        };

        int[][] titlesColors = new int[][]{
                new int[]{0xff222222, 0xff3bbd79},
                new int[]{0xff222222, 0xff3bbd79},
                new int[]{0xff222222, 0xff666666},
        };

        int[][] icons = new int[][]{
                new int[]{R.drawable.project_home_button_public_star, R.drawable.project_home_button_public_ok},
                new int[]{R.drawable.project_home_button_public_watch, R.drawable.project_home_button_public_ok},
                new int[]{R.drawable.project_home_button_public_fork, R.drawable.project_home_button_public_fork},
        };

        mButtonStar = new ProjectMarkButton(buttonStar, titles[0], titlesColors[0], icons[0], mProjectObject.stared, mProjectObject.star_count);
        mButtonWatch = new ProjectMarkButton(buttonWatch, titles[1], titlesColors[1], icons[1], mProjectObject.watched, mProjectObject.watch_count);
        mButtonFork = new ProjectMarkButton(buttonFork, titles[2], titlesColors[2], icons[2], mProjectObject.forked, mProjectObject.fork_count);
    }

    private void initHead3(View root) {
        final int[] buttons = new int[]{
                R.id.button0,
                R.id.button1,
                R.id.button2,
                R.id.button3,
        };

        final int[] buttonBackgrounds = new int[]{
                R.drawable.project_button_icon_dynamic,
                R.drawable.project_button_icon_topic,
                R.drawable.project_button_icon_code,
                R.drawable.project_button_icon_member
        };

        final String[] titles = new String[]{
                "动态",
                "讨论",
                "代码",
                "成员",
        };

        for (int i = 0; i < buttons.length; ++i) {
            View v = root.findViewById(buttons[i]);
            v.findViewById(R.id.icon).setBackgroundResource(buttonBackgrounds[i]);
            ((TextView) v.findViewById(R.id.title)).setText(titles[i]);
            final int pos = i;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProjectActivity_.intent(PublicProjectHomeFragment.this)
                            .mProjectObject(mProjectObject)
                            .mJumpType(ProjectActivity.PUBLIC_JUMP_TYPES[pos])
                            .start();
                }
            });
        }
    }

    String mUrlStar;
    String mUrlUnstar;

    String mUrlWatch;
    String mUrlUnwatch;

    protected void postNetwork(String url) {
        postNetwork(url, new RequestParams(), url);
    }

    public void getNetwork(String url) {
        getNetwork(url, url);
    }

    @Click
    protected final void buttonStar(View v) {
        mButtonStar.changeState();
        if (mButtonStar.isChecked()) {
            postNetwork(mUrlStar);
        } else {
            postNetwork(mUrlUnstar);
        }
    }

    @Click
    protected final void buttonWatch(View v) {
        mButtonWatch.changeState();
        if (mButtonWatch.isChecked()) {
            postNetwork(mUrlWatch);
        } else {
            postNetwork(mUrlUnwatch);
        }
    }

    @Click
    protected final void buttonFork(View v) {
        if (mProjectObject.isMy()) {
            showButtomToast("不能fork自己的项目");
        } else {
            forkUrl = Global.HOST + "/api" + mProjectObject.backend_project_path + "/git/fork";
            showDialog("fork", "fork将会将此项目复制到您的个人空间，确定要fork吗?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getNetwork(forkUrl);
                    showProgressBar(true, "正在fork项目");
                }
            });
        }
    }

    class ProjectMarkButton {

        // 普通和选中
        String[] title;
        int[] titleColor;
        int[] icon;

        boolean mCheck = false;
        int mCount = 0;
        View mButton;

        ProjectMarkButton(View button, String[] title, int[] titleColor, int[] icon, final boolean mCheck, int mCount) {
            this.title = title;
            this.titleColor = titleColor;
            this.icon = icon;
            this.mCheck = mCheck;
            this.mCount = mCount;
            this.mButton = button;

            updateControl();
        }

        public void changeState() {
            checkButton(!mCheck);
        }

        public void checkButton(boolean check) {
            if (mCheck == check) {
                return;
            }

            mCheck = check;
            if (mCheck) {
                ++mCount;
            } else {
                --mCount;
            }

            updateControl();
        }

        private void updateControl() {
            mButton.setTag(mCheck);
            ((TextView) mButton.findViewById(R.id.count)).setText(String.valueOf(mCount));

            mButton.findViewById(R.id.icon).setBackgroundResource(!mCheck ? icon[0] : icon[1]);
            TextView title = (TextView) mButton.findViewById(R.id.title);
            title.setText(!mCheck ? this.title[0] : this.title[1]);
            title.setTextColor(!mCheck ? titleColor[0] : titleColor[1]);
        }

        public boolean isChecked() {
            return mCheck;
        }
    }
}
