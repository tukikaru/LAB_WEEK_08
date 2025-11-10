package com.example.lab_week_08.worker

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class FirstWorker(
    context: Context, workerParams: WorkerParameters
): Worker(context, workerParams) { [cite: 67-69]
    // Fungsi ini mengeksekusi proses yang telah ditentukan
    // dan mengembalikan output setelah selesai
    override fun doWork(): Result { [cite: 72]
        // Mendapatkan parameter input
        val id = inputData.getString(INPUT_DATA_ID) [cite: 74]

        // Menidurkan proses selama 3 detik untuk simulasi
        Thread.sleep(3000L) [cite: 76, 92]

        // Membangun output berdasarkan hasil proses
        val outputData = Data.Builder() [cite: 78]
        .putString(OUTPUT_DATA_ID, id) [cite: 79]
        .build() [cite: 80]

        // Mengembalikan output
        return Result.success(outputData) [cite: 82]
    }

    companion object { [cite: 86]
        const val INPUT_DATA_ID = "inId" [cite: 87]
        const val OUTPUT_DATA_ID = "outId" [cite: 88]
    } [cite: 89]
} [cite: 91]