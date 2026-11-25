package com.um.carpool.feature.auth

import com.um.carpool.feature.auth.domain.model.CampusCatalog
import com.um.carpool.feature.auth.domain.validation.AuthValidator
import com.um.carpool.feature.auth.domain.validation.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun `valid siswa email passes domain validation`() {
        val result = AuthValidator.validateSiswaEmail("u2105541@siswa.um.edu.my")
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun `valid siswa email with uppercase and surrounding whitespace passes`() {
        val result = AuthValidator.validateSiswaEmail("  U2105541.Student@SISWA.UM.EDU.MY  ")
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun `gmail address fails strict siswa domain validation`() {
        val result = AuthValidator.validateSiswaEmail("student@gmail.com")
        assertTrue(result is ValidationResult.Invalid)
        assertNotNull(result.errorMessageOrNull)
    }

    @Test
    fun `staff um email without siswa subdomain is rejected per Role 1 rule`() {
        val result = AuthValidator.validateSiswaEmail("lecturer@um.edu.my")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `spoofed suffix domain is rejected`() {
        val result = AuthValidator.validateSiswaEmail("attacker@fakesiswa.um.edu.my")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `missing local username before domain is rejected`() {
        val result = AuthValidator.validateSiswaEmail("@siswa.um.edu.my")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `local username with spaces or consecutive dots is rejected`() {
        assertTrue(AuthValidator.validateSiswaEmail("john doe@siswa.um.edu.my") is ValidationResult.Invalid)
        assertTrue(AuthValidator.validateSiswaEmail("john..doe@siswa.um.edu.my") is ValidationResult.Invalid)
    }

    @Test
    fun `seat capacity validation enforces 1 to 6 bounds`() {
        assertEquals(ValidationResult.Valid, AuthValidator.validateSeatCapacity(1))
        assertEquals(ValidationResult.Valid, AuthValidator.validateSeatCapacity(6))
        assertTrue(AuthValidator.validateSeatCapacity(0) is ValidationResult.Invalid)
        assertTrue(AuthValidator.validateSeatCapacity(7) is ValidationResult.Invalid)
    }

    @Test
    fun `faculty and residential zone catalogs match specification`() {
        assertEquals(listOf("FSKTM", "Engineering", "Law", "Science", "Business"), CampusCatalog.FACULTIES)
        assertTrue("KK1" in CampusCatalog.RESIDENTIAL_ZONES)
        assertTrue("KK12" in CampusCatalog.RESIDENTIAL_ZONES)
        assertTrue("Section 17" in CampusCatalog.RESIDENTIAL_ZONES)
        assertTrue("Bangsar South" in CampusCatalog.RESIDENTIAL_ZONES)
    }

    @Test
    fun `license plate validation requires letters and digits`() {
        assertEquals(ValidationResult.Valid, AuthValidator.validateLicensePlate("VAB 1234"))
        assertTrue(AuthValidator.validateLicensePlate("ONLYLETTERS") is ValidationResult.Invalid)
        assertTrue(AuthValidator.validateLicensePlate("12345") is ValidationResult.Invalid)
    }
}
