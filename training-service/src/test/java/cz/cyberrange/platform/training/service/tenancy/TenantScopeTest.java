package cz.cyberrange.platform.training.service.tenancy;

import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cz.cyberrange.platform.training.service.tenancy.TenantTestSupport.signIn;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantScopeTest {

    private final TenantScope tenantScope = new TenantScope();

    @AfterEach
    void tearDown() {
        TenantTestSupport.signOut();
    }

    @Test
    void claimAsArrayTakesFirstAlias() {
        assertEquals("vit", TenantScope.tenantFromClaim(List.of("vit", "other")));
    }

    @Test
    void claimAsObjectTakesFirstAlias() {
        Map<String, Object> claim = new LinkedHashMap<>();
        claim.put("vit", Map.of("id", "6f1c"));
        claim.put("other", Map.of("id", "9a2b"));
        assertEquals("vit", TenantScope.tenantFromClaim(claim));
    }

    @Test
    void missingOrUnusableClaimMeansNoTenant() {
        assertNull(TenantScope.tenantFromClaim(null));
        assertNull(TenantScope.tenantFromClaim(List.of()));
        assertNull(TenantScope.tenantFromClaim(Map.of()));
        assertNull(TenantScope.tenantFromClaim(List.of(" ")));
        assertNull(TenantScope.tenantFromClaim(42));
        assertNull(TenantScope.tenantFromClaim(List.of("x".repeat(TenantScope.MAX_TENANT_KEY_LENGTH + 1))));
    }

    @Test
    void currentTenantComesFromTheValidatedToken() {
        assertNull(tenantScope.currentTenant(), "no authentication");
        signIn(null);
        assertNull(tenantScope.currentTenant(), "no organization claim");
        signIn(List.of("vit"));
        assertEquals("vit", tenantScope.currentTenant());
        signIn(Map.of("vit", Map.of()));
        assertEquals("vit", tenantScope.currentTenant());
    }

    @Test
    void visibilityRule() {
        // callers without a tenant see everything, as before
        assertTrue(TenantScope.isVisible(null, null));
        assertTrue(TenantScope.isVisible(null, "vit"));
        // tenant callers see their own rows and the global library
        assertTrue(TenantScope.isVisible("vit", "vit"));
        assertTrue(TenantScope.isVisible("vit", null));
        // but never another tenant's rows
        assertFalse(TenantScope.isVisible("vit", "other"));
    }

    @Test
    void otherTenantsRowsAreNotFound() {
        signIn(List.of("vit"));
        assertThrows(EntityNotFoundException.class, () -> tenantScope.checkReadable("other", TrainingDefinition.class, 1L));
        assertThrows(EntityNotFoundException.class, () -> tenantScope.checkWritable("other", TrainingDefinition.class, 1L));
        assertDoesNotThrow(() -> tenantScope.checkReadable(null, TrainingDefinition.class, 1L));
        assertDoesNotThrow(() -> tenantScope.checkReadable("vit", TrainingDefinition.class, 1L));
    }

    @Test
    void globalLibraryIsReadOnlyForTenantCallers() {
        signIn(List.of("vit"));
        assertThrows(ForbiddenException.class, () -> tenantScope.checkWritable(null, TrainingDefinition.class, 1L));
        assertDoesNotThrow(() -> tenantScope.checkWritable("vit", TrainingDefinition.class, 1L));
    }

    @Test
    void callersWithoutClaimAreUnrestricted() {
        signIn(null);
        assertDoesNotThrow(() -> tenantScope.checkReadable("other", TrainingDefinition.class, 1L));
        assertDoesNotThrow(() -> tenantScope.checkWritable("other", TrainingDefinition.class, 1L));
        assertDoesNotThrow(() -> tenantScope.checkWritable(null, TrainingDefinition.class, 1L));
        assertEquals("unfiltered", tenantScope.filtered(() -> "unfiltered"));
    }

    @Test
    void tenantCallersCannotChooseAnotherTenant() {
        signIn(List.of("vit"), "ROLE_TRAINING_ADMINISTRATOR");
        assertEquals("vit", tenantScope.tenantForNewRow("other"));
        assertEquals("vit", tenantScope.tenantForNewRow(null));
    }

    @Test
    void onlyAdministratorsWithoutClaimMayCreateRowsForATenant() {
        signIn(null, "ROLE_TRAINING_ADMINISTRATOR");
        assertEquals("vit", tenantScope.tenantForNewRow("vit"));
        assertNull(tenantScope.tenantForNewRow(null));

        signIn(null, "ROLE_TRAINING_DESIGNER");
        assertNull(tenantScope.tenantForNewRow("vit"));
    }
}
