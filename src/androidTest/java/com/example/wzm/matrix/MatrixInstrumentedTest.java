package com.example.wzm.matrix;

import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MatrixInstrumentedTest {
    @Rule
    public ActivityTestRule<ControlPanelActivity> activityTestRule =
            new ActivityTestRule<>(ControlPanelActivity.class);


    @Test
    public void verifyMatrix() {
        onView(withText("Traffic Forum")).check(matches(isDisplayed()));
    }

    @Test
    public void verifyLogin() {
        onView(withText("Account")).perform(click());

        //Replace the username & password to your credentials
        onView(withId(R.id.editTextLogin)).perform(ViewActions.clearText()).
                perform(ViewActions.typeText("1111"));
        onView(withId(R.id.editTextPassword)).perform(ViewActions.clearText()).
                perform(ViewActions.typeText("test"));

        //Check login
        onView(withId(R.id.submit)).perform(click());

        try {
            Thread.sleep(3000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //Check logcat
        onView(withText("LOGOUT")).check(matches(isDisplayed()));
    }


}
