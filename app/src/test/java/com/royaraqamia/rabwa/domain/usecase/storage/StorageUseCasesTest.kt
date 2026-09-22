package com.royaraqamia.rabwa.domain.usecase.storage

import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.storage.RemoteFile
import com.royaraqamia.rabwa.domain.model.storage.UploadFileRequest
import com.royaraqamia.rabwa.domain.repository.StorageRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageUseCasesTest {

    private class FakeStorageRepository : StorageRepository {
        val files = mutableListOf<RemoteFile>()

        override suspend fun uploadFile(request: UploadFileRequest): AppResult<RemoteFile> {
            val file = RemoteFile(
                id = request.path,
                name = request.path.substringAfterLast('/'),
                bucket = request.bucket,
                path = request.path,
                sizeBytes = request.data.size.toLong(),
                publicUrl = "https://supabase.co/storage/v1/object/public/${request.bucket}/${request.path}"
            )
            files.add(file)
            return AppResult.Success(file)
        }

        override suspend fun getPublicUrl(bucket: String, path: String): AppResult<String> {
            return AppResult.Success("https://supabase.co/storage/v1/object/public/$bucket/$path")
        }

        override suspend fun deleteFile(bucket: String, path: String): AppResult<Unit> {
            files.removeAll { it.bucket == bucket && it.path == path }
            return AppResult.Success(Unit)
        }

        override suspend fun listFiles(bucket: String, pathPrefix: String?): AppResult<List<RemoteFile>> {
            return AppResult.Success(files.filter { it.bucket == bucket })
        }
    }

    @Test
    fun `UploadStorageFileUseCase rejects invalid bucket name`() = runBlocking {
        val repo = FakeStorageRepository()
        val useCase = UploadStorageFileUseCase(repo)

        val invalidRequest = UploadFileRequest(
            bucket = "INVALID_UPPERCASE",
            path = "images/logo.png",
            data = byteArrayOf(1, 2, 3)
        )
        val result = useCase(invalidRequest)
        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.Validation)
    }

    @Test
    fun `UploadStorageFileUseCase rejects empty data`() = runBlocking {
        val repo = FakeStorageRepository()
        val useCase = UploadStorageFileUseCase(repo)

        val emptyDataRequest = UploadFileRequest(
            bucket = "documents",
            path = "files/doc.pdf",
            data = byteArrayOf()
        )
        val result = useCase(emptyDataRequest)
        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun `UploadStorageFileUseCase uploads successfully with sanitized path`() = runBlocking {
        val repo = FakeStorageRepository()
        val useCase = UploadStorageFileUseCase(repo)

        val validRequest = UploadFileRequest(
            bucket = "documents",
            path = " //files//doc.pdf ",
            data = byteArrayOf(1, 2, 3, 4)
        )
        val result = useCase(validRequest)
        assertTrue(result is AppResult.Success)
        val file = (result as AppResult.Success).data
        assertEquals("files/doc.pdf", file.path)
        assertEquals("doc.pdf", file.name)
    }
}
