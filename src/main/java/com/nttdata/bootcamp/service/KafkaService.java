package com.nttdata.bootcamp.service;

import com.nttdata.bootcamp.entity.ChargeConsumption;
import com.nttdata.bootcamp.entity.enums.EventType;
import reactor.core.publisher.Mono;

public interface KafkaService {
    Mono<Void> publish(ChargeConsumption chargeConsumption,EventType eventTyp);
}
