package com.jjtube

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers

class App : Application() {
    private val TAG = "App"
    override fun onCreate() {
        super.onCreate()
        configureRxJavaErrorHandler()
        Completable.fromAction { this.initLibraries() }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableCompletableObserver() {
                override fun onComplete() {
                    // it worked
                }

                override fun onError(e: Throwable) {
                    if (BuildConfig.DEBUG) Log.e(
                        TAG,
                        "failed to initialize youtubedl-android",
                        e
                    )
                    Toast.makeText(
                        applicationContext,
                        "initialization failed: " + e.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


    private fun configureRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler { e: Throwable ->
            if (e is UndeliverableException) {
                Log.e(TAG, "Undeliverable exception received, not sure what to do", e.cause!!)
            }
            if (e is InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return@setErrorHandler
            }
            Log.e(TAG, "Undeliverable exception received, not sure what to do", e)
            // As UndeliverableException is a wrapper, get the cause of it to get the "real" exception
        }
    }

    @Throws(YoutubeDLException::class)
    private fun initLibraries() {
        YoutubeDL.getInstance().init(this)
        FFmpeg.getInstance().init(this)
    }
}