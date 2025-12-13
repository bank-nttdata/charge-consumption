package com.nttdata.bootcamp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "charge-consumption")
public class ChargeConsumption {

    @Id
    private String id;

    private String dni;
    private String accountNumber;
    private String chargeNumber;
    private Double amount;

    private String typeAccount;
    private String status;
    private Double commission;

    private Date creationDate;
    private Date modificationDate;
}
