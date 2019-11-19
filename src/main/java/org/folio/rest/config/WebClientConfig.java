package org.folio.rest.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

@Configuration
public class WebClientConfig {

  @Bean
  public NioEventLoopGroup nioEventLoopGroup() {
    return new NioEventLoopGroup(32);
  }

  @Bean
  public ReactorResourceFactory reactorResourceFactory(NioEventLoopGroup eventLoopGroup) {
    ReactorResourceFactory factory = new ReactorResourceFactory();
    factory.setLoopResources(new LoopResources() {
      @Override
      public EventLoopGroup onServer(boolean b) {
        return eventLoopGroup;
      }
    });
    factory.setUseGlobalResources(false);
    ConnectionProvider connectionProvider = ConnectionProvider.elastic("camunda-web-client", Duration.ofMillis(60000));
    factory.setConnectionProvider(connectionProvider);
    return factory;
  }

  @Bean
  public ReactorClientHttpConnector reactorClientHttpConnector(ReactorResourceFactory factory) {
    return new ReactorClientHttpConnector(factory, connection -> {
      return connection;
    });
  }

  @Bean
  public WebClient webClient(WebClient.Builder webClientBuilder, ReactorClientHttpConnector connector) {
    return webClientBuilder.clientConnector(connector).build();
  }

}
