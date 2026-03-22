import React, { useState, useEffect } from "react";
import { Tabs, message, Spin, Card } from "antd";
import {
  SettingOutlined,
  SecurityScanOutlined,
  ToolOutlined,
  SettingFilled,
} from "@ant-design/icons";
import type {
  SystemSettingsResponse,
  SystemBasicSettings,
  SecuritySettings,
  MaintenanceSettings,
  EmailConfig,
  StorageGlobalConfig,
  FeatureFlags,
  LogConfig,
} from "../../../types/settings";
import { getSystemSettings } from "../../../services/settings";
import BasicSettingsPage from "./BasicSettingsPage";
import SecuritySettingsPage from "./SecuritySettingsPage";
import MaintenanceSettingsPage from "./MaintenanceSettingsPage";
import OtherConfigPage from "./OtherConfigPage";

const SystemSettings: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [settingsData, setSettingsData] =
    useState<SystemSettingsResponse | null>(null);
  const [activeKey, setActiveKey] = useState("basic");

  const fetchSettings = async () => {
    setLoading(true);
    try {
      const data = await getSystemSettings();
      setSettingsData(data);
    } catch {
      message.error("获取系统设置失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSettings();
  }, []);

  const handleUpdate = () => {
    fetchSettings();
  };

  if (loading) {
    return (
      <Card>
        <Spin
          tip="加载中..."
          style={{ display: "flex", justifyContent: "center", padding: 100 }}
        />
      </Card>
    );
  }

  const basicData: SystemBasicSettings | undefined = settingsData?.basic;
  const securityData: SecuritySettings | undefined = settingsData?.security;
  const maintenanceData: MaintenanceSettings | undefined =
    settingsData?.maintenance;
  const emailData: EmailConfig | undefined = settingsData?.email;
  const storageData: StorageGlobalConfig | undefined = settingsData?.storage;
  const featureData: FeatureFlags | undefined = settingsData?.features;
  const logData: LogConfig | undefined = settingsData?.log;

  const tabItems = [
    {
      key: "basic",
      label: (
        <span>
          <SettingOutlined />
          基础设置
        </span>
      ),
      children: <BasicSettingsPage data={basicData} onUpdate={handleUpdate} />,
    },
    {
      key: "security",
      label: (
        <span>
          <SecurityScanOutlined />
          安全设置
        </span>
      ),
      children: (
        <SecuritySettingsPage data={securityData} onUpdate={handleUpdate} />
      ),
    },
    {
      key: "maintenance",
      label: (
        <span>
          <ToolOutlined />
          系统维护
        </span>
      ),
      children: (
        <MaintenanceSettingsPage
          data={maintenanceData}
          onUpdate={handleUpdate}
        />
      ),
    },
    {
      key: "other",
      label: (
        <span>
          <SettingFilled />
          其他配置
        </span>
      ),
      children: (
        <OtherConfigPage
          emailData={emailData}
          storageData={storageData}
          featureData={featureData}
          logData={logData}
          onUpdate={handleUpdate}
        />
      ),
    },
  ];

  return (
    <Card title="系统全局设置">
      <Tabs activeKey={activeKey} onChange={setActiveKey} items={tabItems} />
    </Card>
  );
};

export default SystemSettings;
