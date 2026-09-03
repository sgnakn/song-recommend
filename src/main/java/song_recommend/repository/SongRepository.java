package song_recommend.repository;

import song_recommend.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {
    // 中身は空でOK
}