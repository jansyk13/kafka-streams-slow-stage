package io.jansyk.stream

import io.confluent.parallelconsumer.ParallelStreamProcessor
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class KafkaStream {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(KafkaStream::class.java)
    }

    @Autowired
    fun buildStream(
        builder: StreamsBuilder,
        parallelStream: ParallelStreamProcessor<String, String>
    ) {
        builder.stream(
            "in",
            Consumed.with(Serdes.String(), Serdes.String())
        )
            .peek { k, v -> logger.info("in {} {}", k, v) }
            .to("slow-stage-in")

        parallelStream.subscribe(listOf("slow-stage-in"))
        parallelStream.pollAndProduce { pullContext ->
            ProducerRecord("slow-stage-out", pullContext.key(), pullContext.value())
        }

        builder.stream(
            "slow-stage-out",
            Consumed.with(Serdes.String(), Serdes.String())
        )
            .peek { k, v -> logger.info("out {} {}", k, v) }
            .to("out")
    }

}