package net.coding.program.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import net.coding.program.R;


/**
 * Created by zjh on 2017/2/15.
 * 抽取AlertDialog
 */

public class AlertDialogMessage {
    private Context context;

    public AlertDialogMessage(Context context) {
        this.context = context;
    }

    public void initDialog(String title, String editHint, OnBottomClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        LayoutInflater li = LayoutInflater.from(context);
        View v1 = li.inflate(R.layout.dialog_input, null);
        final EditText input = (EditText) v1.findViewById(R.id.value);
        input.setHint(editHint);
        builder.setTitle(title)
                .setView(v1).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onPositiveButton(input.getText().toString());
                }

            }
        }).setNegativeButton("取消", null)
                .show();
        input.requestFocus();
    }

    public interface OnBottomClickListener {
        void onPositiveButton(String editStr);

        void onNegativeButton();
    }
}
