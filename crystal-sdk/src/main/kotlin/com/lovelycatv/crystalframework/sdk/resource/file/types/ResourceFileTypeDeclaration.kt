package com.lovelycatv.crystalframework.sdk.resource.file.types

import com.lovelycatv.crystalframework.shared.types.common.ResourceVisibility

/**
 * A pluggable file-resource "type" contributed to [com.lovelycatv.crystalframework.sdk.resource.file.ResourceFileTypeRegistry].
 *
 * The framework's built-in types (user avatar, tenant icon, tenant member avatar) implement this
 * interface directly on their enum values so they and third-party contributions flow through the
 * same lookup path; [typeId] is what lands in `file_resources.type`.
 *
 * Third parties must:
 *  - pick a [typeId] outside the framework's reserved range (`>= 1000`, see the registry doc);
 *  - namespace [key] under their vendor id (`"acme.corporate_avatar"`);
 *  - declare the content-type / file-extension whitelist and default visibility that ship with
 *    this type. Deployment-time overrides (see `crystalframework.resource.file-type-overrides`)
 *    layer on top of these built-in defaults without recompiling.
 */
interface ResourceFileTypeDeclaration {
    /** Stable integer written to `file_resources.type`. Built-in: 0..2; third-party: >= 1000. */
    val typeId: Int

    /** Globally-unique string identifier, namespaced (`"builtin.user_avatar"`, `"acme.corporate_avatar"`). */
    val key: String

    /** Human-readable label used by admin UIs. */
    val displayName: String

    /** Long-form description; safe to be empty. */
    val description: String
        get() = ""

    /**
     * MIME content-types accepted at upload time. Empty set = accept nothing (fail-closed default).
     * Deployment-time YAML override wins over this value when present.
     */
    val supportedContentTypes: Set<String>
        get() = emptySet()

    /**
     * File extensions accepted at upload time, lowercase, without leading dot.
     * Deployment-time YAML override wins over this value when present.
     */
    val supportedFileExtensions: Set<String>
        get() = emptySet()

    /**
     * Fallback visibility used when no other source resolves one. For built-in types the runtime
     * still prefers system-settings-configured visibility; for third-party types this is the
     * effective value.
     */
    val defaultVisibility: ResourceVisibility
        get() = ResourceVisibility.AUTHENTICATED

    /**
     * Prefix used inside the storage bucket for objects of this type
     * (e.g. `user_avatar` produces object keys like `user_avatar/<uuid>.png`).
     *
     * **Frozen once written.** Changing this for an existing type orphans every previously-uploaded
     * file — the DB still points at the old prefix while new uploads land under the new one. Pick
     * something stable up front and never touch it. The default derives the last dot-segment of
     * [key], which is safe for a new declaration but not for a rename of an existing one.
     */
    val objectKeyPrefix: String
        get() = key.substringAfterLast('.').ifEmpty { key }
}
