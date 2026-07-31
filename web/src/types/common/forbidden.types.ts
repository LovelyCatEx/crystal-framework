/**
 * Corresponds to backend ForbiddenReason enum
 * (crystal-shared/exception/ForbiddenReason.kt).
 *
 * Serialized as the enum name string via Jackson default enum serialization.
 */
export enum ForbiddenReason {
    MISSING_PERMISSION = "MISSING_PERMISSION",
    SCOPE_MISMATCH = "SCOPE_MISMATCH",
    PROTECTED_RESOURCE = "PROTECTED_RESOURCE",
    NOT_TENANT_MEMBER = "NOT_TENANT_MEMBER",
    ROLE_PROTECTED = "ROLE_PROTECTED",
    PERMISSION_ESCALATION = "PERMISSION_ESCALATION",
}

/**
 * ResourceScope value as it appears on ForbiddenContext.
 *
 * The backend `ResourceScope` enum is serialized as its name string
 * (Jackson default), not its numeric typeId, when returned inside
 * ForbiddenContext. Do not confuse with the numeric ResourceScope in
 * types/BaseScopedEntity.ts used for BaseScopedEntity.scope (typeId form).
 */
export enum ForbiddenScope {
    SYSTEM = "SYSTEM",
    TENANT = "TENANT",
}

/**
 * Corresponds to backend ForbiddenContext data class. Attached to 403
 * ApiResponse.data when the server explains an authorization failure.
 */
export interface ForbiddenContext {
    requiredPermissions: string[];
    reason: ForbiddenReason;
    scope: ForbiddenScope;
}

/**
 * Type guard used by the API layer to decide whether to render the
 * structured ForbiddenModal or fall back to the flat message toast.
 */
export function isForbiddenContext(value: unknown): value is ForbiddenContext {
    if (typeof value !== "object" || value === null) return false;
    const obj = value as Record<string, unknown>;
    return typeof obj.reason === "string"
        && typeof obj.scope === "string"
        && Array.isArray(obj.requiredPermissions);
}
