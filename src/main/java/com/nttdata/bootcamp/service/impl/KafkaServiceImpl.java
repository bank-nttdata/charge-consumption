package com.nttdata.bootcamp.service.impl;

import com.nttdata.bootcamp.entity.ChargeConsumption;
import com.nttdata.bootcamp.entity.enums.EventType;
import com.nttdata.bootcamp.events.EventKafka;
import com.nttdata.bootcamp.events.ChargeConsumptionCreatedEventKafka;
import com.nttdata.bootcamp.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import java.util.Date;
import java.util.UUID;

@Service
public class KafkaServiceImpl implements KafkaService {

    private final KafkaSender<String, EventKafka<?>> kafkaSender;
    private final String topicCharge;

    @Autowired
    public KafkaServiceImpl(KafkaSender<String, EventKafka<?>> kafkaSender, @Value("${topic.charge.name}") String topicCharge) {
        this.kafkaSender = kafkaSender;
        this.topicCharge = topicCharge;
    }

    // Método para publicar eventos de manera reactiva
    public Mono<Void> publish(ChargeConsumption chargeConsumption, EventType eventTyp) {
        ChargeConsumptionCreatedEventKafka createdEvent = new ChargeConsumptionCreatedEventKafka();
        createdEvent.setData(chargeConsumption);
        createdEvent.setId(UUID.randomUUID().toString());
        createdEvent.setType(EventType.CREATED);
        createdEvent.setDate(new Date());

        // Crear un SenderRecord para el evento
        SenderRecord<String, EventKafka<?>, String> senderRecord = SenderRecord.create(
                new org.apache.kafka.clients.producer.ProducerRecord<>(topicCharge, createdEvent.getId(), createdEvent),
                createdEvent.getId()
        );

        // Enviar el mensaje de manera reactiva y manejar el éxito y error correctamente
        return kafkaSender.send(Mono.just(senderRecord))
                .doOnTerminate(() -> {
                     System.out.println("Message sending process completed.");
                })
                .doOnError(error -> {
                    System.err.println("Error sending message to Kafka: " + error.getMessage());
                })
                .then();  // Mono<Void> para indicar que la operación ha terminado
    }

}
