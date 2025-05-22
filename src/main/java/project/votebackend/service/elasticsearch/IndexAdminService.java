package project.votebackend.service.elasticsearch;

import org.apache.http.entity.ContentType;
import lombok.RequiredArgsConstructor;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class IndexAdminService {

    private final RestClient restClient;

    //투표 검색 커스터마이징
    public void createVotesIndex() throws IOException {
        String body = """
        {
          "settings": {
            "index": {
              "max_ngram_diff": 19
            },
            "analysis": {
              "analyzer": {
                "edge_ngram_analyzer": {
                  "type": "custom",
                  "tokenizer": "edge_ngram_tokenizer",
                  "filter": ["lowercase"]
                },
                "ngram_analyzer": {
                  "type": "custom",
                  "tokenizer": "ngram_tokenizer",
                  "filter": ["lowercase"]
                }
              },
              "tokenizer": {
                "edge_ngram_tokenizer": {
                  "type": "edge_ngram",
                  "min_gram": 2,
                  "max_gram": 20,
                  "token_chars": ["letter", "digit", "whitespace", "punctuation"]
                },
                "ngram_tokenizer": {
                  "type": "ngram",
                  "min_gram": 2,
                  "max_gram": 20,
                  "token_chars": ["letter", "digit", "whitespace", "punctuation"]
                }
              }
            }
          },
          "mappings": {
            "properties": {
              "id": { "type": "long" },
              "title": {
                "type": "text",
                "analyzer": "edge_ngram_analyzer",
                "search_analyzer": "standard",
                "fields": {
                  "standard": {
                    "type": "text",
                    "analyzer": "standard"
                  },
                  "ngram": {
                    "type": "text",
                    "analyzer": "ngram_analyzer"
                  }
                }
              },
              "username": {
                "type": "text",
                "analyzer": "edge_ngram_analyzer",
                "search_analyzer": "standard"
              },
              "category": { "type": "keyword" }
            }
          }
        }
        """;

        Request request = new Request("PUT", "/votes");
        request.setEntity(new NStringEntity(body, ContentType.APPLICATION_JSON));
        Response response = restClient.performRequest(request);
        System.out.println("Elasticsearch 인덱스 생성 결과: " + response.getStatusLine());
    }

    //유저 검색 커스터마이징
    public void createUsersIndex() throws IOException {
        String body = """
        {
          "settings": {
            "index": {
              "max_ngram_diff": 18
            },
            "analysis": {
              "analyzer": {
                "edge_ngram_analyzer": {
                  "type": "custom",
                  "tokenizer": "edge_ngram_tokenizer",
                  "filter": ["lowercase"]
                },
                "ngram_analyzer": {
                  "type": "custom",
                  "tokenizer": "ngram_tokenizer",
                  "filter": ["lowercase"]
                }
              },
              "tokenizer": {
                "edge_ngram_tokenizer": {
                  "type": "edge_ngram",
                  "min_gram": 1,
                  "max_gram": 20,
                  "token_chars": ["letter", "digit", "whitespace"]
                },
                "ngram_tokenizer": {
                  "type": "ngram",
                  "min_gram": 2,
                  "max_gram": 20,
                  "token_chars": ["letter", "digit", "whitespace"]
                }
              }
            }
          },
          "mappings": {
            "properties": {
              "id": { "type": "long" },
              "username": {
                "type": "text",
                "fields": {
                  "edge": {
                    "type": "text",
                    "analyzer": "edge_ngram_analyzer",
                    "search_analyzer": "standard"
                  },
                  "ngram": {
                    "type": "text",
                    "analyzer": "ngram_analyzer",
                    "search_analyzer": "standard"
                  },
                  "standard": {
                    "type": "text",
                    "analyzer": "standard"
                  }
                }
              },
              "profileImage": { "type": "keyword" }
            }
          }
        }
        """;

        Request request = new Request("PUT", "/users");
        request.setEntity(new NStringEntity(body, ContentType.APPLICATION_JSON));
        Response response = restClient.performRequest(request);
        System.out.println("Elasticsearch users 인덱스 생성 결과: " + response.getStatusLine());
    }
}
