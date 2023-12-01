package dev.m0b1.mighty.metrics.logging;

import lombok.Builder;
import lombok.Data;
import org.slf4j.event.Level;

import java.util.Map;

@Builder
@Data
public class LogData {

	private Level level;
	private String message;
	private Throwable throwable;
	private Map<String, Object> markers;

}
