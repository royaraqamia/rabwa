package com.example.data.remote.mapper

import com.example.domain.model.storage.RemoteFile
import io.github.jan.supabase.storage.FileObject

object StorageMapper {

    fun toDomain(fileObject: FileObject, bucket: String, publicUrl: String? = null): RemoteFile {
        return RemoteFile(
            id = fileObject.id ?: fileObject.name,
            name = fileObject.name,
            bucket = bucket,
            path = fileObject.name,
            sizeBytes = fileObject.metadata?.get("size")?.toString()?.toLongOrNull(),
            mimeType = fileObject.metadata?.get("mimetype")?.toString()?.replace("\"", ""),
            publicUrl = publicUrl,
            createdAt = fileObject.createdAt?.toString(),
            updatedAt = fileObject.updatedAt?.toString()
        )
    }
}
