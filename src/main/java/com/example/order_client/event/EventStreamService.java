package com.example.order_client.event;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** Mantem as conexoes SSE abertas pelos navegadores e faz broadcast dos eventos. */
@Slf4j
@Service
public class EventStreamService {

  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  /** Cada conexao SSE fica aberta: o limite evita esgotar memoria e conexoes do servidor. */
  @Value("${order.sse.max-connections:200}")
  private int maxConnections;

  public SseEmitter subscribe() {
    if (emitters.size() >= maxConnections) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Limite de conexoes atingido");
    }
    SseEmitter emitter = new SseEmitter(0L); // sem timeout: a conexao fica aberta
    emitters.add(emitter);
    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    emitter.onError(e -> emitters.remove(emitter));
    send(emitter, "connected", "ok");
    return emitter;
  }

  public boolean hasSubscribers() {
    return !emitters.isEmpty();
  }

  public void broadcast(String eventName, Object payload) {
    for (SseEmitter emitter : emitters) {
      send(emitter, eventName, payload);
    }
  }

  private void send(SseEmitter emitter, String eventName, Object payload) {
    // SseEmitter nao e thread-safe: listener do RabbitMQ e scheduler enviam em paralelo
    synchronized (emitter) {
      try {
        emitter.send(SseEmitter.event().name(eventName).data(payload));
      } catch (IOException | IllegalStateException e) {
        emitters.remove(emitter);
        log.debug("Conexao SSE encerrada: {}", e.getMessage());
      }
    }
  }
}
