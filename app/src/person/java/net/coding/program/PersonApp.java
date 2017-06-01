package net.coding.program;

import net.coding.program.compatible.CodingCompat;
import net.coding.program.compatible.CodingCompatImp;

/**
 * Created by chenchao on 2017/1/23.
 *
 */

public class PersonApp extends MyApp {

    @Override
    public void onCreate() {
        super.onCreate();

        CodingCompat.init(new CodingCompatImp());
    }
}
