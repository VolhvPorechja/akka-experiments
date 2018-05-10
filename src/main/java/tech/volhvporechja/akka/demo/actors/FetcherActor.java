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

	static public Props props() {
		return Props.create(FetcherActor.class, FetcherActor::new);
	}

	/**
	 * Processor for actors system initialization
	 *
	 * @param config initialization config
	 */
	private void intializePrinterSystem(FetcherInitMessage config) {
		List<Routee> printerRoutees = new ArrayList<>();
		for (int i = 0; i < config.getPrintersCount(); i++) {
			ActorRef printer = getContext().actorOf(Props.create(PrinterActor.class, i));
			getContext().watch(printer);
			printerRoutees.add(new ActorRefRoutee(printer));
		}

		Router printerRouter = new Router(new RoundRobinRoutingLogic(), printerRoutees);

		List<Routee> greeterRoutees = new ArrayList<>();
		for (int i = 0; i < config.getGreetersCount(); i++) {
			ActorRef greeter = getContext().actorOf(Props.create(GreeterActor.class, "Aloha, %s!!", printerRouter, i));
			getContext().watch(greeter);
			greeterRoutees.add(new ActorRefRoutee(greeter));
		}

		broadcastRouter = new Router(new BroadcastRoutingLogic(), greeterRoutees);
		router = new Router(new RoundRobinRoutingLogic(), greeterRoutees);
	}

	/**
	 * Processor for listening for queue
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

					IncommingMessage request = null;
					try {
						ObjectMapper mapper = new ObjectMapper();
						request = mapper.readValue(message, IncommingMessage.class);
					} catch (Exception ex) {
						log.error(ex, "Unable to parse request.");
					}

					if (request != null)
						if (request.getType().equals("welcome"))
							router.route(new WelcomeMessage(request.getLoad()), getSelf());
						else if (request.getType().equals("config"))
							broadcastRouter.route(new GreetingConfigMessage(request.getLoad()), getSelf());
						else
							log.warning("Unknown message type");
				}
			};

			log.info(String.format("Listening RabbitMq server=%s:%s queue=%s", config.getHost(), config.getPort(), config.getQueueName()));
			channel.basicConsume(config.getQueueName(), true, consumer);
		} catch (Exception ex) {
			log.error(ex, "Error occurred during rabbitmq messages fetching");
		}
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(FetchStartMessage.class, this::listen)
				.match(FetcherInitMessage.class, this::intializePrinterSystem)
				.build();
	}
}
