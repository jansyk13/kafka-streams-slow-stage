# Handling slow stage in Kafka Streams

Example project of combining Spring Boot application with 
[Kafka Streams](https://kafka.apache.org/documentation/streams/) and
[parallel-consumer](https://github.com/confluentinc/parallel-consumer).

# Motivation
Kafka is a great tool, but it is misused in many cases. One of the common pitfalls is slow stages with scale-on-demand
requirement. Kafka itself is not very flexible in a subscription model and scaling it; you can see this as good or bad.
As an engineer, I want to focus on writing my application using an opinionated framework, simplifying things.
That is why I like Spring Boot and Kafka Streams; they provide a broad ecosystem and many tools. All of this is great
until you hit the mentioned pitfall with Kafka. This project aims to showcase how to solve it without introducing
a different messaging/streaming/queueing tool.

I recommend having a look at [introducing-confluent-labs-parallel-consumer-client](https://www.confluent.io/en-gb/events/kafka-summit-europe-2021/introducing-confluent-labs-parallel-consumer-client/).

# Stream set up

```kotlin
builder.stream(
    "in",
    Consumed.with(Serdes.String(), Serdes.String())
) // start stream from input topic
    .peek { k, v -> logger.info("in {} {}", k, v) } // do your business logic before slow stage
    .to("slow-stage-in") // reroute to stage topic for slow stage

parallelStream.subscribe(listOf("slow-stage-in")) // consume slow stage input topic
parallelStream.react { pc ->
    // any slow logic
    WebClient.create()
        .get()
        .uri("http://example.com")
        .retrieve()
        .toMono()
        .flatMap { rs -> rs.bodyToMono<String>() }
        .flatMap { body ->
            kafkaProducer.send(ProducerRecord("slow-stage-out", pc.key(), body)).toMono() // produce result to slow stage output topic
        }
}

builder.stream(
    "slow-stage-out",
    Consumed.with(Serdes.String(), Serdes.String())
) // continue stream from slow stage output topic
    .peek { k, v -> logger.info("out {} {}", k, v) } // do your business logic after slow stage
    .to("out") // end stream into output topic
```
