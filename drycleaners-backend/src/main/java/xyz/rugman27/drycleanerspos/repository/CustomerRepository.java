package xyz.rugman27.drycleanerspos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.rugman27.drycleanerspos.model.CustomerModel;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerModel, String> {
    CustomerModel findTopByIdStartingWithOrderByIdDesc(String prefix);
    List<CustomerModel> findByLastNameContainingIgnoreCase(String lastName);
    List<CustomerModel> findByFirstNameContainingIgnoreCase(String firstName);
    List<CustomerModel> findByPhoneContaining(String phone);
    List<CustomerModel> findByEmailContainingIgnoreCase(String email);
}

