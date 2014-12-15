package net.coding.program.common;

import android.content.Intent;

/**
 * Created by chaochen on 14-10-29.
 */
public interface StartActivity {
    public void startActivityForResult(Intent intent, int requestCode);
}
