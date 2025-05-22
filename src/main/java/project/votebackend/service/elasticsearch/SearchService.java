package project.votebackend.service.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.votebackend.elasticSearch.UserDocument;
import project.votebackend.elasticSearch.VoteDocument;
import co.elastic.clients.elasticsearch.core.search.Hit;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient elasticsearchClient;

    // [투표 검색] 키워드를 기반으로 vote 문서(votes 인덱스)를 검색
    public List<VoteDocument> searchVotes(String keyword) throws IOException {
        SearchResponse<VoteDocument> response = elasticsearchClient.search(s -> s
                        .index("votes")         // 검색 대상 인덱스
                        .size(20)               // 최대 검색 결과 개수
                        .query(q -> q.bool(b -> b // Bool 쿼리로 다양한 조건 결합
                                .should(m -> m.match(mm -> mm
                                        .field("title.ngram")     // ngram 기반: 일부 단어 매칭 가능 (자동완성)
                                        .query(keyword)
                                        .boost(4.0f)))            // 중요도 가장 높음
                                .should(m -> m.matchPhrase(mp -> mp
                                        .field("title.standard")  // 정확한 문장 전체 매칭
                                        .query(keyword)))
                                .should(m -> m.match(mm -> mm
                                        .field("title")           // 일반 title 필드에 대해 오타 허용
                                        .query(keyword)
                                        .fuzziness("AUTO")        // 자동 오타 허용 (Levenshtein Distance)
                                        .boost(2.0f)))            // 가중치 중간
                                .should(m -> m.match(mm -> mm
                                        .field("username")        // 작성자 username 검색 (오타 허용)
                                        .query(keyword)
                                        .fuzziness("AUTO")))
                                .should(m -> m.term(t -> t
                                        .field("category.keyword") // 정확한 카테고리 명칭
                                        .value(keyword)))
                        )),
                VoteDocument.class
        );

        // 검색 결과에서 source (VoteDocument)만 추출하여 리스트로 반환
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    // [유저 검색] 키워드를 기반으로 user 문서(users 인덱스)를 검색
    public List<UserDocument> searchUsers(String keyword) throws IOException {
        SearchResponse<UserDocument> response = elasticsearchClient.search(s -> s
                        .index("users")         // 검색 대상 인덱스
                        .size(50)               // 최대 검색 결과 개수
                        .query(q -> q.bool(b -> b
                                // edge ngram 기반: 접두어 자동완성
                                .should(QueryBuilders.match(m -> m
                                        .field("username.edge")
                                        .query(keyword)
                                        .boost(3.0f)))
                                // 일반 ngram 기반: 중간 문자열 검색 가능
                                .should(QueryBuilders.match(m -> m
                                        .field("username.ngram")
                                        .query(keyword)
                                        .boost(2.0f)))
                                // 표준 분석기로 오타 허용 검색
                                .should(QueryBuilders.match(m -> m
                                        .field("username.standard")
                                        .query(keyword)
                                        .fuzziness("AUTO")
                                        .boost(1.0f)))
                        )),
                UserDocument.class
        );

        // 검색 결과에서 source (UserDocument)만 추출하여 리스트로 반환
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }
}
