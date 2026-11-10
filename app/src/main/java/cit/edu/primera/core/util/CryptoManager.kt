package cit.edu.primera.core.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {
    private const val PREFIX = "ENC:v1:"
    private const val KEY_ALIAS = "primera_field_encryption_key"
    private const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    private val isoDateFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

    private var fallbackKey: SecretKey? = null

    @Synchronized
    private fun getSecretKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply { load(null) }
            if (keyStore.containsAlias(KEY_ALIAS)) {
                val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
                entry?.secretKey ?: generateAndroidKeyStoreKey()
            } else {
                generateAndroidKeyStoreKey()
            }
        } catch (_: Exception) {
            // Fallback for JVM unit tests or environments where AndroidKeyStore provider is missing
            getOrCreateFallbackKey()
        }
    }

    private fun generateAndroidKeyStoreKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE_PROVIDER
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun getOrCreateFallbackKey(): SecretKey {
        if (fallbackKey == null) {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256)
            fallbackKey = keyGen.generateKey()
        }
        return fallbackKey!!
    }

    fun encrypt(plainText: String?): String? {
        if (plainText.isNullOrEmpty()) return plainText
        if (plainText.startsWith(PREFIX)) return plainText

        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

            val base64 = Base64.getEncoder().encodeToString(combined)
            "$PREFIX$base64"
        } catch (_: Exception) {
            plainText
        }
    }

    fun decrypt(cipherText: String?): String? {
        if (cipherText.isNullOrEmpty()) return cipherText
        if (!cipherText.startsWith(PREFIX)) return cipherText

        return try {
            val base64Payload = cipherText.substring(PREFIX.length)
            val combined = Base64.getDecoder().decode(base64Payload)

            if (combined.size <= GCM_IV_LENGTH) return cipherText

            val iv = ByteArray(GCM_IV_LENGTH)
            val encryptedBytes = ByteArray(combined.size - GCM_IV_LENGTH)

            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (_: Exception) {
            cipherText
        }
    }

    fun encryptDate(date: Date?): String? {
        if (date == null) return null
        val formatted = isoDateFormat.format(date)
        return encrypt(formatted)
    }

    fun decryptDate(value: Any?): Date? {
        if (value == null) return null
        if (value is com.google.firebase.Timestamp) return value.toDate()
        if (value is Date) return value
        if (value is Number) return Date(value.toLong())

        val strVal = value.toString()
        val decrypted = decrypt(strVal) ?: return null
        return try {
            isoDateFormat.parse(decrypted)
        } catch (_: Exception) {
            try {
                Date(decrypted.toLong())
            } catch (_: Exception) {
                null
            }
        }
    }

    fun encryptInt(value: Int?): String? = value?.let { encrypt(it.toString()) }

    fun decryptInt(value: Any?): Int? {
        if (value == null) return null
        if (value is Number) return value.toInt()
        val decrypted = decrypt(value.toString())
        return decrypted?.toIntOrNull()
    }

    fun encryptLong(value: Long?): String? = value?.let { encrypt(it.toString()) }

    fun decryptLong(value: Any?): Long? {
        if (value == null) return null
        if (value is Number) return value.toLong()
        val decrypted = decrypt(value.toString())
        return decrypted?.toLongOrNull()
    }

    fun encryptBoolean(value: Boolean?): String? = value?.let { encrypt(it.toString()) }

    fun decryptBoolean(value: Any?): Boolean? {
        if (value == null) return null
        if (value is Boolean) return value
        val decrypted = decrypt(value.toString())
        return decrypted?.toBooleanStrictOrNull()
    }

    fun encryptStringList(list: List<String>?): String? {
        if (list == null) return null
        val jsonString = list.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]") {
            it.replace("\\", "\\\\").replace("\"", "\\\"")
        }
        return encrypt(jsonString)
    }

    fun decryptStringList(value: Any?): List<String>? {
        if (value == null) return null
        if (value is List<*>) {
            return value.mapNotNull { it?.toString() }
        }
        val decrypted = decrypt(value.toString()) ?: return null
        if (!decrypted.startsWith("[") || !decrypted.endsWith("]")) return emptyList()
        val content = decrypted.substring(1, decrypted.length - 1).trim()
        if (content.isEmpty()) return emptyList()

        val items = mutableListOf<String>()
        var i = 0
        var inQuotes = false
        val current = StringBuilder()
        var isEscaped = false

        while (i < content.length) {
            val c = content[i]
            if (isEscaped) {
                current.append(c)
                isEscaped = false
            } else if (c == '\\') {
                isEscaped = true
            } else if (c == '"') {
                if (inQuotes) {
                    items.add(current.toString())
                    current.clear()
                    inQuotes = false
                } else {
                    inQuotes = true
                }
            } else if (inQuotes) {
                current.append(c)
            }
            i++
        }
        return items
    }

    fun encryptJsonMapList(list: List<Map<String, Any?>>?): String? {
        if (list == null) return null
        val sb = StringBuilder("[")
        list.forEachIndexed { index, map ->
            if (index > 0) sb.append(",")
            sb.append("{")
            var first = true
            map.forEach { (k, v) ->
                if (!first) sb.append(",")
                first = false
                sb.append("\"").append(k).append("\":")
                when (v) {
                    null -> sb.append("null")
                    is Boolean, is Number -> sb.append(v.toString())
                    is Date -> sb.append("\"").append(isoDateFormat.format(v)).append("\"")
                    is List<*> -> {
                        val itemsStr =
                            v.joinToString(prefix = "[", separator = ",", postfix = "]") { item ->
                                if (item == null) "null"
                                else "\"${
                                    item.toString().replace("\\", "\\\\").replace("\"", "\\\"")
                                }\""
                            }
                        sb.append(itemsStr)
                    }

                    else -> sb.append("\"")
                        .append(v.toString().replace("\\", "\\\\").replace("\"", "\\\""))
                        .append("\"")
                }
            }
            sb.append("}")
        }
        sb.append("]")
        return encrypt(sb.toString())
    }

    fun decryptJsonMapList(value: Any?): List<Map<String, Any>>? {
        if (value == null) return null
        if (value is List<*>) {
            @Suppress("UNCHECKED_CAST")
            return value as? List<Map<String, Any>>
        }
        val decrypted = decrypt(value.toString()) ?: return null
        if (!decrypted.startsWith("[") || !decrypted.endsWith("]")) return null
        val content = decrypted.substring(1, decrypted.length - 1).trim()
        if (content.isEmpty()) return emptyList()

        return try {
            val result = mutableListOf<Map<String, Any>>()
            val objRegex = Regex("\\{([^}]*)\\}")
            val matches = objRegex.findAll(content)

            for (match in matches) {
                val map = mutableMapOf<String, Any>()
                val pairsStr = match.groupValues[1]
                val pairRegex =
                    Regex("\"([^\"]+)\"\\s*:\\s*(null|true|false|-?\\d+(?:\\.\\d+)?|\\[[^\\]]*\\]|\"[^\"]*\")")
                for (pairMatch in pairRegex.findAll(pairsStr)) {
                    val key = pairMatch.groupValues[1]
                    val rawVal = pairMatch.groupValues[2]
                    val parsedVal: Any? = when {
                        rawVal == "null" -> null
                        rawVal == "true" -> true
                        rawVal == "false" -> false
                        rawVal.startsWith("[") && rawVal.endsWith("]") -> decryptStringList(rawVal)
                            ?: emptyList<String>()

                        rawVal.startsWith("\"") && rawVal.endsWith("\"") -> rawVal.substring(
                            1,
                            rawVal.length - 1
                        )

                        rawVal.contains(".") -> rawVal.toDoubleOrNull()
                        else -> rawVal.toLongOrNull()
                    }
                    if (parsedVal != null) {
                        map[key] = parsedVal
                    }
                }
                result.add(map)
            }
            result
        } catch (_: Exception) {
            null
        }
    }
}
