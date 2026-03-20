import React from "react";
import { Result, Button, Descriptions, Tag, Space } from "antd";
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  RocketOutlined,
  ReloadOutlined,
} from "@ant-design/icons";
import type { DeployResult as DeployResultType } from "../../../../types";

interface DeployResultProps {
  result: DeployResultType | null;
  onReset: () => void;
  onGoToAgents: () => void;
}

const statusColors: Record<string, string> = {
  DEPLOYED: "success",
  ONLINE: "success",
  DEPLOYING: "processing",
  OFFLINE: "error",
  EXCEPTION: "error",
};

const statusText: Record<string, string> = {
  DEPLOYED: "已部署",
  ONLINE: "在线",
  DEPLOYING: "部署中",
  OFFLINE: "离线",
  EXCEPTION: "异常",
};

export const DeployResult: React.FC<DeployResultProps> = ({
  result,
  onReset,
  onGoToAgents,
}) => {
  if (!result) {
    return (
      <Result
        status="warning"
        title="无部署结果"
        subTitle="请重新进行部署流程"
      />
    );
  }

  const isSuccess = result.status === "DEPLOYED" || result.status === "ONLINE";

  return (
    <div>
      <Result
        icon={
          isSuccess ? (
            <CheckCircleOutlined style={{ color: "#52c41a" }} />
          ) : (
            <CloseCircleOutlined style={{ color: "#ff4d4f" }} />
          )
        }
        title={isSuccess ? "部署成功!" : "部署失败"}
        subTitle={
          result.message ||
          (isSuccess ? "Agent 已成功部署到目标主机" : "请检查错误信息")
        }
      >
        {isSuccess && (
          <Descriptions column={2} bordered size="small">
            <Descriptions.Item label="实例 ID">
              {result.instanceId}
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={statusColors[result.status] || "default"}>
                {statusText[result.status] || result.status}
              </Tag>
            </Descriptions.Item>
            {result.deployedAt && (
              <Descriptions.Item label="部署时间">
                {new Date(result.deployedAt).toLocaleString()}
              </Descriptions.Item>
            )}
          </Descriptions>
        )}

        <Space style={{ marginTop: 24 }}>
          <Button
            type="primary"
            onClick={onGoToAgents}
            icon={<RocketOutlined />}
          >
            查看 Agent 列表
          </Button>
          <Button onClick={onReset} icon={<ReloadOutlined />}>
            重新部署
          </Button>
        </Space>
      </Result>
    </div>
  );
};

export default DeployResult;
