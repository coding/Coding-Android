package net.coding.program.maopao;

import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.PostRequest;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_inform_maopao)
public class InformMaopaoActivity extends BackActivity {
    private final String[] types = new String[]{
            "淫秽色情",
            "垃圾广告",
            "敏感信息",
            "抄袭内容",
            "侵犯版权",
            "骚扰我"
    };

    private static final String TAG_INFORM_MAOPAO = "TAG_INFORM_MAOPAO";

    @Extra
    int maopaoId = 1;

    @ViewById
    ListView listView;

    @AfterViews
    void initInformMaopaoActivity() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_list_item_text,
                R.id.text1, types);
        listView.setAdapter(adapter);
    }

    @ItemClick
    void listView(final String item) {
        showDialog("举报", item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String url = Global.HOST_API + "/inform/tweet";
                RequestParams params = new RequestParams();
                params.put("user", MyApp.sUserObject.global_key);
                params.put("content", maopaoId);
                params.put("reason", item);
                postNetwork(new PostRequest(url, params), TAG_INFORM_MAOPAO);
            }
        });
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_INFORM_MAOPAO)) {
            showProgressBar(false);
            showButtomToast("举报成功");
            finish();
        }
    }
}
