package com.example.Flicktionary.global.utils

import java.nio.ByteBuffer
import java.util.*

/**
 * UUID 관련 유틸리티 메소드를 담고 있는 클래스.
 */
object UuidUtils {
    /**
     * 주어진 UUID를 base64로 인코딩한다.
     * @param uuid 인코딩할 UUIDv4
     * @return base64로 인코딩된 문자열
     */
    @JvmStatic
    fun uuidV4ToBase64String(uuid: UUID): String {
        val byteBuffer = ByteBuffer.wrap(ByteArray(16))
        byteBuffer.putLong(uuid.leastSignificantBits)
        byteBuffer.putLong(uuid.mostSignificantBits)
        return Base64.getEncoder().encodeToString(byteBuffer.array())
    }
}