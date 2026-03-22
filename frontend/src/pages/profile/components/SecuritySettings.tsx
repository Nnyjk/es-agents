import React, { useEffect, useState } from "react";
import {
  Card,
  Form,
  Input,
  Button,
  message,
  Table,
  Tag,
  Space,
  Modal,
} from "antd";
import { LockOutlined, SafetyOutlined } from "@ant-design/icons";
import { changePassword, getLoginHistory } from "../../../services/userProfile";
import type { LoginHistory } from "../../../types/userProfile";

const SecuritySettings: React.FC = () => {
  const [passwordForm] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);
  const [loginHistory, setLoginHistory] = useState<LoginHistory[]>([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyModalVisible, setHistoryModalVisible] = useState(false);

  useEffect(() => {
    fetchLoginHistory();
  }, []);

  const fetchLoginHistory = async () => {
    try {
      setHistoryLoading(true);
      const result = await getLoginHistory({ pageSize: 20 });
      setLoginHistory(result.list);
    } catch {
      // 静默失败
    } finally {
      setHistoryLoading(false);
    }
  };

  const handleChangePassword = async (values: {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
  }) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error("两次输入的密码不一致");
      return;
    }
    try {
      setSubmitting(true);
      await changePassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
        confirmPassword: values.confirmPassword,
      });
      message.success("密码修改成功，请重新登录");
      passwordForm.resetFields();
    } catch {
      message.error("密码修改失败");
    } finally {
      setSubmitting(false);
    }
  };

  const getStatusTag = (status: string) => {
    return status === "SUCCESS" ? (
      <Tag color="green">成功</Tag>
    ) : (
      <Tag color="red">失败</Tag>
    );
  };

  const historyColumns = [
    {
      title: "登录时间",
      dataIndex: "loginAt",
      key: "loginAt",
      render: (value: string) => new Date(value).toLocaleString(),
    },
    {
      title: "IP地址",
      dataIndex: "ip",
      key: "ip",
    },
    {
      title: "设备",
      dataIndex: "device",
      key: "device",
    },
    {
      title: "地点",
      dataIndex: "location",
      key: "location",
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (status: string) => getStatusTag(status),
    },
  ];

  return (
    <div>
      {/* 修改密码 */}
      <Card title="修改密码" style={{ marginBottom: 24 }}>
        <Form
          form={passwordForm}
          layout="vertical"
          onFinish={handleChangePassword}
          style={{ maxWidth: 400 }}
        >
          <Form.Item
            name="currentPassword"
            label="当前密码"
            rules={[{ required: true, message: "请输入当前密码" }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="请输入当前密码"
            />
          </Form.Item>
          <Form.Item
            name="newPassword"
            label="新密码"
            rules={[
              { required: true, message: "请输入新密码" },
              { min: 8, message: "密码长度不能少于8位" },
              {
                pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/,
                message: "密码必须包含大小写字母和数字",
              },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="请输入新密码"
            />
          </Form.Item>
          <Form.Item
            name="confirmPassword"
            label="确认新密码"
            rules={[{ required: true, message: "请确认新密码" }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="请再次输入新密码"
            />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting}>
              修改密码
            </Button>
          </Form.Item>
        </Form>
      </Card>

      {/* 两步验证（预留） */}
      <Card title="两步验证" style={{ marginBottom: 24 }}>
        <Space>
          <SafetyOutlined style={{ fontSize: 24, color: "#1890ff" }} />
          <span>两步验证功能即将上线，敬请期待...</span>
        </Space>
      </Card>

      {/* 登录历史 */}
      <Card
        title="登录历史"
        extra={
          <Button type="link" onClick={() => setHistoryModalVisible(true)}>
            查看全部
          </Button>
        }
      >
        <Table
          columns={historyColumns}
          dataSource={loginHistory.slice(0, 5)}
          rowKey="id"
          pagination={false}
          loading={historyLoading}
          size="small"
        />
      </Card>

      {/* 登录历史弹窗 */}
      <Modal
        title="登录历史"
        open={historyModalVisible}
        onCancel={() => setHistoryModalVisible(false)}
        footer={null}
        width={800}
      >
        <Table
          columns={historyColumns}
          dataSource={loginHistory}
          rowKey="id"
          pagination={{ pageSize: 10 }}
          loading={historyLoading}
        />
      </Modal>
    </div>
  );
};

export default SecuritySettings;
