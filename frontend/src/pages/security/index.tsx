import React from "react";
import { Tabs } from "antd";
import {
  SafetyCertificateOutlined,
  BugOutlined,
  CheckCircleOutlined,
  FileProtectOutlined,
} from "@ant-design/icons";
import BaselineCheck from "./components/BaselineCheck";
import VulnerabilityScan from "./components/VulnerabilityScan";
import ComplianceCheck from "./components/ComplianceCheck";
import AuditSupport from "./components/AuditSupport";

const SecurityPage: React.FC = () => {
  const items = [
    {
      key: "baseline",
      label: (
        <span>
          <SafetyCertificateOutlined />
          安全基线检查
        </span>
      ),
      children: <BaselineCheck />,
    },
    {
      key: "vulnerability",
      label: (
        <span>
          <BugOutlined />
          漏洞扫描管理
        </span>
      ),
      children: <VulnerabilityScan />,
    },
    {
      key: "compliance",
      label: (
        <span>
          <CheckCircleOutlined />
          合规自查管理
        </span>
      ),
      children: <ComplianceCheck />,
    },
    {
      key: "audit",
      label: (
        <span>
          <FileProtectOutlined />
          等保测评支持
        </span>
      ),
      children: <AuditSupport />,
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Tabs
        defaultActiveKey="baseline"
        items={items}
        size="large"
        tabBarStyle={{
          marginBottom: 24,
          borderBottom: "1px solid #f0f0f0",
        }}
      />
    </div>
  );
};

export default SecurityPage;