package com.jjtube

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.*
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yausername.youtubedl_android.DownloadProgressCallback
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {
    private lateinit var discoverContentWebView: WebView

    private var running = false
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    var viewDialog:View? = null
    var dialog: MaterialAlertDialogBuilder? = null
    private var textProgress: TextView? = null
    private var progressBar: ProgressBar? = null
    private var btnMp3: Button? = null
    private var btnMp4: Button? = null
    private var btnClose: ImageView? = null
    private var progressLoading : ProgressBar? = null
    private val CHANNEL_ID = "myid"
    private  var flag = false
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        discoverContentWebView = view.findViewById(R.id.discover_webview)
        discoverContentWebView.apply {
            webChromeClient = youtubeWebClient
            settings.javaScriptEnabled = true
            loadUrl("https://m.youtube.com")
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val link = Util.convertMobileToStandard(discoverContentWebView.url.toString())
        Log.e(TAG, link)
        if(isVideo(link)){
            Log.e(TAG, "show")

            download_btn.show()
        }else{
            Log.e(TAG, "hide")
            download_btn.hide()
        }

        download_btn.setOnClickListener {

            viewDialog = LayoutInflater.from(this.context)
                .inflate(R.layout.dialog_layout, null)
            dialog = this.context?.let {
                MaterialAlertDialogBuilder(it, getShapeTheme())
                    .setView(viewDialog)
                    .setCancelable(false)

            }
            textProgress  = viewDialog?.findViewById(R.id.textPercent)
            progressBar = viewDialog?.findViewById(R.id.progressBar)
            btnMp3  = viewDialog?.findViewById(R.id.btn_mp3)
            btnMp4  = viewDialog?.findViewById(R.id.btn_mp4)
            btnClose = viewDialog?.findViewById(R.id.btn_close)
            progressLoading = viewDialog?.findViewById(R.id.progressLoading)
            textProgress?.visibility = View.GONE
            progressBar?.visibility = View.GONE
            progressLoading?.visibility = View.GONE

            val alert = dialog?.show()

            btnMp3?.setOnClickListener {
                flag = true
                textProgress?.text = "0%"
                progressBar?.progress = 0

                progressLoading?.visibility = View.VISIBLE
                textProgress?.visibility = View.GONE
                progressBar?.visibility = View.GONE
                downloadSource(progressLoading,textProgress, progressBar, 2)

            }
            btnMp4?.setOnClickListener {
                flag = true
                progressLoading?.visibility = View.VISIBLE
                textProgress?.visibility = View.GONE
                progressBar?.visibility = View.GONE

                textProgress?.text = "0%"
                progressBar?.progress = 0
                downloadSource(progressLoading,textProgress, progressBar, 1)
            }

            btnClose?.setOnClickListener {
                alert?.dismiss()
            }
            return@setOnClickListener

        }
    }

    @StyleRes
    fun getShapeTheme(): Int {
        return R.style.ThemeOverlay_Crane
    }


    private val youtubeWebClient = object : WebChromeClient() {

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)

            val link = Util.convertMobileToStandard(discoverContentWebView.url.toString())
            Log.e(TAG, link)
            if(isVideo(link)){
                Log.e(TAG, "show")
                if (download_btn != null)
                    download_btn.show()
            }else{
                if (download_btn != null)
                    download_btn.hide()
            }
        }



    }

    @SuppressLint("SetTextI18n")
    private val callback =
        DownloadProgressCallback{ progress, etaInSeconds ->
          //  notificationManager.notify(0,notifi.build());
            activity?.runOnUiThread {

                if (progress > 0 && flag){
                    flag = false
                    progressLoading?.visibility = View.GONE
                    textProgress?.visibility = View.VISIBLE
                    progressBar?.visibility = View.VISIBLE
                }

                progressBar?.progress = progress.toInt()
                textProgress?.text = "$progress% (Tiempo  $etaInSeconds segundos)"
                Log.e(TAG, "$progress% (Tiempo  $etaInSeconds segundos)")
            }
        }

    companion object {
        private const val TAG = "Download"
    }


    fun downloadSource(progressLoading:ProgressBar?, textPercent: TextView?, progress: ProgressBar?, type: Int){
        val link = Util.convertMobileToStandard(discoverContentWebView.url.toString())
        if (link.isBlank()) return

      /*  val vidInfo = YoutubeDL.getInstance().getInfo(link)

       if(vidInfo.formats.size > 0){

           for (format in vidInfo.formats){
               Log.e("ext", format.ext )
               Log.e("acode", format.acodec )
               Log.e("format", format.format )
               Log.e("url", format.url )
               Log.e("vcode", format.vcodec )
               Log.e("size", ""+(format.filesize.toDouble() / 1048576) )
           }

        }


        return
        */

        val youtubeDLDir: File = getDownloadLocation()
        val command =
            "--extract-audio --audio-format mp3 -o $youtubeDLDir/%(title)s.%(ext)s $link"

        var request = YoutubeDLRequest(link)

        if(type == 1){

            request.addOption("-o", youtubeDLDir.absolutePath + "/%(title)s.%(ext)s")
            request.addOption("--force-ipv4")
        }else if(type == 2){
            request = YoutubeDLRequest(emptyList())
            val commandRegex = "\"([^\"]*)\"|(\\S+)"
            val m = Pattern.compile(commandRegex).matcher(command)
            while (m.find()) {
                if (m.group(1) != null) {
                    request.addOption(m.group(1))
                } else {
                    request.addOption(m.group(2))
                }
            }
            request.addOption("--force-ipv4")
        }

        running = true
        val disposable = Observable.fromCallable {
            YoutubeDL.getInstance().execute(request, callback)
        }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ youtubeDLResponse: YoutubeDLResponse ->
                textPercent?.text = "Descarga completa"
                progress?.progress = 100

                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val contentUri =
                    Uri.fromFile(File(Environment.getExternalStorageDirectory().absolutePath + "/jjtube"))
                mediaScanIntent.data = contentUri
                activity?.sendBroadcast(mediaScanIntent)

              //  Toast.makeText(activity, "command successful", Toast.LENGTH_LONG).show()
                running = false
            }) { e: Throwable ->
                if (BuildConfig.DEBUG)
                    Log.e(TAG, "command failed", e)
                progressLoading?.visibility = View.GONE
                textPercent?.text = "Error al descargar"
                textPercent?.visibility = View.VISIBLE
                running = false
            }
        compositeDisposable.add(disposable)
        Log.e(TAG, "download option clicked: $link")
    }

    fun isVideo(youtubeUrl: String): Boolean {

        val pattern =
            "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*"
        val compiledPattern = Pattern.compile(pattern)
        //url is youtube url for which you want to extract the id.
        val matcher: Matcher = compiledPattern.matcher(youtubeUrl)
        return matcher.find()
    }

   /* private fun getDownloadLocation(): File {
        val downloadsDir =
            Environment.getExternalStorageDirectory()
        val youtubeDLDir = File(downloadsDir, "jjtube")
        if (!youtubeDLDir.exists()) youtubeDLDir.mkdir()
        return youtubeDLDir
    }*/

    private fun getDownloadLocation(): File {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val youtubeDLDir = File(downloadsDir, "youtubedl-android")
        if (!youtubeDLDir.exists()) {
            youtubeDLDir.mkdir()
        }
        return youtubeDLDir
    }

    private fun createNotfiChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, "my_chanell", NotificationManager.IMPORTANCE_HIGH)
            channel.setDescription("download notifi")
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager =  activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}