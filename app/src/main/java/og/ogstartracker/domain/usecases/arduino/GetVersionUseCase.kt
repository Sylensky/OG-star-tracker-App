package og.ogstartracker.domain.usecases.arduino

import og.ogstartracker.domain.usecases.base.ResourceSuspendProviderUseCase
import og.ogstartracker.repository.ArduinoRepository
import og.ogstartracker.utils.map

class GetVersionUseCase constructor(
	private val repository: ArduinoRepository
) : ResourceSuspendProviderUseCase<Int?> {

	override suspend fun invoke() = repository.getVersion().map {
		// Extract version number from VersionResponse
		// Try to parse the version string to an integer (e.g., "2.0.0" -> null, "7" -> 7)
		it?.version?.toIntOrNull()
	}
}