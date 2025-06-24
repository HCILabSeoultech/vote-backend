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
    public void createVotesIndex() {
        try {
            // 1. 인덱스 존재 여부 확인
            Request checkRequest = new Request("HEAD", "/votes");
            Response checkResponse = restClient.performRequest(checkRequest);
            int statusCode = checkResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                System.out.println("✅ votes 인덱스 이미 존재함. 생략");
                return;
            }
        } catch (IOException e) {
            System.out.println("⚠️ 인덱스 존재 확인 실패 (ES 준비 안됐을 수 있음): " + e.getMessage());
            // 계속 시도함 (최초 실행이거나 아직 연결 안 됐을 수도 있음)
        }

        try {
            // 2. 인덱스 생성
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

            Request createRequest = new Request("PUT", "/votes");
            createRequest.setEntity(new NStringEntity(body, ContentType.APPLICATION_JSON));
            Response createResponse = restClient.performRequest(createRequest);
            System.out.println("✅ votes 인덱스 생성 완료: " + createResponse.getStatusLine());
        } catch (IOException e) {
            System.err.println("❌ 인덱스 생성 실패: " + e.getMessage());
        }
    }

    public void createUsersIndex() {
        try {
            Request checkRequest = new Request("HEAD", "/users");
            Response checkResponse = restClient.performRequest(checkRequest);
            if (checkResponse.getStatusLine().getStatusCode() == 200) {
                System.out.println("✅ users 인덱스 이미 존재함. 생략");
                return;
            }
        } catch (IOException e) {
            System.out.println("⚠️ 인덱스 존재 확인 실패 (ES 준비 안됐을 수 있음): " + e.getMessage());
        }

        try {
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

            Request createRequest = new Request("PUT", "/users");
            createRequest.setEntity(new NStringEntity(body, ContentType.APPLICATION_JSON));
            Response createResponse = restClient.performRequest(createRequest);
            System.out.println("✅ users 인덱스 생성 완료: " + createResponse.getStatusLine());
        } catch (IOException e) {
            System.err.println("❌ 인덱스 생성 실패: " + e.getMessage());
        }
    }
}
