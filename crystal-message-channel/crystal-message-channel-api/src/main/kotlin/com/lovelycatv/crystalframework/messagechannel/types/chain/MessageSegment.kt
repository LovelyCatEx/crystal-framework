/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.types.chain

/**
 * A single element inside a [MessageChain].
 * Each channel provider is responsible for translating the segments it supports
 * into the channel's native representation, and for handling unsupported segments
 * (e.g. degrade to plain text, drop, or fail) on its own.
 */
sealed interface MessageSegment
