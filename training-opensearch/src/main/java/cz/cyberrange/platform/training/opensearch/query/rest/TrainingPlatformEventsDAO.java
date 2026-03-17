package cz.cyberrange.platform.training.opensearch.query.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.elasticsearch.data.enums.TrainingType;
import cz.cyberrange.platform.elasticsearch.data.exceptions.ElasticsearchTrainingDataLayerException;
import cz.cyberrange.platform.elasticsearch.data.indexpaths.AbstractCrczpElasticTermQueryFields;
import cz.cyberrange.platform.elasticsearch.data.indexpaths.AbstractCrczpIndexPath;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The type Training events dao.
 */
@Repository
public class TrainingPlatformEventsDAO extends AbstractElasticClientDAO {

    @Value("${elasticsearch.max-result-window:10000}")
    private int indexDocumentsMaxReturnNumber;

    /**
     * Instantiates a new Training events dao.
     *
     * @param restHighLevelClient the rest high level client
     * @param objectMapper        the object mapper
     */
    @Autowired
    public TrainingPlatformEventsDAO(@Qualifier("crczpRestHighLevelClient") RestHighLevelClient restHighLevelClient,
                                     @Qualifier("objMapperForElasticsearch") ObjectMapper objectMapper) {
        super(restHighLevelClient, objectMapper);
    }

    /**
     * Find all events by training definition.
     *
     * @param trainingDefinitionId the training definition id
     * @return the list
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     * @throws IOException                             the io exception
     */
    public List<Map<String, Object>> findAllEventsByTrainingDefinition(Long trainingDefinitionId, TrainingType trainingType) throws ElasticsearchTrainingDataLayerException, IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.sort(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TIMESTAMP, SortOrder.ASC);
        searchSourceBuilder.size(indexDocumentsMaxReturnNumber);
        searchSourceBuilder.timeout(new TimeValue(5, TimeUnit.MINUTES));

        SearchRequest searchRequest = new SearchRequest(getEventsIndexPath(trainingType) + "*.definition=" + trainingDefinitionId + ".*");
        searchRequest.source(searchSourceBuilder);

