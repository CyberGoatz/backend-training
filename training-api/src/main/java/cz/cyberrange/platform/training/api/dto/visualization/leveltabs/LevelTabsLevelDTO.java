package cz.cyberrange.platform.training.api.dto.visualization.leveltabs;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LevelTabsLevelDTO extends VisualizationAbstractLevelDTO {

  private final String title;
  private final long estimatedTime;
  private final int maxLevelScore;
  private final String content;
  private final String correctAnswer;
  private final List<LevelTabsHintDTO> hints;
  private final List<LevelTabsPlayerDTO> players;
  private final AssessmentType assessmentType;

  public LevelTabsLevelDTO(LevelTabsLevelBuilder builder) {
    super(builder);
    this.title = builder.title;
    this.estimatedTime = builder.estimatedTime;
    this.maxLevelScore = builder.maxLevelScore;
    this.content = builder.content;
    this.correctAnswer = builder.correctAnswer;
    this.hints = builder.hints;
    this.players = builder.players;
    this.assessmentType = builder.assessmentType;
  }

  public String getTitle() {
    return title;
  }

  public long getEstimatedTime() {
    return estimatedTime;
  }

  public int getMaxLevelScore() {
    return maxLevelScore;
  }

  public String getContent() {
    return content;
  }

  public String getCorrectAnswer() {
    return correctAnswer;
  }

  public List<LevelTabsHintDTO> getHints() {
    return hints;
  }

  public List<LevelTabsPlayerDTO> getPlayers() {
    return players;
  }

  public AssessmentType getAssessmentType() {
    return assessmentType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    LevelTabsLevelDTO that = (LevelTabsLevelDTO) o;
    return getTitle().equals(that.getTitle());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getTitle());
  }

  public static class LevelTabsLevelBuilder
      extends VisualizationAbstractLevelDTO.BaseBuilder<LevelTabsLevelDTO, LevelTabsLevelBuilder> {

    private String title;
    private long estimatedTime;
    private String content;
    private int maxLevelScore;
    private String correctAnswer;
    private List<LevelTabsHintDTO> hints;
    private List<LevelTabsPlayerDTO> players = new ArrayList<>();
    private AssessmentType assessmentType;

    @Override
    protected LevelTabsLevelBuilder getActualBuilder() {
      return this;
    }

    public LevelTabsLevelBuilder assessmentType(AssessmentType assessmentType) {
      this.assessmentType = assessmentType;
      return actualClassBuilder;
    }

    public LevelTabsLevelBuilder title(String title) {
      this.title = title;
      return actualClassBuilder;
    }

    public LevelTabsLevelBuilder estimatedTime(long estimatedTime) {
      this.estimatedTime = estimatedTime;
      return actualClassBuilder;
    }

    public LevelTabsLevelBuilder maxLevelScore(int maxLevelScore) {
      this.maxLevelScore = maxLevelScore;
      return actualClassBuilder;
    }

    public LevelTabsLevelBuilder content(String content) {
      this.content = content;
      return actualClassBuilder;
    }

    public LevelTabsLevelBuilder correctAnswer(String correctAnswer) {
      this.correctAnswer = correctAnswer;
      return this;
    }

    public LevelTabsLevelBuilder hints(List<LevelTabsHintDTO> hints) {
      this.hints = hints;
      return this;
    }

    public LevelTabsLevelBuilder players(List<LevelTabsPlayerDTO> players) {
      this.players = players;
      return this;
    }

    @Override
    public LevelTabsLevelDTO build() {
      return new LevelTabsLevelDTO(this);
    }
  }
}
