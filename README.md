# Handling slow stage in Kafka Streams

Example project of combining Spring Boot application with 
[Kafka Streams](https://kafka.apache.org/documentation/streams/) and
[parallel-consumer](https://github.com/confluentinc/parallel-consumer).

# Motivation
Kafka is a great tool, but it is misused in many cases. One of the common pitfalls are slow stages with scale on demand
requirement. Kafka itself is not very flexible in subscription model and scaling it, you can see this as good or bad.
As an engineer I want to focus on writing my application using opinionated framework, which simplifies things for me.
That is why I like Spring Boot and Kafka Streams, they provide with wide ecosystem and many tools. All of this is great
until you hit the mentioned pitfall with Kafka. This projects aims to showcase how to solve it without introducing
different tool for messaging/streaming/queueing.

# Stream set up

```kotlin
builder.stream(
    "in",
    Consumed.with(Serdes.String(), Serdes.String())
) // start stream from input topic
    .peek { k, v -> logger.info("in {} {}", k, v) } // do your business logic before slow stage
    .to("slow-stage-in") // reroute to stage topic for slow stage

parallelStream.subscribe(listOf("slow-stage-in")) // consume slow stage input topic
parallelStream.pollAndProduce { pullContext -> 
    // any slow logic
    ProducerRecord("slow-stage-out", pullContext.key(), pullContext.value()) // produce result to slow stage output topic
}

builder.stream(
    "slow-stage-out",
    Consumed.with(Serdes.String(), Serdes.String())
) // continue stream from slow stage output topic
    .peek { k, v -> logger.info("out {} {}", k, v) } // do your business logic after slow stage
    .to("out") // end stream into output topic
```
