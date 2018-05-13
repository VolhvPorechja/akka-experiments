package tech.volhvporechja.akka.demo.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import tech.volhvporechja.akka.demo.actors.Contracts.FetchStartMessage;
import tech.volhvporechja.akka.demo.actors.Contracts.FetcherInitMessage;
import tech.volhvporechja.akka.demo.actors.config.RabbitMQConfig;

public class SuperMessagingSystem {
	public static void main(String[] args) {
		// Создаем среду для наших акторов (систему акторов)
		final ActorSystem system = ActorSystem.create("super-messaging-system");

		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		// Порождаем нашего главного актора с путем /user/rabbit-mq-fetcher
		final ActorRef fetcherActor =
				system.actorOf(FetcherActor.props(), "rabbit-mq-fetcher");

		// Подготавливаем сообщение, которое должно вызвать создание наше системы акторов
		FetcherInitMessage fetcherInit = FetcherInitMessage.builder()
				.greetersCount(20000) // Количество шаблонизаторов
				.printersCount(20000) // Количество принтеров
				.build();

		// И отправляем это сообщение нашему Fetcher'у
		fetcherActor.tell(fetcherInit, ActorRef.noSender());

		// Подготавливаем сообщение, которое должно вызвать процесс прослушивание
		// очередин на RabbitMQ и передачи заданий дальше в соответствии с логикой
		// обработки заявок нашей системой
		RabbitMQConfig config = RabbitMQConfig.Load("settings.yml");
		FetchStartMessage configMessage = FetchStartMessage.builder()
				.host(config.getHost())       // Хост, здесь настроено на Docker в Windows через DockerTools (https://docs.docker.com/toolbox/toolbox_install_windows/)
				.port(config.getPort())       // Стандартный порт
				.user(config.getUser())       // Стандартный пользователь
				.password(config.getPass())   // Стандартный пароль
				.queueName(config.getQueue()) // Очередь которую будет прослушивать наша система
				.build();

		// И отправляем это сообщение нашему Fetcher'у через путь, чтобы все закрутилось
		ActorRef fetcherByAddress = system.actorFor("akka://super-messaging-system/user/rabbit-mq-fetcher");
		fetcherByAddress.tell(configMessage, ActorRef.noSender());
	}
}
