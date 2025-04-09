package com.example.Flicktionary.global.utils

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals

@DisplayName("UUID 유틸리티 클래스 테스트")
class UuidUtilsTest {

    @DisplayName("UUIDv4가 주어졌을때 24글자 길이의 base64 인코딩된 문자열로 변환된다.")
    @Test
    fun isUuidCorrectlyConvertedToBase64String() {
        val uuidString = UuidUtils.uuidV4ToBase64String(UUID.randomUUID());
        assertEquals(24, uuidString.length);
        // base64 인코딩시 패딩 문자 확인
        assertEquals("==", uuidString.substring(22));
    }
}