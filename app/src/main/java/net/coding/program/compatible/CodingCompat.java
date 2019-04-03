package net.coding.program.compatible;

/**
 * Created by chenchao on 2016/12/28.
 * 实现同 coding 普通版有差异的地方
 */

public class CodingCompat {

    private static ClassCompatInterface substance;

    private CodingCompat() {
    }

    public static void init(ClassCompatInterface substance) {
        CodingCompat.substance = substance;
    }

    public static ClassCompatInterface instance() {
        return substance;
    }

}

