package com.aditya.isiquest

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class LockService : AccessibilityService() {
    private var lastKick = 0L

    override fun onAccessibilityEvent(e: AccessibilityEvent?) {
        if (e == null || e.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = e.packageName?.toString() ?: return
        val p = getSharedPreferences("q", MODE_PRIVATE)
        val now = System.currentTimeMillis()
        if (p.getLong("end", 0) <= now) return          // no active study block
        val study = p.getString("app", "") ?: ""
        if (pkg == study || pkg in safe()) return
        if (now - lastKick < 1500) return                // avoid kick loops
        lastKick = now
        p.edit().putInt("leaks", p.getInt("leaks", 0) + 1).apply()
        performGlobalAction(GLOBAL_ACTION_HOME)          // reliable
        packageManager.getLaunchIntentForPackage(study)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try { startActivity(it) } catch (e: Exception) {}  // best effort
        }
    }

    private fun safe(): Set<String> {
        val s = mutableSetOf(
            packageName, "com.android.systemui",
            "com.google.android.documentsui", "com.android.documentsui",
            "com.google.android.permissioncontroller", "com.android.permissioncontroller"
        )
        fun res(i: Intent) = packageManager.resolveActivity(i, 0)?.activityInfo?.packageName
        res(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME))?.let { s += it }
        res(Intent(Intent.ACTION_DIAL))?.let { s += it }
        Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            ?.substringBefore("/")?.let { s += it }
        return s
    }

    override fun onInterrupt() {}
}
