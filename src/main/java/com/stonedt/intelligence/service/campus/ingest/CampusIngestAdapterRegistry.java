package com.stonedt.intelligence.service.campus.ingest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CampusIngestAdapterRegistry {

    private final Map<String, CampusIngestAdapter> adapters = new HashMap<>();

    public CampusIngestAdapterRegistry(List<CampusIngestAdapter> adapterList) {
        for (CampusIngestAdapter adapter : adapterList) {
            adapters.put(adapter.adapterType(), adapter);
        }
    }

    public CampusIngestAdapter getAdapter(String adapterType) {
        if (StringUtils.isBlank(adapterType)) {
            throw new IllegalArgumentException("接入适配器类型不能为空");
        }
        CampusIngestAdapter adapter = adapters.get(adapterType);
        if (adapter == null) {
            throw new IllegalArgumentException("接入适配器未注册: " + adapterType);
        }
        return adapter;
    }
}
