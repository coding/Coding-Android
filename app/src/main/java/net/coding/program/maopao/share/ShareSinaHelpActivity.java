package net.coding.program.maopao.share;

import android.content.Intent;
import android.os.Bundle;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;

import net.coding.program.common.ui.BaseActivity;

public class ShareSinaHelpActivity extends BaseActivity {

    public static final String EXTRA_SHARE_DATA = "EXTRA_SHARE_DATA";

    private CustomShareBoard.ShareData mShareData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_share_sina_help);

        mShareData = getIntent().getParcelableExtra(EXTRA_SHARE_DATA);

        addSinaWeibo();
        CustomShareBoard.performShare(SHARE_MEDIA.SINA, this, mShareData);
    }

    private void addSinaWeibo() {
//        SinaSsoHandler sinaSsoHandler = new SinaSsoHandler();
//        sinaSsoHandler.setTargetUrl(mShareData.link);
//        sinaSsoHandler.addToSocialSDK();
        CustomShareBoard.getShareController().getConfig().setSsoHandler(new SinaSsoHandler());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMSsoHandler ssoHandler = CustomShareBoard.getShareController().getConfig().getSsoHandler(
                requestCode);
        if (ssoHandler != null) {
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }

        finish();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_share_sina_help, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
