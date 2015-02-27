package net.coding.program;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;


@EActivity(R.layout.activity_base_annotation)
public class BaseAnnotationActivity extends BaseActivity {

    @AfterViews
    protected void annotationDispayHomeAsUp() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OptionsItem(android.R.id.home)
    protected void annotaionClose() {
        onBackPressed();
    }
}
