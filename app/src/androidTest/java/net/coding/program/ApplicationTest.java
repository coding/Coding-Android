package net.coding.program;

import android.app.Application;
import android.test.ApplicationTestCase;

import net.coding.program.third.EmojiFilter;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testEmojiFilter() {
        String s[] = new String[] {"°" ,
                "(" ,
                "・" ,
                "∀" ,
                "・" ,
                "≡" ,
                "＝" ,
                "－" ,
                "▪",
                "▪",
                "(・∀・ ≡＝－(・∀・ ≡＝－"
        };

        for (String item : s) {
            assertFalse(EmojiFilter.containsEmoji(item));
        }
    }
}