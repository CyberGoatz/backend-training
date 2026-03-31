package cz.cyberrange.platform.training.opensearch.query.rest;

import static cz.cyberrange.platform.elasticsearch.data.indexpaths.AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_EVENT_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.elasticsearch.data.exceptions.ElasticsearchTrainingDataLayerException;
import cz.cyberrange.platform.elasticsearch.data.indexpaths.AbstractCrczpElasticTermQueryFields;
import cz.cyberrange.platform.elasticsearch.data.indexpaths.AbstractCrczpIndexPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Min;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/** The type Adaptive Training Statistics dao. */
@Repository
public class AdaptiveTrainingStatisticsDAO extends AbstractElasticClientDAO {

  private static final int INDEX_DOCUMENTS_MIN_RETURN_NUMBER = 0;
  private static final String MAX_TIMESTAMP_SUB_AGGREGATION = "max_timestamp";
  private static final String MIN_TIMESTAMP_SUB_AGGREGATION = "min_timestamp";
  private static final String TYPE_FILTER_SUB_AGGREGATION = "type_filter";
  private static final String KEYWORD_FILTER_SUB_AGGREGATION = "keyword_filter";
  private static final String PHASES_AGGREGATION = "phases_aggregation";
  private static final String PHASES_COLLAPSE = "phases_collapse";

  @Value("${elasticsearch.max-result-window:10000}")
  private int indexDocumentsMaxReturnNumber;

  /**
   * Instantiates a new Adaptive Training Statistics dao.
   *
   * @param restHighLevelClient the rest high level client
   * @param objectMapper the object mapper
   */
  @Autowired
  public AdaptiveTrainingStatisticsDAO(
      @Qualifier("crczpRestHighLevelClient") RestHighLevelClient restHighLevelClient,
      @Qualifier("objMapperForElasticsearch") ObjectMapper objectMapper) {
    super(restHighLevelClient, objectMapper);
  }

