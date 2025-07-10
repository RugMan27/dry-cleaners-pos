package xyz.rugman27.drycleanerspos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import xyz.rugman27.drycleanerspos.model.EmployeeModel;
import xyz.rugman27.drycleanerspos.repository.EmployeeRepository;
import xyz.rugman27.drycleanerspos.utilites.JsonUtils;
import xyz.rugman27.drycleanerspos.utilites.PhoneNumberUtil;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public EmployeeModel saveEmployee(EmployeeModel employee) {
        employee.setPhone(PhoneNumberUtil.normalizeForStorage(employee.getPhone()));
        System.out.println("PICTURE = " + employee.getProfilePic());
        try {
            EmployeeModel employeeModel = employeeRepository.save(employee);
            employeeRepository.flush();
            System.out.println("EmployeeModel = " + JsonUtils.toJson(employeeModel));
            return employeeModel;
        } catch (DataIntegrityViolationException ex) {
            System.err.println(ex.getMessage());
            System.err.println("HAD TO DISABLE EMPLOYEE");
            employee.setEnabled(false);
            employee.setUsername(employee.getId()); // fallback username
            EmployeeModel fallbackModel = employeeRepository.save(employee);
            employeeRepository.flush();
            return fallbackModel;
        }
    }


    public EmployeeModel getEmployeeById(String id) {
        return employeeRepository.findById(id).orElse(null);
    }

    public EmployeeModel getEmployeeByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }

    public boolean deleteEmployeeById(String id) {
        if (!employeeRepository.existsById(id)) return false;
        employeeRepository.deleteById(id);
        return true;
    }

    public List<EmployeeModel> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public List<EmployeeModel> searchEmployeeByLastName(String lastName) {
        return employeeRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    public String generateEmployeeId(String lastName) {
        String prefix = "E" + lastName.substring(0, 1).toUpperCase();
        EmployeeModel lastEmployee = employeeRepository.findTopByIdStartingWithOrderByIdDesc(prefix);
        int nextNum = 1;

        if (lastEmployee != null) {
            String numericPart = lastEmployee.getId().substring(2);
            nextNum = Integer.parseInt(numericPart) + 1;
        }

        return String.format("%s%05d", prefix, nextNum);
    }

    public EmployeeModel createEmployeeFromLastName(String lastName, String firstName, String password) {
        String id = generateEmployeeId(lastName);
        String capitalized = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();
        String username = (capitalized + firstName.charAt(0)).toLowerCase();

        EmployeeModel employee = new EmployeeModel(id, capitalized, username, password);
        employee.setEmployeeType(EmployeeModel.EmployeeType.EMPLOYEE);
        return saveEmployee(employee);
    }

    public EmployeeModel getEmployeeByPhotoUuid(String photoUuid) {
        return employeeRepository.findEmployeeModelByPhotoUuid(UUID.fromString(photoUuid));
    }
}
