package com.lovelycatv.crystalframework.shared.controller

/**
 * DSL marker for [PermissionMatrix]. Prevents accidental scope-leak between layer builders —
 * writing `create = ...` inside a `super { ... }` block cannot accidentally see the outer
 * matrix builder's members.
 */
@DslMarker
annotation class PermissionMatrixDsl
