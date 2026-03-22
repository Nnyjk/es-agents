import React, { useState } from "react";
import { Tabs, Card } from "antd";
import {
  AppstoreOutlined,
  UnorderedListOutlined,
  ApartmentOutlined,
  BellOutlined,
} from "@ant-design/icons";
import ResourceTypePage from "./ResourceTypePage";
import ResourceListPage from "./ResourceListPage";
import ResourceTopologyPage from "./ResourceTopologyPage";
import ExpiryReminderPage from "./ExpiryReminderPage";

const CMDBPage: React.FC = () => {
  const [activeKey, setActiveKey] = useState("list");

  return (
    <Card bordered={false}>
      <Tabs
        activeKey={activeKey}
        onChange={setActiveKey}
        items={[
          {
            key: "list",
            label: "资源台账",
            icon: <UnorderedListOutlined />,
            children: <ResourceListPage />,
          },
          {
            key: "types",
            label: "资源类型",
            icon: <AppstoreOutlined />,
            children: <ResourceTypePage />,
          },
          {
            key: "topology",
            label: "资源拓扑",
            icon: <ApartmentOutlined />,
            children: <ResourceTopologyPage />,
          },
          {
            key: "reminders",
            label: "到期提醒",
            icon: <BellOutlined />,
            children: <ExpiryReminderPage />,
          },
        ]}
      />
    </Card>
  );
};

export default CMDBPage;
