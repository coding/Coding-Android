package net.coding.program.third;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import net.coding.program.common.Global;

/**
 * Created by chenchao on 15/7/31.
 * 只是用来捕获异常，以免 crash
 */
public class MyQRCodeReaderView extends QRCodeReaderView {
    public MyQRCodeReaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            super.surfaceCreated(holder);
        } catch (Exception e) {
            Global.errorLog(e);
            Toast.makeText(getContext(), "打开相机失败，请检查是否关闭了 Coding 的相机权限", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            super.surfaceChanged(holder, format, width, height);
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            super.surfaceDestroyed(holder);
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }
}
