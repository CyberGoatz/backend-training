package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * This class is used to replace Page class and reduce number of returned elements. Used for
 * responses from Python api.
 */
public class PageResultResourcePython<E> {
  private int page;

  @JsonProperty("page_size")
  private int pageSize;

  @JsonProperty("page_count")
  private int pageCount;

  private int count;

  @JsonProperty("total_count")
  private int totalCount;

  private List<E> results;

  /** Instantiates a new Page result resource python. */
  public PageResultResourcePython() {}

  /**
   * Gets page.
   *
   * @return the page
   */
  public int getPage() {
    return page;
  }

  /**
   * Sets page.
   *
   * @param page the page
   */
  public void setPage(int page) {
    this.page = page;
  }

  /**
   * Gets page size.
   *
   * @return the page size
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Sets page size.
   *
   * @param pageSize the page size
   */
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * Gets page count.
   *
   * @return the page count
   */
  public int getPageCount() {
    return pageCount;
  }

  /**
   * Sets page count.
   *
   * @param pageCount the page count
   */
  public void setPageCount(int pageCount) {
    this.pageCount = pageCount;
  }

  /**
   * Gets count.
   *
   * @return the count
   */
  public int getCount() {
    return count;
  }

  /**
   * Sets count.
   *
   * @param count the count
   */
  public void setCount(int count) {
    this.count = count;
  }

  /**
   * Gets total count.
   *
   * @return the total count
   */
  public int getTotalCount() {
    return totalCount;
  }

  /**
   * Sets total count.
   *
   * @param totalCount the total count
   */
  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }

  /**
   * Gets results.
   *
   * @return the results
   */
  public List<E> getResults() {
    return results;
  }

  /**
   * Sets results.
   *
   * @param results the results
   */
  public void setResults(List<E> results) {
    this.results = results;
  }

  @Override
  public String toString() {
    return "PageResultResourcePython{"
        + "page="
        + page
        + ", pageSize="
        + pageSize
        + ", pageCount="
        + pageCount
        + ", count="
        + count
        + ", totalCount="
        + totalCount
        + ", results="
        + results
        + '}';
  }
}
