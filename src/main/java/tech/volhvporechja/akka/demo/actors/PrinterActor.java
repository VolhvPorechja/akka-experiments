package tech.volhvporechja.akka.demo.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import tech.volhvporechja.akka.demo.actors.Contracts.GreetingMessage;

public class PrinterActor extends AbstractActor {
	private final int id;
	private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	static public Props props(int id) {
		return Props.create(PrinterActor.class, () -> new PrinterActor(id));
	}

	public PrinterActor(int id) {
		this.id = id;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(GreetingMessage.class, wtg -> {
					log.info(String.format("PRINTER-%s: <<%s>>", id, wtg.getMessage()));
				})
				.build();
	}
}
