package com.example.elasticsearch.config;

import com.example.elasticsearch.member.domain.search.MemberSearchQueryRepository;
import com.example.elasticsearch.member.domain.search.MemberSearchRepository;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

@TestConfiguration
@EnableElasticsearchRepositories(basePackageClasses = {MemberSearchRepository.class, MemberSearchQueryRepository.class})
public class ElasticTestContainer extends AbstractElasticsearchConfiguration{


    private static final GenericContainer container;

    static {
        container = new GenericContainer(
                new ImageFromDockerfile()
                    .withDockerfileFromBuilder(builder -> {
                        builder
                                // ES 이미지 가져오기
                                .from("docker.elastic.co/elasticsearch/elasticsearch:7.15.2")
                                // nori 분석기 설치
                                .run("bin/elasticsearch-plugin install analysis-nori")
                                .build();
                    })
        ).withExposedPorts(9200,9300)
        .withEnv("discovery.type","single-node");

        container.start();
    }

    @Override
    public RestHighLevelClient elasticsearchClient() {
        // ElasticearchContainer에서 제공해주던 httpHostAddress를 사용할수 없기 때문에
        // 직접 꺼내서 만들어줘야 합니다.
        String hostAddress = new StringBuilder()
                .append(container.getHost())
                .append(":")
                .append(container.getMappedPort(9200))
                .toString();

        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(hostAddress)
                .build();
        return RestClients.create(clientConfiguration).rest();
    }

    /**
     *  only ES container (not install plugin)
     */
//    private static final String ELASTICSEARCH_VERSION = "7.15.2";
//    private static final DockerImageName ELASTICSEARCH_IMAGE =
//            DockerImageName
//                    .parse("docker.elastic.co/elasticsearch/elasticsearch")
//                    .withTag(ELASTICSEARCH_VERSION);
//    private static final ElasticsearchContainer container;
//
//    // testContainer 띄우기
//    static {
//        container = new ElasticsearchContainer(ELASTICSEARCH_IMAGE);
//        container.start();
//    }
//
//    // 띄운 컨테이너로 ESCilent 재정의
//    @Override
//    public RestHighLevelClient elasticsearchClient() {
//        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
//                .connectedTo(container.getHttpHostAddress())
//                .build();
//        return RestClients.create(clientConfiguration).rest();
//    }
}
