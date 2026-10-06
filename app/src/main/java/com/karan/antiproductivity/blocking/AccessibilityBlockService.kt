package com.karan.antiproductivity.blocking

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.karan.antiproductivity.challenge.ChallengeActivity
import com.karan.antiproductivity.data.datastore.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AccessibilityBlockService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastBlockedPackage: String? = null
    private var lastBlockTime: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        BlockManager.init(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Ignore ourselves and system ui if needed
            if (packageName == packageName || packageName == "com.android.systemui") {
                // wait, check against our own package name
            }
            if (packageName == this.packageName) return

            if (BlockManager.isBlockedAndLocked(packageName)) {
                // Prevent spamming challenge activity for the exact same package within 2 seconds
                val now = System.currentTimeMillis()
                if (lastBlockedPackage == packageName && (now - lastBlockTime) < 2000) {
                    return
                }
                lastBlockedPackage = packageName
                lastBlockTime = now

                // Record blocked attempt statistic
                serviceScope.launch {
                    val dataStore = SettingsDataStore(applicationContext)
                    dataStore.incrementBlockedAttempt()
                }

                // Launch ChallengeActivity
                val intent = Intent(this, ChallengeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("target_package", packageName)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        // Required override
    }
}
