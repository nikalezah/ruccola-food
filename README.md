This is a Kotlin Multiplatform project targeting Android, iOS, Web, Desktop, and Server with separate Admin and Customer
apps.

* `/app/admin/androidApp` and `/app/customer/androidApp` are the Android application entry points (AGP 9).
* `/app/admin/iosApp` and `/app/customer/iosApp` are the iOS application entry points (Xcode).
* `/app/admin/desktopApp/src` and `/app/customer/desktopApp/src` are the Desktop application entry points.
* `/app/admin/shared/src` is the Admin KMP library used by Android, iOS, Web, and Desktop.
* `/app/admin/webApp/src` is the Admin Web application entry point.
* `/app/customer/shared/src` is the Customer KMP library used by Android, iOS, Web, and Desktop.
* `/app/customer/webApp/src` is the Customer Web application entry point.
* `/app/common/src` is for Compose/UI code shared by the Admin and Customer apps.
* `/server/src/main/kotlin` is for the Ktor server application.
* `/core/src` is for API contracts, models, and platform-neutral code shared between apps and server. The most
  important subfolder is `/core/src/commonMain/kotlin`. You can add platform-specific code under the corresponding
  source sets.

### Build and Run Android Applications

To build and run the development version of the Android apps, use the run configuration from the run widget
in your IDE's toolbar or build them directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :app:admin:androidApp:assembleDebug
  ./gradlew :app:customer:androidApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :app:admin:androidApp:assembleDebug
  .\gradlew.bat :app:customer:androidApp:assembleDebug
  ```

### Build and Run Server

To build and run the development version of the server, use the run configuration from the run widget
in your IDE's toolbar or run it directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :server:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :server:run
  ```

### Build and Run Web Applications

To build and run the development version of the web apps, use the run configuration from the run widget
in your IDE's toolbar or run them directly from the terminal:

- for the Wasm target (faster, modern browsers):
    - on macOS/Linux
      ```shell
      ./gradlew :app:admin:webApp:wasmJsBrowserDevelopmentRun
      ./gradlew :app:customer:webApp:wasmJsBrowserDevelopmentRun
      ```
    - on Windows
      ```shell
      .\gradlew.bat :app:admin:webApp:wasmJsBrowserDevelopmentRun
      .\gradlew.bat :app:customer:webApp:wasmJsBrowserDevelopmentRun
      ```
- for the JS target (slower, supports older browsers):
    - on macOS/Linux
      ```shell
      ./gradlew :app:admin:webApp:jsBrowserDevelopmentRun
      ./gradlew :app:customer:webApp:jsBrowserDevelopmentRun
      ```
    - on Windows
      ```shell
      .\gradlew.bat :app:admin:webApp:jsBrowserDevelopmentRun
      .\gradlew.bat :app:customer:webApp:jsBrowserDevelopmentRun
      ```

### Build and Run Desktop Applications

To build and run the development version of the desktop apps, use the run configuration from the run widget
in your IDE's toolbar or run them directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :app:admin:desktopApp:run
  ./gradlew :app:customer:desktopApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :app:admin:desktopApp:run
  .\gradlew.bat :app:customer:desktopApp:run
  ```

### Build and Run iOS Applications

iOS apps require macOS with Xcode installed. Open the Xcode project, select a simulator or device, and run.

- Admin: open `app/admin/iosApp/iosApp.xcodeproj` in Xcode
- Customer: open `app/customer/iosApp/iosApp.xcodeproj` in Xcode

Before the first run, set your development team in `Configuration/Config.xcconfig` if needed.

The Xcode build invokes Gradle to compile the shared Kotlin framework (`AdminShared` or `CustomerShared`).
`BASE_URL` is `http://localhost:8080` for the iOS Simulator; on a physical device, use your machine's LAN IP instead.

---

Learn more about Kotlin Multiplatform, Compose Multiplatform, and Kotlin/Wasm:
- https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html
- https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform
- https://kotl.in/wasm/
