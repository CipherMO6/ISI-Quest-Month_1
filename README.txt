ISI Quest (Android) - build with GitHub, no Android Studio

1. Unzip. Make a new GitHub repo. Upload everything in this folder, keeping the folder structure (app/, .github/, settings.gradle.kts, build.gradle.kts, gradle.properties).
   If .github didn't upload (hidden folder), open the repo's Actions tab > "set up a workflow yourself", name it build.yml, paste the contents of .github/workflows/build.yml, commit.
2. Every push builds automatically. Actions tab > latest run > Artifacts > isi-quest-apk (a zip containing app-debug.apk).
3. Unzip, install the APK on the phone (allow "install unknown apps" for your browser/files app).
4. Settings > Accessibility > ISI Quest Lock > On. If greyed out: Settings > Apps > ISI Quest > three-dot menu > Allow restricted settings.
5. Settings > Battery > ISI Quest > Unrestricted.
If the build fails, open the failed run, copy the red error lines, and send them to Claude.
