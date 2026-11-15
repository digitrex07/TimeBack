# Take Back

Personal Android app for blocking Instagram Reels and YouTube Shorts, including clips opened from messages and links. It uses an Accessibility Service to detect a short-video viewer, places a black interruption over it, exits the viewer, and shows a brief reminder about reclaiming time. Browser content is outside scope.

## Build and install

No Android Studio is needed if you use the included GitHub Actions workflow. Put this project in a **private GitHub repository**, open the repository's **Actions** tab, select **Build Android APK**, and choose **Run workflow**. Once it finishes, download the `take-back-debug-apk` artifact, unzip it, and transfer `app-debug.apk` to the Galaxy S24. Open the APK on the phone, allow installation from that source when Android asks, and enable **Take Back short video blocker** in Android accessibility settings. The app uses no account or internet permission.

This workspace has no Android SDK, Java runtime, or Gradle installation, so compilation and device validation have not been run here. The online workflow installs the toolchain and Gradle 8.13 for its build.

## Device validation

1. Open Instagram Reels from its tab, feed, search, a DM, a notification, and an external link. Each should show the black screen and exit the clip. Confirm DMs, posts, and ordinary videos work.
2. Repeat for YouTube Shorts from its tab, recommendations, search, notifications, and links. Confirm long videos still work.
3. Check that video audio stops, the interruption can be dismissed after 3.5 seconds, and it disappears automatically after 12 seconds.
4. Turn either switch off and verify only that service stops blocking. Re-enable it afterward.

The detector intentionally uses strong viewer identifiers or accessibility descriptions. Instagram and YouTube can change those identifiers, so these rules require validation on the user's installed versions and may need adjustment. The service cannot remove the feed or prevent the phone owner from disabling it.
