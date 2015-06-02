package net.coding.program.common.htmltext;

import net.coding.program.common.Global;
import net.coding.program.model.Maopao;

/**
 * Created by chenchao on 15/3/9.
 */
public class LinkCreate {

    public static String maopao(Maopao.MaopaoObject maopao) {
        return Global.HOST + maopao.path;
    }


}
