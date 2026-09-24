package song_recommend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import song_recommend.model.Keyword;
import song_recommend.model.Song;
import song_recommend.repository.KeywordRepository;
import song_recommend.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SongService implements CommandLineRunner {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Override
    public void run(String... args) {
        if (songRepository.count() == 0) {
            loadSongsFromCsv();
        }
    }

    private void loadSongsFromCsv() {
        try {
            org.springframework.core.io.ClassPathResource resource =
                    new org.springframework.core.io.ClassPathResource("songs.csv"); 
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { // 1行目（ヘッダー）は読み飛ばす
                    firstLine = false;
                    continue;
                }
                if (line.isBlank()) continue;

             // title, artist, keywords の3つに分割
                String[] parts = line.split(",", 3); 
                String title = parts[0].trim();
                String artist = parts[1].trim();
                List<String> keywords = Arrays.asList(parts[2].trim().split(";"));

                // 外部APIの呼び出しと保存
                addSong(title, artist, keywords);
                
                System.out.println("登録しました: " + title + " / " + artist);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addSong(String title, String artist, List<String> keywords) {
        // すでに同じ曲名・アーティストの組み合わせが登録されていないかチェック
        boolean alreadyExists = songRepository.findAll().stream()
                .anyMatch(s -> s.getTitle().equals(title) && s.getArtist().equals(artist));
        if (alreadyExists) {
            System.out.println("すでに登録済みのためスキップ: " + title + " / " + artist);
            return;
        }

        SpotifyResult spotify = fetchFromSpotify(title, artist);
        String appleMusicUrl = fetchAppleMusicUrl(title, artist);
        String youtubeUrl = buildYoutubeSearchUrl(title, artist);

        String imageUrl = (spotify != null) ? spotify.imageUrl : "https://picsum.photos/seed/fallback/200";
        String spotifyUrl = (spotify != null) ? spotify.trackUrl : null;
 
        // キーワードの重複を避ける
        List<Keyword> resolvedKeywords = resolveKeywords(keywords);

       // DBに書き込む　※save()はJPA（ライブラリ）が元々定義しているメソッド
        songRepository.save(new Song(title, artist, resolvedKeywords, imageUrl, spotifyUrl, appleMusicUrl, youtubeUrl));
    }

    // 文字列のキーワードを、既存のKeywordがあれば再利用し、なければ新規作成する
    // DBにキーワードの重複がないようにする
    private List<Keyword> resolveKeywords(List<String> keywordStrings) {
        List<Keyword> result = new ArrayList<>();
        for (String kw : keywordStrings) {
            Keyword keyword = keywordRepository.findByKeyword(kw)
                    .orElseGet(() -> keywordRepository.save(new Keyword(kw)));
            result.add(keyword);
        }
        return result;
    }

    private static class SpotifyResult {
        String imageUrl;
        String trackUrl;
    }

    @SuppressWarnings("unchecked")
    private SpotifyResult fetchFromSpotify(String title, String artist) {
        try {
            String token = getAccessToken();
            if (token == null) return null;

            String query = title + " " + artist;
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            String url = "https://api.spotify.com/v1/search?q=" + encoded + "&type=track&market=JP&limit=5";

            RestTemplate rt = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            URI uri = new URI(url);

            String jsonString = rt.exchange(uri, HttpMethod.GET, entity, String.class).getBody();

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> response = mapper.readValue(jsonString, Map.class);
            Map<String, Object> tracksObj = (Map<String, Object>) response.get("tracks");
            List<Map<String, Object>> items = (List<Map<String, Object>>) tracksObj.get("items");

            if (!items.isEmpty()) {
                Map<String, Object> first = items.get(0);
                Map<String, Object> album = (Map<String, Object>) first.get("album");
                List<Map<String, Object>> images = (List<Map<String, Object>>) album.get("images");
                Map<String, Object> externalUrls = (Map<String, Object>) first.get("external_urls");

                SpotifyResult result = new SpotifyResult();
                result.imageUrl = images.isEmpty() ? null : (String) images.get(0).get("url");
                result.trackUrl = (String) externalUrls.get("spotify");
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String getAccessToken() {
        try {
            RestTemplate rt = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(clientId, clientSecret);
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            String response = rt.postForObject("https://accounts.spotify.com/api/token", request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> json = mapper.readValue(response, Map.class);
            return (String) json.get("access_token");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String fetchAppleMusicUrl(String title, String artist) {
        try {
            String query = title + " " + artist;
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            String url = "https://itunes.apple.com/search?term=" + encoded + "&entity=song&limit=1&country=JP";

            RestTemplate rt = new RestTemplate();
            URI uri = new URI(url);
            String jsonString = rt.exchange(uri, HttpMethod.GET, null, String.class).getBody();

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> response = mapper.readValue(jsonString, Map.class);
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

            if (results != null && !results.isEmpty()) {
                return (String) results.get(0).get("trackViewUrl");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String buildYoutubeSearchUrl(String title, String artist) {
        try {
            String query = title + " " + artist;
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            return "https://www.youtube.com/results?search_query=" + encoded;
        } catch (Exception e) {
            e.printStackTrace();
            return "https://www.youtube.com";
        }
    }

    // keywordsがKeywordオブジェクトのリストになったので、getKeyword()で文字列を取り出して比較する
    public List<Song> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return songRepository.findAll().stream()
                .filter(song -> song.getKeywords().stream()
                        .anyMatch(k -> k.getKeyword().equals(keyword)))
                .collect(Collectors.toList());
    }
    
 // 複数キーワード（スペース区切り）でAND検索する
    public List<Song> searchByKeywords(String keywordText) {
        if (keywordText == null || keywordText.isBlank()) {
            return List.of();
        }
        String[] keywords = keywordText.trim().split("\\s+");

        return songRepository.findAll().stream()
                .filter(song -> Arrays.stream(keywords).allMatch(kw ->
                        song.getKeywords().stream().anyMatch(k -> k.getKeyword().equals(kw))
                ))
                .collect(Collectors.toList());
    }

    // ランダムなキーワードを取得する
    public List<String> getRandomKeywords(int count) {
        return keywordRepository.findRandomKeywords(count).stream()
                .map(Keyword::getKeyword)
                .collect(Collectors.toList());
    }
    
    
    
    
    
}