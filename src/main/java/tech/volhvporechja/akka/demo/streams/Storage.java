package tech.volhvporechja.akka.demo.streams;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Storage {
	CompletionStage<Double> save(Double value) {
		return CompletableFuture.supplyAsync(() -> {
			System.out.println("saving value: " + value);
			return value;
		});
	}
}