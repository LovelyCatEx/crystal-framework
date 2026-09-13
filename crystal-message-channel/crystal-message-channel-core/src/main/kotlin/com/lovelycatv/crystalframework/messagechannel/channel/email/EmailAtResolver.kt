/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.channel.email

import com.lovelycatv.crystalframework.messagechannel.types.chain.AtSegment
import com.lovelycatv.crystalframework.messagechannel.types.resolver.AtResolver

/**
 * Implement and expose as a Spring bean to enable rich rendering of [AtSegment] in email HTML.
 * Without an implementation, the email renderer degrades [AtSegment] to plain text using
 * the segment's own `displayName` (or a `@unknown` placeholder).
 */
interface EmailAtResolver : AtResolver<EmailAtFragment>
