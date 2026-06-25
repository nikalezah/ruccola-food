This is a Kotlin Multiplatform project targeting Android, Web, and Server with separate Admin and Customer apps.

* `/app/admin/androidApp` and `/app/customer/androidApp` are the Android application entry points (AGP 9).
* `/app/admin/shared/src` is the Admin KMP library used by Android and Web.
* `/app/admin/webApp/src` is the Admin Web application entry point.
* `/app/customer/shared/src` is the Customer KMP library used by Android and Web.
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

---

Learn more about Kotlin Multiplatform, Compose Multiplatform, and Kotlin/Wasm:
- https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html
- https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform
- https://kotl.in/wasm/
