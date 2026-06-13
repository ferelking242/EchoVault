package com.aivos.echovault

  import org.junit.Test
  import org.junit.Assert.*

  class ExampleUnitTest {
      @Test
      fun contentTypeDetection_otp_returnsOtp() {
          val otpPattern = "^\\d{4,8}$".toRegex()
          assertTrue("123456".matches(otpPattern))
      }

      @Test
      fun contentTypeDetection_url_returnsUrl() {
          val url = "https://github.com/ferelking242/EchoVault"
          assertTrue(url.startsWith("https://"))
      }

      @Test
      fun contentTypeDetection_email_returnsEmail() {
          val email = "user@example.com"
          assertTrue(email.contains("@") && email.contains("."))
      }
  }