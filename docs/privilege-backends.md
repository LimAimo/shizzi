# Privilege backends

Shizzi needs a small helper running with the Android `shell` identity in order to
reach the protected test-network and tethering APIs. The app-facing session code
now depends on `PrivilegedClient` rather than on Shizuku directly.

Two backends are provided:

- **Shizuku** remains the default and uses a Shizuku UserService, preserving the
  existing behavior.
- **Wireless debugging (Local ADB)** pairs with the device's own ADB daemon on
  Android 11+ and uses ADB only to launch a dedicated `app_process` helper. Once
  launched, the app communicates with that helper over an authenticated loopback
  RPC socket. Normal session operations do not execute one ADB command per call.

The selected backend is persisted in `SettingsStore`; `SessionService`,
compatibility checks, diagnostics, and the Quick Settings tile all select the same
backend.

## Local ADB lifecycle

1. The user enables Wireless debugging and asks Android to pair using a code.
2. Shizzi discovers the TLS pairing endpoint with mDNS and stores its ADB RSA
   identity in Android Keystore.
3. Later launches reconnect to the local ADB daemon using the persisted pairing.
4. ADB starts `LocalAdbHelperMain` as `shell` using `app_process` and the installed
   APK as its class path.
5. The helper hosts an authenticated loopback RPC endpoint and delegates to the
   existing `TetherService` implementation.
6. Stopping the backend asks the helper to stop the tethering session and exit.

This keeps the privilege mechanism isolated from the tethering implementation and
allows additional backends (for example a root backend) without duplicating the
session logic.
