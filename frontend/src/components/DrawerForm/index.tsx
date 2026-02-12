import React from 'react';
import { Drawer, Button, Form, Space } from 'antd';

interface DrawerFormProps {
  visible: boolean;
  title: string;
  width?: number;
  onClose: () => void;
  onSave: (data: any) => Promise<void>;
  initialValues?: Record<string, any>;
  children?: React.ReactNode;
}

const DrawerForm: React.FC<DrawerFormProps> = ({
  visible,
  title,
  width = 600,
  onClose,
  onSave,
  initialValues,
  children,
}) => {
  const [form] = Form.useForm();

  React.useEffect(() => {
    if (visible && initialValues) {
      form.setFieldsValue(initialValues);
    } else if (!visible) {
      form.resetFields();
    }
  }, [visible, initialValues, form]);

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      await onSave(values);
    } catch (error) {
      console.error('表单验证失败:', error);
    }
  };

  return (
    <Drawer
      title={title}
      width={width}
      onClose={onClose}
      open={visible}
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
        onFinish={onSave}
      >
        {children}
      </Form>
    </Drawer>
  );
};

export { DrawerForm };