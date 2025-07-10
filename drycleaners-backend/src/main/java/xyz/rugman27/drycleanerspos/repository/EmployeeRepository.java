package xyz.rugman27.drycleanerspos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.rugman27.drycleanerspos.model.EmployeeModel;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeModel, String> {
    EmployeeModel findTopByIdStartingWithOrderByIdDesc(String prefix);
    EmployeeModel findByUsername(String username);
    List<EmployeeModel> findByLastNameContainingIgnoreCase(String lastName);
    EmployeeModel findEmployeeModelByPhotoUuid(UUID photoUuid);

}

