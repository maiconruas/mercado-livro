package com.mercadolivro.controller.request

import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

class PutCustomerRequest (

        @field:NotEmpty(message = "O nome deve ser informado")
        var name: String,

        @field:Email(message = "E-mail dever válido")
        var email: String
)
