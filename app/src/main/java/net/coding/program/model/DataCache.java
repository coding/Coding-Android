package net.coding.program.model;

import android.content.Context;

import java.io.File;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by chaochen on 14-12-8.
 */
class DataCache<T> {

    private static final String MESSAGE_USERS = "MESSAGE_USERS";

    public static void saveMessageUsers(Context ctx, ArrayList<Message.MessageObject> data) {
        if (ctx == null) {
            return;
        }

        File file = new File(ctx.getFilesDir(), MESSAGE_USERS);
        if (file.exists()) {
            file.delete();
        }

        try {
            ObjectOutputStream oos = new ObjectOutputStream(ctx.openFileOutput(MESSAGE_USERS, Context.MODE_PRIVATE));
            oos.writeObject(data);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sav(Context ctx, ArrayList<T> data) {

    }


}
