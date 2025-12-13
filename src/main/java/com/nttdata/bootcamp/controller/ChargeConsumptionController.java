package com.nttdata.bootcamp.controller;

import com.nttdata.bootcamp.entity.ChargeConsumption;
import com.nttdata.bootcamp.util.Constant;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nttdata.bootcamp.service.ChargeConsumptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.nttdata.bootcamp.entity.dto.ChargeConsumptionDto;
import javax.validation.Valid;
import java.util.Date;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/chargeConsumption")
public class ChargeConsumptionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChargeConsumptionController.class);
    private final ChargeConsumptionService chargeConsumptionService;

    @Autowired
    public ChargeConsumptionController(ChargeConsumptionService chargeConsumptionService) {
        this.chargeConsumptionService = chargeConsumptionService;
    }

    // Charge consumption search
    @GetMapping("/findAllLoadBalance")
    public Flux<ChargeConsumption> findAllChargeConsumption() {
        return chargeConsumptionService.findAll()
                .doOnSubscribe(subscription -> LOGGER.info("Registered charge consumption"))
                .doOnError(error -> LOGGER.error("Error fetching charge consumption", error));
    }

    // Charge consumption by AccountNumber
    @GetMapping("/findAllChargeConsumptionByAccountNumber/{accountNumber}")
    public Flux<ChargeConsumption> findAllChargeConsumptionByAccountNumber(@PathVariable("accountNumber") String accountNumber) {
        return chargeConsumptionService.findByAccountNumber(accountNumber)
                .doOnSubscribe(subscription -> LOGGER.info("Registered charge consumption of account number: {}", accountNumber))
                .doOnError(error -> LOGGER.error("Error fetching charge consumption for account {}", accountNumber, error));
    }

    // Charge consumption by Number
    @CircuitBreaker(name = "charge-consumption", fallbackMethod = "fallBackGetChargeConsumption")
    @GetMapping("/findByChargeConsumptionNumber/{numberDeposits}")
    public Mono<ChargeConsumption> findByChargeConsumptionNumber(@PathVariable("numberDeposits") String numberDeposits) {
        LOGGER.info("Searching charge consumption by number: {}", numberDeposits);
        return chargeConsumptionService.findByNumber(numberDeposits)
                .doOnError(error -> LOGGER.error("Error fetching charge consumption by number {}", numberDeposits, error));
    }

    // Save charge consumption
    @CircuitBreaker(name = "charge-consumption", fallbackMethod = "fallBackGetChargeConsumption")
    @PostMapping(value = "/saveChargeConsumption/{creditLimit}")
    public Mono<ChargeConsumption> saveChargeConsumption(@RequestBody @Valid ChargeConsumptionDto dataChargeConsumption,
                                                         @PathVariable("creditLimit") Double creditLimit) {
        if (creditLimit >= dataChargeConsumption.getAmount()) {
            ChargeConsumption datacharge = new ChargeConsumption();
            datacharge.setDni(dataChargeConsumption.getDni());
            datacharge.setAmount(dataChargeConsumption.getAmount());
            datacharge.setChargeNumber(dataChargeConsumption.getChargeNumber());
            datacharge.setTypeAccount(Constant.TYPE_ACCOUNT);
            datacharge.setAccountNumber(dataChargeConsumption.getAccountNumber());
            datacharge.setStatus(Constant.STATUS_ACTIVE);
            datacharge.setCommission(0.00);
            datacharge.setCreationDate(new Date());
            datacharge.setModificationDate(new Date());

            return chargeConsumptionService.saveChargeConsumption(datacharge)
                    .doOnSuccess(charge -> LOGGER.info("Charge consumption saved successfully"))
                    .doOnError(error -> LOGGER.error("Error saving charge consumption", error));
        } else {
            LOGGER.error("Credit limit exceeded. Cannot save charge consumption.");
            return Mono.empty();
        }
    }

    // Update charge consumption
    @CircuitBreaker(name = "charge-consumption", fallbackMethod = "fallBackGetChargeConsumption")
    @PutMapping("/updateChargeConsumption/{numberTransaction}")
    public Mono<ChargeConsumption> updateChargeConsumption(@PathVariable("numberTransaction") String numberTransaction,
                                                           @Valid @RequestBody ChargeConsumption dataChargeConsumption) {
        dataChargeConsumption.setChargeNumber(numberTransaction);
        dataChargeConsumption.setModificationDate(new Date());

        return chargeConsumptionService.updateChargeConsumption(dataChargeConsumption)
                .doOnSuccess(charge -> LOGGER.info("Charge consumption updated successfully"))
                .doOnError(error -> LOGGER.error("Error updating charge consumption", error));
    }

    // Delete charge consumption
    @CircuitBreaker(name = "charge-consumption", fallbackMethod = "fallBackGetChargeConsumption")
    @DeleteMapping("/deleteChargeConsumption/{numberTransaction}")
    public Mono<Void> deleteChargeConsumption(@PathVariable("numberTransaction") String numberTransaction) {
        LOGGER.info("Deleting charge consumption by number: {}", numberTransaction);
        return chargeConsumptionService.deleteChargeConsumption(numberTransaction)
                .doOnSuccess(aVoid -> LOGGER.info("Charge consumption deleted successfully"))
                .doOnError(error -> LOGGER.error("Error deleting charge consumption", error));
    }

    @GetMapping("/getCountChargeConsumption/{accountNumber}")
    public Mono<Long> getCountChargeConsumption(@PathVariable("accountNumber") String accountNumber) {
        return chargeConsumptionService.findByAccountNumber(accountNumber)
                .count()
                .doOnError(error -> LOGGER.error("Error counting charge consumption for account {}", accountNumber, error));
    }

    private Mono<ChargeConsumption> fallBackGetChargeConsumption(Exception e) {
        ChargeConsumption chargeConsumption = new ChargeConsumption();
        return Mono.just(chargeConsumption);
    }
}
