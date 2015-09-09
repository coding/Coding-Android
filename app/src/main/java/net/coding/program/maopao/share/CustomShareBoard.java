/**
 *
 */

package net.coding.program.maopao.share;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.bean.StatusCode;
import com.umeng.socialize.controller.UMEvernoteHandler;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;

import net.coding.program.AllThirdKeys;
import net.coding.program.R;
import net.coding.program.common.Global;

/**
 *
 */
public class CustomShareBoard extends PopupWindow implements OnClickListener {

    private UMSocialService mController = UMServiceFactory.getUMSocialService("net.coding.program");
    private Activity mActivity;
    private ShareData mShareData;

    public CustomShareBoard(Activity activity, ShareData shareData) {
        super(activity);
        this.mActivity = activity;
        initView(activity);

        mShareData = shareData;
    }

    private void addQQ() {
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(mActivity, AllThirdKeys.QQ_APP_ID, AllThirdKeys.QQ_APP_KEY);
        qqSsoHandler.setTargetUrl(mShareData.link);
        qqSsoHandler.setTitle(mShareData.name);
        qqSsoHandler.addToSocialSDK();
    }

    private void addWX() {
        UMWXHandler wxHandler = new UMWXHandler(mActivity, AllThirdKeys.WX_APP_ID, AllThirdKeys.WX_APP_KEY);
        wxHandler.setTargetUrl(mShareData.link);
        wxHandler.setTitle(mShareData.name);
        wxHandler.addToSocialSDK();
    }

    private void addWXCircle() {
        UMWXHandler wxCircleHandler = new UMWXHandler(mActivity, AllThirdKeys.WX_APP_ID, AllThirdKeys.WX_APP_KEY);
        wxCircleHandler.setTargetUrl(mShareData.link);
        wxCircleHandler.setTitle(mShareData.name);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
    }

    private void addQQZone() {
        // 添加QZone平台
        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(mActivity, AllThirdKeys.QQ_APP_ID, AllThirdKeys.QQ_APP_KEY);
        qZoneSsoHandler.setTargetUrl(mShareData.link);
        qZoneSsoHandler.addToSocialSDK();
    }

    private void addSinaWeibo() {
//        SinaSsoHandler sinaSsoHandler = new SinaSsoHandler();
//        sinaSsoHandler.setTargetUrl(mShareData.link);
//        sinaSsoHandler.addToSocialSDK();
        mController.getConfig().setSsoHandler(new SinaSsoHandler());
    }

    private void addEvernote() {
        UMEvernoteHandler evernoteHandler = new UMEvernoteHandler(mActivity);
        evernoteHandler.addToSocialSDK();

        // 设置evernote的分享内容
//        EvernoteShareContent shareContent = new EvernoteShareContent(
//                "来自友盟社会化组件（SDK）让移动应用快速整合社交分享功能-EverNote。http://www.umeng.com/social");
//        shareContent.setShareMedia(new UMImage(getActivity(), R.drawable.test));
//        mController.setShareMedia(shareContent);
    }

    public static class ShareData implements Parcelable {
        public String name = "";
        public String link = "";
        private String img = "";
        public String des = "";

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

        private ShareData(Parcel in) {
            name = in.readString();
            link = in.readString();
            img = in.readString();
            des = in.readString();
        }

        public ShareData(String name, String des, String link, String img) {
            this.name = name;
            this.des = des;
            this.link = link;
            this.img = img;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

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
    }

//    private static class ButtonPair {
//        Button likeBt, shareBt, commentBt;
//
//        public ButtonPair(Button likeBt, Button shareBt, Button commentBt) {
//            super();
//            this.likeBt = likeBt;
//            this.shareBt = shareBt;
//            this.commentBt = commentBt;
//        }
//
//    }

    @SuppressWarnings("deprecation")
    private void initView(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.share_custom_board, null);
        final int[] buttns = new int[]{
                R.id.wechat,
                R.id.wechat_circle,
                R.id.qq,
                R.id.qzone,
                R.id.sinaWeibo,
                R.id.evernote,
                R.id.codingFriend,
                R.id.linkCopy
        };
        for (int id : buttns) {
            rootView.findViewById(id).setOnClickListener(this);
        }

        setContentView(rootView);
        setWidth(LayoutParams.MATCH_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setTouchable(true);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.wechat:
                addWX();
                performShare(SHARE_MEDIA.WEIXIN);
                break;
            case R.id.wechat_circle:
                addWXCircle();
                performShare(SHARE_MEDIA.WEIXIN_CIRCLE);
                break;
            case R.id.qq:
                addQQ();
                performShare(SHARE_MEDIA.QQ);
                break;
            case R.id.qzone:
                addQQZone();
                performShare(SHARE_MEDIA.QZONE);
                break;

            case R.id.sinaWeibo:
                addSinaWeibo();
                performShare(SHARE_MEDIA.SINA);
                break;

            case R.id.evernote:
                addEvernote();
                performShare(SHARE_MEDIA.EVERNOTE);
                break;

            case R.id.codingFriend:
                break;

            case R.id.linkCopy:
                Global.copy(mActivity, mShareData.link);
                Toast.makeText(mActivity, "链接已复制 " + mShareData.link, Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
    }

    private void performShare(SHARE_MEDIA platform) {
        mController.setShareContent(mShareData.des);
        if (!mShareData.getImg().isEmpty()) {
            UMImage umImage = new UMImage(mActivity, mShareData.getImg());
            mController.setShareImage(umImage);
        }

        if (platform == SHARE_MEDIA.SINA) {
            SinaShareContent sinaContent = new SinaShareContent();
            sinaContent.setShareContent(mShareData.des + " " + mShareData.link);
            sinaContent.setTargetUrl(mShareData.link);
            UMImage umImage = new UMImage(mActivity, mShareData.img);
            sinaContent.setShareImage(umImage);
            sinaContent.setTitle(mShareData.name);
            sinaContent.setAppWebSite(mShareData.link);
            mController.setShareMedia(sinaContent);
        }

        mController.postShare(mActivity, platform, new SocializeListeners.SnsPostListener() {

                    @Override
                    public void onStart() {
                        dismiss();
                    }

                    @Override
                    public void onComplete(SHARE_MEDIA platform, int eCode, SocializeEntity entity) {
                        String showText = platform.toString();
                        if (eCode == StatusCode.ST_CODE_SUCCESSED) {
                            showText += "平台分享成功";
                        } else {
                            showText += "平台分享失败";
                        }
                        Log.d("", "umengshare " + eCode);
                        Toast.makeText(mActivity, showText, Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }

        );
    }

}
