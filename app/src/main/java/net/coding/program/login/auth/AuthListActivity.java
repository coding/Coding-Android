package net.coding.program.login.auth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.BaseActivity;
import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.Global;
import net.coding.program.model.AccountInfo;

import java.util.ArrayList;

public class AuthListActivity extends BaseActivity {

    private static final int TIME_UPDATE = 100;
    private final int RESULT_ADD_ACCOUNT = 1000;
    private AuthAdapter mAuthAdapter;
    private TotpClock mClock;
    private ListView listView;
    android.os.Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(0, TIME_UPDATE);

                long now = mClock.currentTimeMillis();
                setTotpCountdownPhaseFromTimeTillNextValue(getTimeTillNextCounterValue(now));
                mAuthAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mClock = new TotpClock(this);
        mAuthAdapter = new AuthAdapter(this, 0);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(mAuthAdapter);

        ArrayList<String> uris = AccountInfo.loadAuthDatas(this);
        mAuthAdapter.setNotifyOnChange(false);
        for (String uri : uris) {
            AuthInfo info = new AuthInfo(uri, mClock);
            mAuthAdapter.add(info);
        }
        mAuthAdapter.setNotifyOnChange(true);
        mAuthAdapter.notifyDataSetChanged();

        String extraData = getIntent().getStringExtra("data");
        if (extraData != null && !extraData.isEmpty()) {
            AuthInfo info = new AuthInfo(extraData, mClock);
            mAuthAdapter.add(info);
            mAuthAdapter.saveData();
        }

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final AuthInfo info = mAuthAdapter.getItem((int) id);

                AlertDialog.Builder builder = new AlertDialog.Builder(AuthListActivity.this);
                builder.setItems(R.array.auth_item_actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Global.copy(AuthListActivity.this, info.getCode());
                            showButtomToast(R.string.copy_code_finish);
                        } else {
                            showDialog("删除", "这是一个危险的操作，删除后可能会导致无法登录，确定删除吗？",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mAuthAdapter.remove(info);
                                            mAuthAdapter.saveData();
                                        }
                                    });
                        }
                    }
                });

                AlertDialog dialog = builder.show();
                CustomDialog.dialogTitleLineColor(view.getContext(), dialog);
                return true;
            }
        });

        mHandler.sendEmptyMessageDelayed(0, TIME_UPDATE);
    }

    @Override
    protected void onDestroy() {
        mHandler.removeMessages(0);
        mHandler = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_auth_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            Intent intent = new Intent(this, QRScanActivity.class);
            startActivityForResult(intent, RESULT_ADD_ACCOUNT);
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_ADD_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                String uriString = data.getStringExtra("data");
                AuthInfo item = new AuthInfo(uriString, mClock);
                mAuthAdapter.add(item);
                mAuthAdapter.saveData();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setTotpCountdownPhaseFromTimeTillNextValue(long millisRemaining) {
        setTotpCountdownPhase(
                ((double) millisRemaining) / Utilities.secondsToMillis(AuthInfo.getTotpCountet().getTimeStep()));
    }

    private long getTimeTillNextCounterValue(long time) {
        long currentValue = getCounterValue(time);
        long nextValue = currentValue + 1;
        long nextValueStartTime = Utilities.secondsToMillis(AuthInfo.getTotpCountet().getValueStartTime(nextValue));
        return nextValueStartTime - time;
    }

    private long getCounterValue(long time) {
        return AuthInfo.getTotpCountet().getValueAtTime(Utilities.millisToSeconds(time));
    }

    private void setTotpCountdownPhase(double phase) {
        for (int i = 0; i < listView.getChildCount(); ++i) {
            View listItemView = listView.getChildAt(i);
            CountdownIndicator indicator = (CountdownIndicator) listItemView.findViewById(R.id.indicator);
            indicator.setPhase(phase);

            TextView tv = (TextView) listItemView.findViewById(R.id.code);
            if (phase >= 0.1) {
                tv.setTextColor(0xff3bbd79);
            } else {
                tv.setTextColor(0xffe15957);
            }
        }
    }
}
