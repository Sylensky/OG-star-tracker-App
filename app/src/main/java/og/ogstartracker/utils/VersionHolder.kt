package og.ogstartracker.utils

import og.ogstartracker.network.models.VersionResponse

object VersionHolder {
    @Volatile
    var version: VersionResponse? = null
}
