package com.masum.fathom.core.sanctuary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppIconTest {

    @Test
    fun testAppIconDefaultsAndIds() {
        assertEquals(AppIcon.DEFAULT, AppIcon.fromId("default"))
        assertEquals(AppIcon.OBSIDIAN, AppIcon.fromId("obsidian"))
        assertEquals(AppIcon.ABYSS, AppIcon.fromId("abyss"))
        assertEquals(AppIcon.SAHARA, AppIcon.fromId("sahara"))
        assertEquals(AppIcon.FOREST, AppIcon.fromId("forest"))
        assertEquals(AppIcon.CINNABAR, AppIcon.fromId("cinnabar"))
        assertEquals(AppIcon.DEFAULT, AppIcon.fromId("unknown_id"))
        assertEquals(AppIcon.DEFAULT, AppIcon.fromId(""))
    }

    @Test
    fun testAppIconPatronRestrictions() {
        assertFalse(AppIcon.DEFAULT.isPatronOnly)
        assertTrue(AppIcon.OBSIDIAN.isPatronOnly)
        assertTrue(AppIcon.ABYSS.isPatronOnly)
        assertTrue(AppIcon.SAHARA.isPatronOnly)
        assertTrue(AppIcon.FOREST.isPatronOnly)
        assertTrue(AppIcon.CINNABAR.isPatronOnly)
    }

    @Test
    fun testAppIconActivityAliasNames() {
        assertEquals("com.masum.fathom.MainActivityDefault", AppIcon.DEFAULT.activityAliasName)
        assertEquals("com.masum.fathom.MainActivityObsidian", AppIcon.OBSIDIAN.activityAliasName)
        assertEquals("com.masum.fathom.MainActivityAbyss", AppIcon.ABYSS.activityAliasName)
        assertEquals("com.masum.fathom.MainActivitySahara", AppIcon.SAHARA.activityAliasName)
        assertEquals("com.masum.fathom.MainActivityForest", AppIcon.FOREST.activityAliasName)
        assertEquals("com.masum.fathom.MainActivityCinnabar", AppIcon.CINNABAR.activityAliasName)
    }
}
