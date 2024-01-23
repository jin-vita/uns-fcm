package org.techtown.unsfcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import org.techtown.unsfcm.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val tag: String = javaClass.simpleName

    private val redisManager by lazy { RedisManager() }

    private lateinit var receiver: BroadcastReceiver
    private lateinit var filter: IntentFilter

    private var channelList = arrayOf("test01", "test02")

    private var myChannel: String = ""

    override fun onStart() {
        super.onStart()
        val method = Thread.currentThread().stackTrace[2].methodName
        AppData.debug(tag, "$method called.")
        ContextCompat.registerReceiver(baseContext, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStop() {
        val method = Thread.currentThread().stackTrace[2].methodName
        AppData.debug(tag, "$method called.")
        unregisterReceiver(receiver)
        super.onStop()
    }

    override fun onDestroy() {
        redisManager.close()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initReceiver()

        initView()
    }

    private fun initView() = with(binding) {
        val method = Thread.currentThread().stackTrace[2].methodName
        AppData.debug(tag, "$method called. SDK: ${Build.VERSION.SDK_INT}")

        resultText.movementMethod = ScrollingMovementMethod()

        connect1Button.setOnClickListener {
            binding.statusText.text = getString(R.string.connecting)
            myChannel = channelList.first()
            setToken(myChannel)
            connect1Button.isEnabled = false
            connect2Button.isEnabled = true
        }

        connect2Button.setOnClickListener {
            binding.statusText.text = getString(R.string.connecting)
            myChannel = channelList.last()
            setToken(myChannel)
            connect1Button.isEnabled = true
            connect2Button.isEnabled = false
        }

        disconnectButton.setOnClickListener {
            if (myChannel.isBlank()) {
                AppData.showToast(this@MainActivity, "현재 연결이 없음")
                return@setOnClickListener
            }
            setToken(myChannel, disconnect = true)
            connect1Button.isEnabled = true
            connect2Button.isEnabled = true
        }

        sendButton.setOnClickListener {
            binding.chatInput.apply {
                when {
                    myChannel.isBlank() ->
                        AppData.showToast(this@MainActivity, "현재 연결이 없음")

                    else -> {
                        val channel = channelList.first { it != myChannel }
                        sendData(channel, this.text.toString().trim())
                    }
                }
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(this.windowToken, 0)
                this.setText("")
            }
        }
    }

    // 리시버 초기화
    private fun initReceiver() {
        val method = Thread.currentThread().stackTrace[2].methodName
        AppData.debug(tag, "$method called.")
        filter = IntentFilter()
        filter.addAction(AppData.ACTION_REMOTE_DATA)
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = setReceivedData(intent)
        }
    }

    fun setReceivedData(intent: Intent) {
        val command = intent.getStringExtra(Extras.COMMAND)
        val channel = intent.getStringExtra(Extras.CHANNEL)
        val data = intent.getStringExtra(Extras.DATA)
        AppData.debug(tag, "$command : $channel - $data")
        printLog("$command : $channel - $data")
        data?.apply {
            when {
                startsWith("check redis connection") -> return
                startsWith("already connected") or startsWith("successfully connected") -> {
                    binding.statusText.text = getString(R.string.connected)
                    binding.idText.text = channel
                }

                endsWith("unsubscribed") -> {
                    binding.statusText.text = getString(R.string.disconnect)
                    binding.idText.text = Extras.UNKNOWN
                }

                equals("fail to connect") ->
                    binding.statusText.text = getString(R.string.fail_to_connect)

                equals("fail to reconnect") ->
                    binding.statusText.text = getString(R.string.fail_to_reconnect)

                else -> setData(this)
            }
        }
    }

    private fun setData(data: String) {
        val method = Thread.currentThread().stackTrace[2].methodName
        AppData.debug(tag, "$method called. data: $data")
        // TODO: 받은 메시지 처리 로직 작성
    }

    private fun setToken(channel: String, disconnect: Boolean = false) {
        if (disconnect) {
            redisManager.set(channel, "")
            binding.statusText.text = getString(R.string.disconnect)
            binding.idText.text = Extras.UNKNOWN
        } else FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                AppData.error(tag, "Fetching FCM registration token failed", it.exception)
                binding.statusText.text = getString(R.string.fail_to_connect)
                binding.connect1Button.isEnabled = true
                binding.connect2Button.isEnabled = true
                myChannel = ""
                return@addOnCompleteListener
            }
            val token = it.result
            AppData.debug(tag, "get Token called. Token: ${token.substring(0, 20)}...")
            AppData.error("getToken: ", token)

            redisManager.set(channel, token)
            binding.statusText.text = getString(R.string.connected)
            binding.idText.text = channel
            return@addOnCompleteListener
        }
    }

    private fun sendData(channel: String, data: String) {
        printLog("to $channel : $data")
        val token = redisManager.get(channel) ?: ""
        AppData.debug(tag, "$channel token : $token")
        requestSendData(channel, token, data)
    }

    private fun requestSendData(channel: String, token: String, data: String) {
        val method = Thread.currentThread().stackTrace[2].methodName
        AppData.debug(tag, "$method called. channel: $channel")
        FcmClient.api.sendData(
            sender = myChannel,
            receiver = channel,
            token = token,
            data = data
        ).enqueue(object : Callback<FcmResponse> {
            override fun onResponse(call: Call<FcmResponse>, response: Response<FcmResponse>) {
                val code = response.code()
                if (response.isSuccessful) {
                    response.body()?.apply {
                        AppData.error(tag, "$method isSuccessful.")
                        AppData.debug(tag, "data: $this")
                    }
                } else binding.resultText.text = "$method isNotSuccessful. code: $code"
            }

            override fun onFailure(call: Call<FcmResponse>, t: Throwable) {
                binding.resultText.text = "$method isFail, ${t.message}"
            }
        })
    }

    private fun printLog(message: String) = runOnUiThread {
        val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")
        val now = LocalDateTime.now().format(formatter)
        val log = "[$now] $message"
        if (AppData.logList.size > 1000) AppData.logList.removeAt(1)
        AppData.logList.add(log)
        val sb = StringBuilder()
        AppData.logList.forEach { sb.appendLine(it) }
        binding.resultText.text = sb
        moveToBottom(binding.resultText)
    }

    private fun moveToBottom(textView: TextView) = textView.post {
        val scrollAmount = try {
            textView.layout.getLineTop(textView.lineCount) - textView.height
        } catch (_: NullPointerException) {
            0
        }
        if (scrollAmount > 0) textView.scrollTo(0, scrollAmount)
        else textView.scrollTo(0, 0)
    }
}