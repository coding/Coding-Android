package net.coding.program.compatible;

import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by chenchao on 2017/1/17.
 */

@SharedPref
public interface CompatShareData {

    @DefaultString("")
    String getEnterpriseGK();

}
