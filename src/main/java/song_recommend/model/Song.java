package song_recommend.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.List;

@Entity
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String artist;
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "song_keywords")
    private List<String> keywords;

    public Song() {}

    public Song(String title, String artist, List<String> keywords, String imageUrl) {
        this.title = title;
        this.artist = artist;
        this.keywords = keywords;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public List<String> getKeywords() { return keywords; }
    public String getImageUrl() { return imageUrl; }
}