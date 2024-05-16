package com.bitwarden.authenticator.data.platform.manager.imports

import com.bitwarden.authenticator.data.authenticator.datasource.disk.AuthenticatorDiskSource
import com.bitwarden.authenticator.data.platform.manager.imports.model.ImportDataResult
import com.bitwarden.authenticator.data.platform.manager.imports.model.ImportFileFormat
import com.bitwarden.authenticator.data.platform.manager.imports.parsers.BitwardenExportParser
import com.bitwarden.authenticator.data.platform.manager.imports.parsers.ExportParser
import com.bitwarden.authenticator.data.platform.manager.imports.parsers.TwoFasExportParser

/**
 * Default implementation of [ImportManager] for managing importing files exported by various
 * authenticator applications.
 */
class ImportManagerImpl(
    private val authenticatorDiskSource: AuthenticatorDiskSource,
) : ImportManager {
    override suspend fun import(
        importFileFormat: ImportFileFormat,
        byteArray: ByteArray,
    ): ImportDataResult {

        val parser: ExportParser = when (importFileFormat) {
            ImportFileFormat.BITWARDEN_JSON -> BitwardenExportParser(importFileFormat)
            ImportFileFormat.TWO_FAS_JSON -> TwoFasExportParser()
        }

        return parser.parse(byteArray)
            .map { authenticatorDiskSource.saveItem(*it.toTypedArray()) }
            .fold(
                onSuccess = { ImportDataResult.Success },
                onFailure = { ImportDataResult.Error }
            )
    }
}