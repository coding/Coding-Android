package net.coding.program;

import android.test.AndroidTestCase;

import net.coding.program.common.CommentBackup;

/**
 * Created by chaochen on 15/1/27.
 */
public class CommonTest extends AndroidTestCase {

    public void testSave() {
        CommentBackup commentBackup = CommentBackup.getInstance();

        String id = "22";
        String global = "g1";
        String comment = "very good";

        CommentBackup.BackupParam param = new CommentBackup.BackupParam(CommentBackup.Type.Maopao,
                id, global);

        commentBackup.save(param, comment);
        assertEquals(commentBackup.load(param), comment);

        commentBackup.delete(param);
        assertEquals(commentBackup.load(param), "");


        commentBackup.save(param, comment);

        String comment2 = "早上好";
        CommentBackup.BackupParam param2 = new CommentBackup.BackupParam(CommentBackup.Type.Maopao,
                id, global);
        commentBackup.save(param2, comment2);
        assertEquals(commentBackup.load(param), comment2);
        assertEquals(commentBackup.load(param2), comment2);

        commentBackup.delete(param2);
        assertEquals(commentBackup.load(param), "");
    }
}
