package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.TrainingLevelResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingLevelResultRepository extends JpaRepository<TrainingLevelResult, Long> {

    Optional<TrainingLevelResult> findByTrainingRunIdAndLevelId(Long trainingRunId, Long levelId);

    List<TrainingLevelResult> findAllByTrainingRunIdIn(Collection<Long> trainingRunIds);

    @Modifying
    void deleteAllByTrainingRunId(Long trainingRunId);
}
