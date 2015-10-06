package net.coding.program.login.auth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.coding.program.common.ui.BaseActivity;
import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.Global;
import net.coding.program.common.WeakRefHander;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.model.AccountInfo;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class AuthListActivity extends BaseActivity implements Handler.Callback {

    private static final int TIME_UPDATE = 1000;
    private final int RESULT_ADD_ACCOUNT = 1000;
    WeakRefHander mWeakRefHandler;
    private AuthAdapter mAuthAdapter;
    private TotpClock mClock;
    private StickyListHeadersListView listView;

    @Override
    public boolean handleMessage(Message msg) {
        long now = mClock.currentTimeMillis();
        setTotpCountdownPhaseFromTimeTillNextValue(getTimeTillNextCounterValue(now));
        mAuthAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_list);

        umengEvent(UmengEvent.LOCAL, "查看2fa码");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mClock = new TotpClock(this);
        mAuthAdapter = new AuthAdapter(this, 0);
        listView = (StickyListHeadersListView) findViewById(R.id.listView);
        View footer = getLayoutInflater().inflate(R.layout.divide_15_top, null);
        listView.addFooterView(footer);
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
            showCoverDialog(extraData);
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

        mWeakRefHandler = new WeakRefHander(this, TIME_UPDATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mWeakRefHandler.start();
    }

    @Override
    public void onPause() {
        mWeakRefHandler.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWeakRefHandler.clear();
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
                showCoverDialog(uriString);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showCoverDialog(String uriString) {
        final AuthInfo item = new AuthInfo(uriString, mClock);
        if (mAuthAdapter.containItem(item)) {
            Toast.makeText(this, "这个账号已经添加过了", Toast.LENGTH_SHORT).show();
        } else if (mAuthAdapter.containItemDiffSecrect(item)) {
            showDialog("警告", "存在同名账号，确定覆盖？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    addAndSave(item);
                }
            });
        } else {
            Toast.makeText(this, "成功添加账号", Toast.LENGTH_SHORT).show();
            addAndSave(item);
        }
    }

    private void addAndSave(AuthInfo item) {
        mAuthAdapter.add(item);
        mAuthAdapter.saveData();
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
        ListView realList = listView.getWrappedList();
        for (int i = 0; i < realList.getChildCount(); ++i) {
            View listItemView = realList.getChildAt(i);
            View indicatorView = listItemView.findViewById(R.id.indicator);
            if (indicatorView != null) { // 因为 listview 的 child 不包含indicator
                ((CountdownIndicator) indicatorView).setPhase(phase);

                TextView tv = (TextView) listItemView.findViewById(R.id.code);
                if (phase >= 0.1) {
                    tv.setTextColor(0xff3bbd79);
                } else {
                    tv.setTextColor(0xffe15957);
                }
            }
        }
    }
}
