package book.account.bank_account.vo


enum class BankAccountType {
    CHECKING, //일반 저축
    DEPOSIT, //적금 및 예금
    RETIREMENT, //퇴직
    INVESTMENT, //투자
    OVERDRAFT //마이너스
}