import React, { useEffect, useState } from "react";
import {
  Form,
  Input,
  Button,
  Avatar,
  Upload,
  message,
  Spin,
  Descriptions,
  Tag,
  Space,
} from "antd";
import { UserOutlined, UploadOutlined } from "@ant-design/icons";
import type { UploadProps } from "antd";
import {
  getMyProfile,
  updateMyProfile,
  uploadAvatar,
} from "../../../services/userProfile";
import type { UserProfile } from "../../../types/userProfile";

const ProfileForm: React.FC = () => {
  const [form] = Form.useForm();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const data = await getMyProfile();
      setProfile(data);
      form.setFieldsValue({
        nickname: data.nickname,
        email: data.email,
        phone: data.phone,
      });
    } catch {
      message.error("获取个人资料失败");
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (values: {
    nickname?: string;
    phone?: string;
  }) => {
    try {
      setSubmitting(true);
      await updateMyProfile(values);
      message.success("更新成功");
      fetchProfile();
    } catch {
      message.error("更新失败");
    } finally {
      setSubmitting(false);
    }
  };

  const uploadProps: UploadProps = {
    showUploadList: false,
    accept: "image/*",
    beforeUpload: async (file) => {
      const isImage = file.type.startsWith("image/");
      if (!isImage) {
        message.error("只能上传图片文件");
        return false;
      }
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isLt2M) {
        message.error("图片大小不能超过 2MB");
        return false;
      }
      try {
        await uploadAvatar(file);
        message.success("头像上传成功");
        fetchProfile();
      } catch {
        message.error("头像上传失败");
      }
      return false;
    },
  };

  const getStatusTag = (status: string) => {
    const statusMap: Record<string, { color: string; text: string }> = {
      ACTIVE: { color: "green", text: "正常" },
      INACTIVE: { color: "default", text: "未激活" },
      LOCKED: { color: "red", text: "锁定" },
    };
    const config = statusMap[status] || { color: "default", text: status };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  if (loading) {
    return (
      <div style={{ textAlign: "center", padding: 50 }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div>
      {/* 基本信息 */}
      <Descriptions
        title="基本信息"
        bordered
        column={2}
        style={{ marginBottom: 24 }}
      >
        <Descriptions.Item label="用户名">
          {profile?.username}
        </Descriptions.Item>
        <Descriptions.Item label="邮箱">{profile?.email}</Descriptions.Item>
        <Descriptions.Item label="状态">
          {profile?.status && getStatusTag(profile.status)}
        </Descriptions.Item>
        <Descriptions.Item label="角色">
          <Space>
            {profile?.roles.map((role) => (
              <Tag key={role.id} color="blue">
                {role.name}
              </Tag>
            ))}
          </Space>
        </Descriptions.Item>
        <Descriptions.Item label="创建时间">
          {profile?.createdAt
            ? new Date(profile.createdAt).toLocaleString()
            : "-"}
        </Descriptions.Item>
        <Descriptions.Item label="最后登录">
          {profile?.lastLoginAt
            ? new Date(profile.lastLoginAt).toLocaleString()
            : "-"}
        </Descriptions.Item>
      </Descriptions>

      {/* 头像上传 */}
      <div style={{ marginBottom: 24 }}>
        <h4 style={{ marginBottom: 16 }}>头像</h4>
        <Space>
          <Avatar size={80} src={profile?.avatar} icon={<UserOutlined />} />
          <Upload {...uploadProps}>
            <Button icon={<UploadOutlined />}>上传头像</Button>
          </Upload>
        </Space>
      </div>

      {/* 编辑表单 */}
      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        style={{ maxWidth: 400 }}
      >
        <Form.Item name="nickname" label="昵称">
          <Input placeholder="请输入昵称" />
        </Form.Item>
        <Form.Item name="phone" label="手机号">
          <Input placeholder="请输入手机号" />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" loading={submitting}>
            保存修改
          </Button>
        </Form.Item>
      </Form>
    </div>
  );
};

export default ProfileForm;
