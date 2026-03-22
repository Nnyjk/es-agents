import React, { useState, useEffect, useRef, useCallback } from "react";
import {
  Card,
  Space,
  Button,
  Select,
  message,
  Modal,
  Form,
  Input,
  Tag,
  Empty,
  Spin,
} from "antd";
import {
  ZoomInOutlined,
  ZoomOutOutlined,
  ReloadOutlined,
  FullscreenOutlined,
  PlusOutlined,
  DeleteOutlined,
  LinkOutlined,
} from "@ant-design/icons";
import {
  getResourceTopology,
  getResourceRelations,
  createResourceRelation,
  deleteResourceRelation,
  queryResources,
} from "@/services/cmdb";
import type {
  ResourceTopology,
  TopologyNode,
  TopologyEdge,
  Resource,
  RelationType,
} from "@/types/cmdb";

const relationTypeOptions = [
  { value: "depends_on", label: "依赖", color: "#1890ff" },
  { value: "contains", label: "包含", color: "#52c41a" },
  { value: "connects_to", label: "连接", color: "#722ed1" },
  { value: "runs_on", label: "运行于", color: "#fa8c16" },
  { value: "manages", label: "管理", color: "#13c2c2" },
  { value: "backup_for", label: "备份", color: "#eb2f96" },
];

const statusColors: Record<string, string> = {
  planning: "#d9d9d9",
  purchasing: "#1890ff",
  online: "#52c41a",
  maintaining: "#faad14",
  offline: "#ff4d4f",
  scrapped: "#8c8c8c",
};

const typeColors: Record<string, string> = {
  server: "#1890ff",
  network_device: "#13c2c2",
  database: "#fa8c16",
  middleware: "#722ed1",
  application: "#52c41a",
  storage: "#eb2f96",
  other: "#8c8c8c",
};

