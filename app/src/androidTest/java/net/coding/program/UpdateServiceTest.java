package net.coding.program;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * Created by chaochen on 15/1/25.
 */
public class UpdateServiceTest extends ServiceTestCase<UpdateService> {

    public UpdateServiceTest() {
        super(UpdateService.class);
    }

    @SmallTest
    public void test1() {
        Intent intent = new Intent(getContext(), UpdateService.class);
        startService(intent);

    }
}
