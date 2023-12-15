package com.example.kyn.config;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.socket.nio.NioChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.http.client.HttpClient;

import java.net.InetSocketAddress;
import java.time.Duration;

@Configuration
public class WebClientConfiguration {

    private final String weatherApiUrl;
    private final String subscriptionKey;

    public WebClientConfiguration(final @Value("${weather.api.url}") String weatherApiUrl,
                                  final @Value("${weather.api.key}") String subscriptionKey) {
        this.weatherApiUrl = weatherApiUrl;
        this.subscriptionKey = subscriptionKey;

    }
    @Bean
    public WebClient weatherWebClient() {

        Http11SslContextSpec http11SslContextSpec = Http11SslContextSpec.forClient();

        HttpClient client =
                HttpClient.create()
                        .secure(spec -> spec.sslContext(http11SslContextSpec)
                                .handshakeTimeout(Duration.ofSeconds(30))
                                .closeNotifyFlushTimeout(Duration.ofSeconds(10))
                                .closeNotifyReadTimeout(Duration.ofSeconds(10)))
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                        .option(ChannelOption.SO_KEEPALIVE, true);

        return WebClient.builder()
                .baseUrl(weatherApiUrl)
                .clientConnector(new ReactorClientHttpConnector(client))
                .defaultHeader("key", subscriptionKey)
                .build();

    }
}
