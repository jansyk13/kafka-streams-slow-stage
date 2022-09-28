package io.jansyk.stream

import io.confluent.parallelconsumer.ParallelConsumerOptions
import io.confluent.parallelconsumer.ParallelConsumerOptions.ProcessingOrder.UNORDERED
import io.confluent.parallelconsumer.ParallelStreamProcessor
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Producer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.ProducerFactory


@Configuration
class KafkaConfig {

    @Bean
    fun parallelStream(
        consumerFactory: ConsumerFactory<String, String>,
        producerFactory: ProducerFactory<String, String>
    ): ParallelStreamProcessor<String, String> {
        val kafkaConsumer: Consumer<String, String> = consumerFactory.createConsumer("slow-stage-consumer","")
        val kafkaProducer: Producer<String, String> = producerFactory.createProducer()

        val options = ParallelConsumerOptions.builder<String, String>()
            .ordering(UNORDERED)
            .maxConcurrency(1000)
            .consumer(kafkaConsumer)
            .producer(kafkaProducer)
            .build()

        return ParallelStreamProcessor.createEosStreamProcessor(options)
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