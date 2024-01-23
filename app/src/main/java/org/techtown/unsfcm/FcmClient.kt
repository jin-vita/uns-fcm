package org.techtown.unsfcm

import android.annotation.SuppressLint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

object FcmClient {
    interface TestApi {
        @GET("test")
        fun sendData(
            @Query("sender") sender: String,
            @Query("receiver") receiver: String,
            @Query("token") token: String,
            @Query("data") data: String,
        ): Call<FcmResponse>
    }

    private const val TAG = "RobotClient"

    @Volatile
    private var instance: TestApi? = null
    val api: TestApi get() = getInstance()

    @Synchronized
    fun getInstance(): TestApi {
        if (instance == null) instance = create()
        return instance as TestApi
    }

    @Synchronized
    fun resetInstance() {
        instance = create()
    }

    private fun create(): TestApi {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val clientBuilder = OkHttpClient.Builder()

        @SuppressLint("CustomX509TrustManager")
        val x509TrustManager: X509TrustManager = object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                AppData.debug(TAG, ": authType: $authType")
            }

            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                AppData.debug(TAG, ": authType: $authType")
            }
        }

        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            clientBuilder.sslSocketFactory(sslSocketFactory, x509TrustManager)
        } catch (e: Exception) {
            AppData.error(TAG, e.message!!)
        }

        clientBuilder.hostnameVerifier(RelaxedHostNameVerifier())

        val headerInterceptor = Interceptor {
            val request = it.request()
                .newBuilder()
                .build()
            return@Interceptor it.proceed(request)
        }
        if (AppData.isDebug) {
            clientBuilder.addInterceptor(headerInterceptor)
            clientBuilder.addInterceptor(httpLoggingInterceptor)
        }

        clientBuilder.connectTimeout(5, TimeUnit.SECONDS)
        clientBuilder.readTimeout(5, TimeUnit.SECONDS)
        clientBuilder.writeTimeout(5, TimeUnit.SECONDS)

        val client = clientBuilder.build()

        return Retrofit.Builder()
            .baseUrl(AppData.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TestApi::class.java)
    }

    @SuppressLint("CustomX509TrustManager")
    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }
    })

    class RelaxedHostNameVerifier : HostnameVerifier {
        @SuppressLint("BadHostnameVerifier")
        override fun verify(hostname: String, session: SSLSession): Boolean {
            return true
        }
    }
}