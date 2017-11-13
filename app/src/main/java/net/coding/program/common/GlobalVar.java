package net.coding.program.common;

import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by chenchao on 2017/11/13.
 */

@SharedPref(value = SharedPref.Scope.APPLICATION_DEFAULT)
public interface GlobalVar {

    @DefaultString("")
    String pushToken();

    @DefaultString("")
    String pushType();
}
