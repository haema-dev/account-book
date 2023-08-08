package book.account.account.entity

import book.account.account.vo.AccountType
import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
data class Account (
    @NotNull
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var bankCode: String,
    var accountNumber: String,
    var accountName: String,

    @Enumerated(EnumType.STRING) // Enum을 문자열로 저장
    var accountType: AccountType
)