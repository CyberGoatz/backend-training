package cz.cyberrange.platform.training.opensearch.sql;

public enum QuerySecurityLevel {
  /** Results will contain full OpenSearch contents - <b>DANGEROUS</b> */
  SHOW_ALL,
  /**
   * Only <i>crczp.events.trainings.*</i> and <i>crczp.events.trainings.*</i> indexes are visible.
   * <br>
   * <b>Syslog entries</b> remain visible!
   */
  RESTRICT_INDEXES,
  /**
   * Only <i>crczp.events.trainings.*</i> and <i>crczp.events.trainings.*</i> indexes are visible.
   * <br>
   * <b>Syslog entries</b> are removed from the results. <br>
   * Suitable for Organiser role
   */
  RESTRICT_INDEXES_REMOVE_SYSTEM_INFO,
  /**
   * Only <i>crczp.events.trainings.*</i> and <i>crczp.events.trainings.*</i> indexes are visible.
   * <br>
   * Following fields are removed from the results:
   *
   * <ul>
   *   <li>Syslog entries
   *   <li>Level answer contents
   * </ul>
   */
  RESTRICT_INDEXES_REMOVE_SYSTEM_INFO_REMOVE_TRAINEE_SENSITIVE_DATA
}
