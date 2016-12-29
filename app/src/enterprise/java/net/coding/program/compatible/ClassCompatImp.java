package net.coding.program.compatible;

import net.coding.program.EnterpriseMainActivity_;

/**
 * Created by chenchao on 2016/12/28.
 */

public class ClassCompatImp implements ClassCompatInterface {

    @Override
    public Class<?> getMainActivity() {
        return EnterpriseMainActivity_.class;
    }
}
