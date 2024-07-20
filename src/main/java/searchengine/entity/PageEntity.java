package searchengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "page")
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "page_id_seq")
    @SequenceGenerator(name = "page_id_seq", sequenceName = "page_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false)
    private SiteEntity site;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL)
    private List<IndexEntity> indexEntities = new ArrayList<>();

    @Column(columnDefinition = "VARCHAR(300)", nullable = false)
    private String path;

    @Column(name = "code", nullable = false)
    private int code;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
}
