package cz.cyberrange.platform.training.opensearch.query.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.RestHighLevelClient;

/** The type Abstract elastic client dao. */
public abstract class AbstractElasticClientDAO {

  private RestHighLevelClient restHighLevelClient;
  private ObjectMapper mapper;

  /**
   * Instantiates a new Abstract elastic client dao.
   *
   * @param client the client
   * @param mapper the mapper
   */
  protected AbstractElasticClientDAO(RestHighLevelClient client, ObjectMapper mapper) {
    this.restHighLevelClient = client;
    this.mapper = mapper;
  }

  /**
   * Gets rest high level client.
   *
   * @return the rest high level client
   */
  protected RestHighLevelClient getRestHighLevelClient() {
    return restHighLevelClient;
  }

  /**
   * Sets rest high level client.
   *
   * @param restHighLevelClient the rest high level client
   */
  protected void setRestHighLevelClient(RestHighLevelClient restHighLevelClient) {
    this.restHighLevelClient = restHighLevelClient;
  }

  /**
   * Gets mapper.
   *
   * @return the mapper
   */
  protected ObjectMapper getMapper() {
    return mapper;
  }

  /**
   * Sets mapper.
   *
   * @param mapper the mapper
   */
  protected void setMapper(ObjectMapper mapper) {
    this.mapper = mapper;
  }
}
