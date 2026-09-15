/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.types.chain

/**
 * Carries a pre-rendered HTML fragment that bypasses the normal segment-to-HTML
 * escape pipeline. Intended for legacy mail templates whose body is a full HTML
 * document and cannot be re-expressed as structured segments.
 *
 * Constraints:
 *  - Not serializable to the [MessageChain] XML form (the writer rejects it).
 *  - Non-HTML channels (feishu, sms, ...) decide on their own whether to strip,
 *    convert to plain text, or fail. The message-channel module makes no promise.
 */
data class RawHtmlSegment(val html: String) : MessageSegment
