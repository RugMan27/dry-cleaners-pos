package xyz.rugman27.drycleanerspos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.rugman27.drycleanerspos.model.CustomerModel;
import xyz.rugman27.drycleanerspos.repository.CustomerRepository;
import xyz.rugman27.drycleanerspos.utilites.PhoneNumberUtil;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public CustomerModel saveCustomer(CustomerModel customer) {
        customer.setPhone(PhoneNumberUtil.normalizeForStorage(customer.getPhone()));
        return customerRepository.save(customer);
    }

    public boolean deleteCustomerById(String id) {
        if (!customerRepository.existsById(id)) return false;
        customerRepository.deleteById(id);
        return true;
    }

    public CustomerModel getCustomerById(String id) {
        return customerRepository.findById(id).orElse(null);
    }

    public List<CustomerModel> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<CustomerModel> searchByFirstName(String firstName) {
        return customerRepository.findByFirstNameContainingIgnoreCase(firstName);
    }

    public List<CustomerModel> searchByLastName(String lastName) {
        return customerRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    public List<CustomerModel> searchByPhone(String phone) {
        return customerRepository.findByPhoneContaining(phone);
    }

    public List<CustomerModel> searchByEmail(String email) {
        return customerRepository.findByEmailContainingIgnoreCase(email);
    }

    public String generateCustomerId(String lastName) {
        String prefix = lastName.substring(0, 1).toUpperCase();
        CustomerModel lastCustomer = customerRepository.findTopByIdStartingWithOrderByIdDesc(prefix);
        int nextNum = 1;

        if (lastCustomer != null) {
            String numericPart = lastCustomer.getId().substring(1);
            nextNum = Integer.parseInt(numericPart) + 1;
        }

        return String.format("%s%05d", prefix, nextNum);
    }

    public CustomerModel createCustomerFromLastName(String lastName) {
        String id = generateCustomerId(lastName);
        String capitalized = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();
        CustomerModel customer = new CustomerModel(id, capitalized);
        return saveCustomer(customer);
    }
}