const ResourceTopologyPage: React.FC = () => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const [loading, setLoading] = useState(false);
  const [topology, setTopology] = useState<ResourceTopology | null>(null);
  const [nodes, setNodes] = useState<TopologyNode[]>([]);
  const [edges, setEdges] = useState<TopologyEdge[]>([]);
  const [resources, setResources] = useState<Resource[]>([]);
  const [scale, setScale] = useState(1);
  const [offset, setOffset] = useState({ x: 0, y: 0 });
  const [dragging, setDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const [selectedNode, setSelectedNode] = useState<TopologyNode | null>(null);
  const [relationModalVisible, setRelationModalVisible] = useState(false);
  const [selectedResourceId, setSelectedResourceId] = useState<string>("");
  const [relationForm] = Form.useForm();

  useEffect(() => {
    loadTopology();
    loadResources();
  }, []);

  useEffect(() => {
    if (topology && canvasRef.current) {
      drawTopology();
    }
  }, [topology, scale, offset, selectedNode]);

  const loadTopology = async (resourceId?: string) => {
    setLoading(true);
    try {
      const result = await getResourceTopology(resourceId);
      setTopology(result);
      setNodes(result.nodes);
      setEdges(result.edges);
    } catch {
      message.error("加载拓扑图失败");
    } finally {
      setLoading(false);
    }
  };

  const loadResources = async () => {
    try {
      const result = await queryResources({ current: 1, pageSize: 1000 });
      setResources(result.list);
    } catch {
      // Ignore
    }
  };

  const drawTopology = useCallback(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const container = containerRef.current;
    if (container) {
      canvas.width = container.clientWidth;
      canvas.height = container.clientHeight;
    }

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.save();
    ctx.translate(offset.x, offset.y);
    ctx.scale(scale, scale);

    // Draw edges
    edges.forEach((edge) => {
      const sourceNode = nodes.find((n) => n.id === edge.source);
      const targetNode = nodes.find((n) => n.id === edge.target);
      if (!sourceNode || !targetNode) return;

      const relation = relationTypeOptions.find(
        (r) => r.value === edge.relationType,
      );

      ctx.beginPath();
      ctx.moveTo(sourceNode.x, sourceNode.y);
      ctx.lineTo(targetNode.x, targetNode.y);
      ctx.strokeStyle = relation?.color || "#999";
      ctx.lineWidth = 2;
      ctx.stroke();

      // Draw arrow
      const angle = Math.atan2(
        targetNode.y - sourceNode.y,
        targetNode.x - sourceNode.x,
      );
      const arrowLength = 12;
      const arrowX = targetNode.x - 40 * Math.cos(angle);
      const arrowY = targetNode.y - 40 * Math.sin(angle);

      ctx.beginPath();
      ctx.moveTo(arrowX, arrowY);
      ctx.lineTo(
        arrowX - arrowLength * Math.cos(angle - Math.PI / 6),
        arrowY - arrowLength * Math.sin(angle - Math.PI / 6),
      );
      ctx.moveTo(arrowX, arrowY);
      ctx.lineTo(
        arrowX - arrowLength * Math.cos(angle + Math.PI / 6),
        arrowY - arrowLength * Math.sin(angle + Math.PI / 6),
      );
      ctx.stroke();

      // Draw relation label
      const midX = (sourceNode.x + targetNode.x) / 2;
      const midY = (sourceNode.y + targetNode.y) / 2;
      ctx.fillStyle = "#fff";
      ctx.fillRect(midX - 20, midY - 10, 40, 20);
      ctx.strokeStyle = relation?.color || "#999";
      ctx.strokeRect(midX - 20, midY - 10, 40, 20);
      ctx.fillStyle = "#333";
      ctx.font = "12px Arial";
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillText(relation?.label || "", midX, midY);
    });

    // Draw nodes
    nodes.forEach((node) => {
      const isSelected = selectedNode?.id === node.id;
      const typeColor = typeColors[node.type] || "#8c8c8c";
      const statusColor = statusColors[node.status] || "#d9d9d9";

      // Node circle
      ctx.beginPath();
      ctx.arc(node.x, node.y, 30, 0, 2 * Math.PI);
      ctx.fillStyle = isSelected ? "#e6f7ff" : "#fff";
      ctx.fill();
      ctx.strokeStyle = typeColor;
      ctx.lineWidth = isSelected ? 4 : 2;
      ctx.stroke();

      // Status indicator
      ctx.beginPath();
      ctx.arc(node.x + 20, node.y - 20, 8, 0, 2 * Math.PI);
      ctx.fillStyle = statusColor;
      ctx.fill();
      ctx.strokeStyle = "#fff";
      ctx.lineWidth = 2;
      ctx.stroke();

      // Node name
      ctx.fillStyle = "#333";
      ctx.font = "bold 12px Arial";
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      const name =
        node.name.length > 8 ? node.name.slice(0, 8) + "..." : node.name;
      ctx.fillText(name, node.x, node.y + 45);
    });

    ctx.restore();
  }, [nodes, edges, scale, offset, selectedNode]);

  const handleCanvasMouseDown = (e: React.MouseEvent) => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const x = (e.clientX - rect.left - offset.x) / scale;
    const y = (e.clientY - rect.top - offset.y) / scale;

    // Check if clicked on a node
    const clickedNode = nodes.find(
      (node) => Math.sqrt((node.x - x) ** 2 + (node.y - y) ** 2) < 30,
    );

    if (clickedNode) {
      setSelectedNode(clickedNode);
    } else {
      setDragging(true);
      setDragStart({ x: e.clientX - offset.x, y: e.clientY - offset.y });
      setSelectedNode(null);
    }
  };

  const handleCanvasMouseMove = (e: React.MouseEvent) => {
    if (!dragging) return;
    setOffset({
      x: e.clientX - dragStart.x,
      y: e.clientY - dragStart.y,
    });
  };

  const handleCanvasMouseUp = () => {
    setDragging(false);
  };

  const handleZoomIn = () => {
    setScale((s) => Math.min(s * 1.2, 3));
  };

  const handleZoomOut = () => {
    setScale((s) => Math.max(s / 1.2, 0.3));
  };

  const handleReset = () => {
    setScale(1);
    setOffset({ x: 0, y: 0 });
    setSelectedNode(null);
  };

  const handleFullscreen = () => {
    const container = containerRef.current;
    if (container) {
      if (document.fullscreenElement) {
        document.exitFullscreen();
      } else {
        container.requestFullscreen();
      }
    }
  };

  const handleFilterByResource = (resourceId: string) => {
    setSelectedResourceId(resourceId);
    loadTopology(resourceId || undefined);
  };

  const handleAddRelation = async (values: {
    targetId: string;
    relationType: RelationType;
    description?: string;
  }) => {
    if (!selectedNode) return;

    try {
      await createResourceRelation({
        sourceId: selectedNode.id,
        targetId: values.targetId,
        relationType: values.relationType,
        description: values.description,
      });
      message.success("添加关联成功");
      setRelationModalVisible(false);
      relationForm.resetFields();
      loadTopology(selectedResourceId || undefined);
    } catch {
      message.error("添加关联失败");
    }
  };

  const handleDeleteRelation = async (relationId: string) => {
    try {
      await deleteResourceRelation(relationId);
      message.success("删除关联成功");
      loadTopology(selectedResourceId || undefined);
    } catch {
      message.error("删除关联失败");
    }
  };

  return (
    <Card
      title="资源拓扑图"
      extra={
        <Space>
          <Select
            allowClear
            placeholder="筛选资源"
            style={{ width: 200 }}
            value={selectedResourceId || undefined}
            onChange={handleFilterByResource}
            options={resources.map((r) => ({
              label: r.name,
              value: r.id,
            }))}
          />
          <Button icon={<ZoomInOutlined />} onClick={handleZoomIn}>
            放大
          </Button>
          <Button icon={<ZoomOutOutlined />} onClick={handleZoomOut}>
            缩小
          </Button>
          <Button icon={<ReloadOutlined />} onClick={handleReset}>
            重置
          </Button>
          <Button icon={<FullscreenOutlined />} onClick={handleFullscreen}>
            全屏
          </Button>
        </Space>
      }
    >
      <Spin spinning={loading}>
        <div
          ref={containerRef}
          style={{
            width: "100%",
            height: 600,
            border: "1px solid #d9d9d9",
            borderRadius: 4,
            position: "relative",
            overflow: "hidden",
          }}
        >
          {!topology || topology.nodes.length === 0 ? (
            <Empty description="暂无拓扑数据" style={{ marginTop: 200 }} />
          ) : (
            <>
              <canvas
                ref={canvasRef}
                style={{ cursor: dragging ? "grabbing" : "grab" }}
                onMouseDown={handleCanvasMouseDown}
                onMouseMove={handleCanvasMouseMove}
                onMouseUp={handleCanvasMouseUp}
                onMouseLeave={handleCanvasMouseUp}
              />
              {selectedNode && (
                <div
                  style={{
                    position: "absolute",
                    right: 16,
                    top: 16,
                    background: "#fff",
                    padding: 16,
                    borderRadius: 8,
                    boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
                    width: 300,
                  }}
                >
                  <h4 style={{ marginBottom: 8 }}>{selectedNode.name}</h4>
                  <Space direction="vertical" style={{ width: "100%" }}>
                    <div>
                      <Tag color={typeColors[selectedNode.type]}>
                        {selectedNode.type}
                      </Tag>
                      <Tag color={statusColors[selectedNode.status]}>
                        {selectedNode.status}
                      </Tag>
                    </div>
                    <Button
                      type="primary"
                      icon={<PlusOutlined />}
                      onClick={() => setRelationModalVisible(true)}
                      block
                    >
                      添加关联
                    </Button>
                    <Button
                      icon={<LinkOutlined />}
                      onClick={async () => {
                        try {
                          const relations = await getResourceRelations(
                            selectedNode.id,
                          );
                          if (relations.length === 0) {
                            message.info("暂无关联关系");
                            return;
                          }
                          Modal.info({
                            title: "关联关系",
                            content: (
                              <div>
                                {relations.map((r) => (
                                  <div
                                    key={r.id}
                                    style={{
                                      marginBottom: 8,
                                      padding: 8,
                                      background: "#f5f5f5",
                                      borderRadius: 4,
                                    }}
                                  >
                                    <div>
                                      {r.targetName} -{" "}
                                      {
                                        relationTypeOptions.find(
                                          (o) => o.value === r.relationType,
                                        )?.label
                                      }
                                    </div>
                                    <Button
                                      type="link"
                                      size="small"
                                      danger
                                      icon={<DeleteOutlined />}
                                      onClick={() => {
                                        Modal.destroyAll();
                                        handleDeleteRelation(r.id);
                                      }}
                                    >
                                      删除
                                    </Button>
                                  </div>
                                ))}
                              </div>
                            ),
                          });
                        } catch {
                          message.error("获取关联关系失败");
                        }
                      }}
                      block
                    >
                      查看关联
                    </Button>
                  </Space>
                </div>
              )}
            </>
          )}
        </div>
      </Spin>

      {/* 图例 */}
      <div style={{ marginTop: 16, display: "flex", gap: 16 }}>
        <div>
          <span style={{ marginRight: 8 }}>资源类型：</span>
          {Object.entries(typeColors).map(([type, color]) => (
            <Tag key={type} color={color}>
              {type}
            </Tag>
          ))}
        </div>
        <div>
          <span style={{ marginRight: 8 }}>状态：</span>
          {Object.entries(statusColors).map(([status, color]) => (
            <Tag key={status} color={color}>
              {status}
            </Tag>
          ))}
        </div>
      </div>

      {/* 添加关联弹窗 */}
      <Modal
        title="添加关联"
        open={relationModalVisible}
        onCancel={() => setRelationModalVisible(false)}
        onOk={() => relationForm.submit()}
      >
        <Form
          form={relationForm}
          onFinish={handleAddRelation}
          layout="vertical"
        >
          <Form.Item label="源资源">
            <Input disabled value={selectedNode?.name} />
          </Form.Item>
          <Form.Item
            name="targetId"
            label="目标资源"
            rules={[{ required: true, message: "请选择目标资源" }]}
          >
            <Select
              placeholder="请选择目标资源"
              showSearch
              optionFilterProp="label"
              options={resources
                .filter((r) => r.id !== selectedNode?.id)
                .map((r) => ({
                  label: r.name,
                  value: r.id,
                }))}
            />
          </Form.Item>
          <Form.Item
            name="relationType"
            label="关系类型"
            rules={[{ required: true, message: "请选择关系类型" }]}
          >
            <Select
              placeholder="请选择关系类型"
              options={relationTypeOptions}
            />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} placeholder="请输入描述" />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default ResourceTopologyPage;
