package net.coding.program.user;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.BlankViewHelp;
import net.coding.program.network.HttpObserverRaw;
import net.coding.program.network.Network;
import net.coding.program.network.model.HttpResult;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_user_detail_more)
@OptionsMenu(R.menu.menu_user_detail_more)
public class UserDetailMoreActivity extends BackActivity {

    @Extra
    UserObject mUserObject;

    @Extra
    String globalKey;

    @StringArrayRes
    String[] user_detail_more_list_first;

    @StringArrayRes
    String[] sexs;

    @ViewById
    View blankLayout;

    @ViewById
    View createAtLayout,
            lastLoginLayout,
            globalKeyLayout,
            sexLayout,
            birthdayLayout,
            locateLayout,
            loginsLayout,
            degreeLayout,
            schoolLayout,
            companyLayout,
            jobLayout,
            skillLayout,
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
            degreeTextView,
            schoolTextView,
            skillTextView,
            tagsTextView;

    @ViewById
    ImageView icon;

    @StringArrayRes(R.array.user_degree)
    String[] userDegree;

    @ViewById
    TextView nameTextView;

    @AfterViews
    protected final void initUserDetailMoreActivity() {
        if (mUserObject != null) {
            bindUI();
        } else if (!TextUtils.isEmpty(globalKey)) {
            onRefrush();
        } else {
            finish();
        }
    }

    private void onRefrush() {
        BlankViewHelp.setBlankLoading(blankLayout, true);
        Network.getRetrofit(this)
                .getUserInfo(globalKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserverRaw<HttpResult<UserObject>>(this) {
                    @Override
                    public void onSuccess(HttpResult<UserObject> data) {
                        super.onSuccess(data);
                        mUserObject = data.data;
                        bindUI();
                        BlankViewHelp.setBlankLoading(blankLayout, false);
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        BlankViewHelp.setBlank(0, UserDetailMoreActivity.this, false, blankLayout, v -> onRefrush());
                    }
                });
    }

    private void bindUI() {
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
                degreeLayout,
                schoolLayout,
                companyLayout,
                jobLayout,
                skillLayout,
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
        degreeTextView = (TextView) degreeLayout.findViewById(R.id.second);
        schoolTextView = (TextView) schoolLayout.findViewById(R.id.second);
        skillTextView = (TextView) skillLayout.findViewById(R.id.second);

        createAtTextView.setText(notEmpty(Global.dayToNow(mUserObject.created_at)));
        lastLoginTextView.setText(notEmpty(Global.dayToNow(mUserObject.last_activity_at)));
        globalKeyTextView.setText(notEmpty(mUserObject.global_key));
        sexTextView.setText(notEmpty(sexs[mUserObject.sex]));
        birthdayTextView.setText(notEmpty(mUserObject.birthday));
        locateTextView.setText(notEmpty(mUserObject.location));
        loginsTextView.setText(notEmpty(mUserObject.slogan));
        companyTextView.setText(notEmpty(mUserObject.company));
        jobTextView.setText(notEmpty(mUserObject.job_str));
        tagsTextView.setText(notEmpty(mUserObject.tags_str));
        degreeTextView.setText(notEmpty(mUserObject.getUserDegree()));
        schoolTextView.setText(notEmpty(mUserObject.school));
        skillTextView.setText(notEmpty(mUserObject.getUserSkills()));
    }

    private String notEmpty(String s) {
        if (s.isEmpty()) {
            return "未填写";
        }

        return s;
    }
}
