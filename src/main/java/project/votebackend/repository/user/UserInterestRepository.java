package project.votebackend.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.user.User;
import project.votebackend.domain.user.UserInterest;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    void deleteByUser(User user);

    List<UserInterest> findByUser(User user);
}
