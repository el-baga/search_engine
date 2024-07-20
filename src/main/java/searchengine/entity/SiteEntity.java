package searchengine.entity;

import lombok.*;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "site")
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "site_id_seq")
    @SequenceGenerator(name = "site_id_seq", sequenceName = "site_id_seq", allocationSize = 1)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "site", cascade = CascadeType.ALL)
    private List<PageEntity> pageEntities = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "site", cascade = CascadeType.ALL)
    private List<LemmaEntity> lemmaEntities = new ArrayList<>();

    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(255)")
    @Enumerated(EnumType.STRING)
    private StatusType status;

    @Column(name = "status_time", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "url", nullable = false, columnDefinition = "VARCHAR(255)")
    private String url;

    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;
}
