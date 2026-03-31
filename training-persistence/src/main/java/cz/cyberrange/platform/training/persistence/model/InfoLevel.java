package cz.cyberrange.platform.training.persistence.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Class specifying Abstract level as Info level. Info levels contain information for trainees. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "info_level")
@PrimaryKeyJoinColumn(name = "id")
public class InfoLevel extends AbstractLevel {

  @Lob
  @Column(name = "content", nullable = false)
  private String content;

  @Override
  public int hashCode() {
    return Objects.hashCode(content);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof InfoLevel)) return false;
    InfoLevel other = (InfoLevel) obj;
    return Objects.equals(content, other.getContent());
  }

  @Override
  public String toString() {
    return "InfoLevel{" + "content='" + content + '\'' + '}';
  }
}
