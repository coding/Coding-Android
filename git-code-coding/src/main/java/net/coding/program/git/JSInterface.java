package net.coding.program.git;

import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Created by chenchao on 2018/3/26.
 */
public class JSInterface {

    private String codeContent = "";

    public void setCodeContent(String codeContent) {
        this.codeContent = codeContent;
    }

    @JavascriptInterface
    public String getSource() {
        Log.d("", "calllllllllllll000000000000");
        return codeContent;
    }


    @JavascriptInterface
    public void getSource1() {
        Log.d("", "calllllllllllll");
    }
}
