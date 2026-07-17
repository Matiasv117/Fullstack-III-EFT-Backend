package com.saludrednorte.ms_listas_espera.client;

import com.saludrednorte.ms_listas_espera.dto.NotificationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-notificaciones", url = "${MS_NOTIFICACIONES_URL:http://localhost:8085}")
public interface NotificationClient {

    @PostMapping("/api/notificaciones")
    ResponseEntity<Void> createNotification(@RequestBody NotificationRequestDTO request);
}
