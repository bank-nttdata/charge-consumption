package com.nttdata.bootcamp.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChargeConsumptionDto {
    private String dni;
    private String accountNumber;
    private String chargeNumber;
    private Double amount;
}
