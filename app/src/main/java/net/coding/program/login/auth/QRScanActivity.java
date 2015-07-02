package net.coding.program.login.auth;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import net.coding.program.R;

public class QRScanActivity extends ActionBarActivity implements QRCodeReaderView.OnQRCodeReadListener {

    // 说明是由tip界面跳转过来的
    public static final String EXTRA_TIP = "EXTRA_TIP";

    private QRCodeReaderView qrCodeView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        qrCodeView = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        qrCodeView.setOnQRCodeReadListener(this);

        textView = (TextView) findViewById(R.id.textView);
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

    @Override
    protected void onResume() {
        super.onResume();
        qrCodeView.getCameraManager().startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrCodeView.getCameraManager().stopPreview();
    }

    @Override
    public void onQRCodeRead(String s, PointF[] pointFs) {
        textView.setText(s);
        Log.d("", "url " + s);


        if (getIntent().getBooleanExtra(EXTRA_TIP, false)) {
            Intent intent = new Intent(this, AuthListActivity.class);
            intent.putExtra("data", s);
            startActivity(intent);
        } else {
            Intent intent = new Intent();
            intent.putExtra("data", s);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public void cameraNotFound() {
        textView.setText("cameraNotFound");
    }

    @Override
    public void QRCodeNotFoundOnCamImage() {
        textView.setText("QRCodeNotFoundOnCamImage");
    }

}
