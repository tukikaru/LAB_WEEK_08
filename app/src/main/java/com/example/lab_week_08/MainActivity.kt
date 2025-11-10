package com.example.lab_week_08

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.lab_week_08.worker.FirstWorker
import com.example.lab_week_08.worker.SecondWorker
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.lab_week_08.worker.ThirdWorker
class MainActivity : AppCompatActivity() {

    // Membuat instance dari WorkManager
    private val workManager by lazy {
        WorkManager.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        // Membuat batasan (constraint)
        // Worker hanya berjalan jika ada koneksi internet
        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val id = "001"

        // Membuat request satu kali untuk FirstWorker
        val firstRequest = OneTimeWorkRequest
            .Builder(FirstWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(FirstWorker.INPUT_DATA_ID, id))
            .build()

        // Membuat request satu kali untuk SecondWorker
        val secondRequest = OneTimeWorkRequest
            .Builder(SecondWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(SecondWorker.INPUT_DATA_ID, id))
            .build()

        val thirdRequest = OneTimeWorkRequest
            .Builder(ThirdWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(ThirdWorker.INPUT_DATA_ID, id))
            .build()

        // Mengatur urutan proses: FirstWorker dulu, baru SecondWorker
        workManager.beginWith(firstRequest)
            .then(secondRequest)
            .then(thirdRequest)
            .enqueue()

        // Mengamati (observe) status FirstWorker
        workManager.getWorkInfoByIdLiveData(firstRequest.id)
            .observe(this) { info ->
                if (info.state.isFinished) {
                    showResult("First process is done")
                }
            }

        // Mengamati (observe) status SecondWorker
        workManager.getWorkInfoByIdLiveData(secondRequest.id)
            .observe(this) { info ->
                if (info.state.isFinished) {
                    showResult("Second process is done")
                    showResult("Second process is done")
                    launchNotificationService()
                }
            }
        workManager.getWorkInfoByIdLiveData(thirdRequest.id)
            .observe(this) { info ->
                if (info.state.isFinished) {
                    showResult("Third process is done")
                    launchSecondNotificationService() // <-- Memanggil service kedua
                }
            }
    }
    private fun launchSecondNotificationService() {
        // Mengamati status penyelesaian dari SecondNotificationService
        SecondNotificationService.trackingCompletion.observe(
            this
        ) { Id ->
            showResult("Process for Notification Channel ID $Id is done!")
        }

        // Membuat Intent untuk memulai SecondNotificationService
        val serviceIntent = Intent(this,
            SecondNotificationService::class.java).apply {
            // Mengirim ID unik untuk service kedua, misal "002"
            // (Pastikan EXTRA_ID juga ada di companion object SecondNotificationService)
            putExtra(SecondNotificationService.EXTRA_ID, "002")
        }

        // Memulai foreground service kedua
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    // Fungsi helper untuk membuat input data
    private fun getIdInputData(idKey: String, idValue: String) =
        Data.Builder()
            .putString(idKey, idValue)
            .build()

    // Fungsi helper untuk menampilkan Toast
    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }
    // ... (setelah fungsi showResult)

    // Meluncurkan NotificationService
    private fun launchNotificationService() {
        // Mengamati status penyelesaian dari service
        NotificationService.trackingCompletion.observe(
            this
        ) { Id ->
            showResult("Process for Notification Channel ID $Id is done!")
        }

        // Membuat Intent untuk memulai service
        val serviceIntent = Intent(this,
            NotificationService::class.java).apply {
            putExtra(EXTRA_ID, "001")
        }

        // Memulai foreground service
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    companion object {
        const val EXTRA_ID = "Id"
    }
// } (penutup kelas MainActivity)
}