package com.takeback.app

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShortBlockService : AccessibilityService() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val handler = Handler(Looper.getMainLooper())
    private var enabled = Enabled(true, true)
    private var overlay: View? = null
    private var lastBlockAt = 0L
    private var latestPackage = ""
    private var blockCount = 0

    override fun onServiceConnected() {
        scope.launch { Settings(this@ShortBlockService).enabled.collectLatest { enabled = it } }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg != "com.instagram.android" && pkg != "com.google.android.youtube") return
        latestPackage = pkg
        if (overlay != null) return
        if (pkg == "com.instagram.android" && !enabled.instagram) return
        if (pkg == "com.google.android.youtube" && !enabled.youtube) return
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - lastBlockAt < 1200) return
        if (!ShortDetector.isShort(pkg, rootInActiveWindow)) return
        lastBlockAt = now
        blockCount++
        showOverlay()
        performGlobalAction(GLOBAL_ACTION_BACK)
        // A few viewers consume Back without leaving. Leave the app if that happens.
        handler.postDelayed({
            if (overlay != null && latestPackage == pkg && stillShowingShort(pkg)) {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }, 400)
    }

    private fun stillShowingShort(pkg: String): Boolean = windows.any { window ->
        val root = window.root
        root?.packageName?.toString() == pkg && ShortDetector.isShort(pkg, root)
    }

    private fun showOverlay() {
        val context = this
        val panel = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
            setPadding(48, 48, 48, 48)
            isClickable = true
        }
        val headline = TextView(context).apply {
            text = "Ten minutes a day becomes 61 hours a year."
            setTextColor(Color.WHITE)
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        val body = TextView(context).apply {
            text = "Is this clip worth those hours? You opened a short video $blockCount time${if (blockCount == 1) "" else "s"} since protection started. Take your next ten minutes back."
            setTextColor(Color.LTGRAY)
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 40)
        }
        val leave = Button(context).apply {
            text = "Take my time back"
            isEnabled = false
            setOnClickListener { hideOverlay() }
        }
        panel.addView(headline)
        panel.addView(body)
        panel.addView(leave)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.OPAQUE
        )
        try {
            getSystemService(WindowManager::class.java).addView(panel, params)
            overlay = panel
            handler.postDelayed({ leave.isEnabled = true }, 3500)
            handler.postDelayed({ hideOverlay() }, 12000)
        } catch (_: RuntimeException) {
            // The video was already exited; an overlay failure must not crash the service.
        }
    }

    private fun hideOverlay() {
        val view = overlay ?: return
        overlay = null
        try { getSystemService(WindowManager::class.java).removeView(view) } catch (_: RuntimeException) { }
    }

    override fun onInterrupt() { hideOverlay() }
    override fun onDestroy() {
        hideOverlay()
        handler.removeCallbacksAndMessages(null)
        scope.cancel()
        super.onDestroy()
    }
}
