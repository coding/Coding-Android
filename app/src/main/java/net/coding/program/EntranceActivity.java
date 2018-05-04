package net.coding.program;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.LoginBackground;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.login.ResetPasswordActivity_;
import net.coding.program.login.UserActiveActivity_;
import net.coding.program.login.ZhongQiuGuideActivity;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cc191954 on 14-8-14.
 * 启动页面
 */
@EActivity(R.layout.entrance_image)
public class EntranceActivity extends BaseActivity {

    private static final String TAG = Global.makeLogTag(EntranceActivity.class);

    private String jumpLink = "";
    private String imageJumpLink = "";

    @ViewById
    ImageView image;
    @ViewById
    View rootLayout;

    Uri background = null;

    private boolean openNext = false;

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
//                            WebActivity_.intent(this)
//                                    .url(link)
//                                    .start();
                            // do nothings
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

        settingBackground();

        if (AccountInfo.isLogin(this)) {
            final Context context = getApplicationContext();
            Network.getRetrofit(context)
                    .getCurrentUser()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new HttpObserver<UserObject>(context) {

                        @Override
                        public void onSuccess(UserObject data) {
                            super.onSuccess(data);
                            AccountInfo.saveAccount(context, data);
                            GlobalData.sUserObject = data;
                            AccountInfo.saveReloginInfo(context, data);
                        }

                        @Override
                        public void onFail(int errorCode, @NonNull String error) {
                            // 不显示错误提示
                        }
                    });
        }

        next();
    }

    @Click
    void image() {
        if (!TextUtils.isEmpty(imageJumpLink)) {
            jumpLink = imageJumpLink;
            realNext(true);
        }
    }

    private void settingBackground() {
//        if (ZhongQiuGuideActivity.isZhongqiu()) { // 修改首页图
//            ImageSize imageSize = new ImageSize(MyApp.sWidthPix, MyApp.sHeightPix);
//            image.setImageBitmap(getImageLoad().imageLoader.loadImageSync("drawable://" + R.drawable.zhongqiu_init_photo, imageSize));
//            title.setText("中秋快乐 © Mango");
//            return;
//        }

        LoginBackground loginBackground = new LoginBackground(this);
        loginBackground.update();

        LoginBackground.PhotoItem photoItem = loginBackground.getPhoto();
        File file = photoItem.getCacheFile(this);
        getImageLoad().imageLoader.clearMemoryCache();
        if (file.exists()) {
            background = Uri.fromFile(file);
            image.setImageBitmap(getImageLoad().imageLoader.loadImageSync("file://" + file.getPath(), ImageLoadTool.enterOptions));
            imageJumpLink = photoItem.getGroup().getLink();
        }

//        MarketingHelp.setUrl(photoItem.getGroup().getLink());
    }

    @UiThread(delay = 2000)
    void next() {
        realNext(false);
    }

    private void realNext(boolean openUrl) {
        if (openNext || isFinishing()) return;

        openNext = true;

        Intent intent;
        String mGlobalKey = AccountInfo.loadAccount(this).global_key;
        if (mGlobalKey.isEmpty()) {
            intent = new Intent(this, LoginActivity_.class);
        } else {
            if (AccountInfo.needDisplayGuide(this)) {
                intent = new Intent(this, ZhongQiuGuideActivity.class);
            } else {
                intent = new Intent(this, CodingCompat.instance().getMainActivity());
            }
        }

        startActivity(intent);

        if (openUrl && !TextUtils.isEmpty(jumpLink)) {
            MyApp.openNewActivityFromMain(this, jumpLink);
        } else {
            overridePendingTransition(R.anim.entrance_fade_in, R.anim.entrance_fade_out);
        }

        UnreadNotify.update(this);

        finish();
    }
}

