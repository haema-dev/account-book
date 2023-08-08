package book.account.user.service

import book.account.common.domain.Domain
import book.account.common.exception.DuplicateNotAllowException
import book.account.common.exception.NotFoundException
import book.account.user.entity.User
import book.account.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService (private val userRepository: UserRepository) {

    fun getUser(userId: UUID): User? {
        return userRepository.findById(userId).orElseThrow {
            NotFoundException(Domain.USER, " with ID $userId not found")
        }
    }

    fun createUser(user: User): User {
        // 중복 확인, 예를 들어 email을 기준으로
        userRepository.findByEmail(user.email)?.let {
            throw DuplicateNotAllowException(Domain.USER, " with username ${user.email} already exists")
        }

        user.id = UUID.randomUUID()
        return userRepository.save(user)
    }

}