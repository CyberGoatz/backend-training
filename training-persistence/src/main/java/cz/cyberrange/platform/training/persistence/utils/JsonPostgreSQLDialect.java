package cz.cyberrange.platform.training.persistence.utils;

import java.sql.Types;
import org.hibernate.dialect.PostgreSQL9Dialect;

/** The type Json postgre sql dialect. */
public class JsonPostgreSQLDialect extends PostgreSQL9Dialect {

  /** Instantiates a new Json postgre sql dialect. */
  public JsonPostgreSQLDialect() {
    super();
    this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
    this.registerColumnType(Types.JAVA_OBJECT, "json");
  }
}
