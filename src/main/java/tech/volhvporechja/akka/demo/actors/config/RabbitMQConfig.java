package tech.volhvporechja.akka.demo.actors.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;

/**
 * Настройки доступа к нашему RabbitMQ
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RabbitMQConfig {
	private String host;
	private int port;
	private String user;
	private String pass;
	private String queue;

	/**
	 * Загрузка настроек из указанного yaml файла
	 *
	 * @param file
	 * @return
	 */
	public static RabbitMQConfig Load(String file) {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		try {
			return mapper.readValue(new File(file), RabbitMQConfig.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
