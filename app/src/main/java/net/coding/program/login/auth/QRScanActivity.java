package net.coding.program.login.auth;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.umeng.analytics.MobclickAgent;

import net.coding.program.R;
import net.coding.program.common.umeng.UmengEvent;

public class QRScanActivity extends ActionBarActivity implements QRCodeReaderView.OnQRCodeReadListener {

    // 说明是由tip界面跳转过来的
    public static final String EXTRA_TIP = "EXTRA_TIP";
    Toast mToast;
    private QRCodeReaderView qrCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main1);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    View codeViewRoot;

    @Override
    protected void onResume() {
        super.onResume();

        codeViewRoot = getLayoutInflater().inflate(R.layout.activity_main1, null, false);
        ((ViewGroup) findViewById(android.R.id.content)).addView(codeViewRoot);
        qrCodeView = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        qrCodeView.setOnQRCodeReadListener(this);

        qrCodeView.getCameraManager().startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (codeViewRoot != null) {
            qrCodeView.getCameraManager().stopPreview();
            qrCodeView = null;
            ((ViewGroup) findViewById(android.R.id.content)).removeView(codeViewRoot);
            codeViewRoot = null;
        }

    }

    @Override
    public void onQRCodeRead(String s, PointF[] pointFs) {
        Log.d("", "scan " + s);
        // 可能调用多次，所以做个检测
        if (isFinishing()) {
            return;
        }

        if (!AuthInfo.isAuthUrl(s)) {
            if (mToast == null) {
                mToast = Toast.makeText(this, "不符合要求的二维码", Toast.LENGTH_SHORT);
            }
            mToast.show();
            return;
        }

        if (getIntent().getBooleanExtra(EXTRA_TIP, false)) {
            Intent intent = new Intent(this, AuthListActivity.class);
            intent.putExtra("data", s);
            startActivity(intent);
        } else {
            Intent intent = new Intent();
            intent.putExtra("data", s);
            setResult(RESULT_OK, intent);
        }

        MobclickAgent.onEvent(this, UmengEvent.LOCAL, "扫描2fa成功");
        finish();
    }

    @Override
    public void cameraNotFound() {
    }

    @Override
    public void QRCodeNotFoundOnCamImage() {
    }

}
