package net.coding.program.project.detail;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.TopicLabelObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

@EActivity(R.layout.activity_modify_label)
@OptionsMenu(R.menu.modify_label)
public class ModifyLabelActivity extends BackActivity {

    private static final int RESULT_PICK_COLOR = 1;

    @Extra
    TopicLabelObject labelObject;

    @Extra
    String projectPath;

    @ViewById
    ImageView colorPreview;
    @ViewById
    EditText editText;

    @AfterViews
    final void initModifyLabelActivity() {
        labelObject.setColorValue(0xFF000000 | labelObject.getColorValue());
        editText.setText(labelObject.name);
        editText.setSelection(labelObject.name.length());

        updateColorPreview();
    }

    @Click
    void colorPreview() {
        PickLabelColorActivity_.intent(this)
                .generateColor(labelObject.getColorValue())
                .startForResult(RESULT_PICK_COLOR);
    }

    @OnActivityResult(RESULT_PICK_COLOR)
    void onResultPickColor(int result, @OnActivityResult.Extra int resultData) {
        if (result == RESULT_OK) {
            labelObject.setColorValue(resultData);
            updateColorPreview();
        }
    }

    private void updateColorPreview() {
        GradientDrawable bgDrawable = (GradientDrawable) colorPreview.getBackground();
        if (bgDrawable != null) {
            bgDrawable.setColor(labelObject.getColorValue());
        }
    }

    @OptionsItem
    void action_save() {
        String newName = editText.getText().toString();
        if (TextUtils.isEmpty(newName)) {
            showButtomToast("名字不能为空");
            return;
        }

        labelObject.name = newName;

        String url = Global.HOST_API + projectPath + "/topics/label/" + labelObject.id;
        RequestParams params = new RequestParams();
        params.put("name", labelObject.name);
        String colorStringr = String.format("#%06X", labelObject.getColorValue() & 0x00FFFFFF);
        params.put("color", colorStringr);
        MyAsyncHttpClient.put(this, url, params, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);

                Intent intent = new Intent();
                intent.putExtra("resultData", labelObject);
                setResult(RESULT_OK, intent);

                finish();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                showProgressBar(false);
            }
        });

        showProgressBar(true);

    }
}
