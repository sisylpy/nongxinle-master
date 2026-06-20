package com.nongxinle.dto.platform;

import com.nongxinle.entity.NxDepartmentOrdersEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
public class BillCreationPartition {

    private List<NxDepartmentOrdersEntity> legacyCreatableLines = new ArrayList<>();
    private List<NxDepartmentOrdersEntity> platformCashLines = new ArrayList<>();
    private Set<Integer> platformCashBillIds = new LinkedHashSet<>();
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public Integer getSinglePlatformCashBillId() {
        return platformCashBillIds.size() == 1 ? platformCashBillIds.iterator().next() : null;
    }
}
