package tech.volhvporechja.akka.demo.actors.Contracts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Сообщение для шаблона и последующей печати
 */
@Getter
@AllArgsConstructor
public class WelcomeMessage {
	private String who;
}
