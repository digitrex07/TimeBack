package com.takeback.app

import android.view.accessibility.AccessibilityNodeInfo

/** Only strong viewer signals trigger a block; ordinary navigation labels are ignored. */
object ShortDetector {
    fun isShort(packageName: String, root: AccessibilityNodeInfo?): Boolean {
        if (root == null) return false
        val ids = ArrayList<String>()
        val labels = ArrayList<String>()
        val pending = ArrayDeque<AccessibilityNodeInfo>()
        pending.add(root)
        var visited = 0
        while (pending.isNotEmpty() && visited++ < 350) {
            val node = pending.removeFirst()
            node.viewIdResourceName?.let { ids.add(it.lowercase()) }
            node.contentDescription?.let { labels.add(it.toString().lowercase()) }
            node.text?.let { labels.add(it.toString().lowercase()) }
            for (i in 0 until node.childCount) node.getChild(i)?.let(pending::add)
        }
        return when (packageName) {
            "com.google.android.youtube" ->
                ids.any { it.contains("shorts_player") || it.contains("reel_player") || it.contains("shorts_video") } ||
                labels.any { it.startsWith("shorts video") || it.startsWith("short, ") }
            "com.instagram.android" ->
                ids.any { it.contains("clips_viewer") || it.contains("reels_viewer") || it.contains("reel_viewer") } ||
                labels.any { it.startsWith("reel by ") || it.startsWith("reel from ") }
            else -> false
        }
    }
}
