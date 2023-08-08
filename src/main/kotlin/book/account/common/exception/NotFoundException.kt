package book.account.common.exception

import book.account.common.domain.Domain

class NotFoundException(domain: Domain, details: String)
    : RuntimeException("$domain not found: $details")