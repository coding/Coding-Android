package net.coding.program;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.ImageSize;

import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.LoginBackground;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.WeakRefHander;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.login.ResetPasswordActivity_;
import net.coding.program.login.UserActiveActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.AnimationRes;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by cc191954 on 14-8-14.
 * 启动页面
 */
@EActivity(R.layout.entrance_image)
public class EntranceActivity extends BaseActivity implements Handler.Callback {

    private static final String TAG = Global.makeLogTag(EntranceActivity.class);

    private static final int HANDLER_MESSAGE_ANIMATION = 0;
    private static final int HANDLER_MESSAGE_NEXT_ACTIVITY = 1;
    public final String HOST_CURRENT = getHostCurrent();
    @ViewById
    ImageView image;
    @ViewById
    TextView title;
    @ViewById
    View foreMask;
    @ViewById
    View logo;
    @AnimationRes
    Animation entrance;
    Uri background = null;

    WeakRefHander mWeakRefHandler;

    public static String getHostCurrent() {
        return Global.HOST_API + "/current_user";
    }

    @AfterViews
    void init() {
        Uri uriData = getIntent().getData();
        if (uriData != null) {
            String url = uriData.toString();
            Log.d(TAG, url);
            String path = uriData.getPath();
            switch (path) {
                case "/app/detect": {
                    String link = Global.decodeUtf8(uriData.getQueryParameter("link"));
                    Uri uriLink = Uri.parse(link);
                    String linkPath = uriLink.getPath();

                    switch (linkPath) {
                        case "/activate":
                            UserActiveActivity_.intent(this)
                                    .link(link)
                                    .start();
                            break;
                        case "/user/resetPassword":
                            ResetPasswordActivity_.intent(this)
                                    .link(link)
                                    .start();
                            break;
                        default:
                            WebActivity_.intent(this)
                                    .url(link)
                                    .start();
                            break;
                    }
                    break;
                }

                default: {
                    MyApp.openNewActivityFromMain(this, url);
                }
            }

            finish();
            return;
        }

        mWeakRefHandler = new WeakRefHander(this);

        settingBackground();

        entrance.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mWeakRefHandler.start(HANDLER_MESSAGE_NEXT_ACTIVITY, 500);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        if (AccountInfo.isLogin(this)) {
            getNetwork(HOST_CURRENT, HOST_CURRENT);
        }

        mWeakRefHandler.start(HANDLER_MESSAGE_ANIMATION, 900);
    }

    private void settingBackground() {
//        if (ZhongQiuGuideActivity.isZhongqiu()) { // 修改首页图
//            ImageSize imageSize = new ImageSize(MyApp.sWidthPix, MyApp.sHeightPix);
//            image.setImageBitmap(getImageLoad().imageLoader.loadImageSync("drawable://" + R.drawable.zhongqiu_init_photo, imageSize));
//            title.setText("中秋快乐 © Mango");
//            return;
//        }

        LoginBackground.PhotoItem photoItem = new LoginBackground(this).getPhoto();
        File file = photoItem.getCacheFile(this);
        getImageLoad().imageLoader.clearMemoryCache();
        if (file.exists()) {
            background = Uri.fromFile(file);
            image.setImageBitmap(getImageLoad().imageLoader.loadImageSync("file://" + file.getPath(), ImageLoadTool.enterOptions));
            title.setText(photoItem.getTitle());

            if (photoItem.isGuoguo()) {
                hideLogo();
            }
        } else {
            ImageSize imageSize = new ImageSize(GlobalData.sWidthPix, GlobalData.sHeightPix);
            image.setImageBitmap(getImageLoad().imageLoader.loadImageSync("drawable://" + R.drawable.entrance1, imageSize));
        }

        MarketingHelp.setUrl(photoItem.getGroup().getLink());
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == HANDLER_MESSAGE_ANIMATION) {
            playAnimator1();
        } else if (msg.what == HANDLER_MESSAGE_NEXT_ACTIVITY) {
            next();
        }
        return true;
    }

    private void playAnimator1() {
        foreMask.startAnimation(entrance);
    }

    private void hideLogo() {
//        mask.setVisibility(View.GONE);
        title.setVisibility(View.GONE);
        logo.setVisibility(View.GONE);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_CURRENT)) {
            if (code == 0) {
                UserObject user = new UserObject(respanse.getJSONObject("data"));
                AccountInfo.saveAccount(this, user);
                GlobalData.sUserObject = user;
                AccountInfo.saveReloginInfo(this, user);
                next();
            } else {
//                new AlertDialog.Builder(this, R.style.MyAlertDialogStyle).setTitle("更新")
//                        .setMessage("刷新账户信息失败")
//                        .setPositiveButton("重试", (dialog, which) -> getNetwork(HOST_CURRENT, HOST_CURRENT))
//                        .setNegativeButton("关闭程序", (dialog, which) -> finish())
//                        .show();
                next();

            }
        }
    }

    @UiThread(delay = 2000)
    void next() {
        Intent intent;
        String mGlobalKey = AccountInfo.loadAccount(this).global_key;
        if (mGlobalKey.isEmpty()) {
            intent = new Intent(this, CodingCompat.instance().getGuideActivity());
            if (background != null) {
                intent.putExtra(LoginActivity.EXTRA_BACKGROUND, background);
            }

        } else {
//            if (AccountInfo.needDisplayGuide(this)) {
//                intent = new Intent(this, FeatureActivity_.class);
//            } else {
            intent = new Intent(this, CodingCompat.instance().getMainActivity());
//            }
        }

        startActivity(intent);
        overridePendingTransition(R.anim.entrance_fade_in, R.anim.entrance_fade_out);

        UnreadNotify.update(this);
        finish();
    }
}

