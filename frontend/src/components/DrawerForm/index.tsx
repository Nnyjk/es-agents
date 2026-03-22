import React from "react";
import { Drawer, Button, Form, Space } from "antd";

interface DrawerFormProps {
  visible?: boolean;
  open?: boolean;
  title: string;
  width?: number;
  onClose: () => void;
  onSave?: (data: any) => Promise<void> | Promise<boolean> | void;
  onFinish?: (data: any) => Promise<boolean> | Promise<void> | void;
  initialValues?: Record<string, any>;
  children?: React.ReactNode;
}

const DrawerForm: React.FC<DrawerFormProps> = ({
  visible,
  open,
  title,
  width = 600,
  onClose,
  onSave,
  onFinish,
  initialValues,
  children,
}) => {
  const [form] = Form.useForm();
  const isVisible = open ?? visible ?? false;
  const handleSubmit = onFinish || onSave;

  React.useEffect(() => {
    if (isVisible && initialValues) {
      form.setFieldsValue(initialValues);
    } else if (!isVisible) {
      form.resetFields();
    }
  }, [isVisible, initialValues, form]);

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      if (handleSubmit) {
        await handleSubmit(values);
      }
    } catch (error) {
      console.error("表单验证失败:", error);
    }
  };

  return (
    <Drawer
      title={title}
      width={width}
      onClose={onClose}
      open={isVisible}
      styles={{
        body: {
          paddingBottom: 80,
        },
      }}
      extra={
        <Space>
          <Button onClick={onClose}>取消</Button>
          <Button onClick={handleSave} type="primary">
            保存
          </Button>
        </Space>
      }
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={initialValues}
        onFinish={handleSubmit}
      >
        {children}
      </Form>
    </Drawer>
  );
};

export { DrawerForm };
