package project.votebackend.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    // Elasticsearch의 Low-level REST 클라이언트 빈 생성
    @Bean
    public RestClient restClient() {
        // Elasticsearch 서버가 실행 중인 호스트와 포트를 설정합니다.
        // 현재는 로컬호스트(localhost)의 9200 포트를 사용합니다.
        return RestClient.builder(
                new HttpHost("elasticsearch", 9200)  // Elasticsearch 주소
        ).build();
    }

    // Elasticsearch Java API 클라이언트 빈 생성
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        // JSON 직렬화/역직렬화를 담당하는 Jackson 기반 매퍼를 사용하여
        // RestClientTransport를 구성합니다.
        RestClientTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );
        // 구성된 transport를 기반으로 Elasticsearch 클라이언트를 생성합니다.
        return new ElasticsearchClient(transport);
    }
}

