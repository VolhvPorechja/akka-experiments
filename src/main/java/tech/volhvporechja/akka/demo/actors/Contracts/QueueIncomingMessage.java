package tech.volhvporechja.akka.demo.actors.Contracts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Контракт сообщения получаемого из RabbitMQ
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class QueueIncomingMessage {
	private String type;
	private String load;

	public byte[] marshall() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this).getBytes();
	}
}
