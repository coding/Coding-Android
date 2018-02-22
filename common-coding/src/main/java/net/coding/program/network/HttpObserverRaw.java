package net.coding.program.network;

import android.content.Context;
import android.support.annotation.NonNull;

import com.orhanobut.logger.Logger;

import net.coding.program.common.Global;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.util.SingleToast;
import net.coding.program.common.widget.CommonListView;
import net.coding.program.network.model.BaseHttpResult;

import org.json.JSONObject;

import java.util.Map;

import rx.Observer;

public abstract class HttpObserverRaw<T1 extends BaseHttpResult> implements Observer<T1> {

    static JSONObject sNetworkError;
    static JSONObject sServiceError;

    private CommonListView listView;

    static {
        try {
            String connectFailString = String.format("{\"code\":%s,\"msg\":{\"error\":\"%s\"}}",
                    NetworkImpl.NETWORK_CONNECT_FAIL, NetworkImpl.ERROR_MSG_CONNECT_FAIL);
            sNetworkError = new JSONObject(connectFailString);


            String serviceFailString = String.format("{\"code\":%s,\"msg\":{\"error\":\"%s\"}}",
                    NetworkImpl.NETWORK_ERROR_SERVICE, NetworkImpl.ERROR_MSG_SERVICE_ERROR);
            sServiceError = new JSONObject(serviceFailString);

        } catch (Exception e) {
            Global.errorLog(e);
        }

    }

    // 主要是为了方便用 Toast 提示出错信息,
    private Context mActivity;
    private boolean showErrorTip = true;

    public HttpObserverRaw(Context mActivity) {
        this(mActivity, Network.CacheType.noCache, null);
    }

    public HttpObserverRaw(Context mActivity, CommonListView listView) {
        this(mActivity, Network.CacheType.noCache, listView);
    }

    public HttpObserverRaw(Context context, Network.CacheType cacheType, CommonListView listView) {
        this.mActivity = context;

        if (cacheType == Network.CacheType.onlyCache) {
            showErrorTip = false;
        }

        this.listView = listView;
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
        try {
            String errorMsg = NetworkImpl.ERROR_MSG_CONNECT_FAIL;
            if (e != null) {
                StackTraceElement[] stackTrace = e.getStackTrace();
                StringBuilder sb = new StringBuilder();
                sb.append(e.getMessage());
                sb.append("\n");
                for (StackTraceElement item : stackTrace) {
                    sb.append(item.toString());
                    sb.append("\n");
                }
                Logger.e("", sb.toString());

                String error = e.getMessage();
                if (error != null && !error.isEmpty()) {
                    errorMsg = error;
                }
            }
            onFail(NetworkImpl.NETWORK_CONNECT_FAIL, errorMsg);
        } catch (Exception exception) {
            Global.errorLog(exception);
        }
    }

    @Override
    public void onNext(T1 t1HttpResult) {
        try {
            if (t1HttpResult == null) {
                onFail(NetworkImpl.NETWORK_ERROR_SERVICE, NetworkImpl.ERROR_MSG_SERVICE_ERROR);
                return;
            }

            if (t1HttpResult.code != 0) {
                String showErrorMessage = "未知错误";
                Map errorMessage = t1HttpResult.msg;
                if (errorMessage != null) {
                    showErrorMessage = (String) errorMessage.values().iterator().next();
                }
                onFail(t1HttpResult.code, showErrorMessage);
                if (listView != null) {
                    listView.update(mActivity, CommonListView.Style.fail);
                    listView.setRefreshing(false);
                    listView.getAdapter().notifyDataSetChanged();
                }
                return;
            }

            onSuccess(t1HttpResult);
            if (listView != null) {
                listView.update(mActivity, CommonListView.Style.success);
                listView.setRefreshing(false);
                listView.getAdapter().notifyDataSetChanged();
            }
        } catch (Exception exception) {
            Global.errorLog(exception);
        }
    }

    public void onSuccess(T1 data) {
    }

    public void onFail(int errorCode, @NonNull String error) {
        if (showErrorTip) {
            if (error.startsWith("Unable to resolve host")) {
                error = "网络链接不可用";
            }
            SingleToast.showErrorMsg(mActivity, error);
        }
    }
}
