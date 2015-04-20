package net.coding.program.common;

/**
 * Created by chenchao on 15/4/1.
 */
public class PhoneType {

    public static boolean isX86() {
        String arch = System.getProperty("os.arch").toLowerCase();
        return arch.equals("i686") || arch.equals("x86");
    }

}
