package project.votebackend.dto.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import project.votebackend.domain.search.NewsSearch;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SearchDto {
    private Long id;
    private String keyword;
    private LocalDateTime createdAt;

    public static SearchDto fromEntity(NewsSearch newsSearch) {
        return new SearchDto(
                newsSearch.getSearchId(),
                newsSearch.getKeyword(),
                newsSearch.getCreatedAt()
        );
    }
}
