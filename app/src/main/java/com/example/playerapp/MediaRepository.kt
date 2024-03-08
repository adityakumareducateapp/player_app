package com.example.playerapp

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getVideosFlow(): Flow<List<Video>>
    fun getVideosFlowFromFolderPath(folderPath: String): Flow<List<Video>>
    fun getFoldersFlow(): Flow<List<Folder>>
    suspend fun saveVideoState(uri: String, position: Long, audioTrackIndex: Int?, subtitleTrackIndex: Int?, playbackSpeed: Float?, externalSubs: List<Uri>)
    suspend fun getVideoState(uri: String): VideoState?
}
