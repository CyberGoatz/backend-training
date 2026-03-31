package cz.cyberrange.platform.training.persistence.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Abstract entity.
 *
 * @param <PK> Primary key for a given entity.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@MappedSuperclass
public class AbstractEntity<PK extends Serializable> implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, insertable = false)
  private PK id;

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AbstractEntity other) {
      return id.equals(other.id);
    }
    return false;
  }
}
