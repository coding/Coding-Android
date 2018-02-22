package net.coding.program.login.auth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
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
import com.orhanobut.logger.Logger;

import net.coding.program.R;
import net.coding.program.route.URLSpanNoUnderline;

import java.io.InputStream;

public class QRScanActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {

    // 说明是由tip界面跳转过来的
//    public static final String EXTRA_TIP = "EXTRA_TIP";

    public static final String EXTRA_OPEN_AUTH_LIST = "EXTRA_OPEN_AUTH_LIST"; // true 表示需要打开二次验证列表
    private final int RESULT_REQUEST_PHOTO = 1;
    View codeViewRoot;
    boolean enableScan = true;
    private QRCodeReaderView qrCodeView;
    private boolean openAuthList = true;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main1);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        openAuthList = getIntent().getBooleanExtra(EXTRA_OPEN_AUTH_LIST, true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.qrscan, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_photo:
                photo();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        codeViewRoot = getLayoutInflater().inflate(R.layout.activity_main1, null, false);
        ((ViewGroup) findViewById(android.R.id.content)).addView(codeViewRoot);
        qrCodeView = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        qrCodeView.setOnQRCodeReadListener(this);

        qrCodeView.getCameraManager().startPreview();
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
        Logger.d("scan " + s);

        if (TextUtils.isEmpty(s)) return;

        if (!enableScan) {
            return;
        }

        // 可能调用多次，所以做个检测
        if (isFinishing()) {
            return;
        }

        enableScan = false;

        if (!AuthInfo.isAuthUrl(s)) {
            Uri uri = Uri.parse(s);
            String host = uri.getHost();
            if (TextUtils.isEmpty(host)) {
                enableScan = true;
                return;
            }

            if (host.toLowerCase().endsWith("coding.net"))
            { // coding.net 结尾的使用内部浏览器打开, 比如 mart.coding.net
                URLSpanNoUnderline.openActivityByUri(this, s, false, true, true);
                finish();
            } else{
                new AlertDialog.Builder(QRScanActivity.this, R.style.MyAlertDialogStyle)
                        .setTitle("打开外部链接")
                        .setMessage(s)
                        .setPositiveButton("确定", (dialog, which) -> {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                                startActivity(intent);
                                finish();
                            } catch (Exception e) {
                                Toast.makeText(QRScanActivity.this, "用浏览器打开失败", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setOnDismissListener(dialog -> enableScan = true)
                        .show();
            }
        } else {
//            if (getIntent().getBooleanExtra(EXTRA_TIP, false)) {
            if (openAuthList) {
                Intent intent = new Intent(this, AuthListActivity.class);
                intent.putExtra("data", s);
                startActivity(intent);
            }
//            } else {
            Intent intentResult = new Intent();
            intentResult.putExtra("data", s);
            setResult(RESULT_OK, intentResult);
//            }
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Uri fileUri = data.getData();
                    if (mProgressDialog == null) {
                        mProgressDialog = new ProgressDialog(this);
                        mProgressDialog.setMessage("扫描中...");
                    }
                    mProgressDialog.show();
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
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Rect padding = new Rect(0, 0, 0, 0);
                BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();
                int minLength = Math.min(options.outWidth, options.outHeight);
                int MAX = 512; // 图片短边不超过 512
                if (minLength > MAX) {
                    options.inSampleSize = minLength / MAX;
                }
                options.inJustDecodeBounds = false;
                // 流打开后只能用一次, 需要重新获取
                inputStream = QRScanActivity.this.getContentResolver().openInputStream(params[0]);
                // 对图片裁剪后再扫码, 否则花的时间太长
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                if (bitmap == null) {
                    return null;
                }

                int width = bitmap.getWidth(), height = bitmap.getHeight();
                int[] pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                bitmap.recycle();

                RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
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
            if (mProgressDialog != null) {
                mProgressDialog.hide();
            }

            if (result == null) {
                new AlertDialog.Builder(QRScanActivity.this, R.style.MyAlertDialogStyle)
                        .setTitle("提示")
                        .setMessage("未发现二维码")
                        .setPositiveButton("确定", null)
                        .show();
                return;
            }

            onQRCodeRead(result.getText(), null);
        }
    }
}
