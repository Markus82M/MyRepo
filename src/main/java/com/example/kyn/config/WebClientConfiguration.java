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

    private final String subscriptionKey;

    public WebClientConfiguration(final @Value("${weather.api.key}") String subscriptionKey) {
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
                   //     .bindAddress(() -> new InetSocketAddress("host", 1234))
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                        .option(ChannelOption.SO_KEEPALIVE, true);
                        // The options below are available only when NIO transport (Java 11) is used
                        // on Mac or Linux (Java does not currently support these extended options on Windows)
                        // https://bugs.openjdk.java.net/browse/JDK-8194298
                        //.option(NioChannelOption.of(ExtendedSocketOptions.TCP_KEEPIDLE), 300)
                        //.option(NioChannelOption.of(ExtendedSocketOptions.TCP_KEEPINTERVAL), 60)
                        //.option(NioChannelOption.of(ExtendedSocketOptions.TCP_KEEPCOUNT), 8);
                        // The options below are available only when Epoll transport is used
                   //     .option(EpollChannelOption.TCP_KEEPIDLE, 300)
                   //     .option(EpollChannelOption.TCP_KEEPINTVL, 60)
                   //     .option(EpollChannelOption.TCP_KEEPCNT, 8);

        return WebClient.builder()
                .baseUrl("https://api.weatherapi.com/v1/current.json")
                .clientConnector(new ReactorClientHttpConnector(client))
                .defaultHeader("key", subscriptionKey)
                .build();

    }
}
