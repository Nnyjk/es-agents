export type HostStatus = 'UNCONNECTED' | 'OFFLINE' | 'ONLINE' | 'EXCEPTION' | 'MAINTENANCE';

export interface Environment {
  id: string;
  code: string;
  name: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface HostInstallGuide {
  hostId: string;
  secretKey: string;
  installScript: string;
  dockerCommand: string;
  downloadUrl: string;
}

export interface Host {
  id: string;
  name: string;
  hostname: string;
  os?: string;
  environment?: Partial<Environment>;
  environmentId?: string; // Form usage
  environmentName?: string;
  status: HostStatus;
  secretKey?: string;
  description?: string;
  lastHeartbeat?: string;
  config?: string;
  heartbeatInterval?: number;
  gatewayUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}
