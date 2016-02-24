package net.coding.program.login.auth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.umeng.analytics.MobclickAgent;

import net.coding.program.R;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.common.umeng.UmengEvent;

import java.io.InputStream;

public class QRScanActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {

    // 说明是由tip界面跳转过来的
    public static final String EXTRA_TIP = "EXTRA_TIP";

    // 直接打开扫描到的 url
    public static final String EXTRA_OPEN_URL = "EXTRA_OPEN_URL";

    Toast mToast;
    private QRCodeReaderView qrCodeView;

    private final int RESULT_REQUEST_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main1);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

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

        findViewById(R.id.pickPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photo();
            }
        });
    }
    private void photo() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_REQUEST_PHOTO);
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

        if (getIntent().getBooleanExtra(EXTRA_OPEN_URL, false)) {
            Uri uri = Uri.parse(s);
            String host = uri.getHost();
            if (host.toLowerCase().endsWith("coding.net")) { // coding.net 结尾的使用内部浏览器打开, 比如 mart.coding.net
                URLSpanNoUnderline.openActivityByUri(this, s, false, true, true);
            } else {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(QRScanActivity.this, "用浏览器打开失败", Toast.LENGTH_LONG).show();
                }
            }

        } else {
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
        }
        MobclickAgent.onEvent(this, UmengEvent.LOCAL, "扫描2fa成功");

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Uri fileUri = data.getData();
                    new ScanPhotoTask().execute(fileUri);
                }
            }

        }
    }

        @Override
    public void cameraNotFound() {
    }

    @Override
    public void QRCodeNotFoundOnCamImage() {
    }

    class ScanPhotoTask extends AsyncTask<Uri, Void, Result> {

        String TAG = "readPhot";

        @Override
        protected Result doInBackground(Uri... params) {
            if (params == null || params.length != 1) {
                return null;
            }

            try {
                InputStream inputStream = QRScanActivity.this.getContentResolver().openInputStream(params[0]);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap == null) {
                    return null;
                }
                int width = bitmap.getWidth(), height = bitmap.getHeight();
                int[] pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                bitmap.recycle();
                RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
//                MultiFormatReader reader = new MultiFormatReader();
                QRCodeReader reader = new QRCodeReader();
                Result result = reader.decode(bBitmap);
                return result;
            } catch (Exception e) {
                Log.e(TAG, "can not open file", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            if (result == null) {
                Toast.makeText(QRScanActivity.this, "识别失败", Toast.LENGTH_SHORT).show();
                return;
            }

            onQRCodeRead(result.getText(), null);
        }
    };

}
