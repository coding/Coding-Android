/**
 *
 */

package net.coding.program.maopao.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.model.Maopao;
import net.coding.program.common.param.MessageParse;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.widget.IconTextView;
import net.coding.program.compatible.CodingCompat;

public class CustomShareBoard extends PopupWindow implements OnClickListener {

//    private static UMSocialService mController = UMServiceFactory.getUMSocialService("net.coding.program");

    private Activity mActivity;
    private ShareData mShareData;

    private View mBackground;
    private View mButtonsLayout;
    //    private UMQQSsoHandler mQqSsoHandler;
//    private QZoneSsoHandler mQZoneSsoHandler;
//    private SinaSsoHandler mSinaSsoHandler;
//    private UMWXHandler mWXHandler;
//    private UMEvernoteHandler mEvernoteHandler;
    private ViewGroup allButtonsLayout;

    public CustomShareBoard(Activity activity, ShareData shareData) {
        super(activity);
        this.mActivity = activity;
        initView(activity);
        mShareData = shareData;
    }

    public static void performShare(SHARE_MEDIA platform, Activity mActivity, ShareData mShareData) {
        ShareAction shareAction = new ShareAction(mActivity);
        UMWeb umWeb = new UMWeb(mShareData.link);

        if (!TextUtils.isEmpty(mShareData.getImg())) {
            UMImage umImage = new UMImage(mActivity, mShareData.getImg());
            umWeb.setThumb(umImage);
        }

        umWeb.setTitle(mShareData.name);
        if (platform == SHARE_MEDIA.SINA) {
            umWeb.setDescription(mShareData.des + " " + mShareData.link);
        } else {
            if (TextUtils.isEmpty(mShareData.des)) {
                umWeb.setDescription(mShareData.link);
            } else {
                umWeb.setDescription(mShareData.des);
            }
        }
        shareAction.withMedia(umWeb);

        shareAction.setPlatform(platform)
                .setCallback(new UMShareListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {

                    }

                    @Override
                    public void onResult(SHARE_MEDIA share_media) {

                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                        Logger.e(throwable.getMessage());
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media) {

                    }
                })
                .share();
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data, Context activity) {
        UMShareAPI.get(activity).onActivityResult(requestCode, resultCode, data);
    }

    public static void onDestory(Context context) {
        UMShareAPI.get(context).release();
    }

    private void addQQ() {
//        mQqSsoHandler.setTargetUrl(mShareData.link);
//        mQqSsoHandler.setTitle(mShareData.name);
//        mQqSsoHandler.addToSocialSDK();
    }

    private void addWX() {
//        mWXHandler.setTargetUrl(mShareData.link);
//        mWXHandler.setTitle(mShareData.name);
//        mWXHandler.addToSocialSDK();
    }

    private void addWXCircle() {
//        mWXHandler.setTargetUrl(mShareData.link);
//        mWXHandler.setTitle(mShareData.des);
//        mWXHandler.setToCircle(true);
//        mWXHandler.addToSocialSDK();
    }

    private void addQQZone() {
        // 添加QZone平台
//        mQZoneSsoHandler.setTargetUrl(mShareData.link);
//        mQZoneSsoHandler.addToSocialSDK();
    }

    private void addSinaWeibo() {
//        mController.getConfig().setSsoHandler(mSinaSsoHandler);
    }

    private void addEvernote() {
//        mEvernoteHandler.addToSocialSDK();
    }

    private void addButton(IconTextView.Data data) {
        IconTextView iconTextView = new IconTextView(mActivity, null);
        iconTextView.setData(data);
        iconTextView.setId(data.id);
        iconTextView.setOnClickListener(this);
        allButtonsLayout.addView(iconTextView);
        ViewGroup.LayoutParams lp = iconTextView.getLayoutParams();
        lp.width = GlobalData.sWidthPix / 4;
        iconTextView.setLayoutParams(lp);
    }

    @SuppressWarnings("deprecation")
    private void initView(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.share_custom_board, null);
        final int[] buttns = new int[]{
                R.id.close,
                R.id.rootLayout,
                R.id.buttonsLayout
        };

        for (int id : buttns) {
            rootView.findViewById(id).setOnClickListener(this);
        }

        final IconTextView.Data[] datas = new IconTextView.Data[]{
                new IconTextView.Data(R.id.wechat, "微信好友", R.drawable.icon_share_weixin),
                new IconTextView.Data(R.id.wechat_circle, "朋友圈", R.drawable.icon_share_weixin_friend),
                new IconTextView.Data(R.id.qq, "QQ", R.drawable.icon_share_qq),
                new IconTextView.Data(R.id.qzone, "QQ空间", R.drawable.icon_share_qq_zone),
                new IconTextView.Data(R.id.sinaWeibo, "微博", R.drawable.icon_share_sina),
                new IconTextView.Data(R.id.evernote, "印象笔记", R.drawable.icon_share_evernote),
                new IconTextView.Data(R.id.codingFriend, "Coding好友", R.drawable.icon_share_coding_friend),
                new IconTextView.Data(R.id.linkCopy, "复制链接", R.drawable.icon_share_copy_link)
        };

        allButtonsLayout = rootView.findViewById(R.id.allButtonsLayout);

        UMShareAPI umShareApi = UMShareAPI.get(mActivity);
        if (umShareApi.isInstall(mActivity, SHARE_MEDIA.WEIXIN)) {
            addButton(datas[0]);
            addButton(datas[1]);
        }

        if (umShareApi.isInstall(mActivity, SHARE_MEDIA.QQ)) {
            addButton(datas[2]);
        }

        if (umShareApi.isInstall(mActivity, SHARE_MEDIA.QZONE)) {
            addButton(datas[3]);
        }

        addButton(datas[4]);

        if (umShareApi.isInstall(mActivity, SHARE_MEDIA.EVERNOTE)) {
            addButton(datas[5]);
        }

        addButton(datas[6]);
        addButton(datas[7]);

        setContentView(rootView);
        setWidth(LayoutParams.MATCH_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        ColorDrawable cd = new ColorDrawable(0xb0000000);
        setBackgroundDrawable(cd);
        setTouchable(true);

        mBackground = rootView.findViewById(R.id.rootLayout);
        mBackground.startAnimation(AnimationUtils.loadAnimation(mActivity,
                R.anim.share_dialog_bg_enter));

        mButtonsLayout = rootView.findViewById(R.id.buttonsLayout);
        mButtonsLayout.startAnimation(AnimationUtils.loadAnimation(mActivity,
                R.anim.share_dialog_buttons_layout_enter));
    }

    @Override
    public void dismiss() {
        mBackground.startAnimation(AnimationUtils.loadAnimation(mActivity,
                R.anim.share_dialog_bg_exit));
        mButtonsLayout.startAnimation(AnimationUtils.loadAnimation(mActivity,
                R.anim.share_dialog_buttons_layout_exit));
        super.dismiss();
    }

    private void umengEvent(String s, String param) {
        MobclickAgent.onEvent(mActivity, s, param);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.wechat:
                umengEvent(UmengEvent.MAOPAO, "分享到微信");
                addWX();
                performShare(SHARE_MEDIA.WEIXIN);
                break;
            case R.id.wechat_circle:
                umengEvent(UmengEvent.MAOPAO, "分享到朋友圈");
                addWXCircle();
                performShare(SHARE_MEDIA.WEIXIN_CIRCLE);
                break;
            case R.id.qq:
                umengEvent(UmengEvent.MAOPAO, "分享到qq");
                addQQ();
                performShare(SHARE_MEDIA.QQ);
                break;
            case R.id.qzone:
                umengEvent(UmengEvent.MAOPAO, "分享到qq空间");
                addQQZone();
                performShare(SHARE_MEDIA.QZONE);
                break;

            case R.id.sinaWeibo:
                umengEvent(UmengEvent.MAOPAO, "分享到sina");
                addSinaWeibo();
                performShare(SHARE_MEDIA.SINA);
                break;

            case R.id.evernote:
                umengEvent(UmengEvent.MAOPAO, "分享到evernote");
                addEvernote();
                performShare(SHARE_MEDIA.EVERNOTE);
                break;

            case R.id.codingFriend:
                umengEvent(UmengEvent.MAOPAO, "分享到好友");
                CodingCompat.instance().launchPickUser(mActivity, mShareData.link);
                dismiss();
                break;

            case R.id.linkCopy:
                umengEvent(UmengEvent.MAOPAO, "复制链接");
                Global.copy(mActivity, mShareData.link);
                Toast.makeText(mActivity, "链接已复制 " + mShareData.link, Toast.LENGTH_SHORT).show();
                break;

            case R.id.buttonsLayout:
                return;

            default:
                break;
        }
        dismiss();
    }

    private void performShare(SHARE_MEDIA platform) {
        performShare(platform, mActivity, mShareData);
    }

    public static class ShareData implements Parcelable {
        public static final Parcelable.Creator<ShareData> CREATOR = new Parcelable.Creator<ShareData>() {
            @Override
            public ShareData createFromParcel(Parcel in) {
                return new ShareData(in);
            }

            @Override
            public ShareData[] newArray(int size) {
                return new ShareData[size];
            }
        };
        public String name = "";
        public String link = "";
        public String des = "";
        private String img = "";

        private ShareData(Parcel in) {
            name = in.readString();
            link = in.readString();
            img = in.readString();
            des = in.readString();
        }

        public ShareData(String name, String des, String link) {
            this.name = name;
            this.des = des;
            this.link = link;
            this.img = "";
        }


        public ShareData(Maopao.MaopaoObject mMaopaoObject) {
            this.name = mMaopaoObject.owner.name + "的冒泡";
            this.link = mMaopaoObject.getMobileLink();

            MessageParse parse = HtmlContent.parseMessage(mMaopaoObject.content);
            this.des = HtmlContent.parseToShareText(parse.text);
            if (parse.uris.size() > 0) {
                this.img = parse.uris.get(0);
            }

//            String des = HtmlContent.parseToShareText(mMaopaoObject.content);
//            this.des = HtmlContent.parseToShareText(des);
//            ArrayList<String> uris = HtmlContent.parseMessage(mMaopaoObject).uris;
//            if (uris.size() > 0) {
//                this.img = uris.get(0);
//            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(link);
            dest.writeString(img);
            dest.writeString(des);
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }
    }

}
