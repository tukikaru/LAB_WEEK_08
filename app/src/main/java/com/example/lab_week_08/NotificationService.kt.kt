package com.example.lab_week_08

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NotificationService : Service() {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var serviceHandler: Handler

    // onBind tidak digunakan di tutorial ini
    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // Membuat notifikasi
        notificationBuilder = startForegroundService()

        // Membuat HandlerThread untuk menjalankan service di thread terpisah
        val handlerThread = HandlerThread("SecondThread")
            .apply { start() }
        serviceHandler = Handler(handlerThread.looper)
    }

    // Fungsi untuk membuat dan memulai foreground service
    private fun startForegroundService(): NotificationCompat.Builder {
        // Membuat PendingIntent
        val pendingIntent = getPendingIntent()
        // Membuat Channel ID
        val channelId = createNotificationChannel()
        // Membuat Notification Builder
        val notificationBuilder = getNotificationBuilder(
            pendingIntent, channelId
        )

        // Memulai foreground service
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        return notificationBuilder
    }

    // Fungsi untuk mendapatkan PendingIntent (aksi saat notifikasi diklik)
    private fun getPendingIntent(): PendingIntent {
        // Cek versi SDK untuk Flag
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

        // Mengarahkan ke MainActivity saat diklik
        return PendingIntent.getActivity(
            this, 0, Intent(
                this,
                MainActivity::class.java
            ), flag
        )
    }

    // Fungsi untuk membuat Notification Channel (wajib untuk API 26+)
    private fun createNotificationChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Konfigurasi channel
            val channelId = "001"
            val channelName = "001 Channel"
            val channelPriority = NotificationManager.IMPORTANCE_DEFAULT

            // Membuat channel
            val channel = NotificationChannel(
                channelId,
                channelName,
                channelPriority
            )

            // Mendaftarkan channel ke NotificationManager
            val service = requireNotNull(
                ContextCompat.getSystemService(
                    this,
                    NotificationManager::class.java
                )
            )
            service.createNotificationChannel(channel)
            return channelId
        } else {
            return ""
        }
    }

    // Fungsi untuk membangun notifikasi
    private fun getNotificationBuilder(pendingIntent: PendingIntent, channelId: String) =
        NotificationCompat.Builder(this, channelId)
            .setContentTitle("Second worker process is done")
            .setContentText("Check it out!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setTicker("Second worker process is done, check it out!")
            .setOngoing(true) // Notifikasi tidak bisa di-dismiss pengguna

    // (Kode dari Langkah 4 akan ditambahkan di sini)
    //... (tepat setelah fungsi getNotificationBuilder)

    // Callback ini dipanggil saat service dimulai
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val returnValue = super.onStartCommand(intent, flags, startId)

        // Mendapatkan ID channel dari Intent
        val Id = intent?.getStringExtra(EXTRA_ID)
            ?: throw IllegalStateException("Channel ID must be provided")

        // Menjalankan tugas di thread terpisah (dari serviceHandler)
        serviceHandler.post {
            // Memulai hitung mundur di notifikasi
            countDownFromTenToZero(notificationBuilder)
            // Memberi tahu MainActivity bahwa proses selesai
            notifyCompletion(Id)

            // Menghentikan foreground service (notifikasi hilang)
            stopForeground(STOP_FOREGROUND_REMOVE)
            // Menghentikan dan menghancurkan service
            stopSelf()
        }
        return returnValue
    }

    // Fungsi untuk hitung mundur di notifikasi
    private fun countDownFromTenToZero(notificationBuilder: NotificationCompat.Builder) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Hitung mundur dari 10 ke 0
        for (i in 10 downTo 0) {
            Thread.sleep(1000L)
            // Update teks konten notifikasi
            notificationBuilder.setContentText("$i seconds until last warning")
                .setSilent(true)
            // Tampilkan update ke pengguna
            notificationManager.notify(
                NOTIFICATION_ID,
                notificationBuilder.build()
            )
        }
    }

    // Fungsi untuk update LiveData di Main Thread
    private fun notifyCompletion(Id: String) {
        Handler(Looper.getMainLooper()).post {
            mutableID.value = Id
        }
    }

    // companion object { ... }
    companion object {
        const val NOTIFICATION_ID = 0xCA7
        const val EXTRA_ID = "Id"

        // LiveData untuk melacak status penyelesaian service
        private val mutableID = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableID
    }
}