import React, { useState } from "react";
import { Card, Steps, Button, message } from "antd";
import { useNavigate } from "react-router-dom";
import { SelectEnvironment } from "./components/SelectEnvironment";
import { SelectHost } from "./components/SelectHost";
import { DeployConfig } from "./components/DeployConfig";
import DeployExecution from "./components/DeployExecution";
import { DeployResult } from "./components/DeployResult";
import type { Environment, Host, AgentTemplate, DeployParams, DeployResult as DeployResultType } from "../../../types";

const steps = [
  { title: "选择环境", description: "选择目标部署环境" },
  { title: "选择主机", description: "选择目标主机" },
  { title: "配置部署", description: "设置部署参数" },
  { title: "执行部署", description: "等待部署完成" },
  { title: "部署结果", description: "查看部署结果" },
];

const DeploymentWizard: React.FC = () => {
  const navigate = useNavigate();
  const [current, setCurrent] = useState(0);
  const [_loading, _setLoading] = useState(false);
  
  // Wizard state
  const [selectedEnv, setSelectedEnv] = useState<Environment | null>(null);
  const [selectedHost, setSelectedHost] = useState<Host | null>(null);
  const [selectedTemplate, setSelectedTemplate] = useState<AgentTemplate | null>(null);
  const [deployParams, setDeployParams] = useState<DeployParams>({ version: "" });
  const [deployResult, setDeployResult] = useState<DeployResultType | null>(null);

  const handleNext = () => {
    // Validation before moving to next step
    if (current === 0 && !selectedEnv) {
      message.warning("请选择环境");
      return;
    }
    if (current === 1 && !selectedHost) {
      message.warning("请选择主机");
      return;
    }
    if (current === 2 && !selectedTemplate) {
      message.warning("请选择 Agent 模板");
      return;
    }
    if (current === 2 && !deployParams.version) {
      message.warning("请输入部署版本");
      return;
    }
    setCurrent(current + 1);
  };

  const handlePrev = () => {
    setCurrent(current - 1);
  };

  const handleReset = () => {
    setCurrent(0);
    setSelectedEnv(null);
    setSelectedHost(null);
    setSelectedTemplate(null);
    setDeployParams({ version: "" });
    setDeployResult(null);
  };

  const handleGoToAgents = () => {
    navigate("/agents/instances");
  };

  const renderStepContent = () => {
    switch (current) {
      case 0:
        return (
          <SelectEnvironment
            value={selectedEnv}
            onChange={setSelectedEnv}
          />
        );
      case 1:
        return (
          <SelectHost
            environmentId={selectedEnv?.id}
            value={selectedHost}
            onChange={setSelectedHost}
          />
        );
      case 2:
        return (
          <DeployConfig
            host={selectedHost}
            value={selectedTemplate}
            deployParams={deployParams}
            onTemplateChange={setSelectedTemplate}
            onParamsChange={setDeployParams}
          />
        );
      case 3:
        return (
          <DeployExecution
            host={selectedHost}
            template={selectedTemplate}
            deployParams={deployParams}
            onComplete={(result) => {
              setDeployResult(result);
              setCurrent(4);
            }}
          />
        );
      case 4:
        return (
          <DeployResult
            result={deployResult}
            onReset={handleReset}
            onGoToAgents={handleGoToAgents}
          />
        );
      default:
        return null;
    }
  };

  return (
    <Card>
      <Steps
        current={current}
        items={steps}
        style={{ marginBottom: 24 }}
      />
      <div style={{ minHeight: 400 }}>
        {renderStepContent()}
      </div>
      {current < 4 && (
        <div style={{ marginTop: 24, textAlign: "right" }}>
          {current > 0 && (
            <Button style={{ marginRight: 8 }} onClick={handlePrev}>
              上一步
            </Button>
          )}
          <Button type="primary" onClick={handleNext}>
            {current === 3 ? "开始部署" : "下一步"}
          </Button>
        </div>
      )}
    </Card>
  );
};

export default DeploymentWizard;