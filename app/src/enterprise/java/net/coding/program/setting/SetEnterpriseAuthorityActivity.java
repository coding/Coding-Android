package net.coding.program.setting;

import android.content.Intent;
import android.support.annotation.NonNull;

import net.coding.program.R;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.constant.MemberAuthority;
import net.coding.program.project.detail.DropdownListItemView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_set_enterprise_authority)
public class SetEnterpriseAuthorityActivity extends BackActivity {

    @Extra
    MemberAuthority authority;

    @Extra
    String globayKey;

    @ViewById
    DropdownListItemView enterprseManager, enterpriseMember;

    @AfterViews
    void initMemberAuthorityActivity() {
        enterprseManager.setText("管理员");
        enterpriseMember.setText("普通成员");

        enterprseManager.setChecked(false);
        enterpriseMember.setChecked(false);

        switch (authority) {
            case manager:
                enterprseManager.setChecked(true);
                break;
            case member:
                enterpriseMember.setChecked(true);
                break;
        }
    }

    @Click
    void enterprseManager() {
        modifyAuthority(MemberAuthority.manager.getType());
    }

    @Click
    void enterpriseMember() {
        modifyAuthority(MemberAuthority.member.getType());
    }

    private void modifyAuthority(int id) {
        Network.getRetrofit(this)
                .setEnterpriseRole(GlobalData.getEnterpriseGK(), globayKey, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpObserver(this) {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        umengEvent(UmengEvent.E_USER_CENTER, "企业角色设置");
                        showProgressBar(false);

                        Intent intent = new Intent();
                        intent.putExtra("intentData", id);
                        intent.putExtra("intentData1", globayKey);
                        setResult(RESULT_OK, intent);

                        finish();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        showProgressBar(false);
                    }
                });
        showProgressBar(true);
    }
}
