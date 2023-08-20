package book.account.member.controller

import book.account.member.entity.Member
import book.account.member.service.MemberService
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/users")
@Tag(name = "Member", description = "the Member API")
class MemberController (private val memberService: MemberService) {

    @GetMapping("/{memberId}")
    fun getUser(@PathVariable memberId: Long): Member? {
        return memberService.getUser(memberId)
    }

    @PostMapping
    fun createUser(@RequestBody @Parameter(description = "Member 생성 요청") member: Member): Member {
        return memberService.createUser(member)
    }

}