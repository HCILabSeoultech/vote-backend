package project.votebackend.domain.rank;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_user_ranking")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyUserRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;
    private int month;

    private Long userId;
    private Long score;

    private LocalDateTime createdAt;
}