  /**
   * Count the number of specified events in the given phases.
   *
   * <pre>{@code
   *  GET crczp.events.adaptive.trainings*.run=${RUN_ID}/_search
   * {
   *   "size": 0,
   *   "query": {
   *     "bool" : { "should" : [ {"match" : { "phase_id" : ${PHASE_ID} }}] }
   *   },
   *   "aggs": {
   *     "phases_agg": { "terms": { "field": "phase_id"},
   *       "aggs": {
   *         "type_filters": { "filters" : {
   *               "filters" : [ { "wildcard" : { "type" : "*${EVENT_TYPE}*"   }}, { "wildcard" : { "type" : "*${EVENT_TYPE}*" }} ]
   *            }
   *          }
   *        }
   *     }
   *   }
   * }
   * }*
   * </pre>
   *
   * @param trainingRunId the training run id
   * @param phaseIds ids of phases to count event
   * @param events events to count
   * @return the mapping of phases and number of event occurrences in that phases
   * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
   * @throws IOException the io exception
   */
  public Map<Long, Map<String, Long>> countEventsInPhases(
      Long trainingRunId, List<Long> phaseIds, List<String> events)
      throws ElasticsearchTrainingDataLayerException, IOException {
    if (events == null || events.isEmpty()) {
      throw new ElasticsearchTrainingDataLayerException(
          "Parameter \"events\" cannot be null nor empty.");
    }

    // Phases aggregation query
    TermsAggregationBuilder aggregation =
        AggregationBuilders.terms(PHASES_AGGREGATION)
            .field(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_PHASE_ID)
            .subAggregation(
                AggregationBuilders.filters(
                    TYPE_FILTER_SUB_AGGREGATION,
                    createWildcardQueryFilters(
                        AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_EVENT_TYPE,
                        events)));

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.size(INDEX_DOCUMENTS_MIN_RETURN_NUMBER);
    searchSourceBuilder.timeout(new TimeValue(5, TimeUnit.MINUTES));
    searchSourceBuilder.query(includeOnlySpecifiedPhases(phaseIds));
    searchSourceBuilder.aggregation(aggregation);

    SearchRequest searchRequest =
        new SearchRequest(
            AbstractCrczpIndexPath.CRCZP_ADAPTIVE_EVENTS_INDEX + "*" + ".run=" + trainingRunId);
    searchRequest.source(searchSourceBuilder);

    return handleCountEventsResponse(
        getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT), events);
  }

  private Map<Long, Map<String, Long>> handleCountEventsResponse(
      SearchResponse response, List<String> events) {
    Map<Long, Map<String, Long>> eventMapping = new HashMap<>();

    if (response != null) {
      if (response.getAggregations() != null) {
        Terms levelsAggregation = response.getAggregations().get(PHASES_AGGREGATION);
        for (Terms.Bucket typeFilterBucket : levelsAggregation.getBuckets()) {
          Filters typeFilterAggregation =
              typeFilterBucket.getAggregations().get(TYPE_FILTER_SUB_AGGREGATION);
          Map<String, Long> eventsOccurrences = new HashMap<>();
          for (String event : events) {
            eventsOccurrences.put(event, typeFilterAggregation.getBucketByKey(event).getDocCount());
          }
          eventMapping.put((Long) typeFilterBucket.getKeyAsNumber(), eventsOccurrences);
        }
      }
    } else {
      throw new ElasticsearchTrainingDataLayerException(
          "Client could not connect to Elastic. Please, restart Elasticsearch service.");
    }
    return eventMapping;
  }

  /**
   * Get all wrong answers submitted in the given phases.
   *
   * <pre>{@code
   * GET crczp.events.adaptive.trainings*.run=${RUN_ID}/_search
   * {
   *   "size": 1000,
   *   "query": {
   *     "bool" : {
   *       "must" : [
   *       { "bool" : { "should" : [ {"match" : { "phase_id" : ${PHASE_ID} }}, {"match" : { "phase_id" : ${PHASE_ID} }}]}},
   *       { "bool": { "must": { "wildcard": { "type": {"value": "*WrongAnswerSubmitted" }}}}}
   *       ]
   *     }
   *   },
   *   "collapse": {
   *     "field": "phase_id",
   *     "inner_hits": {
   *       "name": "wrong_answers",
   *       "size": 1000
   *     }
   *   }
   * }
   * }*
   * </pre>
   *
   * @param trainingRunId the training run id
   * @param phaseIds ids of phases to get submitted wrong flags
   * @return the mapping of phases and list of wrong answers submitted in that phases
   * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
   * @throws IOException the io exception
   */
  public Map<Long, List<String>> getWrongAnswersInPhases(Long trainingRunId, List<Long> phaseIds)
      throws ElasticsearchTrainingDataLayerException, IOException {
    // Phases collapse builder query
    InnerHitBuilder innerHitBuilder =
        new InnerHitBuilder(PHASES_COLLAPSE).setSize(indexDocumentsMaxReturnNumber);
    CollapseBuilder collapseBuilder =
        new CollapseBuilder(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_PHASE_ID)
            .setInnerHits(innerHitBuilder);

    BoolQueryBuilder boolQueryBuilder =
        QueryBuilders.boolQuery()
            .must(includeOnlySpecifiedPhases(phaseIds))
            .must(includeOnlyWrongAnswers());

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder
        .size(indexDocumentsMaxReturnNumber)
        .timeout(new TimeValue(5, TimeUnit.MINUTES))
        .query(boolQueryBuilder)
        .collapse(collapseBuilder);

    SearchRequest searchRequest =
        new SearchRequest(
            AbstractCrczpIndexPath.CRCZP_ADAPTIVE_EVENTS_INDEX + "*" + ".run=" + trainingRunId);
    searchRequest.source(searchSourceBuilder);

    Map<Long, List<String>> result =
        handleWrongAnswersResponse(
            getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT), phaseIds);
    for (Long phaseId : phaseIds) {
      result.putIfAbsent(phaseId, new ArrayList<>());
    }
    return result;
  }

  private Map<Long, List<String>> handleWrongAnswersResponse(
      SearchResponse response, List<Long> phaseIds) {
    Map<Long, List<String>> wrongAnswersMapping = new HashMap<>();
    if (response != null) {
      if (response.getHits() != null) {
        for (SearchHit hit : response.getHits().getHits()) {
          Long phaseId =
              ((Integer)
                      hit.getSourceAsMap()
                          .get(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_PHASE_ID))
                  .longValue();
          List<String> wrongAnswers =
              Arrays.stream(hit.getInnerHits().get(PHASES_COLLAPSE).getHits())
                  .map(
                      searchHit ->
                          searchHit
                              .getSourceAsMap()
                              .get(
                                  AbstractCrczpElasticTermQueryFields
                                      .CRCZP_ELASTICSEARCH_ANSWER_CONTENT)
                              .toString())
                  .collect(Collectors.toList());
          wrongAnswersMapping.put(phaseId, wrongAnswers);
        }
      }
    } else {
      throw new ElasticsearchTrainingDataLayerException(
          "Client could not connect to Elastic. Please, restart Elasticsearch service.");
    }
    return wrongAnswersMapping;
  }

  /**
   * Get time spent at the specified phases.
   *
   * <pre>{@code
   *  GET crczp.events.adaptive.trainings*.run=${RUN_ID}/_search
   * {
   *   "size": 0,
   *   "query": {
   *     "bool" : {
   *       "must" : [{
   *         "bool" : {
   *           "should" : [ {"match" : { "phase_id" : ${PHASE_ID} }} ]
   *         }
   *       },
   *       {
   *         "bool": {
   *           "should": [
   *             { "wildcard": { "type": {"value": "*PhaseStarted" }}},
   *             { "wildcard": { "type": {"value": "*PhaseCompleted" }}}
   *           ]
   *         }
   *       }
   *      ]
   *   },
   *   "aggs": {
   *     "phases_agg": {
   *       "terms": { "field": "phase_id"},
   *       "aggs": {
   *           "max_timestamp": { "max": { "field": "timestamp"} },
   *           "min_timestamp": { "min": { "field": "timestamp" }}
   *        }
   *     }
   *   }
   * }
   * }*
   * </pre>
   *
   * @param trainingRunId the training run id
   * @return the mapping of phases and time spent at the phases
   * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
   * @throws IOException                             the io exception
   */
  public Map<Long, Pair<Long, Long>> findTimeBoundariesOfPhases(
      Long trainingRunId, List<Long> phaseIds)
      throws ElasticsearchTrainingDataLayerException, IOException {
    // Phase aggregation query
    TermsAggregationBuilder aggregation =
        AggregationBuilders.terms(PHASES_AGGREGATION)
            .field(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_PHASE_ID)
            .subAggregation(
                AggregationBuilders.max(MAX_TIMESTAMP_SUB_AGGREGATION)
                    .field(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TIMESTAMP))
            .subAggregation(
                AggregationBuilders.min(MIN_TIMESTAMP_SUB_AGGREGATION)
                    .field(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TIMESTAMP));

    BoolQueryBuilder boolQueryBuilder =
        QueryBuilders.boolQuery()
            .must(includeOnlySpecifiedPhases(phaseIds))
            .must(includeOnlyPhaseStartedAndCompleted());

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.size(INDEX_DOCUMENTS_MIN_RETURN_NUMBER);
    searchSourceBuilder.timeout(new TimeValue(5, TimeUnit.MINUTES));
    searchSourceBuilder.query(boolQueryBuilder);
    searchSourceBuilder.aggregation(aggregation);

    SearchRequest searchRequest =
        new SearchRequest(
            AbstractCrczpIndexPath.CRCZP_ADAPTIVE_EVENTS_INDEX + "*" + ".run=" + trainingRunId);
    searchRequest.source(searchSourceBuilder);

    return handleTimeBoundariesResponse(
        getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT));
  }

  private Map<Long, Pair<Long, Long>> handleTimeBoundariesResponse(SearchResponse response) {
    Map<Long, Pair<Long, Long>> timestampMapping = new HashMap<>();
    if (response != null) {
      if (response.getAggregations() != null) {
        Terms phasesAggregation = response.getAggregations().get(PHASES_AGGREGATION);
        for (Terms.Bucket phaseBucket : phasesAggregation.getBuckets()) {
          Min minTimestampAggregation =
              phaseBucket.getAggregations().get(MIN_TIMESTAMP_SUB_AGGREGATION);
          Max maxTimestampAggregation =
              phaseBucket.getAggregations().get(MAX_TIMESTAMP_SUB_AGGREGATION);
          Long minTimestamp = Double.valueOf(minTimestampAggregation.getValue()).longValue();
          Long maxTimestamp = Double.valueOf(maxTimestampAggregation.getValue()).longValue();
          timestampMapping.put(
              (Long) phaseBucket.getKeyAsNumber(),
              Pair.with(
                  minTimestamp, maxTimestamp.equals(minTimestamp) ? Long.MAX_VALUE : maxTimestamp));
        }
      }
    } else {
      throw new ElasticsearchTrainingDataLayerException(
          "Client could not connect to Elastic. Please, restart Elasticsearch service.");
    }
    return timestampMapping;
  }

  /**
   * Get value of the unique field from the training event of a particular training run. For now,
   * unique fields are: sandbox_id, user_ref_id, training_definition_id, training_instance_id, host,
   * pool_id
   *
   * @param trainingRunId the training run id
   * @param fieldName name of the field
   * @return the value of the specified field
   * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
   * @throws IOException the io exception
   */
  public Object getUniqueFieldValueFromTrainingEvent(Long trainingRunId, String fieldName)
      throws ElasticsearchTrainingDataLayerException, IOException {
    SearchSourceBuilder searchSourceBuilder =
        new SearchSourceBuilder().size(1).timeout(new TimeValue(5, TimeUnit.MINUTES));

    SearchRequest searchRequest =
        new SearchRequest(
            AbstractCrczpIndexPath.CRCZP_ADAPTIVE_EVENTS_INDEX + "*" + ".run=" + trainingRunId);
    searchRequest.source(searchSourceBuilder);
    SearchResponse response =
        getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);

    if (response != null) {
      SearchHits responseHits = response.getHits();
      if (responseHits != null && responseHits.getHits().length > 0) {
        return responseHits.getHits()[0].getSourceAsMap().get(fieldName);
      }
      throw new ElasticsearchTrainingDataLayerException(
          "No event found for specified training run.");
    } else {
      throw new ElasticsearchTrainingDataLayerException(
          "Client could not connect to Elastic. Please, restart Elasticsearch service.");
    }
  }

  /**
   * Get value of the unique field from the training event of a particular training run. For now,
   * unique fields are: sandbox_id, user_ref_id, training_definition_id, training_instance_id, host,
   * pool_id
   *
   * <p>Elasticserach query:
   *
   * <pre>{@code
   * GET crczp.events.trainings*.run=${RUN_ID}/_search
   * {
   *   "size": 10,
   *   "query": {
   *     "bool" : {
   *       "must" : [{
   *         "bool" : {
   *           "should" : [
   *             {"match" : { "phase_id" : ${PHASE_ID} }},
   *             {"match" : { "phase_id" : ${PHASE_ID} }}
   *           ]
   *         }
   *       }
   *       ],
   *       "filter" : [
   *           {"wildcard" : { "type" : "*PhaseStarted" }}
   *         ]
   *       }
   *   }
   * }
   * }</pre>
   *
   * @param trainingRunId the training run id
   * @param phaseIds list of phase ids
   * @return the value of the specified field
   * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
   * @throws IOException the io exception
   */
  public Map<Long, Long> getTaskIdsOfPhases(Long trainingRunId, List<Long> phaseIds)
      throws ElasticsearchTrainingDataLayerException, IOException {
    SearchRequest searchRequest =
        new SearchRequest(
            AbstractCrczpIndexPath.CRCZP_ADAPTIVE_EVENTS_INDEX + "*" + ".run=" + trainingRunId);

    BoolQueryBuilder boolQueryBuilder =
        QueryBuilders.boolQuery()
            .must(includeOnlySpecifiedPhases(phaseIds))
            .filter(
                QueryBuilders.wildcardQuery(
                        AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_EVENT_TYPE,
                        "*PhaseStarted")
                    .caseInsensitive(true));
    SearchSourceBuilder searchSourceBuilder =
        new SearchSourceBuilder()
            .size(indexDocumentsMaxReturnNumber)
            .timeout(new TimeValue(5, TimeUnit.MINUTES))
            .query(boolQueryBuilder);
    searchRequest.source(searchSourceBuilder);
    SearchResponse response =
        getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);

    if (response != null) {
      SearchHits responseHits = response.getHits();
      Map<Long, Long> phasesTasksMapping = new HashMap<>();
      if (responseHits != null && responseHits.getHits().length > 0) {
        for (SearchHit hit : responseHits.getHits()) {
          Long phaseId =
              ((Integer)
                      hit.getSourceAsMap()
                          .get(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_PHASE_ID))
                  .longValue();
          Long taskId =
              convertObjectToLong(
                  hit.getSourceAsMap()
                      .get(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TASK_ID));
          phasesTasksMapping.put(phaseId, taskId);
        }
      }
      return phasesTasksMapping;
    } else {
      throw new ElasticsearchTrainingDataLayerException(
          "Client could not connect to Elastic. Please, restart Elasticsearch service.");
    }
  }

  private Long convertObjectToLong(Object longValue) {
    return longValue == null ? null : Long.valueOf((Integer) longValue);
  }

  /**
   * Get number of commands in time range and occurrences of specified keywords.
   *
   * <p>Elasticserach query:
   *
   * <pre>{@code
   * GET crczp.logs.console*.sandbox={sandboxId}/_search
   * {
   *   "query": {
   *     "range": {
   *       "timestamp_str": {
   *         "gte": ${FROM}
   *         "lte": ${TO}
   *       }
   *     }
   *   },
   *   "aggs": {
   *     "keyword_filters": {
   *        "filters" : {
   *         "filters" : [
   *           { "wildcard" : { "cmd" : ${KEYWORD} }},
   *           { "wildcard" : { "cmd" : ${KEYWORD} }}
   *         ]
   *       }
   *     }
   *   }
   * }
   * }</pre>
   *
   * @param sandboxId the sandbox id
   * @param from the lower bound of the time range (epoch_millis timestamp format)
   * @param to the upper bound of the time range (epoch_millis timestamp format)
   * @param keywords searched keywords
   * @return the commands statistics in specified time range
   * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
   * @throws IOException the io exception
   */
  public Pair<Long, Map<String, Long>> findCommandsStatisticsInTimeRange(
      String sandboxId, Long from, Long to, List<String> keywords)
      throws ElasticsearchTrainingDataLayerException, IOException {
    // Timestamp range query
    RangeQueryBuilder dateRangeBuilder =
        QueryBuilders.rangeQuery(
                AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TIMESTAMP_STR)
            .gte(from)
            .lte(to);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.sort(
        AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TIMESTAMP_STR, SortOrder.ASC);
    searchSourceBuilder.size(indexDocumentsMaxReturnNumber);
    searchSourceBuilder.timeout(new TimeValue(5, TimeUnit.MINUTES));
    searchSourceBuilder.query(dateRangeBuilder);

    // Aggregation of specified keywords
    if (keywords != null) {
      FiltersAggregationBuilder aggregation =
          AggregationBuilders.filters(
              KEYWORD_FILTER_SUB_AGGREGATION, createWildcardQueryFilters("cmd", keywords));
      searchSourceBuilder.aggregation(aggregation);
    }

    SearchRequest searchRequest = new SearchRequest(sandboxId);
    searchRequest.source(searchSourceBuilder);

    return handleCommandsStatisticsResponse(
        getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT), keywords);
  }

  private Pair<Long, Map<String, Long>> handleCommandsStatisticsResponse(
      SearchResponse response, List<String> keywords) {
    if (response != null) {
      Map<String, Long> keywordMapping = null;
      if (keywords != null) {
        keywordMapping = new HashMap<>();
        Filters filtersAggregation = response.getAggregations().get(KEYWORD_FILTER_SUB_AGGREGATION);
        for (String keyword : keywords) {
          Filters.Bucket keywordBucket = filtersAggregation.getBucketByKey(keyword);
          if (keywordBucket != null) {
            keywordMapping.put(keyword, keywordBucket.getDocCount());
          } else {
            keywordMapping.put(keyword, 0L);
          }
        }
      }
      return Pair.with(response.getHits().getTotalHits().value, keywordMapping);
    } else {
      throw new ElasticsearchTrainingDataLayerException(
          "Client could not connect to Elastic. Please, restart Elasticsearch service.");
    }
  }

  private BoolQueryBuilder includeOnlySpecifiedPhases(List<Long> phaseIds) {
    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    for (Long phaseId : phaseIds) {
      boolQueryBuilder.should(
          QueryBuilders.matchQuery(
              AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_PHASE_ID, phaseId));
    }
    return boolQueryBuilder;
  }

  private BoolQueryBuilder includeOnlyPhaseStartedAndCompleted() {
    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    boolQueryBuilder.should(
        QueryBuilders.wildcardQuery(CRCZP_ELASTICSEARCH_EVENT_TYPE, "*PhaseStarted")
            .caseInsensitive(true));
    boolQueryBuilder.should(
        QueryBuilders.wildcardQuery(CRCZP_ELASTICSEARCH_EVENT_TYPE, "*PhaseCompleted")
            .caseInsensitive(true));
    return boolQueryBuilder;
  }

  private BoolQueryBuilder includeOnlyWrongAnswers() {
    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    boolQueryBuilder.should(
        QueryBuilders.wildcardQuery(CRCZP_ELASTICSEARCH_EVENT_TYPE, "*WrongAnswerSubmitted")
            .caseInsensitive(true));
    return boolQueryBuilder;
  }

  private FiltersAggregator.KeyedFilter[] createWildcardQueryFilters(
      String fieldName, List<String> fieldValues) {
    FiltersAggregator.KeyedFilter[] filterArray =
        new FiltersAggregator.KeyedFilter[fieldValues.size()];
    for (int i = 0; i < fieldValues.size(); i++) {
      filterArray[i] =
          new FiltersAggregator.KeyedFilter(
              fieldValues.get(i),
              QueryBuilders.wildcardQuery(fieldName, "*" + fieldValues.get(i) + "*")
                  .caseInsensitive(true));
    }
    return filterArray;
  }
}
