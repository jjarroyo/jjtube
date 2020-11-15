package com.jjtube

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jjtube.model.Song
import java.math.RoundingMode
import java.util.concurrent.TimeUnit


class FilesAdapter(private val data: MutableList<Song>) : RecyclerView.Adapter<FilesAdapter.DirectoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):DirectoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false)
        return DirectoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DirectoryViewHolder, position: Int) {
        val item = data[position]
        // Log.e("poster","https://dowanimes.com"+item?.poster)
       //Glide.with(holder.itemView.context).load("https://dowanimes.com"+item.poster).transform( CenterCrop(), RoundedCorners(25)).into(holder.imageView)
        holder.idView.text = item.displayName
        try {
            val time: Long = item.duration
            var seconds = time / 1000
            val minutes = seconds / 60
            seconds %= 60
            if (seconds < 10) {
                holder.extraView.text  = "$minutes:0$seconds        "+(item.size / 1048576).toDouble()+"MB      MP3"

            } else {
                holder.extraView.text = "$minutes:$seconds      "+round2Decimals((item.size.toDouble() / 1048576),0)+"MB      MP3"

            }
        } catch (e: NumberFormatException) {

        }
        Log.e("vvv",""+(item.size))
        Log.e("poster",""+(item.size / 1048576).toDouble())

        holder.itemView.setOnClickListener {
            viewContent(item.path,holder.itemView.context)
        }

    }

    private fun viewContent(path: String, context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = Uri.parse(path)
            val mimeType = context.contentResolver.getType(uri) ?: "*/*"
            intent.setDataAndType(uri, mimeType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (intent.resolveActivity(context.packageManager) != null) {
                ContextCompat.startActivity(context, intent, null)
            } else {
                Toast.makeText(context,"Aplicacion de musica/video no disponible", Toast.LENGTH_SHORT).show()
            }
        }catch (e: Exception){
            Log.e("ERROR", "no se pudo lanzar")
        }
    }

    fun round2Decimals(number: Double, numDecimalPlaces: Int): Double {
        return number.toBigDecimal().setScale(1, RoundingMode.HALF_UP).toDouble()
    }
    inner class DirectoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.findViewById(R.id.textViewTitle)
        val extraView: TextView = view.findViewById(R.id.textExtra)
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

}