package com.thg.zohodesk.service;

import com.thg.zohodesk.model.Customer;

public interface CustomerService {
    Customer getCustomerByEmail(String email);
    Customer getCustomerById(Long id);
    Customer createCustomer(Customer customer);
    Customer updateCustomer(Customer customer);
}