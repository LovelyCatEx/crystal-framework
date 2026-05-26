import {doGet, doPost} from "../system-request.ts";

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

export async function initializeSystem(dto: InitializeSystemDTO) {
  return doPost<void>('/api/system/initialize', dto, { 'Content-Type': 'application/json' });
}

export async function checkSystemInitialized() {
  return doGet<{ initialized: boolean }>('/api/system/initialize/status');
}
