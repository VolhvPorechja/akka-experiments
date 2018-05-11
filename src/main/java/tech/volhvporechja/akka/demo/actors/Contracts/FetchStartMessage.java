package tech.volhvporechja.akka.demo.actors.Contracts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
