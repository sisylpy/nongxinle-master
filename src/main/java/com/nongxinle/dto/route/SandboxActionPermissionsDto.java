package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SandboxActionPermissionsDto {
    private boolean canConfirmCustomer = true;
    private boolean canPrintBill;
    private boolean canDepart;
}
