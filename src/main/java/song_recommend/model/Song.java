package song_recommend.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String artist;
    private String imageUrl;
    private String spotifyUrl;
    private String appleMusicUrl;
    private String youtubeUrl;
    
    @ManyToMany
    @JoinTable(
        name = "song_keyword_relation",
        joinColumns = @JoinColumn(name = "song_id"),
        inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    private List<Keyword> keywords;

    public Song() {}

    public Song(String title, String artist, List<Keyword> keywords, String imageUrl,
                String spotifyUrl, String appleMusicUrl, String youtubeUrl) {
        this.title = title;
        this.artist = artist;
        this.keywords = keywords;
        this.imageUrl = imageUrl;
        this.spotifyUrl = spotifyUrl;
        this.appleMusicUrl = appleMusicUrl;
        this.youtubeUrl = youtubeUrl;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public List<Keyword> getKeywords() { return keywords; }
    public String getImageUrl() { return imageUrl; }
    public String getSpotifyUrl() { return spotifyUrl; }
    public String getAppleMusicUrl() { return appleMusicUrl; }
    public String getYoutubeUrl() { return youtubeUrl; }
}