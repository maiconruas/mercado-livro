package com.mercadolivro.service

import com.mercadolivro.enums.CostumerStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.helper.buildCustomer
import com.mercadolivro.repositoy.CustomerRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

@ExtendWith(MockKExtension::class)
class CustomerServiceTest {
    @MockK
    private lateinit var customerRepository: CustomerRepository

    @MockK
    private lateinit var bookService: BookService

    @MockK
    private lateinit var bCrypt: BCryptPasswordEncoder

    @InjectMockKs
    @SpyK
    private lateinit var customerService: CustomerService

    @Test
    fun `should return all customers`() {
        val fakeCustomers = listOf(buildCustomer(), buildCustomer())

        every { customerRepository.findAll() } returns fakeCustomers

        val customers = customerService.getAll(null)

        assertEquals(fakeCustomers, customers)
        verify( exactly = 1 ) { customerRepository.findAll()}
        verify( exactly = 0 ) { customerRepository.findByNameContaining(any()) }
    }


    @Test
    fun `should return customers when name is informed`() {
        val name = UUID.randomUUID().toString()
        val fakeCustomers = listOf(buildCustomer(), buildCustomer())

        every { customerRepository.findByNameContaining(name) } returns fakeCustomers

        val customers = customerService.getAll(name)

        assertEquals(fakeCustomers, customers)
        verify( exactly = 0 ) { customerRepository.findAll()}
        verify( exactly = 1 ) { customerRepository.findByNameContaining(any()) }
    }

    @Test
    fun `should create customer and encrypt password`(){
        val initialPassword = Random().nextInt().toString()
        val fakeCustomers = buildCustomer(password = initialPassword)
        val fakePassword = UUID.randomUUID().toString()
        val fakeCustomerEncrypted = fakeCustomers.copy(password = fakePassword)

        every { customerRepository.save(fakeCustomerEncrypted) } returns fakeCustomers
        every { bCrypt.encode(initialPassword) } returns fakePassword

        customerService.create(fakeCustomers)

        verify( exactly = 1 ) { customerRepository.save(fakeCustomerEncrypted)}
        verify( exactly = 1 ) { bCrypt.encode(initialPassword)}
    }


    @Test
    fun`sholud return customer by id`(){
        val id = Random().nextInt()
        val fakecustomer = buildCustomer(id = id)

        every { customerRepository.findById(id) } returns Optional.of(fakecustomer)

        val customer = customerService.findById(id)

        assertEquals(fakecustomer, customer)
        verify( exactly = 1 ) { customerRepository.findById(id)}
    }

    @Test
    fun`should throw not found when find by id`(){
        val id = Random().nextInt()

        every { customerRepository.findById(id) } returns Optional.empty()

        val error = assertThrows<NotFoundException>{customerService.findById(id)}

        assertEquals("Customer [${id}] not exists", error.message)
        assertEquals("ML-201", error.errorCode)
        verify( exactly = 1 ) { customerRepository.findById(id)}
    }


    @Test
    fun`should update customer`(){
        val id = Random().nextInt()
        val fakecustomer = buildCustomer(id = id)

        every { customerRepository.existsById(id) } returns true
        every { customerRepository.save(fakecustomer) } returns fakecustomer

        customerService.update(fakecustomer)

        verify( exactly = 1 ) { customerRepository.existsById(id)}
        verify( exactly = 1 ) { customerRepository.save(fakecustomer)}
    }


    @Test
    fun`should throw not found exception when update customer`(){
        val id = Random().nextInt()
        val fakecustomer = buildCustomer(id = id)

        every { customerRepository.existsById(id) } returns false
        every { customerRepository.save(fakecustomer) } returns fakecustomer

        val error = assertThrows<NotFoundException>{customerService.update(fakecustomer)}

        assertEquals("Customer [${id}] not exists", error.message)
        assertEquals("ML-201", error.errorCode)

        verify( exactly = 1 ) { customerRepository.existsById(id)}
        verify( exactly = 0 ) { customerRepository.save(any())}
    }

    @Test
    fun`should delete customer`(){
        val id = Random().nextInt()
        val fakecustomer = buildCustomer(id = id)
        val expectedCustomer = fakecustomer.copy(status = CostumerStatus.INATIVO)


        every { customerService.findById(id) } returns fakecustomer
        every { customerRepository.save(expectedCustomer) } returns expectedCustomer
        every { bookService.deleteByCustomer(fakecustomer) } just runs

        customerService.delete(id)

        verify( exactly = 1 ) { customerService.findById(id)}
        verify( exactly = 1 ) { bookService.deleteByCustomer(fakecustomer)}
        verify( exactly = 1 ) { customerRepository.save(expectedCustomer)}

    }


    @Test
    fun`should throw not found exception when delete customer`(){
        val id = Random().nextInt()

        every { customerService.findById(id) } throws NotFoundException(Errors.ML201.message.format(id), Errors.ML201.code)

        val error = assertThrows<NotFoundException>{customerService.delete(id)}

        assertEquals("Customer [${id}] not exists", error.message)
        assertEquals("ML-201", error.errorCode)

        verify( exactly = 1 ) { customerService.findById(id)}
        verify( exactly = 0 ) { bookService.deleteByCustomer(any())}
        verify( exactly = 0 ) { customerRepository.save(any())}

    }


    @Test
    fun `should return false when email unavailable`(){
        val email = "${Random().nextInt()}@email.com"

        every { customerRepository.existsByEmail(email) } returns true

        val emailEvailable = customerService.emailAvailable(email)

        assertFalse(emailEvailable)
        verify( exactly = 1 ) { customerRepository.existsByEmail(email)}
    }


    @Test
    fun `should return true when email available`(){
        val email = "${Random().nextInt()}@email.com"

        every { customerRepository.existsByEmail(email) } returns false

        val emailEvailable = customerService.emailAvailable(email)

        assertTrue(emailEvailable)
        verify( exactly = 1 ) { customerRepository.existsByEmail(email)}
    }

}
