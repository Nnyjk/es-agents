import React, { useState } from "react";
import { Tabs } from "antd";
import {
  ScheduleOutlined,
  FileTextOutlined,
  CloudSyncOutlined,
  SettingOutlined,
} from "@ant-design/icons";
import BackupTaskList from "./BackupTaskList";
import BackupRecordList from "./BackupRecordList";
import RestorePage from "./RestorePage";
import BackupConfigPage from "./BackupConfigPage";

const BackupManagement: React.FC = () => {
  const [activeKey, setActiveKey] = useState("tasks");

  return (
    <Tabs
      activeKey={activeKey}
      onChange={setActiveKey}
      items={[
        {
          key: "tasks",
          label: (
            <span>
              <ScheduleOutlined />
              备份任务
            </span>
          ),
          children: <BackupTaskList />,
        },
        {
          key: "records",
          label: (
            <span>
              <FileTextOutlined />
              备份记录
            </span>
          ),
          children: <BackupRecordList />,
        },
        {
          key: "restore",
          label: (
            <span>
              <CloudSyncOutlined />
              数据恢复
            </span>
          ),
          children: <RestorePage />,
        },
        {
          key: "config",
          label: (
            <span>
              <SettingOutlined />
              配置管理
            </span>
          ),
          children: <BackupConfigPage />,
        },
      ]}
    />
  );
};

export default BackupManagement;