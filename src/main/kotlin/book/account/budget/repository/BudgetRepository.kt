package book.account.budget.repository

import book.account.budget.entity.Budget
import book.account.tables.references.BUDGET
import org.springframework.stereotype.Repository
import org.jooq.DSLContext


@Repository
class BudgetRepository(
    private val dslContext: DSLContext
) {
    fun findAll(): List<Budget> {
        return dslContext
            .selectFrom(BUDGET)
            .fetch()
            .into(Budget::class.java)
    }
}