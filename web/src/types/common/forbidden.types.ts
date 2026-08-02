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

/**
 * Corresponds to backend `BanContext` data class. Attached to the 403
 * ApiResponse.data when the server rejects a request because the account
 * is under a login ban.
 *
 * bannedAt / banUntil are millisecond epoch timestamps serialized as strings.
 * banUntil === null means a permanent ban.
 */
export interface BanContext {
    reason: string;
    bannedAt: string;
    banUntil: string | null;
}

/**
 * Type guard distinguishing a ban rejection from a generic forbidden
 * response. Keyed on the ban-specific `bannedAt` field, since
 * ForbiddenContext also carries a string `reason` but never a `bannedAt`.
 *
 * Note: the login failure path serializes via the plain `toJSONString()`
 * mapper (not the WebFlux codec), so the Long `bannedAt` arrives as a raw
 * number here rather than a string.
 */
export function isBanContext(value: unknown): value is BanContext {
    if (typeof value !== "object" || value === null) return false;
    const obj = value as Record<string, unknown>;
    return typeof obj.reason === "string"
        && (typeof obj.bannedAt === "number" || typeof obj.bannedAt === "string");
}

/**
 * Corresponds to backend `DisabledContext` marker. Attached to the 403
 * ApiResponse.data when a login is rejected because the account was disabled
 * by an administrator. Carries no dynamic data — the `disabled` flag only
 * lets the API layer tell it apart from a ban / generic forbidden response.
 */
export interface DisabledContext {
    disabled: boolean;
}

/**
 * Type guard for a disabled-account rejection. Keyed on the boolean
 * `disabled` marker, which neither BanContext nor ForbiddenContext carries.
 */
export function isDisabledContext(value: unknown): value is DisabledContext {
    if (typeof value !== "object" || value === null) return false;
    const obj = value as Record<string, unknown>;
    return obj.disabled === true;
}
