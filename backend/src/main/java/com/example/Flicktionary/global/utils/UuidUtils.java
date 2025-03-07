package com.example.Flicktionary.global.utils;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

/**
 * UUID 관련 유틸리티 메소드를 담고 있는 클래스.
 */
public class UuidUtils {
    /**
     * 주어진 UUID를 base64로 인코딩한다.
     * @param uuid 인코딩할 UUIDv4
     * @return base64로 인코딩된 문자열
     */
    public static String uuidV4ToBase64String(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        byteBuffer.putLong(uuid.getMostSignificantBits());
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }
}
