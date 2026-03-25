package com.stealthvault.app.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisguiseManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val CALCULATOR_ALIAS = "${context.packageName}.CalculatorAlias"
    private val NOTES_ALIAS = "${context.packageName}.NotesAlias"

    enum class DisguiseType { CALCULATOR, NOTES }

    fun setDisguise(type: DisguiseType) {
        val pm = context.packageManager
        
        when (type) {
            DisguiseType.CALCULATOR -> {
                setComponentEnabled(pm, CALCULATOR_ALIAS, true)
                setComponentEnabled(pm, NOTES_ALIAS, false)
            }
            DisguiseType.NOTES -> {
                setComponentEnabled(pm, CALCULATOR_ALIAS, false)
                setComponentEnabled(pm, NOTES_ALIAS, true)
            }
        }
    }

    private fun setComponentEnabled(pm: PackageManager, componentName: String, enabled: Boolean) {
        val state = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        
        pm.setComponentEnabledSetting(
            ComponentName(context.packageName, componentName),
            state,
            PackageManager.DONT_KILL_APP
        )
    }
}
