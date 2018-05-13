package tech.volhvporechja.akka.demo.actors.Contracts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Инициализирующее сообщние
 * содержит параметры в какой начальной конфигурации надо инициализироваться систему
 */
@Getter
@AllArgsConstructor
@Builder
public class FetcherInitMessage {
	private int printersCount;
	private int greetersCount;
}
