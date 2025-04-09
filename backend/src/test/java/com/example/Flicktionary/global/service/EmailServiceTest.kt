package com.example.Flicktionary.global.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.BDDMockito.then
import org.mockito.Mockito.any
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.util.ReflectionTestUtils
import kotlin.test.Test
import kotlin.test.assertEquals

@DisplayName("이메일 서비스 테스트")
@ExtendWith(MockitoExtension::class)
class EmailServiceTest {

    private val testSender = "sender@email.com"

    /** 자바의 nullable 타입과 코틀린의 non-null 타입을 중재하는 헬퍼 함수
     * 참고:
     * https://kotlinlang.org/docs/java-interop.html#null-safety-and-platform-types
     * https://kotlinlang.org/docs/null-safety.html#not-null-assertion-operator
     */
    private fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

    @Mock
    private lateinit var javaMailSender: JavaMailSender

    @InjectMocks
    private lateinit var emailService: EmailService

    @BeforeEach
    fun injectAutowiredValues() {
        ReflectionTestUtils.setField(
            emailService,
            "emailSenderAddress",
            testSender
        )
    }

    @DisplayName("수신자의 이메일 주소와 이메일의 제목/본문이 주어졌을때, 텍스트 기반의 이메일을 발송한다.")
    @Test
    fun givenRecipientAndSimpleEmailContent_whenSendingSimpleEmail_thenSendEmailWithContent() {
        val to = "test@email.com"
        val subject = "testSubject"
        val content = "testContent"
        val captor = ArgumentCaptor.forClass(SimpleMailMessage::class.java)
        doNothing().`when`(javaMailSender).send(capture(captor))

        emailService.sendSimpleEmail(to, subject, content)
        val captured = captor.value

        assertThat(captured).isNotNull()
        assertEquals(testSender, captured.from)
        assertEquals(to, captured.to?.get(0) ?: "수신인 주소가 비어있습니다.")
        assertEquals(subject, captured.subject)
        assertEquals(content, captured.text)
        then(javaMailSender).should().send(any(SimpleMailMessage::class.java))
    }
}