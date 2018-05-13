package tech.volhvporechja.akka.demo.actors.Contracts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Сообщение говорящее о том, что пора зачинать поток заданий
 * содержит все параметры, чтобы начать это делать.
 */
@Getter
@AllArgsConstructor
@Builder
public class FetchStartMessage {
	private String host;
	private String user;
	private String password;
	private int port;
	private String queueName;
}
