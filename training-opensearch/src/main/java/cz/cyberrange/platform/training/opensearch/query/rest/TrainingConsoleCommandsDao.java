package cz.cyberrange.platform.training.opensearch.query.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.elasticsearch.data.enums.CommandType;
import cz.cyberrange.platform.elasticsearch.data.exceptions.ElasticsearchTrainingDataLayerException;
import cz.cyberrange.platform.elasticsearch.data.indexpaths.AbstractCrczpElasticTermQueryFields;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.range.ParsedRange;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
public class TrainingConsoleCommandsDao extends AbstractElasticClientDAO {

    @Value("${elasticsearch.max-result-window:10000}")
    private int indexDocumentsMaxReturnNumber;

    /**
     * Instantiates a new Training events dao.
     *
     * @param restHighLevelClient the rest high level client
     * @param objectMapper        the object mapper
     */
    @Autowired
    public TrainingConsoleCommandsDao(@Qualifier("crczpRestHighLevelClient") RestHighLevelClient restHighLevelClient,
                                      @Qualifier("objMapperForElasticsearch") ObjectMapper objectMapper) {
        super(restHighLevelClient, objectMapper);
    }

    /**
     * Find all bash commands from a pool by its id.
     *
     * @param index             index used to search for commands
     * @param filterCommands    list of commands to filter
     * @param commandType       type of filtered commands
     * @return the list
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     * @throws IOException                             the io exception
     */
    public List<Map<String, Object>> findAllConsoleCommands(String index, List<String> filterCommands, CommandType commandType) throws ElasticsearchTrainingDataLayerException, IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TIMESTAMP_STR, SortOrder.ASC);
        searchSourceBuilder.size(indexDocumentsMaxReturnNumber);
        searchSourceBuilder.timeout(new TimeValue(5, TimeUnit.MINUTES));
        if(filterCommands != null && !filterCommands.isEmpty()) {
            searchSourceBuilder.query(QueryBuilders.regexpQuery(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_COMMAND, StringUtils.collectionToDelimitedString(filterCommands, "|")));
        }
        if (commandType != null) {
            String commandTypeString = commandType.toString().toLowerCase() + "-command";
            searchSourceBuilder.query(QueryBuilders.regexpQuery(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_COMMAND_TYPE, commandTypeString));
        }

        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);

        return handleElasticsearchResponse(getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT));
    }

    /**
     * Find all bash commands from sandbox aggregated by timestamp ranges.
     * <p>
     * Elasticserach query:
     * GET crczp.logs.console*.sandbox={sandboxId}/_search
     * {
     * "query": {
     * "range": {
     * "timestamp_str": {
     * "gte": {from}
     * "lte": {to}
     * }
     * }
     * }
     * }
     *
     * @param index     index under which commands from local or cloud sandboxes are stored
     * @param from      the lower bound of the time range (epoch_millis timestamp format)
     * @param to        the upper bound of the time range (epoch_millis timestamp format)
     * @param filterCommands list of commands to filter
     * @param commandType type of filtered commands
     * @return the list of commands in given time range
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     * @throws IOException                             the io exception
     */
    public List<Map<String, Object>> findAllConsoleCommandsBySandboxAndTimeRange(String index, Long from, Long to, List<String> filterCommands, CommandType commandType) throws ElasticsearchTrainingDataLayerException, IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TIMESTAMP_STR, SortOrder.ASC);
        searchSourceBuilder.size(indexDocumentsMaxReturnNumber);
        searchSourceBuilder.timeout(new TimeValue(5, TimeUnit.MINUTES));

        //Compound queries - Date Range Aggregation Query + Regexp Query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        List<QueryBuilder> boolMustQueries = boolQueryBuilder.must();
        boolMustQueries.add(QueryBuilders.rangeQuery(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_TIMESTAMP_STR)
                .gte(from)
                .lte(to));
        if(filterCommands != null && !filterCommands.isEmpty()) {
            boolMustQueries.add(QueryBuilders.regexpQuery(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_COMMAND, StringUtils.collectionToDelimitedString(filterCommands, "|")));
        }
        if (commandType != null) {
            String commandTypeString = commandType.toString().toLowerCase() + "-command";
            boolMustQueries.add(QueryBuilders.regexpQuery(AbstractCrczpElasticTermQueryFields.CRCZP_ELASTICSEARCH_COMMAND_TYPE, commandTypeString));
        }
        
        searchSourceBuilder.query(boolQueryBuilder);

        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);

        return handleElasticsearchResponse(searchResponse);
    }

    /**
     * @param index the pool id
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     */
    public void deleteConsoleCommands(String index) throws ElasticsearchTrainingDataLayerException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(index);
        try {
            AcknowledgedResponse deleteIndexResponse = getRestHighLevelClient().indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            if (!deleteIndexResponse.isAcknowledged()) {
                throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic.");
            }
        } catch (IOException e) {
            throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic.");
        }
    }

    private List<Map<String, Object>> handleElasticsearchResponse(SearchResponse response) throws ElasticsearchTrainingDataLayerException {
        List<Map<String, Object>> events = new ArrayList<>();
        if (response != null) {
            SearchHits responseHits = response.getHits();
            if (responseHits != null) {
                SearchHit[] results = responseHits.getHits();
                for (SearchHit hit : results) {
                    Map<String, Object> source = hit.getSourceAsMap();
                    events.add(source);
                }
            }
        } else {
            throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elasticsearch. Please, restart Elasticsearch service.");
        }
        return events;
    }

    private List<List<Map<String, Object>>> resolveTimeRangeAggregation(SearchResponse searchResponse) {
        Aggregations aggregations = searchResponse.getAggregations();
        ParsedRange commandsByTimeRanges = aggregations.get("timestamp_ranges");
        Collection<Range.Bucket> timestampRanges = (Collection<Range.Bucket>) commandsByTimeRanges.getBuckets();

        List<List<Map<String, Object>>> commandsDivision = new ArrayList<>();
        for (Range.Bucket rangeBucket : timestampRanges) {
            commandsDivision.add(resolveBucketTopHits(rangeBucket));
        }
        return commandsDivision;
    }

    private List<Map<String, Object>> resolveBucketTopHits(Range.Bucket rangeBucket) throws ElasticsearchTrainingDataLayerException {
        List<Map<String, Object>> resultBucketTopHits = new ArrayList<>();
        TopHits topHits = rangeBucket.getAggregations().get("by_top_hits");
        SearchHits responseHits = topHits.getHits();

        if (responseHits != null) {
            SearchHit[] results = responseHits.getHits();
            for (SearchHit hit : results) {
                resultBucketTopHits.add(hit.getSourceAsMap());
            }
        } else {
            throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elasticsearch. Please, restart Elasticsearch service.");
        }
        return resultBucketTopHits;
    }


}
