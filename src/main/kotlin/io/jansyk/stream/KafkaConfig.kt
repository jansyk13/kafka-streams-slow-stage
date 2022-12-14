package io.jansyk.stream

import io.confluent.parallelconsumer.ParallelConsumerOptions
import io.confluent.parallelconsumer.ParallelConsumerOptions.ProcessingOrder.UNORDERED
import io.confluent.parallelconsumer.ParallelStreamProcessor
import io.confluent.parallelconsumer.reactor.ReactorProcessor
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.ProducerFactory


@Configuration
class KafkaConfig {

    @Bean
    fun kafkaProducer(
        producerFactory: ProducerFactory<String, String>,
    ): Producer<String, String> {
        return producerFactory.createProducer()
    }

    @Bean
    fun parallelStream(
        consumerFactory: ConsumerFactory<String, String>,
        kafkaProducer: Producer<String, String>,
    ): ReactorProcessor<String, String> {
        val kafkaConsumer: Consumer<String, String> = consumerFactory.createConsumer("slow-stage-consumer", "")

        val options = ParallelConsumerOptions.builder<String, String>()
            .ordering(UNORDERED)
            .maxConcurrency(1000)
            .consumer(kafkaConsumer)
            .producer(kafkaProducer)
            .build()

        return ReactorProcessor(options)
    }

    @Bean
    fun inTopic(): NewTopic {
        return TopicBuilder.name("in")
            .partitions(1)
            .replicas(1)
            .build();
    }

    @Bean
    fun outTopic(): NewTopic {
        return TopicBuilder.name("out")
            .partitions(1)
            .replicas(1)
            .build();
    }

    @Bean
    fun slowStageInTopic(): NewTopic {
        return TopicBuilder.name("slow-stage-in")
            .partitions(1)
            .replicas(1)
            .build();
    }

    @Bean
    fun slowStageOutTopic(): NewTopic {
        return TopicBuilder.name("slow-stage-out")
            .partitions(1)
            .replicas(1)
            .build();
    }
}