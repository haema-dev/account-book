package book.account.user.entity

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import java.util.*

@Entity
data class User(
    @NotNull
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: UUID,
    @NotNull
    var name: String,
    @NotNull
    @Column(unique = true)
    var email: String,
    @NotNull
    var password: String
)