package book.account.account.repository

import book.account.account.entity.Account
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<Account, String> {
}