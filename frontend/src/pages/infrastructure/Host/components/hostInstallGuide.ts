import type { HostInstallGuide } from '@/types';

export interface InstallGuideStep {
  key: string;
  title: string;
  description: string;
  command?: string;
}

export interface InstallGuidePresentation {
  osLabel: string;
  unpackCommand: string;
  steps: InstallGuideStep[];
}

const isWindowsOs = (osType?: string): boolean => osType === 'WINDOWS';

export const buildInstallGuidePresentation = (guide: HostInstallGuide): InstallGuidePresentation => {
  const windows = isWindowsOs(guide.source.osType);
  const osLabel = windows ? 'Windows' : guide.source.osType === 'MACOS' ? 'macOS' : 'Linux';
  const unpackCommand = windows
    ? `Expand-Archive -Path ${guide.packageFileName} -DestinationPath .\\host-agent`
    : `mkdir -p ./host-agent && tar -xzf ${guide.packageFileName} -C ./host-agent`;

  return {
    osLabel,
    unpackCommand,
    steps: [
      {
        key: 'download',
        title: '1. 下载部署包',
        description: `下载与 ${osLabel} 主机匹配的部署包 ${guide.packageFileName}。`,
      },
      {
        key: 'extract',
        title: '2. 解压部署包',
        description: '将部署包解压到独立目录，后续启动和更新命令都在该目录中执行。',
        command: unpackCommand,
      },
      {
        key: 'config',
        title: '3. 确认配置',
        description: '部署包中的 config.yaml 已写入当前主机身份信息。升级时执行更新命令会保留现有 config.yaml，无需重新填写密钥。',
      },
      {
        key: 'start',
        title: '4. 后台启动 Agent',
        description: '安装命令会以后台方式启动 Agent，并立即返回控制台。可通过日志与 PID 文件确认进程状态。',
        command: guide.installScript,
      },
      {
        key: 'connect',
        title: '5. 发起连接验证',
        description: 'Agent 后台启动后，回到当前弹窗点击“连接 Host Agent”由服务端主动验证连接。',
      },
    ],
  };
};
