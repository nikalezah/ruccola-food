This is a Kotlin Multiplatform project targeting Android, Web, and Server with separate Admin and Customer apps.

* `/androidAppAdmin` and `/androidAppCustomer` are the Android application entry points (AGP 9).
* `/composeAppAdmin/src` is the Admin KMP library (Android target + Web).
* `/composeAppCustomer/src` is the Customer KMP library (Android target + Web).
* `/server/src/main/kotlin` is for the Ktor server application.
* `/shared/src` is for code shared between all targets in the project. The most important subfolder is
  `/shared/src/commonMain/kotlin`. You can add platform-specific code under the corresponding source sets.

### Build and Run Android Applications

To build and run the development version of the Android apps, use the run configuration from the run widget
in your IDE's toolbar or build them directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :androidAppAdmin:assembleDebug
  ./gradlew :androidAppCustomer:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :androidAppAdmin:assembleDebug
  .\gradlew.bat :androidAppCustomer:assembleDebug
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
      ./gradlew :composeAppAdmin:wasmJsBrowserDevelopmentRun
      ./gradlew :composeAppCustomer:wasmJsBrowserDevelopmentRun
      ```
    - on Windows
      ```shell
      .\gradlew.bat :composeAppAdmin:wasmJsBrowserDevelopmentRun
      .\gradlew.bat :composeAppCustomer:wasmJsBrowserDevelopmentRun
      ```
- for the JS target (slower, supports older browsers):
    - on macOS/Linux
      ```shell
      ./gradlew :composeAppAdmin:jsBrowserDevelopmentRun
      ./gradlew :composeAppCustomer:jsBrowserDevelopmentRun
      ```
    - on Windows
      ```shell
      .\gradlew.bat :composeAppAdmin:jsBrowserDevelopmentRun
      .\gradlew.bat :composeAppCustomer:jsBrowserDevelopmentRun
      ```

---

Learn more about Kotlin Multiplatform, Compose Multiplatform, and Kotlin/Wasm:
- https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html
- https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform
- https://kotl.in/wasm/
