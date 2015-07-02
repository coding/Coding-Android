package net.coding.program.login.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import net.coding.program.R;

public class AuthListActivity extends ActionBarActivity {

    private static final int TIME_UPDATE = 100;
    private final int RESULT_ADD_ACCOUNT = 1000;
    private final TotpCounter mTotpCounter = new TotpCounter(PasscodeGenerator.INTERVAL);
    android.os.Handler mHandler;
    private AuthAdapter mAuthAdapter;
    private TotpClock mClock;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mClock = new TotpClock(this);
        mAuthAdapter = new AuthAdapter(this, 0);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(mAuthAdapter);

        String extraData = getIntent().getStringExtra("data");
        if (extraData != null && !extraData.isEmpty()) {
            AuthInfo info = new AuthInfo(extraData, mClock, mTotpCounter);
            mAuthAdapter.add(info);
        }

        mHandler = new android.os.Handler() {
            @Override
            public void handleMessage(Message msg) {
                mHandler.sendEmptyMessageDelayed(0, TIME_UPDATE);

                long now = mClock.currentTimeMillis();
                setTotpCountdownPhaseFromTimeTillNextValue(getTimeTillNextCounterValue(now));
                mAuthAdapter.notifyDataSetChanged();

            }
        };

        mHandler.sendEmptyMessageDelayed(0, TIME_UPDATE);
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
                AuthInfo item = new AuthInfo(uriString, mClock, mTotpCounter);
                mAuthAdapter.setNotifyOnChange(false);
                for (int i = 0; i < mAuthAdapter.getCount(); ++i) {
                    AuthInfo info = mAuthAdapter.getItem(i);
                    if (item.equals(info)) {
                        mAuthAdapter.remove(info);
                    }
                }
                mAuthAdapter.setNotifyOnChange(true);
                mAuthAdapter.add(item);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setTotpCountdownPhaseFromTimeTillNextValue(long millisRemaining) {
        setTotpCountdownPhase(
                ((double) millisRemaining) / Utilities.secondsToMillis(mTotpCounter.getTimeStep()));
    }

    private long getTimeTillNextCounterValue(long time) {
        long currentValue = getCounterValue(time);
        long nextValue = currentValue + 1;
        long nextValueStartTime = Utilities.secondsToMillis(mTotpCounter.getValueStartTime(nextValue));
        return nextValueStartTime - time;
    }

    private long getCounterValue(long time) {
        return mTotpCounter.getValueAtTime(Utilities.millisToSeconds(time));
    }

    private void setTotpCountdownPhase(double phase) {
        for (int i = 0; i < listView.getChildCount(); ++i) {
            CountdownIndicator indicator = (CountdownIndicator) listView.getChildAt(i).findViewById(R.id.indicator);
            indicator.setPhase(phase);
        }
    }
}
