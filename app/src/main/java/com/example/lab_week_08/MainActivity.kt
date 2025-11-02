package com.example.lab_week_08

import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.work.*
import com.example.lab_week_08.worker.FirstWorker
import com.example.lab_week_08.worker.SecondWorker
import com.example.lab_week_08.worker.ThirdWorker

class MainActivity : AppCompatActivity() {

    private val workManager by lazy { WorkManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //  Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val id = "001"

        val firstRequest = OneTimeWorkRequest.Builder(FirstWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(FirstWorker.INPUT_DATA_ID, id))
            .build()

        val secondRequest = OneTimeWorkRequest.Builder(SecondWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(SecondWorker.INPUT_DATA_ID, id))
            .build()

        val thirdRequest = OneTimeWorkRequest.Builder(ThirdWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(ThirdWorker.INPUT_DATA_ID, id))
            .build()

        //  Chain: First → Second → Third
        workManager.beginWith(firstRequest)
            .then(secondRequest)
            .then(thirdRequest)
            .enqueue()

        // Observe
        workManager.getWorkInfoByIdLiveData(firstRequest.id).observe(this, Observer { info ->
            if (info != null && info.state.isFinished)
                showResult("First process is done")
        })

        workManager.getWorkInfoByIdLiveData(secondRequest.id).observe(this, Observer { info ->
            if (info != null && info.state.isFinished) {
                showResult("Second process is done")
                launchNotificationService()
            }
        })

        workManager.getWorkInfoByIdLiveData(thirdRequest.id).observe(this, Observer { info ->
            if (info != null && info.state.isFinished) {
                showResult("Third process is done")
                launchSecondNotificationService()
            }
        })
    }

    private fun getIdInputData(idKey: String, idValue: String) =
        Data.Builder().putString(idKey, idValue).build()

    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    //  First Foreground Service
    private fun launchNotificationService() {
        NotificationService.trackingCompletion.observe(this, Observer { Id ->
            showResult("NotificationService done for ID $Id")
        })
        val intent = Intent(this, NotificationService::class.java).apply {
            putExtra(EXTRA_ID, "001")
        }
        ContextCompat.startForegroundService(this, intent)
    }

    //  Second Foreground Service
    private fun launchSecondNotificationService() {
        SecondNotificationService.trackingCompletion.observe(this, Observer { Id ->
            showResult("SecondNotificationService done for ID $Id")
        })
        val intent = Intent(this, SecondNotificationService::class.java).apply {
            putExtra(EXTRA_ID, "001")
        }
        ContextCompat.startForegroundService(this, intent)
    }

    companion object {
        const val EXTRA_ID = "Id"
    }
}
