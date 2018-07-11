package net.coding.program.setting;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EActivity(R.layout.activity_about)
public class AboutActivity extends BackActivity {

    @ViewById
    TextView version;

    @AfterViews
    final void initAboutActivity() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;

            Pattern pattern = Pattern.compile("^(.*)\\.\\d{8}\\.\\d+$");
            Matcher matcher = pattern.matcher(versionName);
            String versionSplite = versionName;
            if (matcher.find()) {
                versionSplite = matcher.group(1);
            }

            String versionString = String.format("版本：%s", versionSplite);
            version.setText(versionString);

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    @Click
    void phone() {
        if (GlobalData.isEnterprise()) {
            Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:4009309163"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Click
    void markCoding() {
        Global.updateByMarket(this);
    }

    @Click
    void checkUpdate() {
        Global.updateByMarket(this);
    }

    @Click
    void codingWebsite() {
//        WebActivity_.intent(this).url(Global.HOST).start();
    }

    @Click
    void qq() {
        if (GlobalData.isEnterprise()) {
            Global.copy(this, "2847276903");
            showButtomToast("QQ 号已复制到剪贴板");
        } else {
            Global.copy(this, "617404718");
            showButtomToast("QQ 群号已复制到剪贴板");
        }
    }

    @Click
    void email() {
        if (GlobalData.isEnterprise()) {
            sendEmails(this, new String[]{"enterprise@coding.net"});
        } else {
            sendEmails(this, new String[]{"support@coding.net"});
        }
    }

    @Click
    void wechat() {
        Global.copy(this, "扣钉CODING");
        showButtomToast("微信号已复制到剪贴板");
    }

    @Click
    void icon() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;

            String versionString = String.format("%s", versionName);

            String msg = versionString;
            new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    .setMessage(msg)
                    .show();

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    public static void sendEmails(Context context, String[] addresses) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }
}