        return handleElasticsearchResponse(getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT));
    }

    /**
     * Find all events by training definition and training instance id list.
     *
     * @param trainingDefinitionId the training definition id
     * @param trainingInstanceId   the training instance id
     * @return the list
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     * @throws IOException                             the io exception
     */
    public List<Map<String, Object>> findAllEventsByTrainingDefinitionAndTrainingInstanceId(Long trainingDefinitionId, Long trainingInstanceId, TrainingType trainingType) throws ElasticsearchTrainingDataLayerException, IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.sort(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TIMESTAMP, SortOrder.ASC);
        searchSourceBuilder.size(indexDocumentsMaxReturnNumber);
        searchSourceBuilder.timeout(new TimeValue(5, TimeUnit.MINUTES));

        SearchRequest searchRequest = new SearchRequest(getEventsIndexPath(trainingType) + "*.definition=" + trainingDefinitionId + ".instance=" + trainingInstanceId + ".*");
        searchRequest.source(searchSourceBuilder);

        return handleElasticsearchResponse(getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT));
    }

    /**
     * Find all events from training run list.
     *
     * @param trainingDefinitionId the training definition id
     * @param trainingInstanceId   the training instance id
     * @param trainingRunId        the training run id
     * @return the list
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     * @throws IOException                             the io exception
     */
    public List<Map<String, Object>> findAllEventsFromTrainingRun(Long trainingDefinitionId, Long trainingInstanceId, Long trainingRunId, TrainingType trainingType) throws ElasticsearchTrainingDataLayerException, IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TRAINING_RUN_ID, trainingRunId));
        searchSourceBuilder.sort(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TIMESTAMP, SortOrder.ASC);
        searchSourceBuilder.size(indexDocumentsMaxReturnNumber);
        searchSourceBuilder.timeout(new TimeValue(5, TimeUnit.MINUTES));

        SearchRequest searchRequest = new SearchRequest(getEventsIndexPath(trainingType) + "*.definition=" + trainingDefinitionId + ".instance=" + trainingInstanceId + ".*");
        searchRequest.source(searchSourceBuilder);

        return handleElasticsearchResponse(getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT));
    }

    /**
     * Find all events from training run list.
     *
     * @param trainingInstanceId   the training instance id
     * @param levelId  the level id
     * @param collapseField the field used to collapse data
     * @param trainingType type of the training (linear or adaptive)
     * @return the list
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     * @throws IOException                             the io exception
     */
    public Map<Object, List<Map<String, Object>>> findEventsByInstanceAndLevelAggregatedByGivenField(Long trainingInstanceId, Long levelId, String collapseField, TrainingType trainingType) throws ElasticsearchTrainingDataLayerException, IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchQuery(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_LEVEL_ID, levelId))
                .size(indexDocumentsMaxReturnNumber)
                .timeout(new TimeValue(5, TimeUnit.MINUTES));

        InnerHitBuilder innerHitBuilder = new InnerHitBuilder().setName("by_" + collapseField)
                .setSize(indexDocumentsMaxReturnNumber)
                .addSort(SortBuilders.fieldSort("timestamp"));
        CollapseBuilder collapseBuilder = new CollapseBuilder(collapseField).setInnerHits(innerHitBuilder);
        searchSourceBuilder.collapse(collapseBuilder);

        SearchRequest searchRequest = new SearchRequest(getEventsIndexPath(trainingType) + "*.instance=" + trainingInstanceId + "*");
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);
        return handleAggregatedEvents(searchResponse, collapseField);
    }


    /**
     * Find all events by training definition and training instance id list.
     *
     * <pre>{@code
     *  GET crczp.events.trainings*.definition={definitionId}.instance={instanceId}
     *  {
     *   "query": {
     *     "match_all": {}
     *   },
     *   "collapse": {
     *     "field": "training_run_id",
     *     "inner_hits": {
     *       "name": "by_training_run_id",
     *       "sort": [{"timestamp": "asc" }, {"syslog.@timestamp": "asc" }],
     *       "size": 10000
     *     }
     *   }
     * }
     * }*
     * </pre>
     *
     * @param trainingInstanceId   the training instance id
     * @return the list
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     * @throws IOException                             the io exception
     */
    public Map<Integer, Map<Integer, List<Map<String, Object>>>> getEventsOfTrainingInstanceAggregatedByRunsAndLevels(Long trainingInstanceId) throws ElasticsearchTrainingDataLayerException, IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .sort(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TRAINING_RUN_ID, SortOrder.ASC)
                .size(indexDocumentsMaxReturnNumber)
                .timeout(new TimeValue(5, TimeUnit.MINUTES));

        InnerHitBuilder innerHitBuilder = new InnerHitBuilder().setName("by_" + AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TRAINING_RUN_ID)
                .setSize(indexDocumentsMaxReturnNumber)
                .addSort(SortBuilders.fieldSort("timestamp"));
        CollapseBuilder collapseBuilder = new CollapseBuilder(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TRAINING_RUN_ID).setInnerHits(innerHitBuilder);
        searchSourceBuilder.collapse(collapseBuilder);

        SearchRequest searchRequest = new SearchRequest(AbstractCrczpIndexPath.CRCZP_EVENTS_INDEX + "*.instance=" + trainingInstanceId + "*");
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);

        return handleAggregatedEvents(searchResponse, AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TRAINING_RUN_ID, AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_LEVEL_ID);
    }

    /**
     * Find all events by training definition and training instance id list.
     *
     * <pre>{@code
     *  GET crczp.events.trainings*.definition={definitionId}.instance={instanceId}
     *  {
     *   "query": {
     *     "match_all": {}
     *   },
     *   "collapse": {
     *     "field": "level",
     *     "inner_hits": {
     *       "name": "by_level",
     *       "sort": [{"timestamp": "asc" }, {"syslog.@timestamp": "asc" }],
     *       "size": 10000
     *     }
     *   }
     * }
     * }*
     * </pre>
     *
     * @param trainingInstanceId   the training instance id
     * @return the list
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     * @throws IOException                             the io exception
     */
    public Map<Integer, Map<Integer, List<Map<String, Object>>>> getEventsOfTrainingInstanceAggregatedByLevelsAndRuns(Long trainingInstanceId) throws ElasticsearchTrainingDataLayerException, IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .sort(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_LEVEL_ID, SortOrder.ASC)
                .size(indexDocumentsMaxReturnNumber)
                .timeout(new TimeValue(5, TimeUnit.MINUTES));

        InnerHitBuilder innerHitBuilder = new InnerHitBuilder().setName("by_" + AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_LEVEL_ID)
                .setSize(indexDocumentsMaxReturnNumber)
                .addSort(SortBuilders.fieldSort(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TRAINING_RUN_ID))
                .addSort(SortBuilders.fieldSort("timestamp"));
        CollapseBuilder collapseBuilder = new CollapseBuilder(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_LEVEL_ID).setInnerHits(innerHitBuilder);
        searchSourceBuilder.collapse(collapseBuilder);

        SearchRequest searchRequest = new SearchRequest(AbstractCrczpIndexPath.CRCZP_EVENTS_INDEX + "*.instance=" + trainingInstanceId + "*");
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);

        return handleAggregatedEvents(searchResponse, AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_LEVEL_ID, AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TRAINING_RUN_ID);
    }

    /**
     * Process and aggregate Elasticsearch response that contains events of the users.
     * @param searchResponse Elasticsearch response.
     * @return events aggregated by user.
     */
    private Map<Integer, Map<Integer, List<Map<String, Object>>>> handleAggregatedEvents(
            SearchResponse searchResponse, String firstLevelAggregationField, String secondLevelAggregationField) throws ElasticsearchTrainingDataLayerException, IOException {
        Map<Integer, Map<Integer, List<Map<String, Object>>>> aggregatedEvents = new HashMap<>();

        if (searchResponse != null) {
            SearchHits responseHits = searchResponse.getHits();
            if (responseHits != null) {
                SearchHit[] collapsedResults = responseHits.getHits();
                for (SearchHit hit : collapsedResults) {
                    Integer firstFieldValue = hit.getFields().get(firstLevelAggregationField).getValue();
                    SearchHits innerHitsByFirstField = hit.getInnerHits().get("by_" + firstLevelAggregationField);
                    aggregatedEvents.put(firstFieldValue, aggregateEventsBySecondLevelField(innerHitsByFirstField, secondLevelAggregationField));
                }
            }
        } else {
            throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic. Please, restart Elasticsearch service.");
        }
        return aggregatedEvents;
    }

    private Map<Object, List<Map<String, Object>>> handleAggregatedEvents(SearchResponse searchResponse, String aggregationField) throws ElasticsearchTrainingDataLayerException, IOException {
        Map<Object, List<Map<String, Object>>> aggregatedEventsByField = new HashMap<>();

        if (searchResponse != null) {
            SearchHits responseHits = searchResponse.getHits();
            if (responseHits != null) {
                SearchHit[] collapsedResults = responseHits.getHits();
                for (SearchHit hit : collapsedResults) {
                    Object firstFieldValue = hit.getFields().get(aggregationField).getValue();
                    SearchHits innerHitsByFirstField = hit.getInnerHits().get("by_" + aggregationField);
                    aggregatedEventsByField.put(firstFieldValue, extractDataFromSearchHits(innerHitsByFirstField.getHits()));
                }
            }
        } else {
            throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic. Please, restart Elasticsearch service.");
        }
        return aggregatedEventsByField;
    }

    /**
     * Process and aggregate sorted list of events by level.
     * @param unprocessedEvents list of SearchHits that represents events of the particular user sorted by timestamp.
     * @return events aggregated by level.
     */
    private Map<Integer, List<Map<String, Object>>> aggregateEventsBySecondLevelField(SearchHits unprocessedEvents, String secondLevelAggregationField) {
        Map<Integer, List<Map<String, Object>>> aggregatedEvents = new LinkedHashMap<>();
        List<Map<String, Object>> eventsOfField = new ArrayList<>();
        Integer fieldValue = (Integer) unprocessedEvents.getAt(0).getSourceAsMap().get(secondLevelAggregationField);

        for (SearchHit unprocessedEvent: unprocessedEvents) {
            if(!fieldValue.equals(unprocessedEvent.getSourceAsMap().get(secondLevelAggregationField))) {
                aggregatedEvents.put(fieldValue, eventsOfField);
                fieldValue = (Integer) unprocessedEvent.getSourceAsMap().get(secondLevelAggregationField);
                eventsOfField = new ArrayList<>();
            }
            eventsOfField.add(unprocessedEvent.getSourceAsMap());
        }
        aggregatedEvents.put(fieldValue, eventsOfField);
        return aggregatedEvents;
    }

    /**
     * <pre>{@code
     *  DELETE crczp.events.trainings.*.instance={instanceId}
     * }*
     * </pre>
     *
     * @param trainingInstanceId the training instance id
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     */
    public void deleteEventsByTrainingInstanceId(Long trainingInstanceId, TrainingType trainingType) throws ElasticsearchTrainingDataLayerException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(getEventsIndexPath(trainingType) + "*" + ".instance=" + trainingInstanceId + ".*");
        try {
            AcknowledgedResponse deleteIndexResponse = getRestHighLevelClient().indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            if (!deleteIndexResponse.isAcknowledged()) {
                throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic.");
            }
        } catch (IOException e) {
            throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic.");
        }
    }

    /**
     * @param trainingInstanceId the training instance id
     * @param trainingRunId      the training run id
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     */
    public void deleteEventsFromTrainingRun(Long trainingInstanceId, Long trainingRunId, TrainingType trainingType) throws ElasticsearchTrainingDataLayerException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(getEventsIndexPath(trainingType) + "*" + ".instance=" + trainingInstanceId + ".run=" + trainingRunId);
        try {
            AcknowledgedResponse deleteIndexResponse = getRestHighLevelClient().indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            if (!deleteIndexResponse.isAcknowledged()) {
                throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic.");
            }
        } catch (IOException e) {
            throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic.");
        }
    }


    /**
     * Find all events by training definition and training instance id list.
     *
     * <pre>{@code
     *  GET crczp.events.trainings*.definition={definitionId}.instance={instanceId}
     *  {
     *   "query": {
     *     "match_all": {}
     *   },
     *   "collapse": {
     *     "field": "user_ref_id",
     *     "inner_hits": {
     *       "name": "by_user",
     *       "sort": "timestamp",
     *       "size": 10000
     *     }
     *   }
     * }
     * }*
     * </pre>
     *
     * @param trainingDefinitionId the training definition id
     * @param trainingInstanceId   the training instance id
     * @return the list
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     * @throws IOException                             the io exception
     */
    public Map<Integer, Map<Integer, List<Map<String, Object>>>> findAllEventsByTrainingDefinitionAndTrainingInstanceAggregated(Long trainingDefinitionId, Long trainingInstanceId) throws ElasticsearchTrainingDataLayerException, IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.sort(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_USER_REF_ID, SortOrder.ASC);
        searchSourceBuilder.size(indexDocumentsMaxReturnNumber);
        searchSourceBuilder.timeout(new TimeValue(5, TimeUnit.MINUTES));

        //Collapse query
        InnerHitBuilder innerHitBuilder = new InnerHitBuilder().setName("by_user")
                .setSize(indexDocumentsMaxReturnNumber)
                .addSort(SortBuilders.fieldSort("timestamp"))
                .addSort(SortBuilders.fieldSort("syslog.@timestamp"));
        CollapseBuilder collapseBuilder = new CollapseBuilder("user_ref_id").setInnerHits(innerHitBuilder);
        searchSourceBuilder.collapse(collapseBuilder);

        SearchRequest searchRequest = new SearchRequest(AbstractCrczpIndexPath.CRCZP_EVENTS_INDEX + "*.definition=" + trainingDefinitionId + ".instance=" + trainingInstanceId + "*");
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);
        return handleAggregationOfEventsByUser(searchResponse);
    }

    /**
     * Process and aggregate Elasticsearch response that contains events of the users.
     * @param searchResponse Elasticsearch response.
     * @return events aggregated by user.
     */
    private Map<Integer, Map<Integer, List<Map<String, Object>>>> handleAggregationOfEventsByUser(SearchResponse searchResponse) throws ElasticsearchTrainingDataLayerException, IOException {
        Map<Integer, Map<Integer, List<Map<String, Object>>>> aggregatedEventsByUserRefId = new HashMap<>();

        if (searchResponse != null) {
            SearchHits responseHits = searchResponse.getHits();
            if (responseHits != null) {
                SearchHit[] collapsedResults = responseHits.getHits();
                for (SearchHit hit : collapsedResults) {
                    Integer userRefId = hit.getFields().get("user_ref_id").getValue();
                    SearchHits innerHitsByUser = hit.getInnerHits().get("by_user");
                    aggregatedEventsByUserRefId.put(userRefId, aggregateEventsOfUserByLevels(innerHitsByUser));
                }
            }
        } else {
            throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic. Please, restart Elasticsearch service.");
        }
        return aggregatedEventsByUserRefId;
    }

    /**
     * Process and aggregate sorted list of events by level.
     * @param unprocessedEvents list of SearchHits that represents events of the particular user sorted by timestamp.
     * @return events aggregated by level.
     */
    private Map<Integer, List<Map<String, Object>>> aggregateEventsOfUserByLevels(SearchHits unprocessedEvents) {
        Map<Integer, List<Map<String, Object>>> aggregatedEvents = new HashMap<>();
        List<Map<String, Object>> eventsOfLevel = new ArrayList<>();
        Integer levelId = (Integer) unprocessedEvents.getAt(0).getSourceAsMap().get("level");

        for (SearchHit unprocessedEvent: unprocessedEvents) {
            if(!levelId.equals(unprocessedEvent.getSourceAsMap().get("level"))) {
                aggregatedEvents.put(levelId, eventsOfLevel);
                levelId = (Integer) unprocessedEvent.getSourceAsMap().get("level");
                eventsOfLevel = new ArrayList<>();
            }
            eventsOfLevel.add(unprocessedEvent.getSourceAsMap());
        }
        return aggregatedEvents;
    }

    private List<Map<String, Object>> handleElasticsearchResponse(SearchResponse response) throws ElasticsearchTrainingDataLayerException {
        if (response != null) {
            SearchHits responseHits = response.getHits();
            return responseHits != null ? extractDataFromSearchHits(responseHits.getHits()) : new ArrayList<>();
        } else {
            throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic. Please, restart Elasticsearch service.");
        }
    }

    private List<Map<String, Object>> extractDataFromSearchHits(SearchHit[] hits) {
        List<Map<String, Object>> events = new ArrayList<>();
        for (SearchHit hit : hits) {
            Map<String, Object> source = hit.getSourceAsMap();
            events.add(source);
        }
        return events;
    }

    private String getEventsIndexPath(TrainingType trainingType) {
        switch (trainingType) {
            case ADAPTIVE:
                return AbstractCrczpIndexPath.CRCZP_ADAPTIVE_EVENTS_INDEX;
            case LINEAR:
                return AbstractCrczpIndexPath.CRCZP_EVENTS_INDEX;
            default:
                return null;
        }
    }
}
