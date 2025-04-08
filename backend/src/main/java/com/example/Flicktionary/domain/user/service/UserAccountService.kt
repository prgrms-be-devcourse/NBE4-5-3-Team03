package com.example.Flicktionary.domain.user.service

import com.example.Flicktionary.domain.review.service.ReviewService
import com.example.Flicktionary.domain.user.dto.UserAccountDto
import com.example.Flicktionary.domain.user.entity.UserAccount
import com.example.Flicktionary.domain.user.entity.UserAccountType
import com.example.Flicktionary.domain.user.repository.UserAccountRepository
import com.example.Flicktionary.global.exception.ServiceException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 회원 도메인에 해당하는 기능들을 지원하기 위한 서비스.
 */
@Service
@Transactional
class UserAccountService(
    private val userAccountRepository: UserAccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val reviewService: ReviewService
) {
    /**
     * 새 회원을 생성한 뒤 영속한다.
     *
     * @param userAccountDto 새로 생성할 회원의 정보가 담긴 DTO
     * @return 영속된 회원에 해당하는 DTO
     */
    fun registerUser(userAccountDto: UserAccountDto): UserAccountDto {
        val userAccount: UserAccount = userAccountDto.toEntity()
        userAccount.password = passwordEncoder.encode("{bcrypt}" + userAccount.password)
        return UserAccountDto.from(userAccountRepository.save(userAccount))
    }

    /**
     * 주어진 고유 ID에 해당하는 회원의 정보를 변경한다.
     *
     * @param id             정보를 변경할 회원의 고유 ID
     * @param userAccountDto 새로 변경될 회원정보
     * @return 변경된 회원에 해당하는 DTO
     */
    fun modifyUser(id: Long, userAccountDto: UserAccountDto): UserAccountDto {
        val userAccount = userAccountRepository.findById(id)
            .orElseThrow { ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 유저를 찾을 수 없습니다.") }
        userAccount.username = userAccountDto.username
        userAccount.password = passwordEncoder.encode("{bcrypt}" + userAccountDto.password)
        userAccount.email = userAccountDto.email
        userAccount.nickname = userAccountDto.nickname
        userAccount.role = UserAccountType.valueOf(userAccountDto.role)
        return UserAccountDto.from(userAccountRepository.save(userAccount))
    }

    /**
     * 주어진 고유 ID에 해당하는 회원의 닉네임을 변경한다.
     *
     * @param id             정보를 변경할 회원의 고유 ID
     * @param userAccountDto 새로 변경될 닉네임 정보
     * @return 변경된 회원에 해당하는 DTO
     */
    fun modifyNickname(id: Long, userAccountDto: UserAccountDto): UserAccountDto {
        val userAccount = userAccountRepository.findByIdOrNull(id)?:
            throw ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 유저를 찾을 수 없습니다.")
        userAccount.nickname = userAccountDto.nickname
        return UserAccountDto.from(userAccountRepository.save(userAccount))
    }

    /**
     * 주어진 고유 ID에 해당하는 회원의 비밀번호를 변경한다.
     *
     * @param id             정보를 변경할 회원의 고유 ID
     * @param userAccountDto 새로 변경될 비밀번호 정보
     * @return 변경된 회원에 해당하는 DTO
     */
    fun modifyPassword(id: Long, userAccountDto: UserAccountDto): UserAccountDto {
        val userAccount = userAccountRepository.findById(id)
            .orElseThrow { ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 유저를 찾을 수 없습니다.") }
        userAccount.password = passwordEncoder.encode("{bcrypt}" + userAccountDto.password)
        return UserAccountDto.from(userAccountRepository.save(userAccount))
    }

    /**
     * 주어진 고유 ID에 해당하는 회원을 반환한다.
     *
     * @param id 조회할 회원의 고유 ID
     * @return 조회된 회원에 해당하는 DTO
     */
    fun getUserById(id: Long): UserAccountDto {
        return UserAccountDto.from(userAccountRepository.findById(id)
            .orElseThrow { ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 유저를 찾을 수 없습니다.") })
    }

    /**
     * 주어진 고유 ID에 해당하는 회원을 삭제한다.
     *
     * @param id 삭제할 회원의 고유 ID
     * @return 삭제된 회원의 유저 ID
     */
    fun deleteUserById(id: Long): String {
        val userAccount = userAccountRepository.findById(id)
            .orElseThrow { ServiceException(HttpStatus.NOT_FOUND.value(), "${id}번 유저를 찾을 수 없습니다.") }

        // ReviewService를 통해 리뷰의 userAccount를 null로 설정
        reviewService.disassociateReviewsFromUser(userAccount)

        userAccountRepository.delete(userAccount)
        return userAccount.username
    }

    /**
     * 주어진 유저 ID에 해당하는 회원을 반환한다.
     *
     * @param username 조회할 회원의 유저 ID
     * @return 조회된 회원 오브젝트
     */
    fun getUserByUsername(username: String): UserAccount {
        return userAccountRepository.findByUsername(username)?:
            throw ServiceException(HttpStatus.NOT_FOUND.value(), "유저를 찾을 수 없습니다.")
    }
}