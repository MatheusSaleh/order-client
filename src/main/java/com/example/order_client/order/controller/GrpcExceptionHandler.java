package com.example.order_client.order.controller;

import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte status gRPC em respostas HTTP (RFC 7807). */
@RestControllerAdvice
public class GrpcExceptionHandler {

  @ExceptionHandler(StatusRuntimeException.class)
  public ProblemDetail handle(StatusRuntimeException e) {
    HttpStatus status =
        switch (e.getStatus().getCode()) {
          case NOT_FOUND -> HttpStatus.NOT_FOUND;
          case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
          case ALREADY_EXISTS -> HttpStatus.CONFLICT;
          case RESOURCE_EXHAUSTED -> HttpStatus.TOO_MANY_REQUESTS;
          case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
          case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
          default -> HttpStatus.BAD_GATEWAY;
        };
    String description = e.getStatus().getDescription();
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            status, description != null ? description : e.getStatus().getCode().name());
    problem.setProperty("grpcStatus", e.getStatus().getCode().name());
    return problem;
  }
}
