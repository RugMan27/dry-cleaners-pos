package xyz.rugman27.drycleanerspos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import xyz.rugman27.drycleanerspos.model.InvoiceModel;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceModel, String>, JpaSpecificationExecutor<InvoiceModel> {
    InvoiceModel findTopByIdStartingWithOrderByIdDesc(String prefix);



}
