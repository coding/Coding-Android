package net.coding.program.common.param;

import java.util.ArrayList;

/**
 * Created by chenchao on 2017/4/18.
 * 私信
 */
public class MessageParse {
    public String text = "";
    public ArrayList<String> uris = new ArrayList<>();
    public boolean isVoice;
    public String voiceUrl;
    public int voiceDuration;
    public int played;
    public int id;

    public String toString() {
        String s = "text " + text + "\n";
        for (int i = 0; i < uris.size(); ++i) {
            s += uris.get(i) + "\n";
        }
        return s;
    }
}
