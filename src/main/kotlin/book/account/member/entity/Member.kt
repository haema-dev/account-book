package book.account.member.entity

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
data class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @NotNull
    var name: String,
    @NotNull
    @Column(unique = true)
    var email: String,
    @NotNull
    var password: String
)