package cz.cyberrange.platform.training.api.dto.archive;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QuestionAnswersDetailsDTO {
  private String question;
  private Map<String, Integer> answers = new HashMap<>();
  private int totalAnswers;

  public QuestionAnswersDetailsDTO(String question) {
    this.question = question;
  }

  public void addAnswers(Set<String> answers) {
    totalAnswers++;
    for (String answer : answers) {
      Integer numberOfAnswers = this.answers.getOrDefault(answer, 0) + 1;
      this.answers.put(answer, numberOfAnswers);
    }
  }
}
