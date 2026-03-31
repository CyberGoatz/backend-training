package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.question.QuestionAnswer;
import cz.cyberrange.platform.training.persistence.model.question.QuestionAnswerId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionAnswerRepository
    extends JpaRepository<QuestionAnswer, QuestionAnswerId>,
        QuerydslPredicateExecutor<QuestionAnswer> {

  @Modifying
  void deleteAllByTrainingRunId(Long trainingRunId);

  List<QuestionAnswer> getAllByTrainingRunId(Long trainingRunId);

  List<QuestionAnswer> getAllByQuestionIdAndInstanceId(Long questionId, Long instanceId);
}
