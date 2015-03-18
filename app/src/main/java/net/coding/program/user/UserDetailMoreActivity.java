package net.coding.program.user;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.BaseActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

@EActivity(R.layout.activity_user_detail_more)
@OptionsMenu(R.menu.menu_user_detail_more)
public class UserDetailMoreActivity extends BaseActivity {

    @Extra
    UserObject mUserObject;

    @StringArrayRes
    String[] user_detail_more_list_first;

    @StringArrayRes
    String[] sexs;

    @ViewById
    View createAtLayout,
            lastLoginLayout,
            globalKeyLayout,
            sexLayout,
            birthdayLayout,
            locateLayout,
            loginsLayout,
            companyLayout,
            jobLayout,
            tagsLayout;

    TextView createAtTextView,
            lastLoginTextView,
            globalKeyTextView,
            sexTextView,
            birthdayTextView,
            locateTextView,
            loginsTextView,
            companyTextView,
            jobTextView,
            tagsTextView;

    @ViewById
    ImageView icon;

    @ViewById
    TextView nameTextView;

    @AfterViews
    protected final void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        iconfromNetwork(icon, mUserObject.avatar);
        nameTextView.setText(mUserObject.name);

        View[] views = new View[]{
                createAtLayout,
                lastLoginLayout,
                globalKeyLayout,
                sexLayout,
                birthdayLayout,
                locateLayout,
                loginsLayout,
                companyLayout,
                jobLayout,
                tagsLayout
        };

        for (int i = 0; i < views.length; ++i) {
            View v = views[i];
            ((TextView) v.findViewById(R.id.first)).setText(user_detail_more_list_first[i]);
        }

        createAtTextView = (TextView) createAtLayout.findViewById(R.id.second);
        lastLoginTextView = (TextView) lastLoginLayout.findViewById(R.id.second);
        globalKeyTextView = (TextView) globalKeyLayout.findViewById(R.id.second);
        sexTextView = (TextView) sexLayout.findViewById(R.id.second);
        birthdayTextView = (TextView) birthdayLayout.findViewById(R.id.second);
        locateTextView = (TextView) locateLayout.findViewById(R.id.second);
        loginsTextView = (TextView) loginsLayout.findViewById(R.id.second);
        companyTextView = (TextView) companyLayout.findViewById(R.id.second);
        jobTextView = (TextView) jobLayout.findViewById(R.id.second);
        tagsTextView = (TextView) tagsLayout.findViewById(R.id.second);

        createAtTextView.setText(Global.dayToNow(mUserObject.created_at));
        lastLoginTextView.setText(Global.dayToNow(mUserObject.last_activity_at));
        globalKeyTextView.setText(mUserObject.global_key);
        sexTextView.setText(sexs[mUserObject.sex]);
        birthdayTextView.setText(mUserObject.birthday);
        locateTextView.setText(mUserObject.location);
        loginsTextView.setText(mUserObject.slogan);
        companyTextView.setText(mUserObject.company);
        jobTextView.setText(mUserObject.job_str);
        tagsTextView.setText(mUserObject.tags_str);
    }

    @OptionsItem(android.R.id.home)
    protected final void back() {
        finish();
    }


//    @Click
//    protected final void createAtLayout() {
//    }
//
//    @Click
//    protected final void lastLoginLayout() {
//    }
//
//    @Click
//    protected final void globalKeyLayout() {
//    }
//
//    @Click
//    protected final void sexLayout() {
//    }
//
//    @Click
//    protected final void birthdayLayout() {
//    }
//
//    @Click
//    protected final void locateLayout() {
//    }
//
//    @Click
//    protected final void loginsLayout() {
//    }
//
//    @Click
//    protected final void companyLayout() {
//    }
//
//    @Click
//    protected final void jobLayout() {
//    }
//
//    @Click
//    protected final void tagsLayout() {
//    }
}
