package study.chenchao.push_xiaomi;

import android.content.Context;

/**
 * Created by chenchao on 2017/11/2.
 */

public interface PushAction {

    String TAG = "CodingPush";

    public boolean init(Context context);

    public void bindGK(Context context, String gk);

    public void unbindGK(Context context, String gk);
}
