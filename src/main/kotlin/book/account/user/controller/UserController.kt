package book.account.user.controller

import book.account.user.service.UserService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/users")
class UserController (private val userService: UserService) {

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: String): String {
        return userService.getUser()
    }

    @PostMapping
    fun createUser(): String {
        return userService.createUser()
    }

}