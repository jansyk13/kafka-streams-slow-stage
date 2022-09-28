package io.jansyk.stream

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.EnableKafkaStreams

@SpringBootApplication
@EnableKafka
@EnableKafkaStreams
class KafkaStreamsSlowStageApplication

fun main(args: Array<String>) {
	runApplication<KafkaStreamsSlowStageApplication>(*args)
}
