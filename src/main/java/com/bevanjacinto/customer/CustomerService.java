package com.bevanjacinto.customer;

import com.bevanjacinto.exception.DuplicateResourceException;
import com.bevanjacinto.exception.RequestValidationException;
import com.bevanjacinto.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerDao customerDao;

    public CustomerService(@Qualifier("jdbc") CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public List<Customer> getAllCustomers() {
        return customerDao.selectAllCustomers();
    }

    public Customer getCustomer(Long id) {
        return customerDao.selectCustomerById(id).orElseThrow(() -> new ResourceNotFoundException(
                "customer with id [%s] not found".formatted(id)
        ));
    }

    public void addCustomer(CustomerRegistrationRequest customerRegistrationRequest){
        String email = customerRegistrationRequest.email();
        if(customerDao.existsPersonWithEmail(email)){
            throw new DuplicateResourceException("Email already taken");
        }
        customerDao.insertCustomer(
                new Customer(
                        customerRegistrationRequest.name(),customerRegistrationRequest.email(),customerRegistrationRequest.age()
                )
        );
    }

    public void deleteCustomerById(Long id){

        if(!customerDao.existsPersonWithId(id)){
            throw new ResourceNotFoundException(
                    "customer with id [%s] not found".formatted(id)
            );
        }
        customerDao.deleteCustomer(id);
    }

    public void updateCustomerById(Long id, CustomerUpdateRequest request){
        Customer customer = getCustomer(id);
        boolean changes = false;

        if(request.name() != null && !request.name().equals(customer.getName())){
            customer.setName(request.name());
            changes = true;
        }

        if(request.age() != null && !request.age().equals(customer.getAge())){
            customer.setAge(request.age());
            changes = true;
        }

        if (request.email() != null && !request.email().equals(customer.getEmail())) {
            if(customerDao.existsPersonWithEmail(request.email())){
                throw new DuplicateResourceException("email already taken");

            }
            customer.setEmail(request.email());
            changes = true;
        }

        if(!changes){
            throw new RequestValidationException("no data changes found");
        }

        customerDao.updateCustomer(customer);
    }
}
