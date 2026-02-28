import React from 'react';
import { Alert, Button, Modal, Space, Typography } from 'antd';
import { ApiOutlined, DownloadOutlined } from '@ant-design/icons';
import type { HostInstallGuide } from '@/types';
import { buildInstallGuidePresentation } from './hostInstallGuide';

interface HostInstallGuideModalProps {
  visible: boolean;
  guide: HostInstallGuide | null;
  loading: boolean;
  guideError?: string | null;
  downloadError?: string | null;
  connectError?: string | null;
  connecting: boolean;
  onClose: () => void;
  onDownload: () => void;
  onConnect: () => void;
}

export const HostInstallGuideModal: React.FC<HostInstallGuideModalProps> = ({
  visible,
  guide,
  loading,
  guideError,
  downloadError,
  connectError,
  connecting,
  onClose,
  onDownload,
  onConnect,
}) => {
  const presentation = guide ? buildInstallGuidePresentation(guide) : null;

  return (
    <Modal
      title="Host Agent 接入指南"
      open={visible}
      onCancel={onClose}
      footer={null}
      width={680}
      destroyOnHidden
    >
      <div style={{ padding: 20 }}>
        <Space direction="vertical" size={16} style={{ display: 'flex' }}>
          {guideError ? <Alert type="error" showIcon message="获取接入指南失败" description={guideError} /> : null}
          {downloadError ? <Alert type="error" showIcon message="部署包下载失败" description={downloadError} /> : null}
          {connectError ? <Alert type="error" showIcon message="Host Agent 连接失败" description={connectError} /> : null}

          {loading ? (
            <div style={{ textAlign: 'center', padding: '20px' }}>正在获取接入指南...</div>
          ) : null}

          {!loading && guide && presentation ? (
            <>
              <div>
                <h3 style={{ marginBottom: 16 }}>身份验证信息</h3>
                <div style={{ marginBottom: 16 }}>
                  <div style={{ marginBottom: 4, color: '#666' }}>Host ID</div>
                  <Typography.Paragraph copyable code style={{ marginBottom: 0 }}>
                    {guide.hostId}
                  </Typography.Paragraph>
                </div>
                <div>
                  <div style={{ marginBottom: 4, color: '#666' }}>Secret Key</div>
                  <Typography.Paragraph copyable code style={{ marginBottom: 0 }}>
                    {guide.secretKey}
                  </Typography.Paragraph>
                </div>
              </div>

              <div style={{ borderTop: '1px solid #f0f0f0', paddingTop: 20 }}>
                <h3 style={{ marginBottom: 12 }}>安装步骤</h3>
                <Space direction="vertical" size={12} style={{ display: 'flex' }}>
                  <div>
                    <div style={{ marginBottom: 4, fontWeight: 'bold' }}>已绑定资源</div>
                    <div>{guide.source.sourceName}</div>
                    <Typography.Paragraph copyable code style={{ marginBottom: 0 }}>
                      {guide.source.fileName}
                    </Typography.Paragraph>
                  </div>

                  {presentation.steps.map((step) => (
                    <div key={step.key}>
                      <div style={{ marginBottom: 4, fontWeight: 'bold' }}>{step.title}</div>
                      <div>{step.description}</div>
                      {step.key === 'download' ? (
                        <div style={{ marginTop: 8 }}>
                          <Button type="primary" icon={<DownloadOutlined />} onClick={onDownload}>
                            下载 {guide.packageFileName}
                          </Button>
                        </div>
                      ) : null}
                      {step.command ? (
                        <Typography.Paragraph copyable code style={{ marginTop: 8, marginBottom: 0 }}>
                          {step.command}
                        </Typography.Paragraph>
                      ) : null}
                    </div>
                  ))}

                  <div>
                    <div style={{ marginBottom: 4, fontWeight: 'bold' }}>后台运行验证</div>
                    <div>后台进程会持续写入日志和 PID 文件。</div>
                    <Typography.Paragraph copyable code style={{ marginBottom: 4 }}>
                      {guide.logPath}
                    </Typography.Paragraph>
                    <Typography.Paragraph copyable code style={{ marginBottom: 0 }}>
                      {guide.pidFile}
                    </Typography.Paragraph>
                  </div>

                  <div>
                    <div style={{ marginBottom: 4, fontWeight: 'bold' }}>常用命令</div>
                    <Typography.Paragraph copyable code style={{ marginBottom: 4 }}>
                      {guide.startCommand}
                    </Typography.Paragraph>
                    <Typography.Paragraph copyable code style={{ marginBottom: 4 }}>
                      {guide.stopCommand}
                    </Typography.Paragraph>
                    <Typography.Paragraph copyable code style={{ marginBottom: 0 }}>
                      {guide.updateCommand}
                    </Typography.Paragraph>
                  </div>
                </Space>
              </div>

              <div style={{ borderTop: '1px solid #eee', paddingTop: 20 }}>
                <p>Host Agent 后台启动后，请点击下方按钮由 Server 主动尝试连接。</p>
                <Button type="primary" icon={<ApiOutlined />} loading={connecting} onClick={onConnect} block>
                  连接 Host Agent
                </Button>
              </div>
            </>
          ) : null}
        </Space>
      </div>
    </Modal>
  );
};
