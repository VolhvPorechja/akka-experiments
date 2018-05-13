package tech.volhvporechja.akka.demo.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.Router;
import tech.volhvporechja.akka.demo.actors.Contracts.GreetingConfigMessage;
import tech.volhvporechja.akka.demo.actors.Contracts.GreetingMessage;
import tech.volhvporechja.akka.demo.actors.Contracts.WelcomeMessage;

/**
 * Наш шаблонизатор
 * на борту: шаблон и логика обработки двух типов сообщений.
 */
public class GreeterActor extends AbstractActor {
	private final int id;
	private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private String messageTemplate;
	private Router printer;

	/**
	 * Blueprint, описывающий как надо собирать нашего Актора
	 * @param template печатная форма
	 * @param printersRouter роутер, дающий нам доступ к принтерам
	 * @param id идентификтор эксземпляра этого шаблонизатора
	 * @return
	 */
	static public Props props(String template, Router printersRouter, int id) {
		return Props.create(GreeterActor.class, () -> new GreeterActor(template, printersRouter, id));
	}

	/**
	 * JUST CTOR
	 * @param messageTemplate
	 * @param router
	 * @param id
	 */
	public GreeterActor(String messageTemplate, Router router, int id) {
		this.messageTemplate = messageTemplate;
		this.printer = router;
		this.id = id;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(GreetingConfigMessage.class, wtg -> { // Реконфигурируем
					log.info(String.format("GREETER-%s: Reconfiguring greeter [%s]->[%s]", id, messageTemplate, wtg.getFormatString()));
					messageTemplate = wtg.getFormatString();
				})
				.match(WelcomeMessage.class, wtg -> { // Рендерим печатную псевдоформу с шаблоном
					log.info(String.format("GREETER-%s: Received data [%s]", id, wtg.getWho()));
					printer.route(new GreetingMessage(String.format(messageTemplate, wtg.getWho())), getSelf());
				})
				.matchAny(o -> log.info("received unknown message")) // Обработчик неизвестного сообщения
				.build();
	}
}
