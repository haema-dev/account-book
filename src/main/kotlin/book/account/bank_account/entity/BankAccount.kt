package book.account.bank_account.entity

import book.account.bank_account.vo.BankAccountType
import jakarta.persistence.*

@Entity
data class BankAccount (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var bankCode: Long? = null,
    var accountNumber: String,
    var accountName: String,

    @Enumerated(EnumType.STRING) // Enum을 문자열로 저장
    var bankAccountType: BankAccountType
)