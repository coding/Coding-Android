package net.coding.program;

import net.coding.program.compatible.CodingCompat;
import net.coding.program.compatible.CodingCompatImp;

public class PersonApp extends MyApp {

    @Override
    public void onCreate() {
        super.onCreate();

        CodingCompat.init(new CodingCompatImp());
    }
}
