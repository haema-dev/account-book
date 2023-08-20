package book.account.member.service

import book.account.common.domain.Domain
import book.account.common.exception.DuplicateNotAllowException
import book.account.common.exception.NotFoundException
import book.account.member.entity.Member
import book.account.member.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberService (private val memberRepository: MemberRepository) {

    fun getUser(memberId: Long): Member? {
        return memberRepository.findById(memberId).orElseThrow {
            NotFoundException(Domain.USER, " with ID $memberId not found")
        }
    }

    fun createUser(member: Member): Member {
        // 중복 확인, 예를 들어 email을 기준으로
        memberRepository.findByEmail(member.email)?.let {
            throw DuplicateNotAllowException(Domain.USER, " with username ${member.email} already exists")
        }

        return memberRepository.save(member)
    }

}