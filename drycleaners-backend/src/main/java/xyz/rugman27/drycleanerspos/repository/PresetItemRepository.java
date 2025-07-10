package xyz.rugman27.drycleanerspos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.rugman27.drycleanerspos.model.PresetItemModel;

import java.util.List;

@Repository
public interface PresetItemRepository extends JpaRepository<PresetItemModel, String> {
    List<PresetItemModel> findByNameContainingIgnoreCase(String lastName);

}
