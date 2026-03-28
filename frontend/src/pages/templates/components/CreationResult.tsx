import React from "react";
import {
  Result,
  Button,
  Tag,
  Space,
  Table,
  Typography,
  Progress,
  Statistic,
  Row,
  Col,
  Card,
} from "antd";
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  RocketOutlined,
  ReloadOutlined,
  EyeOutlined,
} from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import type { CreatedInstance } from "../types";

const { Text } = Typography;

interface CreationResultProps {
  results: CreatedInstance[];
  templateName: string;
  onReset: () => void;
}

export const CreationResult: React.FC<CreationResultProps> = ({
  results,
  templateName,
  onReset,
}) => {
  const navigate = useNavigate();

  if (results.length === 0) {
    return (
      <Result
        status="warning"
        title="无创建结果"
        subTitle="请重新进行创建流程"
        extra={
          <Button type="primary" onClick={onReset}>
            重新开始
          </Button>
        }
      />
    );
  }

  const successCount = results.filter((r) => r.status === "success").length;
  const failedCount = results.filter((r) => r.status === "failed").length;
  const totalCount = results.length;
  const successRate = totalCount > 0 ? (successCount / totalCount) * 100 : 0;
  const isAllSuccess = successCount === totalCount;

  const handleGoToAgents = () => {
    navigate("/agents/instances");
  };

  const resultColumns = [
    {
      title: "状态",
      dataIndex: "status",
      width: 80,
      render: (status: string) =>
        status === "success" ? (
          <Tag color="success" icon={<CheckCircleOutlined />}>
            成功
          </Tag>
        ) : (
          <Tag color="error" icon={<CloseCircleOutlined />}>
            失败
          </Tag>
        ),
    },
    {
      title: "实例 ID",
      dataIndex: "instanceId",
      width: 200,
      ellipsis: true,
      copyable: true,
      render: (id: string) =>
        id ? (
          <Text style={{ fontSize: 12 }}>{id}</Text>
        ) : (
          <Text type="secondary">-</Text>
        ),
    },
    {
      title: "实例名称",
      dataIndex: "name",
      width: 180,
      ellipsis: true,
    },
    {
      title: "主机",
      dataIndex: "hostName",
      width: 120,
      render: (name: string, record: CreatedInstance) => (
        <Space>
          <Text>{name}</Text>
          <Text type="secondary" style={{ fontSize: 11 }}>
            ({record.hostId.slice(0, 8)}...)
          </Text>
        </Space>
      ),
    },
    {
      title: "错误信息",
      dataIndex: "error",
      ellipsis: true,
      render: (error: string) =>
        error ? (
          <Text type="danger" style={{ fontSize: 12 }}>
            {error}
          </Text>
        ) : (
          "-"
        ),
    },
    {
      title: "操作",
      key: "action",
      width: 100,
      render: (_: any, record: CreatedInstance) =>
        record.status === "success" && record.instanceId ? (
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/agents/${record.instanceId}`)}
          >
            查看
          </Button>
        ) : null,
    },
  ];

  return (
    <div>
      <Result
        icon={
          isAllSuccess ? (
            <CheckCircleOutlined style={{ color: "#52c41a" }} />
          ) : (
            <CloseCircleOutlined
              style={{ color: failedCount > 0 ? "#ff4d4f" : "#faad14" }}
            />
          )
        }
        title={isAllSuccess ? "批量创建成功!" : "批量创建完成"}
        subTitle={
          isAllSuccess
            ? `已成功创建 ${successCount} 个 ${templateName} 实例`
            : `成功 ${successCount} 个，失败 ${failedCount} 个`
        }
      >
        <div style={{ marginBottom: 24 }}>
          <Row gutter={16}>
            <Col span={6}>
              <Statistic
                title="总数"
                value={totalCount}
                suffix="个实例"
                valueStyle={{ color: "#1677ff" }}
              />
            </Col>
            <Col span={6}>
              <Statistic
                title="成功"
                value={successCount}
                suffix="个"
                valueStyle={{ color: "#52c41a" }}
              />
            </Col>
            <Col span={6}>
              <Statistic
                title="失败"
                value={failedCount}
                suffix="个"
                valueStyle={{ color: failedCount > 0 ? "#ff4d4f" : "#999" }}
              />
            </Col>
            <Col span={6}>
              <Progress
                type="circle"
                percent={successRate}
                size={60}
                strokeColor={isAllSuccess ? "#52c41a" : "#faad14"}
                format={(percent) => `${percent?.toFixed(0)}%`}
              />
            </Col>
          </Row>
        </div>

        <Card title="创建详情" style={{ marginTop: 16 }}>
          <Table
            rowKey={(record) => record.instanceId || record.hostId}
            dataSource={results}
            columns={resultColumns}
            pagination={results.length > 10 ? { pageSize: 10 } : false}
            size="small"
            scroll={{ x: 700 }}
          />
        </Card>

        <Space style={{ marginTop: 24 }}>
          <Button
            type="primary"
            onClick={handleGoToAgents}
            icon={<RocketOutlined />}
          >
            查看 Agent 实例列表
          </Button>
          <Button onClick={onReset} icon={<ReloadOutlined />}>
            重新创建
          </Button>
        </Space>
      </Result>
    </div>
  );
};

export default CreationResult;
