package tech.volhvporechja.akka.demo.streams;

import akka.actor.ActorSystem;

public class SuperStreams {
	public static void main(String[] args) {
		final ActorSystem system = ActorSystem.create("super-streaming-system");
		Streamer streamer = new Streamer(system);

		streamer.calculateAverageForContent("1;2;3;4;5;6;7").thenAccept(d -> system.terminate());
	}
}
