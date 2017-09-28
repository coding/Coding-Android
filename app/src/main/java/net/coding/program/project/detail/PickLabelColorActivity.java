package net.coding.program.project.detail;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.widget.PickLabelColorItem;
import net.coding.program.common.widget.input.SimpleTextWatcher;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;

import java.util.List;

@EActivity(R.layout.activity_pick_label_color)
public class PickLabelColorActivity extends BackActivity {

    @Extra
    int generateColor = 0;

    @ViewById
    EditText inputColor;

    @ViewById
    View colorPreview;

    @ViewById
    ViewGroup rootLayout;

    //    PickLabelColorItem[] items = new PickLabelColorItem[COLORS.length];
    @ViewsById({R.id.color0, R.id.color1, R.id.color2, R.id.color3, R.id.color4, R.id.color5})
    List<PickLabelColorItem> colorItems;

    @AfterViews
    protected void initPickLabelColorActivity() {
        inputColor.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 6) {
                    String input = s.toString();
                    generateColor = Color.parseColor("#" + input);
                    updatePreview();
                }
            }
        });

        if (generateColor == 0) {
            clickGenerateColor();
        } else {
            updatePreview();
            updateInput();
        }

        for (int i = 0; i < colorItems.size(); ++i) {
            PickLabelColorItem item = colorItems.get(i);
            if (item.getColor() == generateColor) {
                item.setPicked();
                break;
            }
        }
    }

    @Click
    void clickGenerateColor() {
        generateColor = TopicLabelActivity.getRandomColor();
        updateInput();
    }

    private void updateInput() {
        String colorString = Global.colorToStringNoWellNumber(generateColor);
        inputColor.setText(colorString);
        inputColor.setSelection(colorString.length());
    }

    private void updatePreview() {
        GradientDrawable bgShape = (GradientDrawable) colorPreview.getBackground();
        if (bgShape != null) {
            bgShape.setColor(generateColor);
        }
    }

    @Click({R.id.color0, R.id.color1, R.id.color2, R.id.color3, R.id.color4, R.id.color5})
    public void onClick(View v) {
        if (v instanceof PickLabelColorItem) {
            PickLabelColorItem item = (PickLabelColorItem) v;
            closeAndPick(item.getColor());
        }
    }

    private void closeAndPick(int color) {
        Intent intent = new Intent();
        intent.putExtra("resultData", color);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        closeAndPick(generateColor);
    }
}
