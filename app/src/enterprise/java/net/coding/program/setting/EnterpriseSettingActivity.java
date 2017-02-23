package net.coding.program.setting;

import android.app.Activity;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.widget.CircleImageView;
import net.coding.program.model.EnterpriseInfo;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_enterprise_setting)
public class EnterpriseSettingActivity extends BackActivity {
    private static final int REQUEST_UPDATE_NAME = 1000;

    @ViewById
    CircleImageView enterpriseHead;
    @ViewById
    TextView enterpriseNameTv;
    @ViewById
    TextView personIp;

    @AfterViews
    void initView(){
        setActionBarTitle(getString(R.string.enterprise_setting));
        ImageLoader.getInstance().displayImage(EnterpriseInfo.instance().getAvatar(), enterpriseHead, ImageLoadTool.options);
        enterpriseNameTv.setText(EnterpriseInfo.instance().getName());
        personIp.setText(EnterpriseInfo.instance().getGlobalkey());
    }

    @Click
    void enterpriseName(){
        EnterpriseNameActivity_.intent(this).startForResult(REQUEST_UPDATE_NAME);
    }

    @Click
    void enterpriseHeadLayout(){

    }

    @OnActivityResult(REQUEST_UPDATE_NAME)
    void updateNameResult(int result){
        if (result == Activity.RESULT_OK) {
            enterpriseNameTv.setText(EnterpriseInfo.instance().getName());
        }
    }
}
