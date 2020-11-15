package com.jjtube

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.BaseColumns
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.jjtube.model.Song
import kotlinx.android.synthetic.main.fragment_second.*
import java.io.File
import java.util.*


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val manager =
            GridLayoutManager(view.context, 1, GridLayoutManager.VERTICAL, false)
        content_list.layoutManager = manager

        loadAudios()
    }

    @SuppressLint("Recycle")
    private fun loadAudios() {

        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri =  Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/jjtube")

        mediaScanIntent.data = contentUri
        activity?.sendBroadcast(mediaScanIntent)

        val selection =   MediaStore.Audio.Media.DATA + " LIKE '%jjtube%'"
        Log.e("selection", selection.toString())
        val data: Cursor? = activity?.contentResolver?.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            getSelectProjection(),
            selection,
            null,
            null
        )

        val size = data?.count ?: 0
        Log.e("id", size.toString())
        if(size > 0 && data != null){
            val result: MutableList<Song> = ArrayList()
            while (data.moveToNext()) {

                val id = data.getInt(data.getColumnIndexOrThrow(BaseColumns._ID))
                val name: String = data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                val path: String = data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                val duration: Long = data.getLong(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION))
                val size: Long = data.getLong(data.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE))
                val modified: Long = data.getLong(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED))
                val album = data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.ALBUM))
                val albumArtist = data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.ALBUM_ARTIST))
                result.add(Song(id, name, path, size, duration, modified,album,albumArtist))
            }

            content_list.adapter = FilesAdapter(result)

            for (item in result) {
                Log.e("name", item.displayName)
                Log.e("album", item.album.toString())
                Log.e("almbun2 ", item.albumArtist.toString())

            }

        }


      /*  MediaLoader.getLoader().loadMedia(activity, OnAudioLoaderCallBack(
            activity,
            OnLoaderCallBack()
        ) {
            override fun onResult(result: AudioResult) {
                for (item in result.items) {
                    Log.e("name", item.displayName)
                    Log.e("duracion", item.duration.toString())
                    Log.e("tamaño", item.size.toString())
                    Log.e("ruta", item.path.toString())

                    Log.e("id", item.id.toString())
                }
            }
        })*/
    }


     fun getSelectProjection(): Array<String>? {
        return arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.DATE_MODIFIED

        )
    }

}
