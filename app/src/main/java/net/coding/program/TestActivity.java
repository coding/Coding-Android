package net.coding.program;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;

import net.coding.program.login.phone.InputAccountActivity_;
import net.coding.program.login.phone.PhoneSetPasswordActivity;
import net.coding.program.login.phone.PhoneSetPasswordActivity_;


public class TestActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        ActionBar supportActionBar = getSupportActionBar();
//
//        supportActionBar.setDisplayShowCustomEnabled(true);
//        supportActionBar.setCustomView(R.layout.actionbar_custom_spinner);
//        Spinner spinner = (Spinner) supportActionBar.getCustomView().findViewById(R.id.spinner);
//
////        spinner = (Spinner) findViewById(R.id.spinner2);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.maopao_action_types, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        findViewById(R.id.clickMain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this, EntranceActivity_.class);
                startActivity(intent);
            }
        });

//        onClick2(null);
//        onClick3(null);
        click1(null);
    }

    public void click1(View v) {
//        finish();
//        test(0);
        onClick3(null);
    }

    private void test(int which) {
        PhoneSetPasswordActivity.Type type;
        if (which == 0) {
            type = PhoneSetPasswordActivity.Type.reset;
        } else {
            type = PhoneSetPasswordActivity.Type.activate;
        }
        InputAccountActivity_.intent(this).type(type).start();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_test, menu);
//        return true;
//    }

    public void onClick2(View v) {
//        UsersListActivity_.intent(this)
//                .type(UsersListActivity.Friend.Follow)
//                .hideFollowButton(true)
//                .relayString("haahahahaha")
//                .start();
//        ForksListActivity_.intent(this)
//                .projectPath("/user/coding/project/Coding-Android")
//                .start();

//        LocalProjectFileActivity_.intent(this)
//                .start();
        test(1);
    }

    public void onClick3(View v) {
        PhoneSetPasswordActivity.Type type = PhoneSetPasswordActivity.Type.register;
        new PhoneSetPasswordActivity_.IntentBuilder_(this)
                .type(type)
                .start();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
