package tech.volhvporechja.akka.demo.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;
import tech.volhvporechja.akka.demo.actors.Contracts.GreetingMessage;

import java.util.concurrent.TimeUnit;

public class PrinterActor extends AbstractActor {
	private final int id;
	private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	static public Props props(int id) {
		return Props.create(PrinterActor.class, () -> new PrinterActor(id));
	}

	public PrinterActor(int id) {
		this.id = id;
		getContext().setReceiveTimeout(Duration.create(10, TimeUnit.SECONDS));
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(GreetingMessage.class, wtg -> {
					log.info(String.format("PRINTER-%s: <<%s>>", id, wtg.getMessage()));
				})
				.match(ReceiveTimeout.class, r -> {
					// log.info("Soooo boooring!!");
					// Switch off - просто висячий код, который позволяет выключить таймер
					// для этой адской машины скуки
					// getContext().setReceiveTimeout(Duration.Undefined());
				})
				.matchAny(o -> log.info("received unknown message")) // Обработчик неизвестного сообщения
				.build();
	}
}
