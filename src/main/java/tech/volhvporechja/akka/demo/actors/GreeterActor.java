package tech.volhvporechja.akka.demo.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.Router;
import tech.volhvporechja.akka.demo.actors.Contracts.GreetingConfigMessage;
import tech.volhvporechja.akka.demo.actors.Contracts.GreetingMessage;
import tech.volhvporechja.akka.demo.actors.Contracts.WelcomeMessage;

public class GreeterActor extends AbstractActor {
	private final int id;
	private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private String messageTemplate;
	private Router printer;

	static public Props props(String message, Router printer, int id) {
		return Props.create(GreeterActor.class, () -> new GreeterActor(message, printer, id));
	}

	public GreeterActor(String messageTemplate, Router router, int id) {
		this.messageTemplate = messageTemplate;
		this.printer = router;
		this.id = id;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(GreetingConfigMessage.class, wtg -> { // Reconfiguration
					log.info(String.format("GREETER-%s: Reconfiguring greeter [%s]->[%s]", id, messageTemplate, wtg.getFormatString()));
					messageTemplate = wtg.getFormatString();
				})
				.match(WelcomeMessage.class, wtg -> { // Execution
					log.info(String.format("GREETER-%s: Received data [%s]", id, wtg.getWho()));
					printer.route(new GreetingMessage(String.format(messageTemplate, wtg.getWho())), getSelf());
				})
				.build();
	}
}
