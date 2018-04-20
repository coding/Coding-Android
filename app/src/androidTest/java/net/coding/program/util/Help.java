package net.coding.program.util;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.util.HumanReadables;
import android.view.View;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.is;

public class Help {

    public static ViewAssertion doesExist() {
        return new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                if (view == null) {
                    assertThat(
                            "View is not exist in the hierarchy: " + HumanReadables.describe(view), true, is(false));
                }
            }
        };
    }
}
