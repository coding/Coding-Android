package net.coding.program.common;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by chenchao on 15/7/13.
 * 使用弱引用的handler，防止出现内存泄露
 */
public class LoopHander extends Handler {

    private final WeakReference<LoopAction> mRef;
    private final int mLoopTime;

    public LoopHander(LoopAction loopAction, int loopTime) {
        super();
        mRef = new WeakReference<>(loopAction);
        mLoopTime = loopTime;
    }

    @Override
    public void handleMessage(Message msg) {
        Log.d("", "loophandle");
        LoopAction action = mRef.get();
        if (action != null) {
            action.loopAction();
            sendEmptyMessageDelayed(0, mLoopTime);
        }
    }

    public interface LoopAction {
        void loopAction();
    }
}
