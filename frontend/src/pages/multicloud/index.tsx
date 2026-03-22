import React, { useState } from "react";
import { Tabs, Card } from "antd";
import {
  CloudServerOutlined,
  AccountBookOutlined,
  ClusterOutlined,
  DollarOutlined,
  ScheduleOutlined,
} from "@ant-design/icons";
import CloudAccountPage from "./CloudAccountPage";
import ResourceOverviewPage from "./ResourceOverviewPage";
import CrossCloudOperationPage from "./CrossCloudOperationPage";
import CostAnalysisPage from "./CostAnalysisPage";
import ScheduleManagementPage from "./ScheduleManagementPage";

const MultiCloudPage: React.FC = () => {
  const [activeKey, setActiveKey] = useState("accounts");

  return (
    <Card bordered={false}>
      <Tabs
        activeKey={activeKey}
        onChange={setActiveKey}
        items={[
          {
            key: "accounts",
            label: "多云账号管理",
            icon: <AccountBookOutlined />,
            children: <CloudAccountPage />,
          },
          {
            key: "overview",
            label: "多云资源视图",
            icon: <ClusterOutlined />,
            children: <ResourceOverviewPage />,
          },
          {
            key: "operations",
            label: "跨云资源操作",
            icon: <CloudServerOutlined />,
            children: <CrossCloudOperationPage />,
          },
          {
            key: "costs",
            label: "多云成本优化",
            icon: <DollarOutlined />,
            children: <CostAnalysisPage />,
          },
          {
            key: "schedules",
            label: "运维调度管理",
            icon: <ScheduleOutlined />,
            children: <ScheduleManagementPage />,
          },
        ]}
      />
    </Card>
  );
};

export default MultiCloudPage;
