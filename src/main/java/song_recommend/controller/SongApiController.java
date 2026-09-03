package song_recommend.controller;

import song_recommend.model.Song;
import song_recommend.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // ← @Controllerとの違いに注目
public class SongApiController {

    @Autowired
    private SongService songService;

    @GetMapping("/api/search")
    public List<Song> search(@RequestParam String keyword) {
        return songService.searchByKeyword(keyword);
        // @RestControllerが自動でJSONに変換してくれる
    }
}