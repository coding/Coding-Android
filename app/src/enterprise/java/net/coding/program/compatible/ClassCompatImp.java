package net.coding.program.compatible;

import net.coding.program.EnterpriseMainActivity_;
import net.coding.program.project.EnterpriseProjectFragment_;

/**
 * Created by chenchao on 2016/12/28.
 */

public class ClassCompatImp implements ClassCompatInterface {

    @Override
    public Class<?> getMainActivity() {
        return EnterpriseMainActivity_.class;
    }

    @Override
    public Class<?> getMainProjectFragment() {
        return EnterpriseProjectFragment_.FragmentBuilder_.class;
    }
}
