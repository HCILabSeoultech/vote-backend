package project.votebackend.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.votebackend.domain.category.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("select c.categoryId from Category c where c.categoryId not in :ids")
    List<Long> findAllCategoryIdsExcept(@Param("ids") List<Long> ids);
}
