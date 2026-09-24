package song_recommend.repository;

import song_recommend.model.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    Optional<Keyword> findByKeyword(String keyword);

    // ランダムにキーワードを取得する（H2のRANDOM()を使う）
    @Query(value = "SELECT * FROM keyword ORDER BY RANDOM() LIMIT :count", nativeQuery = true)
    List<Keyword> findRandomKeywords(int count);
}