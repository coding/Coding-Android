/**
 *
 */

package net.coding.program.maopao.share;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;

import net.coding.program.AllThirdKeys;
import net.coding.program.R;

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
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(mActivity, AllThirdKeys.APP_ID, AllThirdKeys.APP_KEY);
        qqSsoHandler.setTargetUrl(mShareData.link);
        qqSsoHandler.setTitle(mShareData.name);
        qqSsoHandler.addToSocialSDK();
    }

    private void addQQZone() {
        // 添加QZone平台
        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(mActivity, AllThirdKeys.APP_ID, AllThirdKeys.APP_KEY);
        qZoneSsoHandler.setTargetUrl(mShareData.link);
        qZoneSsoHandler.addToSocialSDK();
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
        rootView.findViewById(R.id.wechat).setOnClickListener(this);
        rootView.findViewById(R.id.wechat_circle).setOnClickListener(this);
        rootView.findViewById(R.id.qq).setOnClickListener(this);
        rootView.findViewById(R.id.qzone).setOnClickListener(this);
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
                performShare(SHARE_MEDIA.WEIXIN);
                break;
            case R.id.wechat_circle:
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

        mController.postShare(mActivity, platform, new SocializeListeners.SnsPostListener() {

                    @Override
                    public void onStart() {
                        dismiss();
                    }

                    @Override
                    public void onComplete(SHARE_MEDIA platform, int eCode, SocializeEntity entity) {
//                String showText = platform.toString();
//                if (eCode == StatusCode.ST_CODE_SUCCESSED) {
//                    showText += "平台分享成功";
//                } else {
//                    showText += "平台分享失败";
//                }
//                Toast.makeText(mActivity, showText, Toast.LENGTH_SHORT).show();
//                dismiss();
                    }
                }

        );
    }

}
