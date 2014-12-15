package net.coding.program.user;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import net.coding.program.R;
import net.coding.program.maopao.MaopaoListFragment_;

public class UserMaopaoActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_maopao);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        String userId = getIntent().getStringExtra("id");

        Fragment fragment = new MaopaoListFragment_();
        Bundle bundle = new Bundle();
        bundle.putSerializable("mType", "user");
        bundle.putString("userId", userId);
        fragment.setArguments(bundle);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment, "所有冒泡");
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
