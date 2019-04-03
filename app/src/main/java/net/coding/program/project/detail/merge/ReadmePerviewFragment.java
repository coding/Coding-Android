package net.coding.program.project.detail.merge;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.task.TaskDespPreviewFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

/**
 * Created by chenchao on 16/1/14.
 * 专门用来预览 markdown 文件
 */

@EFragment(R.layout.fragment_topic_preview)
public class ReadmePerviewFragment extends TaskDespPreviewFragment {

    @FragmentArg
    String url = "";

    // 重载此函数，修改预览方法
    protected void mdToHtml(String contentMd) {
        RequestParams params = new RequestParams();
        params.put("data", contentMd);
        MyAsyncHttpClient.post(getActivity(), url, params, myJsonResponse);
    }
}
