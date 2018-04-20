package net.coding.program;

import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginTest {

    public static final String ACCOUNT_NAME = "1984";
    public static final String ACCOUNT_PASSWORD = "222222";
    public static final String ACCOUNT_PASSWORD_ERROR = "111111";

    @Rule
    public ActivityTestRule<LoginActivity_> activityRule = new ActivityTestRule<>(LoginActivity_.class);

    @Test
    public void loginFail() {
        onView(withId(R.id.editName)).perform(replaceText(ACCOUNT_NAME), closeSoftKeyboard());
        onView(withId(R.id.editPassword)).perform(typeText(ACCOUNT_PASSWORD_ERROR), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        onView(withId(R.id.bottomBar)).check(ViewAssertions.doesNotExist());
    }

    @Test
    public void loginSuccess() {
//        onView(withId(R.id.editName)).perform(replaceText(ACCOUNT_NAME), closeSoftKeyboard());
//        onView(withId(R.id.editPassword)).perform(typeText(ACCOUNT_PASSWORD), closeSoftKeyboard());
//        onView(withId(R.id.loginButton)).perform(click());
//
//        onView(withId(R.id.bottomBar)).check(Help.doesExist());
    }
}
