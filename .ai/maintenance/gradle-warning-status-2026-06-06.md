# Gradle Warning Status

Date: 2026-06-06
Branch: `codex/multiloader-spike`

## Summary

Project-owned Gradle 10 deprecation warnings were cleared during the multiloader cleanup pass.

The only remaining warning observed under Java 25 is emitted before project configuration by Gradle's native-platform dependency:

```text
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by net.rubygrapefruit.platform.internal.NativeLibraryLoader
```

This is a JDK/native access warning from the Gradle runtime stack rather than this repository's build scripts.

## Commands Checked

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home ./gradlew --no-daemon --no-configuration-cache --max-workers=1 -Dorg.gradle.jvmargs=-Xmx4g --warning-mode all :common:test
```

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home ./gradlew --no-daemon --no-configuration-cache --max-workers=1 -Dorg.gradle.jvmargs=-Xmx4g -PneoForge.neoFormRuntime.launcherManifestUrl=file:///Users/river_quinn/.gradle/caches/neoformruntime/artifacts/minecraft_launcher_manifest.json --warning-mode all :neoforge:build :fabric_1_21_1:build :fabric_26_1_2:build
```

Both commands completed successfully without Gradle project-script deprecation warnings.

## Fixed

- Replaced deprecated Groovy space-assignment publishing repository syntax in `build.gradle`.
- Replaced the same syntax in `neoforge/build.gradle`.
