package org.folio.rest.config;

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
    return new NioEventLoopGroup(128);
  }

  @Bean
  public ConnectionProvider connectionProvider() {
    return ConnectionProvider.builder("camunda-web-client-thread-pool")
      .maxConnections(100)
      .build();
  }

  @Bean
  public ReactorResourceFactory reactorResourceFactory(NioEventLoopGroup group, ConnectionProvider provider) {
    ReactorResourceFactory factory = new ReactorResourceFactory();
    factory.setLoopResources(new LoopResources() {
      @Override
      public EventLoopGroup onServer(boolean b) {
        return group;
      }
    });
    factory.setUseGlobalResources(false);
    factory.setConnectionProvider(provider);
    return factory;
  }

  @Bean
  public ReactorClientHttpConnector reactorClientHttpConnector(ReactorResourceFactory factory) {
    return new ReactorClientHttpConnector(factory, connection -> {
      return connection;
    });
  }

  @Bean
  public WebClient webClient(WebClient.Builder builder, ReactorClientHttpConnector connector) {
    return builder.clientConnector(connector).build();
  }

}
