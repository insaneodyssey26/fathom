package com.masum.fathom.core.sanctuary

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

class AppIconManager(private val context: Context) {

    fun setAppIcon(targetIcon: AppIcon): Result<Unit> {
        return try {
            val packageManager = context.packageManager
            val packageName = context.packageName

            AppIcon.entries.forEach { icon ->
                val component = ComponentName(packageName, icon.activityAliasName)
                val newState = if (icon == targetIcon) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                packageManager.setComponentEnabledSetting(
                    component,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getActiveAppIcon(): AppIcon {
        return try {
            val packageManager = context.packageManager
            val packageName = context.packageName

            val active = AppIcon.entries.find { icon ->
                val component = ComponentName(packageName, icon.activityAliasName)
                val state = packageManager.getComponentEnabledSetting(component)
                state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }
            active ?: AppIcon.DEFAULT
        } catch (_: Exception) {
            AppIcon.DEFAULT
        }
    }
}
