package og.ogstartracker.domain.usecases.arduino

import og.ogstartracker.domain.usecases.base.ResourceSuspendProviderUseCase
import og.ogstartracker.network.models.VersionResponse
import og.ogstartracker.repository.ArduinoRepository

class GetVersionUseCase constructor(
	private val repository: ArduinoRepository
) : ResourceSuspendProviderUseCase<VersionResponse> {

	override suspend fun invoke() = repository.getVersion()
}