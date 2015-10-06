package net.coding.program.project.init.create;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

/**
 * Created by jack wang on 2015/3/31.
 */
@EActivity(R.layout.init_activity_project_type)
@OptionsMenu(R.menu.menu_project_type_info)
public class ProjectTypeActivity extends BackActivity {

    public static final String TYPE_PUBLIC = "公开";

    public static final String TYPE_PRIVATE = "私有";

    @ViewById
    View projectPrivate;

    @ViewById
    View projectPublic;

    @ViewById
    ImageView iconPrivateRight;

    @ViewById
    ImageView iconPublicRight;

    @ViewById
    TextView maskInfo;

    @AfterViews
    protected final void initProjectTypeActivity() {
        String type = getIntent().getStringExtra("type");
        if (type.equals(TYPE_PRIVATE)) {
            iconPrivateRight.setVisibility(View.VISIBLE);
            iconPublicRight.setVisibility(View.GONE);
        } else {
            iconPrivateRight.setVisibility(View.GONE);
            iconPublicRight.setVisibility(View.VISIBLE);
        }
    }

    @Click
    void projectPrivate() {
        iconPrivateRight.setVisibility(View.VISIBLE);
        iconPublicRight.setVisibility(View.GONE);
        Intent intent = new Intent();
        intent.putExtra("type", TYPE_PRIVATE);
        setResult(-1, intent);
        finish();
    }

    @Click
    void projectPublic() {
        iconPrivateRight.setVisibility(View.GONE);
        iconPublicRight.setVisibility(View.VISIBLE);
        Intent intent = new Intent();
        intent.putExtra("type", TYPE_PUBLIC);
        setResult(-1, intent);
        finish();
    }

    @Click
    void maskInfo() {
        maskInfo.setVisibility(View.GONE);
    }

    @OptionsItem(R.id.action_info)
    protected final void tips() {
        if (maskInfo.getVisibility() == View.GONE) {
            maskInfo.getBackground().setAlpha(200);
            maskInfo.setVisibility(View.VISIBLE);
        } else {
            maskInfo.setVisibility(View.GONE);
        }
    }

}
