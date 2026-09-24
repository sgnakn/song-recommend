package song_recommend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String keyword;

    public Keyword() {}

    public Keyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getId() { return id; }
    public String getKeyword() { return keyword; }
}