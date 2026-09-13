/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {doGet} from "../system-request.ts";
import type {SystemIntegratedInfoVO} from "@/types/system/system-integrated.types.ts";

export function getSystemIntegratedInfo() {
    return doGet<SystemIntegratedInfoVO>('/api/system/integratedInfo')
}
