package net.coding.program.common.model;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.GlobalData;

import java.io.File;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by chenchao on 2017/11/24.
 */
public class MyMessage extends Message.MessageObject implements Serializable {

    public static final int STYLE_SENDING = 0;
    public static final int STYLE_RESEND = 1;

    public static final int REQUEST_TEXT = 0;
    public static final int REQUEST_IMAGE = 1;
    public static final int REQUEST_VOICE = 2;

    public RequestParams requestParams;
    public int myStyle = 0;
    public int myRequestType = 0;

    private File file;


    public MyMessage(int requestType, RequestParams params, UserObject friendUser) {
        myStyle = STYLE_SENDING;

        myRequestType = requestType;
        requestParams = params;

        friend = friendUser;
        sender = GlobalData.sUserObject;

        created_at = Calendar.getInstance().getTimeInMillis();
    }

    public long getCreateTime() {
        return created_at;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }
}
