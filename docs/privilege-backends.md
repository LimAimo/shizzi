# Privilege backends

Shizzi needs a small helper running with the Android `shell` identity to reach
the protected test-network and tethering APIs. App-facing session code depends
on `PrivilegedClient`, not on one privilege transport.

## Shizuku

Shizuku remains supported and uses a UserService. Existing users can continue to
use it without changing their workflow.

## Wireless Debugging / Local ADB

Android 11+ can use the phone's own Wireless Debugging daemon:

1. Shizzi starts a short-lived foreground pairing-discovery service.
2. The user opens **Wireless debugging → Pair device with pairing code**.
3. Shizzi discovers `_adb-tls-pairing._tcp` with mDNS.
4. The foreground notification changes to expose Android's inline
   `RemoteInput` field for the six-digit pairing code, plus Cancel.
5. Shizzi pairs using a standard RSA PKCS#8 private key and X.509 certificate
   stored in app-private no-backup storage.
6. Reconnects discover only `_adb-tls-connect._tcp` and use the platform TLS
   1.3 provider.
7. ADB is then used only to launch `LocalAdbHelperMain` as `shell`.
8. Normal session operations use an authenticated loopback RPC socket to that
   helper instead of executing an ADB shell command for every operation.

The old Android Keystore Local-ADB identity format is versioned out by this
release. Existing Local ADB users pair once again after updating.

## Failure handling

- Pairing discovery has a bounded timeout and can be canceled from the
  notification or app.
- TLS/RSA handshake failures invalidate the stale ADB identity and tell the user
  to toggle Wireless Debugging and pair again.
- Compatibility checking has a 12-second timeout; after three seconds the UI
  exposes troubleshooting guidance instead of leaving two capability cards
  spinning indefinitely.

A future root backend can implement the same `PrivilegedClient` contract
without changing tethering/session logic.
