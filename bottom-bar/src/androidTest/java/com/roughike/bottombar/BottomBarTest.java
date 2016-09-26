package com.roughike.bottombar;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Created by iiro on 13.8.2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BottomBarTest {
    private OnTabSelectListener selectListener;
    private OnTabReselectListener reselectListener;

    private BottomBar bottomBar;

    @Before
    public void setUp() {
        selectListener = Mockito.mock(OnTabSelectListener.class);
        reselectListener = Mockito.mock(OnTabReselectListener.class);

        bottomBar = new BottomBar(InstrumentationRegistry.getContext());
        bottomBar.setItems(com.roughike.bottombar.test.R.xml.dummy_tabs_three);
        bottomBar.setOnTabSelectListener(selectListener);
        bottomBar.setOnTabReselectListener(reselectListener);
    }

    @Test(expected = RuntimeException.class)
    public void setItems_ThrowsExceptionWithNoResource() {
        BottomBar secondBar = new BottomBar(InstrumentationRegistry.getContext());
        secondBar.setItems(0);
    }

    @Test
    public void setItemsWithCustomConfig_OverridesPreviousValues() {
        float inActiveTabAlpha = 0.69f;
        float activeTabAlpha = 0.96f;
        int inActiveTabColor = Color.BLUE;
        int activeTabColor = Color.GREEN;
        int defaultBackgroundColor = Color.CYAN;
        int defaultBadgeBackgroundColor = Color.MAGENTA;
        int titleTextAppearance = com.roughike.bottombar.test.R.style.dummy_text_appearance;

        BottomBarTab.Config config = new BottomBarTab.Config.Builder()
                .inActiveTabAlpha(inActiveTabAlpha)
                .activeTabAlpha(activeTabAlpha)
                .inActiveTabColor(inActiveTabColor)
                .activeTabColor(activeTabColor)
                .barColorWhenSelected(defaultBackgroundColor)
                .badgeBackgroundColor(defaultBadgeBackgroundColor)
                .titleTextAppearance(titleTextAppearance)
                .build();

        BottomBar newBar = new BottomBar(InstrumentationRegistry.getContext());
        newBar.setItems(com.roughike.bottombar.test.R.xml.dummy_tabs_three, config);

        BottomBarTab first = newBar.getTabAtPosition(0);

        assertEquals(inActiveTabAlpha, first.getInActiveAlpha(), 0);
        assertEquals(activeTabAlpha, first.getActiveAlpha(), 0);
        assertEquals(inActiveTabColor, first.getInActiveColor());
        assertEquals(activeTabColor, first.getActiveColor());
        assertEquals(defaultBackgroundColor, first.getBarColorWhenSelected());
        assertEquals(defaultBadgeBackgroundColor, first.getBadgeBackgroundColor());
        assertEquals(titleTextAppearance, first.getTitleTextAppearance());
    }

    @Test
    @UiThreadTest
    public void tabCount_IsCorrect() {
        assertEquals(3, bottomBar.getTabCount());
    }

    @Test
    @UiThreadTest
    public void findingPositionForTabs_ReturnsCorrectPositions() {
        assertEquals(0, bottomBar.findPositionForTabWithId(com.roughike.bottombar.test.R.id.tab_favorites));
        assertEquals(1, bottomBar.findPositionForTabWithId(com.roughike.bottombar.test.R.id.tab_nearby));
        assertEquals(2, bottomBar.findPositionForTabWithId(com.roughike.bottombar.test.R.id.tab_friends));
    }

    @Test
    @UiThreadTest
    public void whenTabIsSelected_SelectionListenerIsFired() {
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_friends);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_favorites);

        InOrder inOrder = inOrder(selectListener);
        inOrder.verify(selectListener, times(1)).onTabSelected(com.roughike.bottombar.test.R.id.tab_friends);
        inOrder.verify(selectListener, times(1)).onTabSelected(com.roughike.bottombar.test.R.id.tab_nearby);
        inOrder.verify(selectListener, times(1)).onTabSelected(com.roughike.bottombar.test.R.id.tab_favorites);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @UiThreadTest
    public void afterConfigurationChanged_SavedStateRestored_AndSelectedTabPersists() {
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_favorites);

        Bundle savedState = bottomBar.saveState();
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);
        bottomBar.restoreState(savedState);

        assertEquals(com.roughike.bottombar.test.R.id.tab_favorites, bottomBar.getCurrentTabId());
    }

    @Test
    @UiThreadTest
    public void whenTabIsReselected_ReselectionListenerIsFired() {
        int firstTabId = com.roughike.bottombar.test.R.id.tab_favorites;
        bottomBar.selectTabWithId(firstTabId);
        verify(reselectListener, times(1)).onTabReSelected(firstTabId);

        int secondTabId = com.roughike.bottombar.test.R.id.tab_nearby;
        bottomBar.selectTabWithId(secondTabId);
        bottomBar.selectTabWithId(secondTabId);
        verify(reselectListener, times(1)).onTabReSelected(secondTabId);

        int thirdTabId = com.roughike.bottombar.test.R.id.tab_friends;
        bottomBar.selectTabWithId(thirdTabId);
        bottomBar.selectTabWithId(thirdTabId);
        verify(reselectListener, times(1)).onTabReSelected(thirdTabId);
    }

    @Test
    @UiThreadTest
    public void whenDefaultTabIsSet_ItsSelectedAtFirst() {
        int defaultTabId = com.roughike.bottombar.test.R.id.tab_friends;

        bottomBar.setDefaultTab(defaultTabId);
        verify(selectListener).onTabSelected(defaultTabId);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void settingTooLowDefaultPosition_Throws() {
        bottomBar.setDefaultTabPosition(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void settingTooHighDefaultPosition_Throws() {
        bottomBar.setDefaultTabPosition(bottomBar.getTabCount());
    }

    @Test
    @UiThreadTest
    public void afterConfigurationChanged_UserSelectedTabPersistsWhenResettingDefaultTab() {
        int defaultTabId = com.roughike.bottombar.test.R.id.tab_friends;

        bottomBar.setDefaultTab(defaultTabId);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);

        Bundle savedState = bottomBar.saveState();
        bottomBar.restoreState(savedState);
        bottomBar.setDefaultTab(defaultTabId);

        assertNotSame(defaultTabId, bottomBar.getCurrentTabId());
        assertEquals(com.roughike.bottombar.test.R.id.tab_nearby, bottomBar.getCurrentTabId());
    }

    @Test
    @UiThreadTest
    public void whenGettingCurrentTab_ReturnsCorrectOne() {
        int firstTabId = com.roughike.bottombar.test.R.id.tab_favorites;
        bottomBar.selectTabWithId(firstTabId);

        assertEquals(firstTabId, bottomBar.getCurrentTabId());
        assertEquals(bottomBar.findPositionForTabWithId(firstTabId), bottomBar.getCurrentTabPosition());
        assertEquals(bottomBar.getTabWithId(firstTabId), bottomBar.getCurrentTab());

        int secondTabId = com.roughike.bottombar.test.R.id.tab_nearby;
        bottomBar.selectTabWithId(secondTabId);

        assertEquals(secondTabId, bottomBar.getCurrentTabId());
        assertEquals(bottomBar.findPositionForTabWithId(secondTabId), bottomBar.getCurrentTabPosition());
        assertEquals(bottomBar.getTabWithId(secondTabId), bottomBar.getCurrentTab());

        int thirdTabId = com.roughike.bottombar.test.R.id.tab_friends;
        bottomBar.selectTabWithId(thirdTabId);

        assertEquals(thirdTabId, bottomBar.getCurrentTabId());
        assertEquals(bottomBar.findPositionForTabWithId(thirdTabId), bottomBar.getCurrentTabPosition());
        assertEquals(bottomBar.getTabWithId(thirdTabId), bottomBar.getCurrentTab());
    }

    @Test
    @UiThreadTest
    public void whenSelectionChanges_AndHasNoListeners_onlyOneTabIsSelectedAtATime() {
        bottomBar.setOnTabSelectListener(null);
        bottomBar.setOnTabReselectListener(null);

        int firstTabId = com.roughike.bottombar.test.R.id.tab_favorites;
        int secondTabId = com.roughike.bottombar.test.R.id.tab_nearby;
        int thirdTabId = com.roughike.bottombar.test.R.id.tab_friends;

        bottomBar.selectTabWithId(secondTabId);
        assertOnlyHasOnlyOneSelectedTabWithId(secondTabId);

        bottomBar.selectTabWithId(thirdTabId);
        assertOnlyHasOnlyOneSelectedTabWithId(thirdTabId);

        bottomBar.selectTabWithId(firstTabId);
        assertOnlyHasOnlyOneSelectedTabWithId(firstTabId);
    }

    private void assertOnlyHasOnlyOneSelectedTabWithId(int tabId) {
        for (int i = 0; i < bottomBar.getTabCount(); i++) {
            BottomBarTab tab = bottomBar.getTabAtPosition(i);

            if (tab.getId() == tabId) {
                assertTrue(tab.isActive());
            } else {
                assertFalse(tab.isActive());
            }
        }
    }

    @Test
    @UiThreadTest
    public void whenTabIsSelectedOnce_AndNoSelectionListenerSet_ReselectionListenerIsNotFired() {
        bottomBar.setOnTabSelectListener(null);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_friends);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_favorites);

        verifyZeroInteractions(reselectListener);
    }

    @Test
    @UiThreadTest
    public void whenInActiveAlphaSetProgrammatically_AlphaIsUpdated() {
        BottomBarTab inActiveTab = bottomBar.getTabAtPosition(1);

        assertNotEquals(bottomBar.getCurrentTab(), inActiveTab);

        float previousAlpha = inActiveTab.getInActiveAlpha();
        float testAlpha = 0.69f;

        assertNotEquals(testAlpha, previousAlpha);
        assertNotEquals(testAlpha, inActiveTab.getIconView().getAlpha());
        assertNotEquals(testAlpha, inActiveTab.getTitleView().getAlpha());

        bottomBar.setInActiveTabAlpha(testAlpha);

        assertEquals(testAlpha, inActiveTab.getInActiveAlpha(), 0);
        assertEquals(testAlpha, inActiveTab.getIconView().getAlpha(), 0);
        assertEquals(testAlpha, inActiveTab.getTitleView().getAlpha(), 0);
    }

    @Test
    @UiThreadTest
    public void whenActiveAlphaSetProgrammatically_AlphaIsUpdated() {
        BottomBarTab activeTab = bottomBar.getCurrentTab();

        float previousAlpha = activeTab.getActiveAlpha();
        float testAlpha = 0.69f;

        assertNotEquals(testAlpha, previousAlpha);
        assertNotEquals(testAlpha, activeTab.getIconView().getAlpha());
        assertNotEquals(testAlpha, activeTab.getTitleView().getAlpha());

        bottomBar.setActiveTabAlpha(testAlpha);

        assertEquals(testAlpha, activeTab.getActiveAlpha(), 0);
        assertEquals(testAlpha, activeTab.getIconView().getAlpha(), 0);
        assertEquals(testAlpha, activeTab.getTitleView().getAlpha(), 0);
    }

    @Test
    @UiThreadTest
    public void whenInActiveColorSetProgrammatically_ColorIsUpdated() {
        BottomBarTab inActiveTab = bottomBar.getTabAtPosition(1);

        assertNotEquals(bottomBar.getCurrentTab(), inActiveTab);

        int previousInActiveColor = inActiveTab.getInActiveColor();
        int previousIconColor = inActiveTab.getCurrentDisplayedIconColor();
        int previousTitleColor = inActiveTab.getCurrentDisplayedTitleColor();

        int testColor = Color.GREEN;

        assertNotEquals(testColor, previousInActiveColor);
        assertNotEquals(testColor, previousIconColor);
        assertNotEquals(testColor, previousTitleColor);

        bottomBar.setInActiveTabColor(testColor);

        assertEquals(testColor, inActiveTab.getInActiveColor());
        assertEquals(testColor, inActiveTab.getCurrentDisplayedIconColor());
        assertEquals(testColor, inActiveTab.getCurrentDisplayedTitleColor());
    }

    @Test
    @UiThreadTest
    public void whenActiveColorSetProgrammatically_ColorIsUpdated() {
        BottomBarTab activeTab = bottomBar.getCurrentTab();

        int previousActiveColor = activeTab.getActiveColor();
        int previousIconColor = activeTab.getCurrentDisplayedIconColor();
        int previousTitleColor = activeTab.getCurrentDisplayedTitleColor();

        int testColor = Color.GREEN;

        assertNotEquals(testColor, previousActiveColor);
        assertNotEquals(testColor, previousIconColor);
        assertNotEquals(testColor, previousTitleColor);

        bottomBar.setActiveTabColor(testColor);

        assertEquals(testColor, activeTab.getActiveColor());
        assertEquals(testColor, activeTab.getCurrentDisplayedIconColor());
        assertEquals(testColor, activeTab.getCurrentDisplayedTitleColor());
    }

    @Test
    @UiThreadTest
    public void whenBadgeBackgroundColorSetProgrammatically_ColorIsUpdated() {
        BottomBarTab inActiveTab = bottomBar.getTabAtPosition(1);
        inActiveTab.setBadgeCount(3);

        int previousBadgeColor = inActiveTab.getBadgeBackgroundColor();
        int testColor = Color.GREEN;

        assertNotEquals(testColor, previousBadgeColor);

        bottomBar.setBadgeBackgroundColor(testColor);

        assertEquals(testColor, inActiveTab.getBadgeBackgroundColor());
    }

    @Test
    @UiThreadTest
    public void whenTitleTextAppearanceSetProgrammatically_AppearanceUpdated() {
        BottomBarTab tab = bottomBar.getCurrentTab();

        int testTextApperance = -666;

        assertNotEquals(testTextApperance, tab.getTitleTextAppearance());
        assertNotEquals(testTextApperance, tab.getCurrentDisplayedTextAppearance());

        bottomBar.setTabTitleTextAppearance(testTextApperance);

        assertEquals(testTextApperance, tab.getTitleTextAppearance());
        assertEquals(testTextApperance, tab.getCurrentDisplayedTextAppearance());
    }

    @Test
    @UiThreadTest
    public void whenTitleTypeFaceSetProgrammatically_TypefaceUpdated() {
        BottomBarTab tab = bottomBar.getCurrentTab();

        Typeface testTypeFace = Typeface.createFromAsset(
                bottomBar.getContext().getAssets(), "fonts/GreatVibes-Regular.otf");

        assertNotEquals(testTypeFace, tab.getTitleTypeFace());
        assertNotEquals(testTypeFace, tab.getTitleView().getTypeface());

        bottomBar.setTabTitleTypeface(testTypeFace);

        assertEquals(testTypeFace, tab.getTitleTypeFace());
        assertEquals(testTypeFace, tab.getTitleView().getTypeface());
    }
}
