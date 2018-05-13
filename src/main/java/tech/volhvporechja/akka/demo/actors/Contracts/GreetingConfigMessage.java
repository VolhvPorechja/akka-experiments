package tech.volhvporechja.akka.demo.actors.Contracts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Сообщение по реконфигурации шаблонов печати
 */
@Getter
@Setter
@AllArgsConstructor
public class GreetingConfigMessage {
	private String formatString;
}
