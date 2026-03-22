import React, { useState } from "react";
import { Tabs, Card } from "antd";
import {
  AppstoreOutlined,
  ApiOutlined,
  RocketOutlined,
  CloudServerOutlined,
} from "@ant-design/icons";
import ApplicationPage from "./ApplicationPage";
import PipelinePage from "./PipelinePage";
import ReleasePage from "./ReleasePage";
import EnvironmentPage from "./EnvironmentPage";

const DeploymentPage: React.FC = () => {
  const [activeKey, setActiveKey] = useState("applications");

  return (
    <Card bordered={false}>
      <Tabs
        activeKey={activeKey}
        onChange={setActiveKey}
        items={[
          {
            key: "applications",
            label: "应用管理",
            icon: <AppstoreOutlined />,
            children: <ApplicationPage />,
          },
          {
            key: "pipelines",
            label: "流水线管理",
            icon: <ApiOutlined />,
            children: <PipelinePage />,
          },
          {
            key: "releases",
            label: "发布管理",
            icon: <RocketOutlined />,
            children: <ReleasePage />,
          },
          {
            key: "environments",
            label: "环境管理",
            icon: <CloudServerOutlined />,
            children: <EnvironmentPage />,
          },
        ]}
      />
    </Card>
  );
};

export default DeploymentPage;