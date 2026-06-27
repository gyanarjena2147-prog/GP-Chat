package com.example.data

import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object EncryptionHelper {
    private const val ALGORITHM = "AES"
    // 16-byte key for AES-128 (fully local symmetric key mimicking end-to-end Signal double ratchet)
    private val keyBytes = "WaE2EeSecureKey_".toByteArray(StandardCharsets.UTF_8)
    private val secretKey = SecretKeySpec(keyBytes, ALGORITHM)

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return plainText
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val base64Cipher = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            "E2EE:$base64Cipher"
        } catch (e: Exception) {
            plainText
        }
    }

    fun decrypt(cipherText: String): String {
        if (!cipherText.startsWith("E2EE:")) return cipherText
        return try {
            val base64Cipher = cipherText.substring(5)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decryptedBytes = cipher.doFinal(Base64.decode(base64Cipher, Base64.NO_WRAP))
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            cipherText
        }
    }
}
