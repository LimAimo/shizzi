package dev.shizzi

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import dev.shizzi.ui.theme.Appearance
import dev.shizzi.ui.theme.ShizziTheme
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {
    private val viewModel: SessionViewModel by viewModels()

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { _, granted ->
        viewModel.refreshPrivilegeState()
        viewModel.refreshPermissions()
        onShizukuResult(granted == PackageManager.PERMISSION_GRANTED)
    }
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener { viewModel.refreshPrivilegeState() }
    private val binderDeadListener = Shizuku.OnBinderDeadListener { viewModel.refreshPrivilegeState() }

    private var requested: AppPermission? = null
    private var isChaining = false
    private val asked = mutableSetOf<AppPermission>()
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.refreshPermissions()
        onPermissionResult()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        registerShizukuListeners()

        setContent {
            val settings by viewModel.settings.collectAsState()
            val loaded = settings ?: return@setContent
            ShizziTheme(Appearance(loaded.theme, loaded.design, loaded.accent)) {
                val colors = ShizziTheme.colors
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !colors.isDark
                        isAppearanceLightNavigationBars = !colors.isDark
                    }
                }
                Surface(color = colors.background) {
                    val state by viewModel.state.collectAsState()
                    val diagnostics by viewModel.diagnosticsState.collectAsState()
                    val compatibility by viewModel.compatibilityState.collectAsState()
                    val permissions by viewModel.permissionState.collectAsState()
                    ShizziApp(
                        state = AppState(state, loaded, diagnostics, permissions),
                        onboarding = OnboardingEntry(
                            compatibility,
                            viewModel::checkCompatibility,
                            viewModel::downloadTetheringApex,
                            viewModel::installTetheringApex,
                            viewModel::rebootDevice,
                            viewModel::completeOnboarding,
                        ),
                        actions = AppActions(
                            onToggle = viewModel::toggle,
                            onCancel = viewModel::cancel,
                            onRequestPermission = viewModel::requestPermission,
                            onRequestAllPermissions = ::requestAllPermissions,
                            onGrantPermission = ::grantPermission,
                            onSetPrivilegeBackend = viewModel::setPrivilegeBackend,
                            onShizukuAction = viewModel::actOnShizuku,
                            onOpenWirelessDebugging = viewModel::openWirelessDebuggingSettings,
                            onPairLocalAdb = viewModel::pairLocalAdb,
                            onSetTheme = viewModel::setTheme,
                            onSetDesign = viewModel::setDesign,
                            onSetAccent = viewModel::setAccent,
                            onSetLogging = viewModel::setLogging,
                            onSetVpnMode = viewModel::setVpnMode,
                            onRunProbes = viewModel::runProbes,
                            onCancelProbes = viewModel::cancelProbes,
                            onDismissDiagnostics = viewModel::dismissDiagnostics,
                            onClearLog = viewModel::clearLog,
                            onRestartOnboarding = viewModel::restartOnboarding,
                            onSetAutomation = viewModel::setAutomation,
                            onRegenerateAutomationToken = viewModel::regenerateAutomationToken,
                        ),
                    )
                }
            }
        }
        holdFirstFrameUntilSettingsLoad()
    }

    private fun holdFirstFrameUntilSettingsLoad() {
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (viewModel.settings.value == null) return false
                content.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
    }

    private fun requestAllPermissions() {
        isChaining = true
        requestNextOutstanding()
    }

    private fun requestNextOutstanding() {
        val backend = viewModel.settings.value?.privilegeBackend ?: PrivilegeBackendType.SHIZUKU
        val privilege = viewModel.state.value.privilegeState
        if (privilege !is PrivilegeState.Ready) {
            when (backend) {
                PrivilegeBackendType.SHIZUKU -> {
                    val shizuku = viewModel.state.value.shizukuState
                    if (shizuku !is ShizukuState.PermissionRequired) stopChain()
                    viewModel.actOnShizuku()
                }
                PrivilegeBackendType.LOCAL_ADB -> stopChain() // Pairing requires the code field in the UI.
            }
            return
        }

        val outstanding = viewModel.permissionState.value.firstOrNull { !it.isGranted }?.permission
        if (outstanding == null) { stopChain(); return }
        grantPermission(outstanding)
    }

    private fun grantPermission(permission: AppPermission) {
        val name = permission.manifestName
        if (name == null || isDialogSuppressed(permission)) {
            stopChain(); viewModel.openPermissionSettings(permission); return
        }
        requested = permission
        asked += permission
        permissionLauncher.launch(name)
    }

    private fun onPermissionResult() {
        val permission = requested ?: return
        requested = null
        if (!isChaining) return
        if (viewModel.isPermissionGranted(permission)) requestNextOutstanding() else stopChain()
    }

    private fun onShizukuResult(isGranted: Boolean) {
        if (!isChaining) return
        if (isGranted) requestNextOutstanding() else stopChain()
    }

    private fun stopChain() { isChaining = false }
    private fun isDialogSuppressed(permission: AppPermission): Boolean {
        val name = permission.manifestName ?: return false
        return permission in asked && !shouldShowRequestPermissionRationale(name)
    }

    private fun registerShizukuListeners() {
        Shizuku.addRequestPermissionResultListener(permissionListener)
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPrivilegeState()
        viewModel.refreshPermissions()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        super.onDestroy()
    }
}
