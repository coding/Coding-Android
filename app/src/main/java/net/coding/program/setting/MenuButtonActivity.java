package net.coding.program.setting;

import android.view.Menu;
import android.view.MenuItem;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.EActivity;

/**
 * Created by chenchao on 2017/6/23.
 */

@EActivity(R.layout.activity_base_annotation)
public abstract class MenuButtonActivity extends BackActivity {

    protected abstract void afterMenuInit(MenuItem actionSend);

    protected abstract void actionSend();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_button, menu);
        MenuItem actionSend = menu.findItem(R.id.actionSend);

        afterMenuInit(actionSend);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionSend) {
            actionSend();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
