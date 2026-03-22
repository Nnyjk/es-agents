import React, { useState } from "react";
import { Card, Tabs } from "antd";
import {
  UserOutlined,
  LockOutlined,
  ApiOutlined,
  SettingOutlined,
} from "@ant-design/icons";
import ProfileForm from "./components/ProfileForm";
import SecuritySettings from "./components/SecuritySettings";
import ApiTokenList from "./components/ApiTokenList";
import Preferences from "./components/Preferences";

const ProfilePage: React.FC = () => {
  const [activeTab, setActiveTab] = useState("profile");

  const tabItems = [
    {
      key: "profile",
      label: (
        <span>
          <UserOutlined />
          个人资料
        </span>
      ),
      children: <ProfileForm />,
    },
    {
      key: "security",
      label: (
        <span>
          <LockOutlined />
          安全设置
        </span>
      ),
      children: <SecuritySettings />,
    },
    {
      key: "tokens",
      label: (
        <span>
          <ApiOutlined />
          API令牌
        </span>
      ),
      children: <ApiTokenList />,
    },
    {
      key: "preferences",
      label: (
        <span>
          <SettingOutlined />
          偏好设置
        </span>
      ),
      children: <Preferences />,
    },
  ];

  return (
    <Card>
      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        items={tabItems}
        tabPosition="left"
        style={{ minHeight: 500 }}
      />
    </Card>
  );
};

export default ProfilePage;
