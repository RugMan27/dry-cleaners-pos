package xyz.rugman27.drycleanerspos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.rugman27.drycleanerspos.data.ServiceType;
import xyz.rugman27.drycleanerspos.model.CustomerModel;
import xyz.rugman27.drycleanerspos.model.PresetItemModel;
import xyz.rugman27.drycleanerspos.repository.PresetItemRepository;
import xyz.rugman27.drycleanerspos.utilites.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.List;

@Service
public class PresetItemService {

    @Autowired
    private PresetItemRepository presetItemRepository;

    public PresetItemModel savePresetItem(PresetItemModel presetItem) {
        return presetItemRepository.save(presetItem);
    }

    public boolean deleteByCode(String code) {
        if (!presetItemRepository.existsById(code)) return false;
        presetItemRepository.deleteById(code);
        return true;
    }

    public PresetItemModel getByCode(String code) {
        return presetItemRepository.findById(code).orElse(null);
    }

    public List<PresetItemModel> getAll() {
        return presetItemRepository.findAll();
    }
    public List<PresetItemModel> getAllByServiceType(ServiceType serviceType) {
        List<PresetItemModel> allPresetItemModels = presetItemRepository.findAll();
        List<PresetItemModel> finalPresetItemModels = new ArrayList<>();
        for (PresetItemModel presetItemModel : allPresetItemModels) {
            if (presetItemModel.getServiceType().equals(serviceType)) {
                finalPresetItemModels.add(presetItemModel);
            }
        }
        return finalPresetItemModels;
    }
    public List<PresetItemModel> searchByName(String name) {
        return presetItemRepository.findByNameContainingIgnoreCase(name);
    }
}
