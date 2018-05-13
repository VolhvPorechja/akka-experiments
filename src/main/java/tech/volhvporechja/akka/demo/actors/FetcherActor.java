package tech.volhvporechja.akka.demo.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import tech.volhvporechja.akka.demo.actors.Contracts.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Actor that fetches messages from RabbitMq queue and ignite printing
 */
public class FetcherActor extends AbstractActor {
	private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private Router broadcastRouter;
	private Router router;

	// Blueprint нашего аткора, то как его следует инстанцировать
	static public Props props() {
		return Props.create(FetcherActor.class, FetcherActor::new);
	}

	/**
	 * Логика инициалиазции нашей системы
	 * Передаем сообщение в котором храниться конфигурация нашей системы
	 *
	 * @param config initialization config
	 */
	private void initializePrinterSystem(FetcherInitMessage config) {
		// готовим наши "принтеры"
		log.info("Initialization start");
		List<Routee> printerRoutees = new ArrayList<>();
		for (int i = 0; i < config.getPrintersCount(); i++) {
			ActorRef printer = getContext().actorOf(PrinterActor.props(i), "printer-" + i);
			getContext().watch(printer);
			printerRoutees.add(new ActorRefRoutee(printer));
		}

		// готовим наши шаблонизаторы
		Router printerRouter = new Router(new RoundRobinRoutingLogic(), printerRoutees);
		List<Routee> greeterRoutees = new ArrayList<>();
		for (int i = 0; i < config.getGreetersCount(); i++) {
			ActorRef greeter = getContext().actorOf(GreeterActor.props("Aloha, %s!!", printerRouter, i), "greeter-" + i);
			getContext().watch(greeter);
			greeterRoutees.add(new ActorRefRoutee(greeter));
		}

		// Роутер для распространения конфигурационного сообщения
		broadcastRouter = new Router(new BroadcastRoutingLogic(), greeterRoutees);

		// Роутер для отправки сообщений предназначенных для шаблонизации и печати
		router = new Router(new RoundRobinRoutingLogic(), greeterRoutees);
		log.info("Initialization finished");
	}

	/**
	 * Метод прослушивания очереди
	 * !! Disclaimer: используется блокирующее API для доступа к RabbitMQ
	 * поток, который будет исполнять этот код будет потерян для пула.
	 * Но так как такой актор только один, для демонстрационных целей - ничего страшного
	 *
	 * @param config listening config
	 */
	private void listen(FetchStartMessage config) {
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(config.getHost());
			factory.setPort(config.getPort());
			factory.setUsername(config.getUser());
			factory.setPassword(config.getPassword());

			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();

			channel.queueDeclare(config.getQueueName(), false, false, false, null);

			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope,
				                           AMQP.BasicProperties properties, byte[] body)
						throws IOException {
					String message = new String(body, "UTF-8");
					log.info(" [x] Received '" + message + "'");

					QueueIncomingMessage request = null;
					try {
						ObjectMapper mapper = new ObjectMapper();
						request = mapper.readValue(message, QueueIncomingMessage.class);
					} catch (Exception ex) {
						log.error(ex, "Unable to parse request.");
					}

					if (request != null)
						if (request.getType().equals(MessagesTypes.WELCOME))
							router.route(new WelcomeMessage(request.getLoad()), getSelf());
						else if (request.getType().equals(MessagesTypes.CONFIG))
							broadcastRouter.route(new GreetingConfigMessage(request.getLoad()), getSelf());
						else
							log.warning("Unknown message type");
				}
			};

			channel.basicConsume(config.getQueueName(), true, consumer);
			log.info(String.format("Listening RabbitMq server=%s:%s queue=%s", config.getHost(), config.getPort(), config.getQueueName()));

		} catch (Exception ex) {
			log.error(ex, "Error occurred during rabbitmq messages fetching");
		}
	}

	// Билдер нашего обраотчика сообщений
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(FetchStartMessage.class, this::listen) // Обработчик сообщения начала процесса прослушивания заданий на печать
				.match(FetcherInitMessage.class, this::initializePrinterSystem) // Обработчик сообщения инициализации системы
				.matchAny(o -> log.info("received unknown message")) // Обработчик неизвестного сообщения
				.build();
	}
}
