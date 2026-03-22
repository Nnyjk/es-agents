import React, { useEffect, useState } from "react";
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  Select,
  message,
  Tabs,
  Tooltip,
  Progress,
  Row,
  Col,
  Descriptions,
  List,
  Timeline,
  Upload,
  Divider,
} from "antd";
import {
  PlusOutlined,
  EyeOutlined,
  ReloadOutlined,
  FileTextOutlined,
  DownloadOutlined,
  UploadOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import type { UploadProps } from "antd";
import dayjs from "dayjs";
import {
  getAssessmentDocuments,
  getAssessmentQuestions,
  getRemediationTasks,
  getAssessmentProgress,
  generateAssessmentDocument,
  createRemediationTask,
  updateRemediationTask,
  answerAssessmentQuestion,
} from "../../../services/security";
import type {
  AssessmentDocument,
  AssessmentQuestion,
  RemediationTask,
  AssessmentProgress,
} from "../../../types/security";

const { TextArea } = Input;
const { Dragger } = Upload;

const AuditSupport: React.FC = () => {
  const [documents, setDocuments] = useState<AssessmentDocument[]>([]);
  const [questions, setQuestions] = useState<AssessmentQuestion[]>([]);
  const [tasks, setTasks] = useState<RemediationTask[]>([]);
  const [progress, setProgress] = useState<AssessmentProgress | null>(null);
  const [loading, setLoading] = useState(false);
  const [docModalVisible, setDocModalVisible] = useState(false);
  const [taskModalVisible, setTaskModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [questionModalVisible, setQuestionModalVisible] = useState(false);
  const [_currentDoc, setCurrentDoc] = useState<AssessmentDocument | null>(
    null,
  );
  const [currentTask, setCurrentTask] = useState<RemediationTask | null>(null);
  const [currentQuestion, setCurrentQuestion] =
    useState<AssessmentQuestion | null>(null);
  const [form] = Form.useForm();
  const [taskForm] = Form.useForm();
  const [questionForm] = Form.useForm();
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  useEffect(() => {
    fetchDocuments();
    fetchQuestions();
    fetchTasks();
    fetchProgress();
  }, []);

  const fetchDocuments = async (page = 1, pageSize = 10) => {
    try {
      setLoading(true);
      const response = await getAssessmentDocuments({ page, pageSize });
      setDocuments(response.data);
      setPagination((prev) => ({
        ...prev,
        current: page,
        pageSize,
        total: response.total,
      }));
    } catch {
      setDocuments([
        {
          id: "1",
          name: "信息系统安全等级保护备案表",
          type: "registration",
          category: "基础资料",
          status: "completed",
          version: "1.0",
          generatedAt: "2024-01-10T10:00:00Z",
          generatedBy: "user1",
          generatedByName: "张三",
        },
        {
          id: "2",
          name: "系统安全设计方案",
          type: "design",
          category: "技术文档",
          status: "pending",
          version: "1.0",
          generatedAt: "",
          generatedBy: "",
        },
        {
          id: "3",
          name: "安全管理制度汇编",
          type: "policy",
          category: "管理文档",
          status: "draft",
          version: "1.0",
          generatedAt: "",
          generatedBy: "",
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const fetchQuestions = async () => {
    try {
      const data = await getAssessmentQuestions();
      setQuestions(data.data || []);
    } catch {
      setQuestions([
        {
          id: "1",
          category: "安全物理环境",
          question: "机房是否配备门禁系统？",
          answer: "是",
          evidence: "机房门口已安装刷卡门禁系统",
          status: "answered",
          answeredAt: "2024-01-10T10:00:00Z",
          answeredBy: "user1",
        },
        {
          id: "2",
          category: "安全物理环境",
          question: "机房是否有视频监控？",
          answer: null,
          evidence: null,
          status: "pending",
        },
        {
          id: "3",
          category: "通信网络安全",
          question: "网络边界是否部署防火墙？",
          answer: null,
          evidence: null,
          status: "pending",
        },
      ]);
    }
  };

  const fetchTasks = async () => {
    try {
      const data = await getRemediationTasks();
      setTasks(data.data || []);
    } catch {
      setTasks([
        {
          id: "1",
          title: "修复密码策略配置",
          description: "将最小密码长度从6位调整为8位",
          priority: "high",
          status: "in-progress",
          assignee: "user2",
          assignedToName: "李四",
          dueDate: "2024-01-20",
          createdAt: "2024-01-10T10:00:00Z",
          createdBy: "user1",
          createdByName: "张三",
          progress: 60,
          comments: [
            {
              user: "李四",
              content: "正在修改配置文件",
              time: "2024-01-12T14:00:00Z",
            },
          ],
        },
        {
          id: "2",
          title: "配置登录失败锁定",
          description: "配置连续失败5次锁定账户策略",
          priority: "high",
          status: "pending",
          assignee: "user3",
          assignedToName: "王五",
          dueDate: "2024-01-25",
          createdAt: "2024-01-10T10:00:00Z",
          createdBy: "user1",
          createdByName: "张三",
          progress: 0,
        },
      ]);
    }
  };

  const fetchProgress = async () => {
    try {
      const data = await getAssessmentProgress();
      setProgress(data);
    } catch {
      setProgress({
        totalDocuments: 15,
        completedDocuments: 8,
        totalQuestions: 50,
        answeredQuestions: 30,
        totalTasks: 10,
        completedTasks: 4,
        overallProgress: 58,
        phase: "自评阶段",
        startDate: "2024-01-01",
        expectedEndDate: "2024-03-31",
      });
    }
  };

  const handleGenerateDocument = async (values: {
    type: string;
    name: string;
  }) => {
    try {
      await generateAssessmentDocument({
        type: values.type as AssessmentDocument["type"],
        name: values.name,
      });
      message.success("文档生成中，请稍后刷新查看");
      setDocModalVisible(false);
      form.resetFields();
      fetchDocuments();
    } catch {
      message.error("文档生成失败");
    }
  };

  const handleCreateTask = async (values: {
    title: string;
    description: string;
    priority: string;
    assignee: string;
    dueDate: string;
  }) => {
    try {
      await createRemediationTask({
        title: values.title,
        description: values.description,
        priority: values.priority as "critical" | "high" | "medium" | "low",
        assignedTo: values.assignee,
        dueDate: values.dueDate,
      });
      message.success("整改任务创建成功");
      setTaskModalVisible(false);
      taskForm.resetFields();
      fetchTasks();
    } catch {
      message.error("创建失败");
    }
  };

  const handleUpdateTaskStatus = async (taskId: string, status: string) => {
    try {
      await updateRemediationTask(taskId, {
        status: status as "pending" | "in-progress" | "completed" | "verified",
      });
      message.success("状态更新成功");
      fetchTasks();
    } catch {
      message.error("状态更新失败");
    }
  };

  const handleAnswerQuestion = async (values: {
    answer: string;
    evidence?: string;
  }) => {
    if (!currentQuestion) return;
    try {
      await answerAssessmentQuestion(
        currentQuestion.id,
        values.answer,
        values.evidence ? [values.evidence] : undefined,
      );
      message.success("回答已提交");
      setQuestionModalVisible(false);
      questionForm.resetFields();
      fetchQuestions();
    } catch {
      message.error("提交失败");
    }
  };

  const getDocTypeTag = (type: string) => {
    const map: Record<string, { color: string; text: string }> = {
      registration: { color: "blue", text: "备案资料" },
      design: { color: "green", text: "设计方案" },
      policy: { color: "orange", text: "管理制度" },
      report: { color: "purple", text: "测评报告" },
      evidence: { color: "cyan", text: "佐证材料" },
    };
    const item = map[type] || { color: "default", text: type };
    return <Tag color={item.color}>{item.text}</Tag>;
  };

  const getDocStatusTag = (status: string) => {
    const map: Record<string, { color: string; text: string }> = {
      draft: { color: "default", text: "草稿" },
      pending: { color: "processing", text: "生成中" },
      completed: { color: "success", text: "已完成" },
      failed: { color: "error", text: "生成失败" },
    };
    const item = map[status] || { color: "default", text: status };
    return <Tag color={item.color}>{item.text}</Tag>;
  };

  const getTaskStatusTag = (status: string) => {
    const map: Record<string, { color: string; text: string }> = {
      pending: { color: "default", text: "待处理" },
      "in-progress": { color: "processing", text: "进行中" },
      completed: { color: "success", text: "已完成" },
      verified: { color: "cyan", text: "已验证" },
    };
    const item = map[status] || { color: "default", text: status };
    return <Tag color={item.color}>{item.text}</Tag>;
  };

  const getPriorityTag = (priority: string) => {
    const map: Record<string, { color: string; text: string }> = {
      high: { color: "red", text: "高" },
      medium: { color: "orange", text: "中" },
      low: { color: "blue", text: "低" },
    };
    const item = map[priority] || { color: "default", text: priority };
    return <Tag color={item.color}>{item.text}</Tag>;
  };

  const getQuestionStatusTag = (status: string) => {
    const map: Record<string, { color: string; text: string }> = {
      pending: { color: "default", text: "待回答" },
      answered: { color: "success", text: "已回答" },
      verified: { color: "blue", text: "已核实" },
    };
    const item = map[status] || { color: "default", text: status };
    return <Tag color={item.color}>{item.text}</Tag>;
  };

  const docColumns: ColumnsType<AssessmentDocument> = [
    {
      title: "文档名称",
      dataIndex: "name",
      key: "name",
      width: 250,
    },
    {
      title: "类型",
      dataIndex: "type",
      key: "type",
      width: 100,
      render: (type: string) => getDocTypeTag(type),
    },
    {
      title: "分类",
      dataIndex: "category",
      key: "category",
      width: 100,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: string) => getDocStatusTag(status),
    },
    {
      title: "版本",
      dataIndex: "version",
      key: "version",
      width: 80,
    },
    {
      title: "生成时间",
      dataIndex: "generatedAt",
      key: "generatedAt",
      width: 160,
      render: (time: string) =>
        time ? dayjs(time).format("YYYY-MM-DD HH:mm") : "-",
    },
    {
      title: "操作",
      key: "action",
      width: 150,
      render: (_: unknown, record: AssessmentDocument) => (
        <Space size="small">
          <Tooltip title="查看">
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => {
                setCurrentDoc(record);
                setDetailModalVisible(true);
              }}
            />
          </Tooltip>
          <Tooltip title="下载">
            <Button
              type="link"
              size="small"
              icon={<DownloadOutlined />}
              disabled={record.status !== "completed"}
            />
          </Tooltip>
        </Space>
      ),
    },
  ];

  const taskColumns: ColumnsType<RemediationTask> = [
    {
      title: "任务标题",
      dataIndex: "title",
      key: "title",
      width: 200,
    },
    {
      title: "优先级",
      dataIndex: "priority",
      key: "priority",
      width: 80,
      render: (priority: string) => getPriorityTag(priority),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: string) => getTaskStatusTag(status),
    },
    {
      title: "负责人",
      dataIndex: "assignedToName",
      key: "assignedToName",
      width: 100,
    },
    {
      title: "进度",
      dataIndex: "progress",
      key: "progress",
      width: 150,
      render: (progress: number) => (
        <Progress percent={progress} size="small" />
      ),
    },
    {
      title: "截止日期",
      dataIndex: "dueDate",
      key: "dueDate",
      width: 120,
      render: (date: string) => dayjs(date).format("YYYY-MM-DD"),
    },
    {
      title: "操作",
      key: "action",
      width: 180,
      render: (_: unknown, record: RemediationTask) => (
        <Space size="small">
          <Tooltip title="查看详情">
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => {
                setCurrentTask(record);
                setDetailModalVisible(true);
              }}
            />
          </Tooltip>
          {record.status === "pending" && (
            <Button
              type="link"
              size="small"
              onClick={() => handleUpdateTaskStatus(record.id, "in-progress")}
            >
              开始
            </Button>
          )}
          {record.status === "in-progress" && (
            <Button
              type="link"
              size="small"
              onClick={() => handleUpdateTaskStatus(record.id, "completed")}
            >
              完成
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const questionColumns: ColumnsType<AssessmentQuestion> = [
    {
      title: "分类",
      dataIndex: "category",
      key: "category",
      width: 150,
    },
    {
      title: "问题",
      dataIndex: "question",
      key: "question",
      width: 300,
      ellipsis: true,
    },
    {
      title: "回答",
      dataIndex: "answer",
      key: "answer",
      width: 150,
      ellipsis: true,
      render: (answer: string) => answer || "-",
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: string) => getQuestionStatusTag(status),
    },
    {
      title: "回答时间",
      dataIndex: "answeredAt",
      key: "answeredAt",
      width: 160,
      render: (time: string) =>
        time ? dayjs(time).format("YYYY-MM-DD HH:mm") : "-",
    },
    {
      title: "操作",
      key: "action",
      width: 100,
      render: (_: unknown, record: AssessmentQuestion) => (
        <Button
          type="link"
          size="small"
          onClick={() => {
            setCurrentQuestion(record);
            setQuestionModalVisible(true);
          }}
          disabled={record.status === "confirmed"}
        >
          {record.status === "pending" ? "回答" : "编辑"}
        </Button>
      ),
    },
  ];

  const uploadProps: UploadProps = {
    name: "file",
    multiple: true,
    action: "/api/upload",
    onChange(info) {
      const { status } = info.file;
      if (status === "done") {
        message.success(`${info.file.name} 上传成功`);
      } else if (status === "error") {
        message.error(`${info.file.name} 上传失败`);
      }
    },
  };

  return (
    <div>
      {/* 进度概览 */}
      <Card style={{ marginBottom: 16 }}>
        <Row gutter={24}>
          <Col span={4}>
            <div style={{ textAlign: "center" }}>
              <Progress
                type="circle"
                percent={progress?.overallProgress || 0}
                format={(percent) => (
                  <span style={{ fontSize: 20 }}>{percent}%</span>
                )}
              />
              <div style={{ marginTop: 8 }}>
                <Tag color="blue">{progress?.phase || "准备中"}</Tag>
              </div>
            </div>
          </Col>
          <Col span={20}>
            <Row gutter={16}>
              <Col span={6}>
                <Card size="small">
                  <div style={{ textAlign: "center" }}>
                    <div style={{ fontSize: 24, fontWeight: "bold" }}>
                      {progress?.completedDocuments || 0}/
                      {progress?.totalDocuments || 0}
                    </div>
                    <div style={{ color: "#666" }}>文档资料</div>
                  </div>
                </Card>
              </Col>
              <Col span={6}>
                <Card size="small">
                  <div style={{ textAlign: "center" }}>
                    <div style={{ fontSize: 24, fontWeight: "bold" }}>
                      {progress?.answeredQuestions || 0}/
                      {progress?.totalQuestions || 0}
                    </div>
                    <div style={{ color: "#666" }}>测评问答</div>
                  </div>
                </Card>
              </Col>
              <Col span={6}>
                <Card size="small">
                  <div style={{ textAlign: "center" }}>
                    <div style={{ fontSize: 24, fontWeight: "bold" }}>
                      {progress?.completedTasks || 0}/
                      {progress?.totalTasks || 0}
                    </div>
                    <div style={{ color: "#666" }}>整改任务</div>
                  </div>
                </Card>
              </Col>
              <Col span={6}>
                <Card size="small">
                  <div style={{ textAlign: "center" }}>
                    <div style={{ fontSize: 14, color: "#666" }}>计划周期</div>
                    <div style={{ marginTop: 4 }}>
                      {progress?.startDate} ~ {progress?.expectedEndDate}
                    </div>
                  </div>
                </Card>
              </Col>
            </Row>
          </Col>
        </Row>
      </Card>

      <Tabs
        items={[
          {
            key: "documents",
            label: "测评资料",
            icon: <FileTextOutlined />,
            children: (
              <Card
                title="等保测评资料"
                extra={
                  <Space>
                    <Button
                      icon={<ReloadOutlined />}
                      onClick={() => fetchDocuments()}
                    >
                      刷新
                    </Button>
                    <Button
                      type="primary"
                      icon={<PlusOutlined />}
                      onClick={() => setDocModalVisible(true)}
                    >
                      生成文档
                    </Button>
                  </Space>
                }
              >
                <Table
                  columns={docColumns}
                  dataSource={documents}
                  rowKey="id"
                  loading={loading}
                  pagination={{
                    ...pagination,
                    showSizeChanger: true,
                    showQuickJumper: true,
                    onChange: (page, pageSize) =>
                      fetchDocuments(page, pageSize),
                  }}
                />
              </Card>
            ),
          },
          {
            key: "questions",
            label: "测评问题",
            icon: <ExclamationCircleOutlined />,
            children: (
              <Card
                title="测评问题清单"
                extra={
                  <Space>
                    <Button icon={<ReloadOutlined />}>刷新</Button>
                  </Space>
                }
              >
                <Table
                  columns={questionColumns}
                  dataSource={questions}
                  rowKey="id"
                  loading={loading}
                  pagination={{ pageSize: 10 }}
                />
              </Card>
            ),
          },
          {
            key: "tasks",
            label: "整改任务",
            icon: <CheckCircleOutlined />,
            children: (
              <Card
                title="整改任务"
                extra={
                  <Space>
                    <Button
                      icon={<ReloadOutlined />}
                      onClick={() => fetchTasks()}
                    >
                      刷新
                    </Button>
                    <Button
                      type="primary"
                      icon={<PlusOutlined />}
                      onClick={() => setTaskModalVisible(true)}
                    >
                      新建任务
                    </Button>
                  </Space>
                }
              >
                <Table
                  columns={taskColumns}
                  dataSource={tasks}
                  rowKey="id"
                  loading={loading}
                  pagination={{ pageSize: 10 }}
                />
              </Card>
            ),
          },
          {
            key: "evidence",
            label: "佐证材料",
            icon: <UploadOutlined />,
            children: (
              <Card title="佐证材料上传">
                <Dragger {...uploadProps}>
                  <p className="ant-upload-drag-icon">
                    <UploadOutlined />
                  </p>
                  <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
                  <p className="ant-upload-hint">
                    支持上传测评所需的各类佐证材料，如系统截图、配置文件、日志记录等
                  </p>
                </Dragger>
                <Divider />
                <List
                  header={<div>已上传材料</div>}
                  dataSource={[
                    {
                      name: "系统架构图.png",
                      size: "2.3MB",
                      time: "2024-01-10",
                    },
                    {
                      name: "网络拓扑图.pdf",
                      size: "1.5MB",
                      time: "2024-01-10",
                    },
                    {
                      name: "安全策略配置.docx",
                      size: "500KB",
                      time: "2024-01-12",
                    },
                  ]}
                  renderItem={(item) => (
                    <List.Item
                      actions={[
                        <Button type="link" size="small">
                          下载
                        </Button>,
                        <Button type="link" size="small" danger>
                          删除
                        </Button>,
                      ]}
                    >
                      <List.Item.Meta
                        title={item.name}
                        description={`${item.size} | 上传于 ${item.time}`}
                      />
                    </List.Item>
                  )}
                />
              </Card>
            ),
          },
        ]}
      />

      <Modal
        title="生成测评文档"
        open={docModalVisible}
        onCancel={() => setDocModalVisible(false)}
        onOk={() => form.submit()}
        width={500}
      >
        <Form form={form} layout="vertical" onFinish={handleGenerateDocument}>
          <Form.Item
            name="type"
            label="文档类型"
            rules={[{ required: true, message: "请选择文档类型" }]}
          >
            <Select placeholder="请选择文档类型">
              <Select.Option value="registration">备案资料</Select.Option>
              <Select.Option value="design">设计方案</Select.Option>
              <Select.Option value="policy">管理制度</Select.Option>
              <Select.Option value="report">测评报告</Select.Option>
              <Select.Option value="evidence">佐证材料</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="name"
            label="文档名称"
            rules={[{ required: true, message: "请输入文档名称" }]}
          >
            <Input placeholder="请输入文档名称" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="新建整改任务"
        open={taskModalVisible}
        onCancel={() => setTaskModalVisible(false)}
        onOk={() => taskForm.submit()}
        width={600}
      >
        <Form form={taskForm} layout="vertical" onFinish={handleCreateTask}>
          <Form.Item
            name="title"
            label="任务标题"
            rules={[{ required: true, message: "请输入任务标题" }]}
          >
            <Input placeholder="请输入任务标题" />
          </Form.Item>
          <Form.Item
            name="description"
            label="任务描述"
            rules={[{ required: true, message: "请输入任务描述" }]}
          >
            <TextArea rows={3} placeholder="请输入任务描述" />
          </Form.Item>
          <Form.Item
            name="priority"
            label="优先级"
            rules={[{ required: true, message: "请选择优先级" }]}
          >
            <Select placeholder="请选择优先级">
              <Select.Option value="high">高</Select.Option>
              <Select.Option value="medium">中</Select.Option>
              <Select.Option value="low">低</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="assignee"
            label="负责人"
            rules={[{ required: true, message: "请选择负责人" }]}
          >
            <Select placeholder="请选择负责人">
              <Select.Option value="user1">张三</Select.Option>
              <Select.Option value="user2">李四</Select.Option>
              <Select.Option value="user3">王五</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="dueDate"
            label="截止日期"
            rules={[{ required: true, message: "请选择截止日期" }]}
          >
            <Input type="date" placeholder="请选择截止日期" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="回答测评问题"
        open={questionModalVisible}
        onCancel={() => setQuestionModalVisible(false)}
        onOk={() => questionForm.submit()}
        width={600}
      >
        {currentQuestion && (
          <Descriptions bordered column={1} style={{ marginBottom: 16 }}>
            <Descriptions.Item label="分类">
              {currentQuestion.category}
            </Descriptions.Item>
            <Descriptions.Item label="问题">
              {currentQuestion.question}
            </Descriptions.Item>
          </Descriptions>
        )}
        <Form
          form={questionForm}
          layout="vertical"
          onFinish={handleAnswerQuestion}
          initialValues={{ answer: currentQuestion?.answer }}
        >
          <Form.Item
            name="answer"
            label="回答"
            rules={[{ required: true, message: "请输入回答" }]}
          >
            <Select placeholder="请选择回答">
              <Select.Option value="是">是</Select.Option>
              <Select.Option value="否">否</Select.Option>
              <Select.Option value="部分满足">部分满足</Select.Option>
              <Select.Option value="不适用">不适用</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="evidence" label="佐证说明">
            <TextArea rows={3} placeholder="请输入佐证说明或相关证据" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="任务详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={700}
      >
        {currentTask && (
          <div>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="任务标题" span={2}>
                {currentTask.title}
              </Descriptions.Item>
              <Descriptions.Item label="描述" span={2}>
                {currentTask.description}
              </Descriptions.Item>
              <Descriptions.Item label="优先级">
                {getPriorityTag(currentTask.priority)}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                {getTaskStatusTag(currentTask.status)}
              </Descriptions.Item>
              <Descriptions.Item label="负责人">
                {currentTask.assignedToName}
              </Descriptions.Item>
              <Descriptions.Item label="截止日期">
                {dayjs(currentTask.dueDate).format("YYYY-MM-DD")}
              </Descriptions.Item>
              <Descriptions.Item label="进度" span={2}>
                <Progress percent={currentTask.progress} />
              </Descriptions.Item>
            </Descriptions>
            {currentTask.comments && currentTask.comments.length > 0 && (
              <Card title="处理记录" style={{ marginTop: 16 }} size="small">
                <Timeline
                  items={currentTask.comments.map((comment) => ({
                    children: (
                      <div>
                        <div style={{ fontWeight: "bold" }}>{comment.user}</div>
                        <div>{comment.content}</div>
                        <div style={{ color: "#999", fontSize: 12 }}>
                          {dayjs(comment.time).format("YYYY-MM-DD HH:mm")}
                        </div>
                      </div>
                    ),
                  }))}
                />
              </Card>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default AuditSupport;
