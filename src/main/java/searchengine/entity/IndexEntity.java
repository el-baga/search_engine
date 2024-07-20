package searchengine.entity;

import lombok.*;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "search_index")
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "search_index_id_seq")
    @SequenceGenerator(name = "search_index_id_seq", sequenceName = "search_index_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", referencedColumnName = "id", nullable = false)
    private LemmaEntity lemma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", referencedColumnName = "id", nullable = false)
    private PageEntity page;

    @Column(name = "index_rank", nullable = false)
    private float rank;
}
