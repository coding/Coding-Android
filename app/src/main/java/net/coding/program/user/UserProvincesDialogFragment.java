package net.coding.program.user;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import net.coding.program.R;

import org.androidannotations.annotations.EFragment;

/**
 * Created by yangzhen on 14-10-13.
 */

@EFragment(R.layout.fragment_user_provinces_dialog)
public class UserProvincesDialogFragment extends DialogFragment implements ProvincesPickerDialog.OnDateSetListener {

    //private UserInfoActivity mActivity;
    private ProvincesPickerDialog.OnDateSetListener mCallBack;

    @Override
    public void onAttach(Activity activity) {
        /*if (activity instanceof UserInfoActivity)
        {
            mActivity = (UserInfoActivity) activity;
        }*/
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(ProvincesPickerDialog.TITLE);
        String location = getArguments().getString(ProvincesPickerDialog.LOCATION);
        return new ProvincesPickerDialog(getActivity(), this, title, location);
    }

    public void setCallBack(ProvincesPickerDialog.OnDateSetListener mCallBack) {
        this.mCallBack = mCallBack;
    }

    @Override
    public void onDateSet(String provinceStr) {
        if (mCallBack != null)
            mCallBack.onDateSet(provinceStr);
    }
}
