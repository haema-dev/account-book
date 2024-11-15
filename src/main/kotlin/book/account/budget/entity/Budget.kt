package book.account.budget.entity

import book.account.member.entity.Member
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate


@Entity
@Table(name = "budget")
data class Budget(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var amount: BigDecimal,

    @Column(nullable = false)
    var category: String,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member
)