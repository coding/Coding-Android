package net.coding.program;

import android.app.Application;
import android.test.ApplicationTestCase;

import net.coding.program.common.LoginBackground;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;
import net.coding.program.third.EmojiFilter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    CountDownLatch signal = new CountDownLatch(1);

    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testEmojiFilter() {
        String s[] = new String[]{"°",
                "(",
                "・",
                "∀",
                "・",
                "≡",
                "＝",
                "－",
                "▪",
                "▪",
                "(・∀・ ≡＝－(・∀・ ≡＝－"
        };

        for (String item : s) {
            assertFalse(EmojiFilter.containsEmoji(item));
        }
    }

    public void testSaveGlobal() {
        String a1 = "chen@qq";
        String a2 = "1234";

        String b1 = "chao@zzz";

        String c2 = "785";

        AccountInfo.saveReloginInfo(mContext, a1, a2);

        assertEquals(AccountInfo.loadRelogininfo(mContext, a1), a2);
        assertEquals(AccountInfo.loadRelogininfo(mContext, a2), a2);
        assertTrue(AccountInfo.loadRelogininfo(mContext, b1).isEmpty());
        assertTrue(AccountInfo.loadRelogininfo(mContext, c2).isEmpty());
    }

    public void testLoginBackground() {
        LoginBackground loginBackground = new LoginBackground(mContext);
        loginBackground.update();
        try {
            signal.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {

        }
        assertEquals(loginBackground.getPhotoCount(), 5);
    }

    public void testPinYing() {
        assertEquals(UserObject.getFirstLetters("陈超").toUpperCase(), "CC");
        assertEquals(UserObject.getFirstLetters("chenchao").toUpperCase(), "CHENCHAO");
    }
}