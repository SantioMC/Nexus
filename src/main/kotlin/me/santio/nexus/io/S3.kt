package me.santio.nexus.io

import io.minio.*
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.internal.threadFactory
import org.bukkit.configuration.file.FileConfiguration
import java.io.InputStream
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class S3 internal constructor(private val config: FileConfiguration) {

    var client: MinioClient? = null
    var bucket: String? = null

    fun connect() {
        if (client != null) return

        val httpClient = OkHttpClient.Builder()
            .dispatcher(Dispatcher(ThreadPoolExecutor(0, Int.MAX_VALUE, 60, TimeUnit.SECONDS,
                SynchronousQueue(), threadFactory("nexus-s3", false))))
            .connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .protocols(listOf(Protocol.HTTP_1_1))
            .build()

        val hasAuth = config.contains("s3.access-key") && config.contains("s3.secret-key")
        val builder = MinioClient.builder()
            .endpoint(config.getString("s3.url", "http://localhost:9000")!!)
            .region(config.getString("s3.region", "us-east-1")!!)
            .httpClient(httpClient)

        if (hasAuth) builder.credentials(config.getString("s3.access-key"), config.getString("s3.secret-key"))

        client = builder.build()
        bucket = config.getString("s3.bucket", "nexus")!!

        ensureExists()
    }

    private fun ensureExists() {
        val exists = client?.bucketExists(BucketExistsArgs.builder().bucket(bucket).build()) ?: false
        if (exists) return

        client!!.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
    }

    fun push(obj: String, data: InputStream) {
        client?.putObject(PutObjectArgs.builder()
            .bucket(bucket)
            .`object`(obj)
            .stream(data, data.available().toLong(), 104857600 /* 100MB */)
            .build()) ?: return
    }

    fun pull(obj: String): ByteArray? {
        return runCatching {
            client?.getObject(GetObjectArgs.builder()
            .bucket(bucket)
            .`object`(obj)
            .build())
        }.getOrNull()?.readAllBytes()
    }

}