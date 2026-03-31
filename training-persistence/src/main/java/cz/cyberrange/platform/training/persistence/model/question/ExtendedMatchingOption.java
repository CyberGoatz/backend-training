package cz.cyberrange.platform.training.persistence.model.question;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "extend_matching_option")
public class ExtendedMatchingOption {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "extendedMatchingOptionGenerator")
  @SequenceGenerator(
      name = "extendedMatchingOptionGenerator",
      sequenceName = "extended_matching_option_seq")
  @Column(name = "extend_matching_option_id", nullable = false, unique = true)
  private Long id;

  @Column(name = "text")
  private String text;

  @Column(name = "order_in_row")
  private int order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id")
  private Question question;
}
