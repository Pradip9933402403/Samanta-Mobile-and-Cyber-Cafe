# Samanta Repair Stock

A small Android app starter for maintaining a mobile-phone repair shop's parts stock. It supports adding, editing, searching, and deleting parts, recording purchase/selling prices, and adding or removing quantities. Data is saved locally on the device using app-private storage; no account or internet connection is required.

## Build the APK

1. Open this repository in Android Studio (Giraffe or newer) and allow Gradle sync to finish. Use JDK 17 for Android Gradle Plugin 8.7.
2. Select **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
3. Android Studio writes the debug APK under `app/build/outputs/apk/debug/`.

The project targets Android 15 (API 35) and supports Android 6.0 (API 23) and later. This repository contains the app source and build configuration; it does not include a prebuilt APK.
