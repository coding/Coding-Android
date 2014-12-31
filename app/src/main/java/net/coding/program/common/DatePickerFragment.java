package net.coding.program.common;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import net.coding.program.Global;
import net.coding.program.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
* Created by chaochen on 14/12/23.
*/
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

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
        String dateString = getArguments().getString("date");
        if (dateString.isEmpty()) {
            dateString = new SimpleDateFormat("yyyy-MM-dd")
                    .format(Calendar.getInstance().getTimeInMillis());
        }
        String[] date = dateString.split("-");
        int year = Integer.valueOf(date[0]);
        int month = Integer.valueOf(date[1]) - 1;
        int day = Integer.valueOf(date[2]);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        if (getArguments().getBoolean("clear", false)) {
            datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "清除", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEUTRAL) {
                        mDateSet.dateSetResult("", true);
                    }
                }
            });
        }

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

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Calendar c = Calendar.getInstance();
        c.set(year, monthOfYear, dayOfMonth);
        if (mDateSet != null) {
            mDateSet.dateSetResult(Global.dayFromTime(c.getTimeInMillis()), false);
        }
    }

    public void setNumberPicker(NumberPicker spindle) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class
                .getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(spindle, getResources().getDrawable(R.drawable.line_green));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public interface DateSet {
        public void dateSetResult(String date, boolean clear);
    }
}
