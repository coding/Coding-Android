package net.coding.program.compatible;

import net.coding.program.MainActivity_;

/**
 * Created by chenchao on 2016/12/29.
 */

public class DefaultCompatImp implements ClassCompatInterface {
    @Override
    public Class<?> getMainActivity() {
        return MainActivity_.class;
    }
}
