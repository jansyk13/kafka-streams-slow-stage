package io.jansyk.stream

import io.confluent.parallelconsumer.reactor.ReactorProcessor
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

private val logger: Logger = LoggerFactory.getLogger(KafkaStream::class.java)

@Component
class KafkaStream {


    @Autowired
    fun buildStream(
        builder: StreamsBuilder,
        parallelStream: ReactorProcessor<String, String>,
        kafkaProducer: Producer<String, String>,
    ) {
        builder.stream(
            "in",
            Consumed.with(Serdes.String(), Serdes.String())
        )
            .peek { k, v -> logger.info("in {} {}", k, v) }
            .to("slow-stage-in")

        parallelStream.subscribe(listOf("slow-stage-in"))
        parallelStream.react { pc ->
            WebClient.create()
                .get()
                .uri("http://example.com")
                .retrieve()
                .toMono()
                .flatMap { rs -> rs.bodyToMono<String>() }
                .flatMap { body ->
                    kafkaProducer.send(ProducerRecord("slow-stage-out", pc.key(), body)).toMono()
                }
        }

        builder.stream(
            "slow-stage-out",
            Consumed.with(Serdes.String(), Serdes.String())
        )
            .peek { k, v -> logger.info("out {} {}", k, v) }
            .to("out")
    }

}