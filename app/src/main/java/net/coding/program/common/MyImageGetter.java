package net.coding.program.common;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;

import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.enter.DrawableTool;

import java.lang.reflect.Field;

/**
 * Created by chaochen on 14-9-15.
 */
public class MyImageGetter implements Html.ImageGetter {

    private Context mActivity;

    public MyImageGetter(Context activity) {
        mActivity = activity;
    }

    @Override
    public Drawable getDrawable(String source) {
        String name = getPhotoName(source);
        int id = getResourceId(name);

        Drawable drawable;
        drawable = mActivity.getResources().getDrawable(id);

        DrawableTool.zoomDrwable(drawable, isMonkey(name));
        return drawable;
    }

    private String getPhotoName(String s) {
        try {
            int begin = s.lastIndexOf('/');
            ++begin;
            int end = s.lastIndexOf('.');
            return s.substring(begin, end);
        } catch (Exception e) {
            Global.errorLog(e);
        }

        return s;
    }

    private boolean isMonkey(String name) {
        return name.indexOf("coding") == 0;
    }

    private int getResourceId(String s) {
        String name = s.replace('-', '_');

        if (name.equals("e_mail")) {
            name = "e_mail";
        } else if (name.equals("non_potable_water")) {
            name = "non_potable_water";
        } else if (name.equals("+1")) {
            name = "a00001";
        } else if (name.equals("_1")) {
            name = "a00002";
        } else if (name.equals("new")) {
            name = "my_new_1";
        } else if (name.equals("8ball")) {
            name = "my8ball";
        } else if (name.equals("100")) {
            name = "my100";
        } else if (name.equals("1234")) {
            name = "my1234";
        }

        try {
            Field field = R.drawable.class.getField(name);
            return Integer.parseInt(field.get(null).toString());
        } catch (Exception e) {
        }

        return R.drawable.ic_launcher;
    }

};