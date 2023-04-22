package com.github.bluspring.mfg.authenticator.database

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/*data class EncryptionData(
    val encrypted: String,
    val iv: ByteArray,
    val salt: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptionData

        if (encrypted != other.encrypted) return false
        if (!iv.contentEquals(other.iv)) return false
        if (!salt.contentEquals(other.salt)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = encrypted.hashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        return result
    }
}*/

object EncryptionManager {
    const val ENCRYPT_ALGO = "AES/GCM/NoPadding"
    const val TAG_LENGTH_BIT = 128
    const val IV_LENGTH_BYTE = 12
    const val AES_KEY_BIT = 256
    const val SALT_LENGTH_BYTE = 16

    fun encrypt(pText: ByteArray, password: String): String {
        val salt = getRandomNonce(SALT_LENGTH_BYTE)
        val iv = getRandomNonce(IV_LENGTH_BYTE)

        val aesKeyFromPassword = getAESKeyFromPassword(password, salt)

        val cipher = Cipher.getInstance(ENCRYPT_ALGO)
        cipher.init(Cipher.ENCRYPT_MODE, aesKeyFromPassword, GCMParameterSpec(TAG_LENGTH_BIT, iv))

        val cipherText = cipher.doFinal(pText)

        val cipherTextWithIvSalt = ByteBuffer.allocate(iv.size + salt.size + cipherText.size)
            .put(iv)
            .put(salt)
            .put(cipherText)
            .array()

        return Base64.getEncoder().encodeToString(cipherTextWithIvSalt)
    }

    fun decrypt(cText: String, password: String): String {
        val decode = Base64.getDecoder().decode(cText.encodeToByteArray())
        val bb = ByteBuffer.wrap(decode)

        val iv = ByteArray(IV_LENGTH_BYTE)
        bb.get(iv)

        val salt = ByteArray(SALT_LENGTH_BYTE)
        bb.get(salt)

        val cipherText = ByteArray(bb.remaining())
        bb.get(cipherText)

        val aesKeyFromPassword = getAESKeyFromPassword(password, salt)

        val cipher = Cipher.getInstance(ENCRYPT_ALGO)
        cipher.init(Cipher.DECRYPT_MODE, aesKeyFromPassword, GCMParameterSpec(TAG_LENGTH_BIT, iv))

        val plainText = cipher.doFinal(cipherText)

        return String(plainText, UTF_8)
    }

    private fun getRandomNonce(numBytes: Int): ByteArray {
        val nonce = ByteArray(numBytes)
        SecureRandom().nextBytes(nonce)
        return nonce
    }

    private fun getAESKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256, SecureRandom.getInstanceStrong())
        return keyGen.generateKey()
    }

    private fun getAESKeyFromPassword(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }
}