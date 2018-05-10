package tech.volhvporechja.akka.demo.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import tech.volhvporechja.akka.demo.actors.Contracts.FetchStartMessage;
import tech.volhvporechja.akka.demo.actors.Contracts.FetcherInitMessage;

public class SuperMessagingSystem {
	public static void main(String[] args) {
		final ActorSystem system = ActorSystem.create("super-messaging-system");

		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		final ActorRef fetcherActor =
				system.actorOf(FetcherActor.props(), "rabbit-mq-fetcher");

		FetcherInitMessage fetcherInit = FetcherInitMessage.builder()
				.greetersCount(10)
				.printersCount(5)
				.build();
		fetcherActor.tell(fetcherInit, ActorRef.noSender());

		FetchStartMessage config = FetchStartMessage.builder()
				.host("192.168.99.100")
				.port(5672)
				.user("guest")
				.password("guest")
				.queueName("hello")
				.build();
		fetcherActor.tell(config, ActorRef.noSender());
	}
}
