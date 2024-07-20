package searchengine.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "indexation_flag")
public class IndexationFlagEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "indexation_flag_id_seq")
    @SequenceGenerator(name = "indexation_flag_id_seq", sequenceName = "indexation_flag_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "is_indexation_running")
    private boolean isIndexationRunning;

    @Column(name = "is_index_one_page_active")
    private boolean isIndexOnePageActive;
}
