package net.coding.program.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.UpdateService;
import net.coding.program.WebActivity_;
import net.coding.program.common.Global;
import net.coding.program.model.AccountInfo;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;

@EActivity(R.layout.activity_about)
public class AboutActivity extends BackActivity {

    @ViewById
    TextView version;
    int clickIconCount = 0;
    long lastClickTime = 0;

    @AfterViews
    final void initAboutActivity() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;

            String versionString = String.format("版本：%s", versionName);
            version.setText(versionString);

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    @Click
    void markCoding() {
        try {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "软件市场里暂时没有找到Coding", Toast.LENGTH_SHORT).show();
        }
    }

    @Click
    void checkUpdate() {
        Intent intent = new Intent(this, UpdateService.class);
        intent.putExtra(UpdateService.EXTRA_BACKGROUND, false);
        startService(intent);
    }

    @Click
    void codingWebsite() {
        WebActivity_.intent(this).url(Global.HOST).start();
    }

    @Click
    void icon() {
        long clickTime = Calendar.getInstance().getTimeInMillis();
        long lastTemp = lastClickTime;
        lastClickTime = clickTime;
        if (clickTime - lastTemp < 1000) {
            ++clickIconCount;
        } else {
            clickIconCount = 1;
        }

        if (clickIconCount >= 5) {
            clickIconCount = 0;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View content = getLayoutInflater().inflate(R.layout.host_setting, null);
            final EditText editText = (EditText) content.findViewById(R.id.edit);
            final EditText editCode = (EditText) content.findViewById(R.id.editCode);
            AccountInfo.CustomHost customHost = AccountInfo.getCustomHost(this);
            editText.setText(customHost.getHost());
            editCode.setText(customHost.getCode());
            editText.setHint(Global.DEFAULT_HOST);
            builder.setView(content)
                    .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String hostString = editText.getText().toString();
                            String hostCode = editCode.getText().toString();
                            AccountInfo.CustomHost customHost = new AccountInfo.CustomHost(hostString, hostCode);
                            if (!hostString.isEmpty()) {
                                AccountInfo.saveCustomHost(AboutActivity.this, customHost);
                            } else {
                                AccountInfo.removeCustomHost(AboutActivity.this);
                            }

                            setResult(RESULT_OK);
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
    }
}
