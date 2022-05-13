package com.mercadolivro.service

import com.mercadolivro.enums.CostumerStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.enums.Role
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repositoy.CustomerRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val bookService: BookService,
    private val bCrypt: BCryptPasswordEncoder
) {

    //LISTAR
    fun getAll(name: String?): List<CustomerModel> {
        name?.let { return customerRepository.findByNameContaining(it) }
        return customerRepository.findAll().toList()
    }

    //CADASTRAR
    fun create(customer: CustomerModel) {
        val customerCopy = customer.copy(
                roles = setOf(Role.CUSTOMER),
                password = bCrypt.encode(customer.password)
        )
        customerRepository.save(customerCopy)
    }

    //LISTAR POR ID
    fun findById(id: Int): CustomerModel {
        return customerRepository.findById(id).orElseThrow{ NotFoundException(Errors.ML201.message.format(id), Errors.ML201.code) }
    }

    //ATUALIZAR
    fun update(customer: CustomerModel) {
        if (!customerRepository.existsById(customer.id!!)) {
            throw NotFoundException(Errors.ML201.message.format(customer.id), Errors.ML201.code)
        }
        customerRepository.save(customer)
    }

    //DELETAR
    fun delete(id: Int) {
       val customer = findById(id)
        bookService.deleteByCustomer(customer)

        customer.status = CostumerStatus.INATIVO
        customerRepository.save(customer)
    }

    fun emailAvailable(email: String): Boolean {
       return !customerRepository.existsByEmail(email)
    }
}
