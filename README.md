# Earnergy

Earnergy is a Jetpack Compose Android app that turns your focus time into potential earnings insights. The project follows a modular Clean Architecture with a Kotlin Multiplatform-friendly domain layer so the core logic can be reused when an iOS client is added later.

## Modules

- **app** – Android UI, navigation, and dependency injection with Hilt.
- **core-domain** – Pure Kotlin (KMP-ready) models and calculators such as `EarningCalculator`.
- **core-data** – Android-specific data sources: Room persistence, DataStore settings, Usage Stats integration, and repositories.
- **core-ui** – Shared Compose Material 3 theme and UI helpers.

## Key features

- Onboarding screen that guides users to grant Usage Access permissions.
- Dashboard showing today's productive vs passive time, plus potential earnings/loss using the configured hourly rate.
- Settings screen backed by DataStore to capture the hourly rate (default $25/hr).
- Usage Stats, Room, and DataStore wired through Hilt DI for testable repositories.

## Getting started

1. Ensure you have Android Studio Jellyfish (or newer) and a JDK 21+ installed (JDK 25 works as well). Point `JAVA_HOME` to that JDK so Gradle can compile using Java 21 compatibility.
2. Open the project in Android Studio and let Gradle sync complete.
3. Run the `app` configuration on a device/emulator with API level 26 or higher.
4. On first launch, grant Usage Access permission when prompted by the onboarding screen, then refresh the dashboard.

Granting Usage Access is required for the dashboard refresh to populate real usage data.
