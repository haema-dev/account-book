package book.account.budget.controller

import book.account.budget.service.BudgetService
import org.springframework.web.bind.annotation.RestController


@RestController
class BudgetController(private val budgetService: BudgetService) {



}