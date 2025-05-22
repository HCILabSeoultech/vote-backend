package project.votebackend.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.user.UserInterest;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
}
