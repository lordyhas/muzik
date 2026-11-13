package com.lordyhas.muzik.data

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat

/**
 * The installed version of the app, read from the package manager rather than BuildConfig,
 * so it always matches what's actually installed and needs no build feature.
 */
data class AppVersion(
    val name: String,
    val code: Long,
    /** Equal to [lastUpdateTime] on a fresh install, later after an update. */
    val firstInstallTime: Long,
    val lastUpdateTime: Long
) {
    val label: String get() = "$name ($code)"
    val isFreshInstall: Boolean get() = firstInstallTime == lastUpdateTime
}

fun Context.readAppVersion(): AppVersion {
    val info: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0)
    }
    return AppVersion(
        name = info.versionName.orEmpty(),
        code = PackageInfoCompat.getLongVersionCode(info),
        firstInstallTime = info.firstInstallTime,
        lastUpdateTime = info.lastUpdateTime
    )
}

/** Links shown on the About screen. */
object AppLinks {
    private const val REPO = "https://github.com/lordyhas/muzik"
    const val SOURCE_CODE = REPO
    const val LICENSE = "$REPO/blob/main/LICENSE"
    const val REPORT_BUG = "$REPO/issues/new?template=bug_report.yml"
    const val SUGGEST_FEATURE = "$REPO/issues/new?template=feature_request.yml"
    const val RELEASES = "$REPO/releases/latest"
}
