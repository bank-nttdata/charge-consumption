package com.nttdata.bootcamp.service;

import com.nttdata.bootcamp.entity.ChargeConsumption;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//Interface Service
public interface ChargeConsumptionService {

    public Flux<ChargeConsumption> findAll();
    public Flux<ChargeConsumption> findByAccountNumber(String accountNumber);

    public Mono<ChargeConsumption> findByNumber(String number);
    public Mono<ChargeConsumption> saveChargeConsumption(ChargeConsumption balance);
    public Mono<ChargeConsumption> updateChargeConsumption(ChargeConsumption balance);
    public Mono<Void> deleteChargeConsumption(String accountNumber);

}
