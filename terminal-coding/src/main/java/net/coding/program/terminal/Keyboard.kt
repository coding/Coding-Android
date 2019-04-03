package net.coding.program.terminal

import android.view.KeyEvent

/**
 * Created by chenchao on 2018/1/29.
 */
internal enum class KeyItem(val text: String = "", val icon: Int = 0, val value: Int) {
    ESC("ESC", 0,  KeyEvent.KEYCODE_ESCAPE),
    CTRL("CTRL", 0, KeyEvent.KEYCODE_CTRL_LEFT),
    ALT("ALT", 0, KeyEvent.KEYCODE_ALT_LEFT),
    TAB("", R.mipmap.key_tab, KeyEvent.KEYCODE_TAB),
    UP("▲", 0, KeyEvent.KEYCODE_DPAD_UP),
    DOWN("▼", 0, KeyEvent.KEYCODE_DPAD_DOWN),
    LEFT("◀", 0, KeyEvent.KEYCODE_DPAD_LEFT),
    RIGHT("▶", 0, KeyEvent.KEYCODE_DPAD_RIGHT),
    SLASH("/", 0, KeyEvent.KEYCODE_SLASH),
    MINUS("-", 0, KeyEvent.KEYCODE_MINUS),
    VER_LINE("|", 0, KeyEvent.KEYCODE_MINUS),
    AT("@", 0, KeyEvent.KEYCODE_AT),
    WAVY_LINE("~", 0, KeyEvent.KEYCODE_MINUS),
    POINT(".", 0, KeyEvent.KEYCODE_MINUS),
    COLON(":", 0, KeyEvent.KEYCODE_SEMICOLON),
    SEMICOLON(";", 0, KeyEvent.KEYCODE_SEMICOLON),
    MORE("", R.mipmap.key_more, KeyEvent.KEYCODE_MINUS);
}


