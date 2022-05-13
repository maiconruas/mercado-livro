package com.mercadolivro.controller.response

import com.mercadolivro.enums.CostumerStatus

data class CustomerResponse(

    var id: Int? = null,

    var name: String,

    var email: String,

    var status: CostumerStatus
)
