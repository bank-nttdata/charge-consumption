package com.nttdata.bootcamp.events;

import com.nttdata.bootcamp.entity.ChargeConsumption;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChargeConsumptionCreatedEventKafka extends EventKafka<ChargeConsumption> {

}
