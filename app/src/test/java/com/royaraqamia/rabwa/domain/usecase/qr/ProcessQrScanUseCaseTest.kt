package com.royaraqamia.rabwa.domain.usecase.qr

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.qr.QrContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProcessQrScanUseCaseTest {

    private lateinit var useCase: ProcessQrScanUseCase

    @Before
    fun setUp() {
        useCase = ProcessQrScanUseCase()
    }

    @Test
    fun `invoke with null or blank payload returns validation failure`() {
        val nullResult = useCase(null)
        assertTrue(nullResult is AppResult.Failure)

        val emptyResult = useCase("")
        assertTrue(emptyResult is AppResult.Failure)

        val whitespaceResult = useCase("   ")
        assertTrue(whitespaceResult is AppResult.Failure)
    }

    @Test
    fun `invoke with valid web url parses as URL content type`() {
        val payload = "https://example.com/item/123"
        val result = useCase(payload)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(QrContentType.URL, data.contentType)
        assertEquals(payload, data.sanitizedContent)
        assertTrue(data.isUrl)
    }

    @Test
    fun `invoke with http url parses correctly`() {
        val payload = "http://my-site.org"
        val result = useCase(payload)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(QrContentType.URL, data.contentType)
        assertTrue(data.isUrl)
    }

    @Test
    fun `invoke with wifi config parses as WIFI content type`() {
        val payload = "WIFI:S:MyNetwork;T:WPA;P:secretPass;;"
        val result = useCase(payload)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(QrContentType.WIFI, data.contentType)
        assertFalse(data.isUrl)
    }

    @Test
    fun `invoke with email parses as EMAIL content type`() {
        val payload = "mailto:support@company.com"
        val result = useCase(payload)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(QrContentType.EMAIL, data.contentType)
    }

    @Test
    fun `invoke with phone parses as PHONE content type`() {
        val payload = "tel:+1234567890"
        val result = useCase(payload)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(QrContentType.PHONE, data.contentType)
    }

    @Test
    fun `invoke with geo parses as GEO content type`() {
        val payload = "geo:37.7749,-122.4194"
        val result = useCase(payload)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(QrContentType.GEO, data.contentType)
    }

    @Test
    fun `invoke with plain text parses as TEXT content type`() {
        val payload = "مرحبا بالعالم - نص تجريبي"
        val result = useCase(payload)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(QrContentType.TEXT, data.contentType)
        assertEquals("مرحبا بالعالم - نص تجريبي", data.sanitizedContent)
    }

    @Test
    fun `invoke with www prefix parses as URL and formats actionUrl`() {
        val payload = "www.google.com"
        val result = useCase(payload)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(QrContentType.URL, data.contentType)
        assertTrue(data.isUrl)
        assertEquals("https://www.google.com", data.actionUrl)
    }

    @Test
    fun `invoke with bare international phone number parses as PHONE`() {
        val payload = "+966500000000"
        val result = useCase(payload)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals(QrContentType.PHONE, data.contentType)
        assertTrue(data.isPhone)
    }

    @Test
    fun `invoke sanitizes illegal control chars and null bytes`() {
        val raw = "https://safe.com/\u0000\u0001\u0007test"
        val result = useCase(raw)

        assertTrue(result is AppResult.Success)
        val data = (result as AppResult.Success).data
        assertEquals("https://safe.com/test", data.sanitizedContent)
    }
}
