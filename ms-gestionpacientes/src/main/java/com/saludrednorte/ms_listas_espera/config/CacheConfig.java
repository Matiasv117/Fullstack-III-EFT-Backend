package com.saludrednorte.ms_listas_espera.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_PACIENTES = "pacientes";
    public static final String CACHE_LISTA_ESPERA = "listaEspera";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CACHE_PACIENTES, CACHE_LISTA_ESPERA);
    }
}
