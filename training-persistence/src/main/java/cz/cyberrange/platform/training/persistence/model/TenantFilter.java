package cz.cyberrange.platform.training.persistence.model;

/**
 * Hibernate filter that limits {@link TrainingDefinition} and {@link TrainingInstance} queries to the rows
 * a tenant may see: its own rows and rows without a tenant (the global library).
 * <p>
 * The filter is disabled by default. It is enabled only around list queries made for a caller whose token
 * carries an organization claim, so callers without the claim see exactly what they saw before.
 * Hibernate filters do not apply to loads by primary key, so single-row lookups check the tenant explicitly.
 */
public final class TenantFilter {

    public static final String NAME = "tenantFilter";
    public static final String PARAM = "tenantKey";
    public static final String CONDITION = "(tenant_key IS NULL OR tenant_key = :" + PARAM + ")";

    private TenantFilter() {
    }
}
