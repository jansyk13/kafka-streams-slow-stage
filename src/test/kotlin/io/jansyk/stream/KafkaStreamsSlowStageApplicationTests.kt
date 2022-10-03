package io.jansyk.stream

import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.test.context.EmbeddedKafka
import java.time.Duration


@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"])
class KafkaStreamsSlowStageApplicationTests {

    @Autowired
    private lateinit var producerFactory: ProducerFactory<String, String>


    @Autowired
    private lateinit var consumerFactory: ConsumerFactory<String, String>

    @Test
    fun testSimple() {
        // given
        val producer = producerFactory.createProducer()
        val consumer = consumerFactory.createConsumer("test-consumer", "")

        // when
        consumer.subscribe(listOf("out"))
        producer.send(ProducerRecord("in", "a", "b"))
        val result = consumer.poll(Duration.ofSeconds(10))

        // then
        val resultIterator = result.records("out").iterator()
        assertTrue(resultIterator.hasNext())
        val record = resultIterator.next()
        assertEquals(record.key(), "a")
        assertTrue(record.value().contains("<h1>Example Domain</h1>"))
    }

}
