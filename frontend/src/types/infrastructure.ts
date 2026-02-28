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
  packageFileName: string;
  startCommand: string;
  stopCommand: string;
  updateCommand: string;
  logPath: string;
  pidFile: string;
  source: {
    sourceId: string;
    sourceName: string;
    fileName: string;
    osType: string;
  };
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
