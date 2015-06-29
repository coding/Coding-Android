package net.coding.program.common.guide;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.HashMap;
import java.util.Map;

class CalligraphyFactory {
    private static final String ACTION_BAR_TITLE = "action_bar_title";
    private static final String ACTION_BAR_SUBTITLE = "action_bar_subtitle";
    private static final Map<Class<? extends TextView>, Integer> sStyles = new HashMap() {
        {
            this.put(TextView.class, Integer.valueOf(16842884));
            this.put(Button.class, Integer.valueOf(16842824));
            this.put(EditText.class, Integer.valueOf(16842862));
            this.put(AutoCompleteTextView.class, Integer.valueOf(16842859));
            this.put(MultiAutoCompleteTextView.class, Integer.valueOf(16842859));
            this.put(CheckBox.class, Integer.valueOf(16842860));
            this.put(RadioButton.class, Integer.valueOf(16842878));
            this.put(ToggleButton.class, Integer.valueOf(16842827));
        }
    };
    private final int mAttributeId;

    public CalligraphyFactory(int attributeId) {
        this.mAttributeId = attributeId;
    }

    protected static int[] getStyleForTextView(TextView view) {
        int[] styleIds = new int[]{-1, -1};
        if (isActionBarTitle(view)) {
            styleIds[0] = 16843470;
            styleIds[1] = 16843512;
        } else if (isActionBarSubTitle(view)) {
            styleIds[0] = 16843470;
            styleIds[1] = 16843513;
        }

        if (styleIds[0] == -1) {
            styleIds[0] = sStyles.containsKey(view.getClass()) ? sStyles.get(view.getClass()).intValue() : 16842804;
        }

        return styleIds;
    }

    @SuppressLint({"NewApi"})
    protected static boolean isActionBarTitle(TextView view) {
        if (matchesResourceIdName(view, "action_bar_title")) {
            return true;
        } else if (parentIsToolbarV7(view)) {
            Toolbar parent = (Toolbar) view.getParent();
            return TextUtils.equals(parent.getTitle(), view.getText());
        } else {
            return false;
        }
    }

    @SuppressLint({"NewApi"})
    protected static boolean isActionBarSubTitle(TextView view) {
        if (matchesResourceIdName(view, "action_bar_subtitle")) {
            return true;
        } else if (parentIsToolbarV7(view)) {
            Toolbar parent = (Toolbar) view.getParent();
            return TextUtils.equals(parent.getSubtitle(), view.getText());
        } else {
            return false;
        }
    }

    protected static boolean parentIsToolbarV7(View view) {
//        return CalligraphyUtils.canCheckForV7Toolbar() && view.getParent() != null && view.getParent() instanceof Toolbar;
        return view.getParent() != null && view.getParent() instanceof Toolbar;
    }

    protected static boolean matchesResourceIdName(View view, String matches) {
        if (view.getId() == -1) {
            return false;
        } else {
            String resourceEntryName = view.getResources().getResourceEntryName(view.getId());
            return resourceEntryName.equalsIgnoreCase(matches);
        }
    }

    public View onViewCreated(View view, Context context, AttributeSet attrs) {
        if (view != null && view.getTag(uk.co.chrisjenx.calligraphy.R.id.calligraphy_tag_id) != Boolean.TRUE) {
            this.onViewCreatedInternal(view, context, attrs);
            view.setTag(uk.co.chrisjenx.calligraphy.R.id.calligraphy_tag_id, Boolean.TRUE);
        }

        return view;
    }

    void onViewCreatedInternal(View view, final Context context, AttributeSet attrs) {
    }
}
