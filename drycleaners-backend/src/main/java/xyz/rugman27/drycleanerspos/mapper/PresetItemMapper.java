package xyz.rugman27.drycleanerspos.mapper;

import xyz.rugman27.drycleanerspos.dto.PresetItemRequest;
import xyz.rugman27.drycleanerspos.model.PresetItemModel;
import xyz.rugman27.drycleanerspos.utilites.MergeUtils;

public class PresetItemMapper {

    public static PresetItemModel fromRequest(PresetItemRequest request){
        PresetItemModel model = new PresetItemModel();
        model.setCode(request.getCode());
        model.setName(request.getName());
        model.setEnabled(true);
        model.setDefaultPrice(request.getDefaultPrice());
        model.setServiceType(request.getServiceType());
        model.setServiceType(request.getServiceType());
        model.setManagerPrice(request.getManagerPrice());
        return model;
    }
    public static void updateFromRequest(PresetItemModel presetItemModel, PresetItemRequest request){
        MergeUtils.mergeNonNullFields(request,presetItemModel);
    }
}
