package book.account.bank_account.repository

import book.account.bank_account.entity.BankAccount
import org.springframework.data.jpa.repository.JpaRepository

interface BankAccountRepository : JpaRepository<BankAccount, String> {
}