package com.jjtube

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import androidx.loader.content.Loader
import com.jiajunhui.xapp.medialoader.bean.AudioItem
import com.jiajunhui.xapp.medialoader.bean.AudioResult
import com.jiajunhui.xapp.medialoader.callback.BaseLoaderCallBack
import com.jiajunhui.xapp.medialoader.callback.OnLoaderCallBack
import java.io.File
import java.util.*

abstract class OnAudioLoaderCallBack(
    val context:Context,
    val onLoaderCallBack:OnLoaderCallBack
) : BaseLoaderCallBack<AudioResult>() {
    override fun getSelectProjection(): Array<String>? {
        return arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.MediaColumns.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED
        )
    }

    override fun getQueryUri(): Uri {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    override fun getSelections(): String? {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val youtubeDLDir = File(downloadsDir, "jjtube")
        return MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                MediaStore.Audio.Media.DATA + " LIKE '"+youtubeDLDir.absolutePath+"/%'"
    }

    override fun onLoadFinish(p0: Loader<Cursor>?, data: Cursor?) {
        val result: MutableList<AudioItem> = ArrayList()
        var item: AudioItem
        var sum_size: Long = 0
        if(data != null){
            while (data.moveToNext()) {
                item = AudioItem()
                val audioId: Int = data.getInt(data.getColumnIndexOrThrow(BaseColumns._ID))
                val name: String = data.getString(data.getColumnIndexOrThrow(MediaColumns.DISPLAY_NAME))
                val path: String = data.getString(data.getColumnIndexOrThrow(MediaColumns.DATA))
                val duration: Long = data.getLong(data.getColumnIndexOrThrow(MediaColumns.DURATION))
                val size: Long = data.getLong(data.getColumnIndexOrThrow(MediaColumns.SIZE))
                val modified: Long =
                    data.getLong(data.getColumnIndexOrThrow(MediaColumns.DATE_MODIFIED))
                item.id = audioId
                item.displayName = name
                item.path = path
                item.duration = duration
                item.size = size
                item.modified = modified
                result.add(item)
                sum_size += size
            }
        }

        onResult(AudioResult(sum_size, result))
    }


}