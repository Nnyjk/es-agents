import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { HostInstallGuideModal } from './HostInstallGuideModal';
import { buildInstallGuidePresentation } from './hostInstallGuide';
import { resolveHostPackageDownloadUrl } from '@/services/infra';
import type { HostInstallGuide } from '@/types';

const baseGuide: HostInstallGuide = {
  hostId: 'host-1',
  secretKey: 'secret-1',
  installScript: './install.sh',
  dockerCommand: '',
  downloadUrl: '/api/infra/hosts/host-1/package?sourceId=source-1',
  packageFileName: 'host-agent-linux.tar.gz',
  startCommand: './start.sh',
  stopCommand: './stop.sh',
  uninstallCommand: './uninstall.sh',
  updateCommand: './update.sh <new-package-dir>',
  logPath: './logs/host-agent.log',
  pidFile: './host-agent.pid',
  source: {
    sourceId: 'source-1',
    sourceName: 'HostAgent Linux',
    fileName: 'host-agent-linux-amd64',
    osType: 'LINUX',
  },
};

describe('resolveHostPackageDownloadUrl', () => {
  it('uses install-guide package api url as-is when it already has /api prefix', () => {
    expect(resolveHostPackageDownloadUrl('/api/infra/hosts/host-1/package?sourceId=source-1')).toBe(
      '/api/infra/hosts/host-1/package?sourceId=source-1',
    );
  });

  it('adds /api prefix for relative host package urls and rejects non-host-package urls', () => {
    expect(resolveHostPackageDownloadUrl('/infra/hosts/host-1/package?sourceId=source-1')).toBe(
      '/api/infra/hosts/host-1/package?sourceId=source-1',
    );
    expect(() => resolveHostPackageDownloadUrl('https://github.com/example/release.zip')).toThrow(
      '安装引导返回了无效的部署包地址',
    );
  });
});

describe('buildInstallGuidePresentation', () => {
  it('builds linux install steps with tar and background start guidance', () => {
    const presentation = buildInstallGuidePresentation(baseGuide);

    expect(presentation.unpackCommand).toBe('mkdir -p ./host-agent && tar -xzf host-agent-linux.tar.gz -C ./host-agent');
    expect(presentation.steps[2].description).toContain('创建 config.yaml');
    expect(presentation.steps[3].description).toContain('后台方式启动 Agent');
  });

  it('builds windows install steps with Expand-Archive', () => {
    const presentation = buildInstallGuidePresentation({
      ...baseGuide,
      installScript: 'install.bat',
      packageFileName: 'host-agent-windows.zip',
      startCommand: 'start.bat',
      stopCommand: 'stop.bat',
      uninstallCommand: 'uninstall.bat',
      updateCommand: 'update.bat <new-package-dir>',
      logPath: '.\\logs\\host-agent.log',
      pidFile: '.\\host-agent.pid',
      source: {
        ...baseGuide.source,
        fileName: 'host-agent-windows.zip',
        osType: 'WINDOWS',
      },
    });

    expect(presentation.unpackCommand).toBe('Expand-Archive -Path host-agent-windows.zip -DestinationPath .\\host-agent');
    expect(presentation.osLabel).toBe('Windows');
  });
});

describe('HostInstallGuideModal', () => {
  it('renders install steps for the current os and hides uninstall actions', () => {
    render(
      <HostInstallGuideModal
        visible
        guide={baseGuide}
        loading={false}
        guideError={null}
        downloadError={null}
        connectError={null}
        downloading={false}
        connecting={false}
        onClose={vi.fn()}
        onDownload={vi.fn()}
        onConnect={vi.fn()}
      />,
    );

    expect(screen.getByText('下载 host-agent-linux.tar.gz')).toBeInTheDocument();
    expect(screen.getByText('2. 解压部署包')).toBeInTheDocument();
    expect(screen.getByText('5. 发起连接验证')).toBeInTheDocument();
    expect(screen.getByText('./install.sh')).toBeInTheDocument();
    expect(screen.queryByText(/卸载/)).not.toBeInTheDocument();
  });
});
