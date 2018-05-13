package tech.volhvporechja.akka.demo.actors.Contracts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Сообщение для печати
 */
@Getter
@AllArgsConstructor
public class GreetingMessage {
	private String message;
}
