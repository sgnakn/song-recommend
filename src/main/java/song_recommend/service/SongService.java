package song_recommend.service;

import song_recommend.model.Song;
import song_recommend.repository.SongRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SongService implements CommandLineRunner {

    @Autowired
    private SongRepository songRepository;

    // アプリ起動時に一度だけ実行される（初期データを入れる）
    @Override
    public void run(String... args) {
        if (songRepository.count() == 0) { // 二重登録を防ぐ
            songRepository.save(new Song("夏の陽炎", "アーティストA",
                    Arrays.asList("夏", "青春", "アップテンポ"), "https://picsum.photos/seed/song1/200"));
            songRepository.save(new Song("雨のち晴れ", "アーティストB",
                    Arrays.asList("失恋", "雨", "バラード"), "https://picsum.photos/seed/song2/200"));
            songRepository.save(new Song("走り出せ", "アーティストC",
                    Arrays.asList("応援", "スポーツ", "アップテンポ"), "https://picsum.photos/seed/song3/200"));
            songRepository.save(new Song("静かな夜に", "アーティストD",
                    Arrays.asList("夜", "バラード", "癒し"), "https://picsum.photos/seed/song4/200"));
        }
    }

    public List<Song> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return songRepository.findAll().stream()
                .filter(song -> song.getKeywords().stream()
                        .anyMatch(k -> k.contains(keyword)))
                .collect(Collectors.toList());
    }
}