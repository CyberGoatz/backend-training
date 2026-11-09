package cz.cyberrange.platform.training.service.tenancy;

import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;
import cz.cyberrange.platform.training.persistence.model.TenantFilter;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Campus tenancy for training definitions and instances.
 * <p>
 * The tenant of a request is the first alias in the validated JWT {@code organization} claim, which Keycloak emits
 * either as an array ({@code ["vit"]}) or as an object keyed by alias ({@code {"vit": {...}}}).
 * A caller without the claim belongs to no tenant and every method here is a no-op for it, so the public stack
 * behaves exactly as before.
 * <p>
 * A tenant caller can read rows of its own tenant and rows without a tenant (the global library). Global rows are
 * read-only for it. Rows of other tenants are reported as not found so that their ids do not leak.
 */
@Component
public class TenantScope {

    public static final String ORGANIZATION_CLAIM = "organization";
    public static final int MAX_TENANT_KEY_LENGTH = 40;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * @return the caller's tenant, or null when the caller has no organization claim.
     */
    public String currentTenant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            return null;
        }
        return tenantFromClaim(((JwtAuthenticationToken) authentication).getToken().getClaims().get(ORGANIZATION_CLAIM));
    }

    /**
     * Reads the tenant from the value of the {@code organization} claim.
     *
     * @param claim the claim value: a list of aliases, a map keyed by alias, a single string, or null.
     * @return the first alias, or null when there is none.
     */
    public static String tenantFromClaim(Object claim) {
        Object first = null;
        if (claim instanceof Collection) {
            first = ((Collection<?>) claim).stream().filter(Objects::nonNull).findFirst().orElse(null);
        } else if (claim instanceof Map) {
            first = ((Map<?, ?>) claim).keySet().stream().filter(Objects::nonNull).findFirst().orElse(null);
        } else if (claim instanceof String) {
            first = claim;
        }
        if (first == null) {
            return null;
        }
        String alias = first.toString().trim();
        if (!StringUtils.hasText(alias) || alias.length() > MAX_TENANT_KEY_LENGTH) {
            return null;
        }
        return alias;
    }

    /**
     * @param callerTenant tenant of the caller, null for callers without a tenant.
     * @param rowTenant    tenant key of the row, null for the global library.
     * @return true if the caller may read the row.
     */
    public static boolean isVisible(String callerTenant, String rowTenant) {
        return callerTenant == null || rowTenant == null || callerTenant.equals(rowTenant);
    }

    public boolean isVisible(String rowTenant) {
        return isVisible(currentTenant(), rowTenant);
    }

    /**
     * Throws {@link EntityNotFoundException} if the caller may not read a row with the given tenant key.
     */
    public void checkReadable(String rowTenant, Class<?> entityClass, Object id) {
        if (!isVisible(rowTenant)) {
            throw notFound(entityClass, id);
        }
    }

    /**
     * Throws {@link EntityNotFoundException} for rows of another tenant and {@link ForbiddenException} for global
     * library rows when the caller is a tenant caller.
     */
    public void checkWritable(String rowTenant, Class<?> entityClass, Object id) {
        String callerTenant = currentTenant();
        if (callerTenant == null) {
            return;
        }
        if (rowTenant == null) {
            throw new ForbiddenException("Rows of the global library are read-only for tenant users.");
        }
        if (!callerTenant.equals(rowTenant)) {
            throw notFound(entityClass, id);
        }
    }

    /**
     * Tenant key for a row the caller is creating.
     * Tenant callers always get their own tenant, whatever the request asked for. Administrators without a tenant may
     * create a row on behalf of a tenant by passing {@code requestedTenant}; everybody else creates rows without a tenant.
     */
    public String tenantForNewRow(String requestedTenant) {
        String callerTenant = currentTenant();
        if (callerTenant != null) {
            return callerTenant;
        }
        if (StringUtils.hasText(requestedTenant) && hasAuthority(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR.name())) {
            String tenant = requestedTenant.trim();
            if (tenant.length() > MAX_TENANT_KEY_LENGTH) {
                throw new BadRequestException(
                        "tenantKey must be at most " + MAX_TENANT_KEY_LENGTH + " characters.");
            }
            return tenant;
        }
        return null;
    }

    /**
     * Runs a list query with the tenant filter enabled, so that it returns only rows of the caller's tenant and global
     * rows. For callers without a tenant the query runs unchanged.
     * Must be called inside a transaction, so that the filter is enabled on the session that runs the query.
     */
    public <T> T filtered(Supplier<T> query) {
        String tenant = currentTenant();
        if (tenant == null) {
            return query.get();
        }
        if (!entityManager.isJoinedToTransaction()) {
            throw new IllegalStateException("Tenant-filtered queries must run inside a transaction.");
        }
        Session session = entityManager.unwrap(Session.class);
        Filter alreadyEnabled = session.getEnabledFilter(TenantFilter.NAME);
        if (alreadyEnabled != null) {
            return query.get();
        }
        session.enableFilter(TenantFilter.NAME).setParameter(TenantFilter.PARAM, tenant);
        try {
            return query.get();
        } finally {
            session.disableFilter(TenantFilter.NAME);
        }
    }

    private static boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if (authority.equals(grantedAuthority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private static EntityNotFoundException notFound(Class<?> entityClass, Object id) {
        return new EntityNotFoundException(new EntityErrorDetail(entityClass, "id", id == null ? Long.class : id.getClass(), id));
    }
}
