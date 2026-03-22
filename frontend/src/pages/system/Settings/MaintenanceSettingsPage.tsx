import React, { useState } from "react";
import {
  Form,
  Switch,
  Button,
  message,
  Card,
  Row,
  Col,
  Input,
  DatePicker,
  Alert,
  Statistic,
  Progress,
  Space,
  Modal,
  Divider,
  Spin,
} from "antd";
import {
  SaveOutlined,
  ToolOutlined,
  ReloadOutlined,
  ClearOutlined,
  WarningOutlined,
  CheckCircleOutlined,
  SyncOutlined,
} from "@ant-design/icons";
import type { MaintenanceSettings, SystemStatus, CacheClearResult } from "../../../types/settings";
import { updateMaintenanceSettings, getSystemStatus, clearSystemCache } from "../../../services/settings";
import dayjs from "dayjs";

interface MaintenanceSettingsPageProps {
  data?: MaintenanceSettings;
  onUpdate: () => void;
}

const MaintenanceSettingsPage: React.FC<MaintenanceSettingsPageProps> = ({
  data,
  onUpdate,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [systemStatus, setSystemStatus] = useState<SystemStatus | null>(null);
  const [statusLoading, setStatusLoading] = useState(false);
  const [clearLoading, setClearLoading] = useState(false);
  const [restartModalVisible, setRestartModalVisible] = useState(false);

  React.useEffect(() => {
    if (data) {
      form.setFieldsValue({
        ...data,
        maintenanceStartTime: data.maintenanceStartTime
          ? dayjs(data.maintenanceStartTime)
          : undefined,
        maintenanceEndTime: data.maintenanceEndTime
          ? dayjs(data.maintenanceEndTime)
          : undefined,
      });
    }
    loadSystemStatus();
  }, [data, form]);

  const loadSystemStatus = async () => {
    setStatusLoading(true);
    try {
      const status = await getSystemStatus();
      setSystemStatus(status);
    } catch {
      // 加载失败
    } finally {
      setStatusLoading(false);
    }
  };

  const handleSubmit = async (values: MaintenanceSettings) => {
    setLoading(true);
    try {
      const submitData = {
        ...values,
        maintenanceStartTime: values.maintenanceStartTime?.toISOString(),
        maintenanceEndTime: values.maintenanceEndTime?.toISOString(),
      };
      await updateMaintenanceSettings(submitData);
      message.success("维护设置保存成功");
      onUpdate();
    } catch {
      message.error("保存失败，请重试");
    } finally {
      setLoading(false);
    }
  };

  const handleClearCache = async () => {
    Modal.confirm({
      title: "确认清理缓存",
      icon: <WarningOutlined />,
      content: "清理系统缓存可能会影响系统性能，确定要继续吗？",
      okText: "确定",
      cancelText: "取消",
      onOk: async () => {
        setClearLoading(true);
        try {
          const result: CacheClearResult = await clearSystemCache();
          message.success(
            `缓存清理成功，释放内存: ${result.freedMemoryFormatted}`
          );
          loadSystemStatus();
        } catch {
          message.error("缓存清理失败");
        } finally {
          setClearLoading(false);
        }
      },
    });
  };

  const formatUptime = (seconds: number): string => {
    const days = Math.floor(seconds / 86400);
    const hours = Math.floor((seconds % 86400) / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${days}天 ${hours}小时 ${minutes}分钟`;
  };

  return (
    <div>
      <Row gutter={24}>
        <Col span={24}>
          <Card
            title={
              <span>
                <ToolOutlined style={{ marginRight: 8 }} />
                系统状态
              </span>
            }
            style={{ marginBottom: 16 }}
            extra={
              <Button icon={<SyncOutlined />} onClick={loadSystemStatus}>
                刷新
              </Button>
            }
          >
            {statusLoading ? (
              <Spin />
            ) : systemStatus ? (
              <>
                <Row gutter={24}>
                  <Col span={6}>
                    <Statistic title="系统版本" value={systemStatus.version} />
                  </Col>
                  <Col span={6}>
                    <Statistic
                      title="运行时间"
                      value={formatUptime(systemStatus.uptime)}
                    />
                  </Col>
                  <Col span={6}>
                    <Statistic
                      title="数据库状态"
                      value={systemStatus.databaseStatus === "HEALTHY" ? "正常" : "异常"}
                      valueStyle={{
                        color:
                          systemStatus.databaseStatus === "HEALTHY"
                            ? "#3f8600"
                            : "#cf1322",
                      }}
                      prefix={
                        systemStatus.databaseStatus === "HEALTHY" ? (
                          <CheckCircleOutlined />
                        ) : (
                          <WarningOutlined />
                        )
                      }
                    />
                  </Col>
                  <Col span={6}>
                    <Statistic
                      title="缓存状态"
                      value={systemStatus.cacheStatus === "HEALTHY" ? "正常" : "异常"}
                      valueStyle={{
                        color:
                          systemStatus.cacheStatus === "HEALTHY"
                            ? "#3f8600"
                            : "#cf1322",
                      }}
                      prefix={
                        systemStatus.cacheStatus === "HEALTHY" ? (
                          <CheckCircleOutlined />
                        ) : (
                          <WarningOutlined />
                        )
                      }
                    />
                  </Col>
                </Row>
                <Divider />
                <Row gutter={24}>
                  <Col span={8}>
                    <div style={{ marginBottom: 8 }}>CPU使用率</div>
                    <Progress
                      percent={systemStatus.cpuUsage}
                      status={systemStatus.cpuUsage > 80 ? "exception" : "active"}
                    />
                  </Col>
                  <Col span={8}>
                    <div style={{ marginBottom: 8 }}>内存使用率</div>
                    <Progress
                      percent={systemStatus.memoryUsage}
                      status={systemStatus.memoryUsage > 80 ? "exception" : "active"}
                    />
                  </Col>
                  <Col span={8}>
                    <div style={{ marginBottom: 8 }}>磁盘使用率</div>
                    <Progress
                      percent={systemStatus.diskUsage}
                      status={systemStatus.diskUsage > 80 ? "exception" : "active"}
                    />
                  </Col>
                </Row>
              </>
            ) : (
              <Alert message="无法获取系统状态" type="warning" />
            )}
          </Card>
        </Col>

        <Col span={24}>
          <Card
            title={
              <span>
                <ToolOutlined style={{ marginRight: 8 }} />
                维护模式设置
              </span>
            }
            style={{ marginBottom: 16 }}
          >
            <Form form={form} layout="vertical" onFinish={handleSubmit}>
              <Alert
                message="启用维护模式后，普通用户将无法访问系统，仅管理员可以登录"
                type="warning"
                showIcon
                style={{ marginBottom: 16 }}
              />
              <Row gutter={16}>
                <Col span={8}>
                  <Form.Item
                    name="maintenanceMode"
                    label="维护模式"
                    valuePropName="checked"
                  >
                    <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    name="allowedAdminsOnly"
                    label="仅允许超级管理员"
                    valuePropName="checked"
                  >
                    <Switch checkedChildren="是" unCheckedChildren="否" />
                  </Form.Item>
                </Col>
              </Row>
              <Form.Item name="maintenanceMessage" label="维护提示信息">
                <Input.TextArea
                  rows={3}
                  placeholder="系统维护期间显示的提示信息"
                  maxLength={500}
                  showCount
                />
              </Form.Item>
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item name="maintenanceStartTime" label="计划开始时间">
                    <DatePicker showTime style={{ width: "100%" }} />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="maintenanceEndTime" label="计划结束时间">
                    <DatePicker showTime style={{ width: "100%" }} />
                  </Form.Item>
                </Col>
              </Row>
              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  icon={<SaveOutlined />}
                  loading={loading}
                >
                  保存设置
                </Button>
              </Form.Item>
            </Form>
          </Card>
        </Col>

        <Col span={24}>
          <Card title="系统操作" style={{ marginBottom: 16 }}>
            <Space size="large">
              <Button
                icon={<ClearOutlined />}
                onClick={handleClearCache}
                loading={clearLoading}
              >
                清理系统缓存
              </Button>
              <Button
                icon={<ReloadOutlined />}
                danger
                onClick={() => setRestartModalVisible(true)}
              >
                重启系统服务
              </Button>
            </Space>
          </Card>
        </Col>
      </Row>

      <Modal
        title="确认重启系统服务"
        open={restartModalVisible}
        onCancel={() => setRestartModalVisible(false)}
        footer={[
          <Button key="cancel" onClick={() => setRestartModalVisible(false)}>
            取消
          </Button>,
          <Button
            key="submit"
            type="primary"
            danger
            onClick={() => {
              message.warning("重启功能需要后端API支持");
              setRestartModalVisible(false);
            }}
          >
            确认重启
          </Button>,
        ]}
      >
        <Alert
          message="警告"
          description="重启系统服务将导致所有用户断开连接，服务将在几秒后自动恢复。确定要继续吗？"
          type="warning"
          showIcon
        />
      </Modal>
    </div>
  );
};

export default MaintenanceSettingsPage;