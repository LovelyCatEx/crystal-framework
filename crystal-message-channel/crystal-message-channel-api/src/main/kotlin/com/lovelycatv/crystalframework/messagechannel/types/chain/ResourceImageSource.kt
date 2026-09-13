/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.types.chain

/**
 * Refers to a file stored via crystal-resource. The email/feishu renderer resolves
 * the resource id to a URL (or uploads to feishu and gets an image_key) when sending.
 */
data class ResourceImageSource(val resourceId: String) : ImageSource
