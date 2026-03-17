package cz.cyberrange.platform.training.opensearch.query.service;

import cz.cyberrange.platform.training.opensearch.query.rest.TrainingPlatformEventsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * The type Training events service.
 */
@Service
public class TrainingPlatformEventsService {

    private final TrainingPlatformEventsDAO trainingPlatformEventsDAO;

    /**
     * Instantiates a new Training events service.
     *
     * @param trainingPlatformEventsDAO the training events dao
     */
    @Autowired
    public TrainingPlatformEventsService(TrainingPlatformEventsDAO trainingPlatformEventsDAO) {
        this.trainingPlatformEventsDAO = trainingPlatformEventsDAO;
    }

    /**
     * Find all events by training definition.
     *
     * @param trainingDefinitionId the training definition id
     * @return the list
     * @throws ElasticsearchTrainingServiceLayerException the elasticsearch training service layer exception
     */
    public List<Map<String, Object>> findAllEventsByTrainingDefinition(Long trainingDefinitionId, TrainingType trainingType) throws ElasticsearchTrainingServiceLayerException {
        try {
            List<Map<String, Object>> eventsFromElasticsearch = trainingPlatformEventsDAO.findAllEventsByTrainingDefinition(trainingDefinitionId, trainingType);
            eventsFromElasticsearch.sort(Comparator.comparing(map -> Long.valueOf(map.get("timestamp").toString())));
            return eventsFromElasticsearch;
        } catch (ElasticsearchTrainingDataLayerException | IOException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

    /**
     * Find all events by training definition and training instance id list.
     *
     * @param trainingDefinitionId the training definition id
     * @param trainingInstanceId   the training instance id
     * @return the list
     * @throws ElasticsearchTrainingServiceLayerException the elasticsearch training service layer exception
     */
    public List<Map<String, Object>> findAllEventsByTrainingDefinitionAndTrainingInstanceId(Long trainingDefinitionId, Long trainingInstanceId, TrainingType trainingType) throws ElasticsearchTrainingServiceLayerException {
        try {
            List<Map<String, Object>> eventsFromElasticsearch = trainingPlatformEventsDAO.findAllEventsByTrainingDefinitionAndTrainingInstanceId(trainingDefinitionId, trainingInstanceId, trainingType);
            eventsFromElasticsearch.sort(Comparator.comparing(map -> Long.valueOf(map.get("timestamp").toString())));
            return eventsFromElasticsearch;
        } catch (ElasticsearchTrainingDataLayerException | IOException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

    /**
     * Find all events from training run list.
     *
     * @param trainingDefinitionId the training definition id
     * @param trainingInstanceId   the training instance id
     * @param trainingRunId        the training run id
     * @return the list
     * @throws ElasticsearchTrainingServiceLayerException the elasticsearch training service layer exception
     */
    public List<Map<String, Object>> findAllEventsFromTrainingRun(Long trainingDefinitionId, Long trainingInstanceId, Long trainingRunId, TrainingType trainingType) throws ElasticsearchTrainingServiceLayerException {
        try {
            List<Map<String, Object>> eventsFromElasticsearch = trainingPlatformEventsDAO.findAllEventsFromTrainingRun(trainingDefinitionId, trainingInstanceId, trainingRunId, trainingType);
            eventsFromElasticsearch.sort(Comparator.comparing(map -> Long.valueOf(map.get("timestamp").toString())));
            return eventsFromElasticsearch;
        } catch (ElasticsearchTrainingDataLayerException | IOException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

    /**
     * Find all events from training run list by levelId
     *
     * @param trainingInstanceId   the training instance id
     * @param levelId  the level id
     * @param collapseField the field used to collapse data
     * @param trainingType type of the training (linear or adaptive)
     * @return the map of the events aggregated by given field
     * @throws ElasticsearchTrainingServiceLayerException the elasticsearch training service layer exception
     */
    public Map<Object, List<Map<String, Object>>> findAllEventsFromTrainingRunByLevelId(Long trainingInstanceId, Long levelId, String collapseField, TrainingType trainingType) throws ElasticsearchTrainingServiceLayerException {
        try {
            return trainingPlatformEventsDAO.findEventsByInstanceAndLevelAggregatedByGivenField(trainingInstanceId, levelId, collapseField, trainingType);
        } catch (ElasticsearchTrainingDataLayerException | IOException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

    /**
     * Find all events by training definition and training instance aggregated by user and levels.
     *
     * @param trainingDefinitionId the training definition id
     * @param trainingInstanceId   the training instance id
     * @return the aggregated events by user and levels sorted by timestamp.
     * @throws ElasticsearchTrainingServiceLayerException the elasticsearch training service layer exception
     */
    public Map<Integer, Map<Integer, List<Map<String, Object>>>> findAllEventsByTrainingDefinitionAndTrainingInstanceAggregated(Long trainingDefinitionId, Long trainingInstanceId) throws ElasticsearchTrainingServiceLayerException {
        try {
            return trainingPlatformEventsDAO.findAllEventsByTrainingDefinitionAndTrainingInstanceAggregated(trainingDefinitionId, trainingInstanceId);
        } catch (ElasticsearchTrainingDataLayerException | IOException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

    /**
     * Delete events by training instance id.
     *
     * @param instanceId the instance id
     * @throws ElasticsearchTrainingServiceLayerException the elasticsearch training service layer exception
     */
    public void deleteEventsByTrainingInstanceId(Long instanceId, TrainingType trainingType) throws ElasticsearchTrainingServiceLayerException {
        try {
            trainingPlatformEventsDAO.deleteEventsByTrainingInstanceId(instanceId, trainingType);
        } catch (ElasticsearchTrainingDataLayerException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

    /**
     * Delete events from training run.
     *
     * @param trainingInstanceId the training instance id
     * @param trainingRunId      the training run id
     * @throws ElasticsearchTrainingServiceLayerException the elasticsearch training service layer exception
     */
    public void deleteEventsFromTrainingRun(Long trainingInstanceId, Long trainingRunId, TrainingType trainingType) throws ElasticsearchTrainingServiceLayerException {
        try {
            trainingPlatformEventsDAO.deleteEventsFromTrainingRun(trainingInstanceId, trainingRunId, trainingType);
        } catch (ElasticsearchTrainingDataLayerException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

    public Map<Integer, Map<Integer, List<Map<String, Object>>>> getAllEventsOfTrainingInstanceAggregatedByRunsAndLevels(Long trainingInstanceId) throws ElasticsearchTrainingServiceLayerException {
        try {
            return trainingPlatformEventsDAO.getEventsOfTrainingInstanceAggregatedByRunsAndLevels(trainingInstanceId);
        } catch (ElasticsearchTrainingDataLayerException | IOException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

    public Map<Integer, Map<Integer, List<Map<String, Object>>>> getAllEventsOfTrainingInstanceAggregatedByLevelsAndRuns(Long trainingInstanceId) throws ElasticsearchTrainingServiceLayerException {
        try {
            return trainingPlatformEventsDAO.getEventsOfTrainingInstanceAggregatedByLevelsAndRuns(trainingInstanceId);
        } catch (ElasticsearchTrainingDataLayerException | IOException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }
}

