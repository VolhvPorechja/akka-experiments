package tech.volhvporechja.akka.demo.actors;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import tech.volhvporechja.akka.demo.actors.Contracts.MessagesTypes;
import tech.volhvporechja.akka.demo.actors.Contracts.QueueIncomingMessage;
import tech.volhvporechja.akka.demo.actors.config.RabbitMQConfig;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MessagesPublisher {

	private static String[] names = {"Alex", "Ben", "Carrol", "Diana", "Evan", "Martin Fawler"};

	public static void main(String[] args) throws IOException, TimeoutException {
		RabbitMQConfig config = RabbitMQConfig.Load("settings.yml");

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(config.getHost());
		factory.setPort(config.getPort());
		factory.setUsername(config.getUser());
		factory.setPassword(config.getPass());

		try (Connection conn = factory.newConnection()) {
			try (Channel channel = conn.createChannel()) {
				channel.queueDeclare(config.getQueue(), false, false, false, null);

				QueueIncomingMessage.QueueIncomingMessageBuilder welcomeMessageBuilder = QueueIncomingMessage.builder().
						type(MessagesTypes.WELCOME);
				for (int i = 0; i < 5e6; i++) {
					final byte[] message = welcomeMessageBuilder
							.load(names[i % names.length])
							.build().marshall();

					channel.basicPublish("", config.getQueue(), null, message);
				}

				byte[] reconfig = QueueIncomingMessage.builder()
						.type(MessagesTypes.CONFIG)
						.load("Heyyo, %s")
						.build().marshall();
				channel.basicPublish("", config.getQueue(), null, reconfig);

				for (int i = 0; i < 5e6; i++) {
					final byte[] message = welcomeMessageBuilder
							.load(names[i % names.length])
							.build().marshall();

					channel.basicPublish("", config.getQueue(), null, message);
				}
			}
		}
	}
}
