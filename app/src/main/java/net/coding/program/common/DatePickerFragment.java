package net.coding.program.common;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import net.coding.program.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by chaochen on 14/12/23.
 * 日期选择
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public static final String PARAM_MAX_TODYA = "PARAM_MAX_TODYA";
    public static final String PARAM_DATA = "date";
    //    SetTimeType mTimeType = SetTimeType.Cancel;
// 小米手机不管按那个按钮都会调用 onDataSet，只好在click事件里面做标记
//    enum SetTimeType {
//        Cancel, Set, Clear;
//    };
    private DateSet mDateSet;

    @Override
    public void onAttach(Activity activity) {
        if (activity instanceof DateSet) {
            mDateSet = (DateSet) activity;
        }
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String dateString = getArguments().getString(PARAM_DATA);
        if (dateString.isEmpty()) {
            dateString = new SimpleDateFormat("yyyy-MM-dd")
                    .format(Calendar.getInstance().getTimeInMillis());
        }
        boolean maxToday = getArguments().getBoolean(PARAM_MAX_TODYA, false);

        String[] date = dateString.split("-");
        int year = Integer.valueOf(date[0]);
        int month = Integer.valueOf(date[1]) - 1;
        int day = Integer.valueOf(date[2]);

        final DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        if (maxToday) {
            datePickerDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
        }

        if (getArguments().getBoolean("clear", false)) {
            datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "清除", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mDateSet.dateSetResult("", true);
                    dialog.cancel();
                }
            });
        }

        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatePicker datePicker = datePickerDialog.getDatePicker();
                dateSet(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                dialog.cancel();

            }
        });

        LinearLayout layoutParent = (LinearLayout) datePickerDialog.getDatePicker().getChildAt(0);
        LinearLayout layout = (LinearLayout) layoutParent.getChildAt(0);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View v = layout.getChildAt(i);
            if (v instanceof NumberPicker) {
                setNumberPicker((NumberPicker) v);
            }
        }
        return datePickerDialog;
    }

    // 用来取代onDateSet
    private void dateSet(int year, int monthOfYear, int dayOfMonth) {
        final Calendar c = Calendar.getInstance();
        c.set(year, monthOfYear, dayOfMonth);
        if (mDateSet != null) {
            mDateSet.dateSetResult(Global.dayFromTime(c.getTimeInMillis()), false);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // 因为小米手机一定会调这个接口，即使选择了取消，干脆不用这个接口算了
    }

    public void setNumberPicker(NumberPicker spindle) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class
                .getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(spindle, getResources().getDrawable(R.drawable.line_green));
                } catch (Exception e) {
                    Global.errorLog(e);
                }
                break;
            }
        }
    }

    public interface DateSet {
        void dateSetResult(String date, boolean clear);
    }
}
