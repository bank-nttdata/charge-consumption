package com.nttdata.bootcamp.service.impl;

import com.nttdata.bootcamp.entity.ChargeConsumption;
import com.nttdata.bootcamp.entity.enums.EventType;
import com.nttdata.bootcamp.repository.ChargeConsumptionRepository;
import com.nttdata.bootcamp.service.ChargeConsumptionService;
import com.nttdata.bootcamp.service.KafkaService;
import com.nttdata.bootcamp.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

//Service implementation
@Service
public class ChargeConsumptionServiceImpl implements ChargeConsumptionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChargeConsumptionServiceImpl.class);

    @Autowired
    private ChargeConsumptionRepository chargeConsumptionRepository;

    @Autowired
    private KafkaService kafkaService;

    // Find all charge consumptions
    @Override
    public Flux<ChargeConsumption> findAll() {
        return chargeConsumptionRepository.findAll();
    }

    // Find charge consumptions by account number
    @Override
    public Flux<ChargeConsumption> findByAccountNumber(String accountNumber) {
        return chargeConsumptionRepository
                .findAll()
                .filter(x -> x.getAccountNumber().equals(accountNumber));
    }

    // Find charge consumption by charge number
    @Override
    public Mono<ChargeConsumption> findByNumber(String number) {
        return chargeConsumptionRepository
                .findAll()
                .filter(x -> x.getChargeNumber().equals(number))
                .next();
    }

    @Override
    public Mono<ChargeConsumption> saveChargeConsumption(ChargeConsumption dataChargeConsumption) {

        return chargeConsumptionRepository
                .findByChargeNumber(dataChargeConsumption.getChargeNumber())
                // Si existe → error
                .flatMap(existing ->
                        Mono.<ChargeConsumption>error(
                                new RuntimeException(
                                        "This charge number "
                                                + dataChargeConsumption.getChargeNumber()
                                                + " already exists"
                                )
                        )
                )
                // Si NO existe → guardar y publicar
                .switchIfEmpty(saveTopic(dataChargeConsumption));
    }





    // Update charge consumption
    @Override
    public Mono<ChargeConsumption> updateChargeConsumption(ChargeConsumption dataChargeConsumption) {
        return findByNumber(dataChargeConsumption.getChargeNumber())
                .flatMap(existingCharge -> {
                    dataChargeConsumption.setDni(existingCharge.getDni());
                    dataChargeConsumption.setAmount(existingCharge.getAmount());
                    dataChargeConsumption.setCreationDate(existingCharge.getCreationDate());
                    return chargeConsumptionRepository.save(dataChargeConsumption);
                })
                .switchIfEmpty(Mono.error(new Exception("Charge consumption " + dataChargeConsumption.getChargeNumber() + " does not exist")));
    }

    // Delete charge consumption
    @Override
    public Mono<Void> deleteChargeConsumption(String number) {
        return findByNumber(number)
                .flatMap(chargeConsumption -> chargeConsumptionRepository.delete(chargeConsumption))
                .switchIfEmpty(Mono.error(new Exception("Charge consumption " + number + " does not exist")));
    }

//    // Save topic and publish to Kafka
//    public Mono<ChargeConsumption> saveTopic(ChargeConsumption chargeConsumption) {
//        // Guardamos el ChargeConsumption en la base de datos
//        return chargeConsumptionRepository.save(chargeConsumption)
//                .flatMap(consumption -> {
//                    // Publicamos en Kafka
//                    LOGGER.info("el nombre del topi es:= ", consumption);
//                    kafkaService.publish(consumption, EventType.CREATED);
//                    return Mono.just(consumption); // Retornamos el Mono<ChargeConsumption> después de guardar
//                })
//                .doOnError(error -> System.err.println("Error sending charge consumption to Kafka: " + error.getMessage())); // Manejamos errores
//    }

    public Mono<ChargeConsumption> saveTopic(ChargeConsumption chargeConsumption) {
        return chargeConsumptionRepository.save(chargeConsumption)
                .flatMap(consumption -> {
                    LOGGER.info("Publishing charge consumption to Kafka. chargeNumber={}",
                            consumption.getChargeNumber());
                    return kafkaService.publish(consumption, EventType.CREATED)
                            .thenReturn(consumption);
                })
                .doOnError(error ->
                        LOGGER.error("Error saving or publishing charge consumption", error));
    }


}
