package book.account.common.exception

import book.account.common.domain.Domain

class DuplicateNotAllowException(domain: Domain, details: String)
    : RuntimeException("$domain duplicate: $details")