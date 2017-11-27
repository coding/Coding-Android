package net.coding.program.common.model.request;

import android.content.Context;

import com.loopj.android.http.ResponseHandlerInterface;

import net.coding.program.common.Global;
import net.coding.program.common.network.MyAsyncHttpClient;

/**
 * Created by chenchao on 16/7/26.
 */
public class Project {

    public static void topicWatchList(Context context, int projectId, int topicId, ResponseHandlerInterface response) {
        String url = String.format(Global.HOST_API + "/project/%s/topic/%s/watchers?pageSize=1000", projectId, topicId);
        MyAsyncHttpClient.get(context, url, response);
    }
}
