/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {doGet, doPost} from "../system-request.ts";
import {HEADER_SYSTEM_INITIALIZE_TOKEN} from "@/global/constants.ts";

export interface InitializeSystemDTO {
  username: string;
  password: string;
  email: string;
  smtpHost: string;
  smtpPort: number;
  smtpUsername: string;
  smtpPassword: string;
  fromEmail: string;
  fromName: string;
}

export async function initializeSystem(dto: InitializeSystemDTO, initializationToken: string) {
  return doPost<void>('/api/system/initialize', dto, {
    'Content-Type': 'application/json',
    [HEADER_SYSTEM_INITIALIZE_TOKEN]: initializationToken,
  });
}

export async function checkSystemInitialized() {
  return doGet<{ initialized: boolean }>('/api/system/initialize/status');
}
