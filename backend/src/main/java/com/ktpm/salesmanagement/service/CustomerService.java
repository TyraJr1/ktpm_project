package com.yourproject.service;

import com.yourproject.entity.Customer;
import com.yourproject.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer updatedData) {
        Customer existing = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        
        existing.setName(updatedData.getName());
        existing.setPhone(updatedData.getPhone());
        existing.setEmail(updatedData.getEmail());
        existing.setAddress(updatedData.getAddress());
        
        return customerRepository.save(existing);
    }
}