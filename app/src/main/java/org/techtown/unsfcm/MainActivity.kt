package org.techtown.unsfcm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import org.techtown.unsfcm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val tag: String = javaClass.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        getToken()
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                AppData.error(tag, "Fetching FCM registration token failed", it.exception)
                return@addOnCompleteListener
            }
            val token = it.result
            AppData.debug(tag, "get Token called. Token: ${token.substring(0, 20)}...")
            AppData.error("getToken: ", token)

            val redisManager = RedisManager()
            redisManager.setTokenInRedis(token)
            redisManager.closeConnection()
        }
    }
}