package com.subocol.taller.ordenes.infrastructure.configuration;

import com.subocol.taller.ordenes.application.port.ActorActualPort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ActorAutenticadoAdapter implements ActorActualPort {
  public String identificador() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName()))
      throw new IllegalStateException("Se requiere un actor autenticado");
    return auth.getName();
  }
}
