package cit.edu.primera.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class CryptoManagerTest {

    @Test
    fun testStringEncryptAndDecrypt() {
        val originalText = "Cramps and fatigue"
        val encrypted = CryptoManager.encrypt(originalText)

        assertNotNull(encrypted)
        assertTrue(encrypted!!.startsWith("ENC:v1:"))

        val decrypted = CryptoManager.decrypt(encrypted)
        assertEquals(originalText, decrypted)
    }

    @Test
    fun testNullAndEmptyString() {
        assertNull(CryptoManager.encrypt(null))
        assertNull(CryptoManager.decrypt(null))
        assertEquals("", CryptoManager.encrypt(""))
        assertEquals("", CryptoManager.decrypt(""))
    }

    @Test
    fun testLegacyUnencryptedString() {
        val legacyText = "Fetal Movement"
        // Unencrypted string without ENC:v1: prefix should be returned as-is
        val decrypted = CryptoManager.decrypt(legacyText)
        assertEquals(legacyText, decrypted)
    }

    @Test
    fun testDateEncryptAndDecrypt() {
        val originalDate = Date(1700000000000L)
        val encrypted = CryptoManager.encryptDate(originalDate)

        assertNotNull(encrypted)
        assertTrue(encrypted!!.startsWith("ENC:v1:"))

        val decryptedDate = CryptoManager.decryptDate(encrypted)
        assertNotNull(decryptedDate)
        assertEquals(originalDate.time, decryptedDate!!.time)

        // Test fallback to raw Date or Long
        assertEquals(originalDate, CryptoManager.decryptDate(originalDate))
        assertEquals(originalDate.time, CryptoManager.decryptDate(originalDate.time)?.time)
    }

    @Test
    fun testIntEncryptAndDecrypt() {
        val originalInt = 68
        val encrypted = CryptoManager.encryptInt(originalInt)

        assertNotNull(encrypted)
        val decrypted = CryptoManager.decryptInt(encrypted)
        assertEquals(originalInt, decrypted)

        // Fallback for raw int/long
        assertEquals(68, CryptoManager.decryptInt(68))
    }

    @Test
    fun testBooleanEncryptAndDecrypt() {
        val originalBool = true
        val encrypted = CryptoManager.encryptBoolean(originalBool)

        assertNotNull(encrypted)
        val decrypted = CryptoManager.decryptBoolean(encrypted)
        assertEquals(originalBool, decrypted)

        // Fallback for raw boolean
        assertEquals(true, CryptoManager.decryptBoolean(true))
    }

    @Test
    fun testStringListEncryptAndDecrypt() {
        val originalList = listOf("Cramps", "Nausea", "Spotting")
        val encrypted = CryptoManager.encryptStringList(originalList)

        assertNotNull(encrypted)
        assertTrue(encrypted!!.startsWith("ENC:v1:"))

        val decrypted = CryptoManager.decryptStringList(encrypted)
        assertEquals(originalList, decrypted)

        // Fallback for raw List
        assertEquals(originalList, CryptoManager.decryptStringList(originalList))
    }

    @Test
    fun testJsonMapListEncryptAndDecrypt() {
        val originalList: List<Map<String, Any?>> = listOf(
            mapOf("pregnancyNumber" to 1, "deliveryType" to "Vaginal", "hasGdm" to true)
        )
        val encrypted = CryptoManager.encryptJsonMapList(originalList)

        assertNotNull(encrypted)
        assertTrue(encrypted!!.startsWith("ENC:v1:"))

        val decrypted = CryptoManager.decryptJsonMapList(encrypted)
        assertNotNull(decrypted)
        assertEquals(1, decrypted!!.size)
        assertEquals(1, (decrypted[0]["pregnancyNumber"] as Number).toInt())
        assertEquals("Vaginal", decrypted[0]["deliveryType"])
        assertEquals(true, decrypted[0]["hasGdm"])
    }

    @Test
    fun testPregnancyHistoryEncryptionAndDecryption() {
        // Test first pregnancy flag encryption
        val isFirstPregnancy = false
        val encryptedIsFirst = CryptoManager.encryptBoolean(isFirstPregnancy)
        assertNotNull(encryptedIsFirst)
        assertTrue(encryptedIsFirst!!.startsWith("ENC:v1:"))
        assertEquals(false, CryptoManager.decryptBoolean(encryptedIsFirst))

        // Test past pregnancy history with complications list encryption
        val historyList: List<Map<String, Any?>> = listOf(
            mapOf(
                "pregnancyNumber" to CryptoManager.encryptInt(1),
                "deliveryType" to CryptoManager.encrypt("Cesarean (C-Section)"),
                "birthOutcome" to CryptoManager.encrypt("Live Birth"),
                "childrenDelivered" to CryptoManager.encrypt("Single"),
                "areChildrenLiving" to CryptoManager.encryptBoolean(true),
                "hasGdm" to CryptoManager.encryptBoolean(true),
                "hasPreeclampsia" to CryptoManager.encryptBoolean(true),
                "additionalComplications" to CryptoManager.encrypt("High blood pressure"),
                "complications" to CryptoManager.encryptStringList(
                    listOf(
                        "Gestational Diabetes",
                        "Preeclampsia"
                    )
                )
            )
        )

        val encryptedPayload = CryptoManager.encryptJsonMapList(historyList)
        assertNotNull(encryptedPayload)
        assertTrue(encryptedPayload!!.startsWith("ENC:v1:"))

        val decryptedMaps = CryptoManager.decryptJsonMapList(encryptedPayload)
        assertNotNull(decryptedMaps)
        assertEquals(1, decryptedMaps!!.size)

        val item = decryptedMaps[0]
        assertEquals("Cesarean (C-Section)", CryptoManager.decrypt(item["deliveryType"] as String))
        assertEquals("Live Birth", CryptoManager.decrypt(item["birthOutcome"] as String))
        assertEquals("Single", CryptoManager.decrypt(item["childrenDelivered"] as String))
        assertEquals(true, CryptoManager.decryptBoolean(item["hasGdm"]))
        assertEquals(true, CryptoManager.decryptBoolean(item["hasPreeclampsia"]))
        assertEquals(
            "High blood pressure",
            CryptoManager.decrypt(item["additionalComplications"] as String)
        )

        val complications = CryptoManager.decryptStringList(item["complications"])
        assertNotNull(complications)
        assertEquals(listOf("Gestational Diabetes", "Preeclampsia"), complications)
    }
}
