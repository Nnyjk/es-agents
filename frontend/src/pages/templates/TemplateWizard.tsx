import React, { useState } from "react";
import { Card, Steps, Button, message, Spin } from "antd";
import { useNavigate } from "react-router-dom";
import TemplateSelector from "./components/TemplateSelector";
import InstanceConfigComponent from "./components/InstanceConfig";
import HostBinding from "./components/HostBinding";
import ConfigPreview from "./components/ConfigPreview";
import CreationResult from "./components/CreationResult";
import { WizardStep, type InstanceConfig as InstanceConfigType, type CreatedInstance } from "./types";
import { saveAgentInstance } from "../../services/agent";
import type { AgentTemplate, Host } from "../../types";
import styles from "./TemplateWizard.module.css";

const steps = [
  { title: "选择模板", description: "选择 Agent 模板" },
  { title: "配置实例", description: "设置实例参数" },
  { title: "绑定主机", description: "选择目标主机" },
  { title: "确认配置", description: "预览并确认" },
  { title: "创建结果", description: "查看创建结果" },
];

const TemplateWizard: React.FC = () => {
  const navigate = useNavigate();

  // Wizard state
  const [currentStep, setCurrentStep] = useState<WizardStep>(WizardStep.SELECT_TEMPLATE);
  const [selectedTemplate, setSelectedTemplate] = useState<AgentTemplate | null>(null);
  const [instanceConfig, setInstanceConfig] = useState<InstanceConfigType>({
    name: "",
    environmentId: undefined,
    config: {},
  });
  const [selectedHosts, setSelectedHosts] = useState<Host[]>([]);
  const [createdInstances, setCreatedInstances] = useState<CreatedInstance[]>([]);
  const [creating, setCreating] = useState(false);

  // Handle template selection with preselection support
  const handleTemplateChange = (template: AgentTemplate | null) => {
    setSelectedTemplate(template);
    // Auto-generate instance name prefix based on template name
    if (template && !instanceConfig.name) {
      setInstanceConfig((prev: InstanceConfigType) => ({
        ...prev,
        name: template.name.toLowerCase().replace(/\s+/g, "-"),
      }));
    }
  };

  // Step validation and navigation
  const validateStep = (): boolean => {
    switch (currentStep) {
      case WizardStep.SELECT_TEMPLATE:
        if (!selectedTemplate) {
          message.warning("请选择 Agent 模板");
          return false;
        }
        return true;
      case WizardStep.CONFIG_INSTANCE:
        if (!instanceConfig.name.trim()) {
          message.warning("请输入实例名称前缀");
          return false;
        }
        return true;
      case WizardStep.BIND_HOSTS:
        if (selectedHosts.length === 0) {
          message.warning("请选择至少一台主机");
          return false;
        }
        return true;
      case WizardStep.PREVIEW:
        return true;
      default:
        return true;
    }
  };

  const handleNext = () => {
    if (!validateStep()) return;
    setCurrentStep((prev) => Math.min(prev + 1, WizardStep.RESULT));
  };

  const handlePrev = () => {
    setCurrentStep((prev) => Math.max(prev - 1, WizardStep.SELECT_TEMPLATE));
  };

  const handleReset = () => {
    setCurrentStep(WizardStep.SELECT_TEMPLATE);
    setSelectedTemplate(null);
    setInstanceConfig({ name: "", environmentId: undefined, config: {} });
    setSelectedHosts([]);
    setCreatedInstances([]);
  };

  // Batch create instances
  const handleCreate = async () => {
    if (!selectedTemplate || selectedHosts.length === 0) {
      message.error("缺少必要参数");
      return;
    }

    setCreating(true);
    setCreatedInstances([]);

    const results: CreatedInstance[] = [];
    const templateId = selectedTemplate.id;

    // Create instances one by one (since no batch API exists)
    for (const host of selectedHosts) {
      const instanceName = `${instanceConfig.name}-${selectedTemplate.name}-${host.name}`;

      try {
        const response = await saveAgentInstance({
          templateId,
          hostId: host.id,
        });

        results.push({
          instanceId: response.id,
          name: instanceName,
          hostId: host.id,
          hostName: host.name,
          status: "success",
        });
      } catch (error: any) {
        const errorMsg = error?.response?.data?.message || error?.message || "创建失败";
        results.push({
          instanceId: "",
          name: instanceName,
          hostId: host.id,
          hostName: host.name,
          status: "failed",
          error: errorMsg,
        });
      }
    }

    setCreatedInstances(results);
    setCreating(false);
    setCurrentStep(WizardStep.RESULT);

    const successCount = results.filter((r) => r.status === "success").length;
    if (successCount === results.length) {
      message.success(`成功创建 ${successCount} 个实例`);
    } else if (successCount > 0) {
      message.warning(`创建完成：成功 ${successCount} 个，失败 ${results.length - successCount} 个`);
    } else {
      message.error("所有实例创建失败");
    }
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case WizardStep.SELECT_TEMPLATE:
        return (
          <TemplateSelector
            value={selectedTemplate}
            onChange={handleTemplateChange}
          />
        );
      case WizardStep.CONFIG_INSTANCE:
        return (
          <InstanceConfigComponent
            template={selectedTemplate}
            value={instanceConfig}
            onChange={setInstanceConfig}
          />
        );
      case WizardStep.BIND_HOSTS:
        return (
          <HostBinding
            instanceConfig={instanceConfig}
            value={selectedHosts}
            onChange={setSelectedHosts}
          />
        );
      case WizardStep.PREVIEW:
        return (
          <ConfigPreview
            template={selectedTemplate}
            instanceConfig={instanceConfig}
            hosts={selectedHosts}
          />
        );
      case WizardStep.RESULT:
        if (creating) {
          return (
            <div className={styles.creatingContainer}>
              <Spin size="large" />
              <div style={{ marginTop: 16 }}>
                <span>正在创建实例...</span>
                <br />
                <span style={{ color: "#666" }}>
                  已处理 0/{selectedHosts.length} 个
                </span>
              </div>
            </div>
          );
        }
        return (
          <CreationResult
            results={createdInstances}
            templateName={selectedTemplate?.name || ""}
            onReset={handleReset}
          />
        );
      default:
        return null;
    }
  };

  const handleCancel = () => {
    navigate("/agents/templates");
  };

  return (
    <Card className={styles.wizardCard}>
      <div className={styles.header}>
        <h2>从模板创建实例向导</h2>
        <Button onClick={handleCancel}>返回模板列表</Button>
      </div>

      <Steps
        current={currentStep}
        items={steps}
        className={styles.steps}
      />

      <div className={styles.content}>
        {renderStepContent()}
      </div>

      {currentStep < WizardStep.RESULT && (
        <div className={styles.footer}>
          <Button onClick={handleCancel} style={{ marginRight: 8 }}>
            取消
          </Button>
          {currentStep > WizardStep.SELECT_TEMPLATE && (
            <Button onClick={handlePrev} style={{ marginRight: 8 }}>
              上一步
            </Button>
          )}
          {currentStep === WizardStep.PREVIEW ? (
            <Button
              type="primary"
              onClick={handleCreate}
              loading={creating}
              disabled={creating || selectedHosts.length === 0}
            >
              开始创建 ({selectedHosts.length} 个实例)
            </Button>
          ) : (
            <Button type="primary" onClick={handleNext}>
              下一步
            </Button>
          )}
        </div>
      )}
    </Card>
  );
};

export default TemplateWizard;