import React from 'react';
import { Alert, Button, Modal, Space, Typography, Steps, Divider } from 'antd';
import { ApiOutlined, DownloadOutlined, CheckCircleOutlined } from '@ant-design/icons';
import type { HostInstallGuide } from '@/types';
import { buildInstallGuidePresentation } from './hostInstallGuide';

interface HostInstallGuideModalProps {
  visible: boolean;
  guide: HostInstallGuide | null;
  loading: boolean;
  guideError?: string | null;
  downloadError?: string | null;
  connectError?: string | null;
  downloading: boolean;
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
  downloading,
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
      width={750}
      destroyOnHidden
    >
      <div style={{ padding: '16px 24px' }}>
        <Space direction="vertical" size={16} style={{ display: 'flex', width: '100%' }}>
          {guideError && <Alert type="error" showIcon message="获取接入指南失败" description={guideError} />}
          {downloadError && <Alert type="error" showIcon message="部署包下载失败" description={downloadError} />}
          {connectError && <Alert type="error" showIcon message="Host Agent 连接失败" description={connectError} />}

          {loading ? (
            <div style={{ textAlign: 'center', padding: '40px' }}>正在获取接入指南...</div>
          ) : null}

          {!loading && guide && presentation ? (
            <>
              <Alert
                type="info"
                showIcon
                message="身份验证信息"
                description={
                  <Space direction="vertical" size={8} style={{ width: '100%' }}>
                    <div>
                      <span style={{ color: '#666', marginRight: 8 }}>Host ID:</span>
                      <Typography.Text copyable code>{guide.hostId}</Typography.Text>
                    </div>
                    <div>
                      <span style={{ color: '#666', marginRight: 8 }}>Secret Key:</span>
                      <Typography.Text copyable code>{guide.secretKey}</Typography.Text>
                    </div>
                  </Space>
                }
              />

              <Divider orientation="left">安装步骤</Divider>

              <Steps
                direction="vertical"
                size="small"
                current={downloading ? 0 : guide.downloadUrl ? 1 : 0}
                items={presentation.steps.map((step) => ({
                  key: step.key,
                  title: step.title,
                  description: (
                    <div style={{ marginTop: 8 }}>
                      <div>{step.description}</div>
                      {step.key === 'download' ? (
                        <Button
                          type="primary"
                          icon={<DownloadOutlined />}
                          loading={downloading}
                          onClick={onDownload}
                          style={{ marginTop: 8 }}
                        >
                          下载 {guide.packageFileName}
                        </Button>
                      ) : null}
                      {step.command ? (
                        <Typography.Paragraph
                          copyable
                          code
                          style={{ marginTop: 8, marginBottom: 0, backgroundColor: '#f5f5f5', padding: '8px 12px' }}
                        >
                          {step.command}
                        </Typography.Paragraph>
                      ) : null}
                    </div>
                  ),
                  icon: step.key === 'connect' ? <ApiOutlined /> : <CheckCircleOutlined />,
                }))}
              />

              <Alert
                type="success"
                showIcon
                message="常用命令"
                description={
                  <Space direction="vertical" size={4} style={{ width: '100%' }}>
                    <Typography.Text code>启动：{guide.startCommand}</Typography.Text>
                    <Typography.Text code>停止：{guide.stopCommand}</Typography.Text>
                    <Typography.Text code>卸载：{guide.uninstallCommand}</Typography.Text>
                    <Typography.Text code>更新：{guide.updateCommand}</Typography.Text>
                  </Space>
                }
              />

              <Divider />

              <div style={{ textAlign: 'center', padding: '16px 0' }}>
                <Typography.Paragraph type="secondary" style={{ marginBottom: 16 }}>
                  Host Agent 后台启动后，请点击下方按钮由 Server 主动尝试连接
                </Typography.Paragraph>
                <Button
                  type="primary"
                  icon={<ApiOutlined />}
                  loading={connecting}
                  onClick={onConnect}
                  size="large"
                  block
                >
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
