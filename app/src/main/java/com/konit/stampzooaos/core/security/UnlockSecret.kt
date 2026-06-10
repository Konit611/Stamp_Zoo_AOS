package com.konit.stampzooaos.core.security

import java.security.MessageDigest

/**
 * 경품 응모 완료 상태를 푸는 숨김 기능용 비밀번호 검증. iOS UnlockSecret와 동일.
 * 평문 비밀번호는 코드/커밋 어디에도 두지 않고 SHA-256 해시만 보관한다.
 */
object UnlockSecret {
    /** 해제 비밀번호의 SHA-256 해시 (소문자 16진수). 평문은 저장하지 않음. */
    private const val PASSWORD_HASH =
        "1a0a6536c7a9bef85c8ec91a76c944028238ce1f194e2ca507235986c64ec9a6"

    /** 입력값의 SHA-256이 저장된 해시와 일치하는지 검사. */
    fun verify(input: String): Boolean {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { "%02x".format(it) }
        return hex == PASSWORD_HASH
    }
}
