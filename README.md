# EchoVault 📋

  > Never lose a copy again.

  **EchoVault** is an ultra-lightweight Android clipboard history manager built with Jetpack Compose, Material 3, and Clean Architecture.

  ## Features

  - 📋 **Unlimited Clipboard History** — captures text, URLs, emails, OTP codes, code snippets
  - 🔍 **Powerful Search** — instant full-text search
  - ⭐ **Favorites & Collections** — pin important entries
  - 📊 **Statistics** — daily/weekly activity charts
  - 🔒 **Biometric Security** — fingerprint & face unlock
  - 🌙 **Dark / Light Mode**
  - 📤 **Export** — TXT, JSON, CSV
  - 📥 **Import** — restore backups
  - 🔐 **Encrypted storage** — AndroidX Security Crypto
  - 🚀 **Background service** — survives reboots & process death

  ## Tech Stack

  | Layer | Tech |
  |-------|------|
  | UI | Jetpack Compose + Material 3 |
  | DI | Hilt |
  | DB | Room + AndroidX Security Crypto |
  | Async | Coroutines + Flow |
  | Architecture | Clean Architecture (MVVM) |
  | Background | Foreground Service + WorkManager |
  | Biometrics | BiometricPrompt |

  ## Build

  ```bash
  ./gradlew assembleRelease
  ```

  ## CI/CD

  GitHub Actions automatically builds and releases APKs on every push and tag.

  ---

  Made with ❤️ by AIVOS
  