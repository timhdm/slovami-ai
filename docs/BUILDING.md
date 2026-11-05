# Building Slovami AI

Use JDK 17 and Android SDK platform 34. Set `ANDROID_HOME` to the SDK folder
or create an untracked `local.properties` with `sdk.dir`.

```sh
./gradlew assembleDebug testDebugUnitTest
```

On Windows use Git Bash for the wrapper. The debug APK is generated in
`app/build/outputs/apk/debug/`. Model fixtures are included in the repository.
The external `app/libs/sherpa-onnx.aar` is ignored by Git and must be provisioned
before building. Use version 1.13.1 and verify the SHA-256 specified in
[`scripts/install-latest-debug.ps1`](../scripts/install-latest-debug.ps1) and
[CI](../.github/workflows/android.yml). Large speech models download on first launch.

For the local Windows build/install script, its clean-tree requirement and the
persistent debug signing key setup, see the
[local workflow](../slovami-ai-build-and-install-instructions.md). Do not commit
or print signing credentials. The wrapper itself does not download the AAR.

The source version is defined in `app/build.gradle.kts`. Historical v1.0.0
release notes are in `RELEASE_NOTES.md`; they do not describe all current features.
Release signing and publishing are separate manual steps.
Do not distribute an unsigned release or change the app signing key.

Before publishing, test recording, import, transcription and DOCX export on
a physical phone, including a small viewport and enlarged system fonts.
