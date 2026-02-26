import React, { useState, useEffect } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { login, getPublicKey } from '../../../services/auth';
import { encrypt } from '../../../utils/encrypt';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [publicKey, setPublicKey] = useState<string>('');
  const initialized = React.useRef(false);

  useEffect(() => {
    if (!initialized.current) {
      initialized.current = true;
      fetchPublicKey();
    }
  }, []);

  const fetchPublicKey = async (): Promise<string> => {
    try {
      const response = await getPublicKey();
      setPublicKey(response.publicKey);
      return response.publicKey;
    } catch (error) {
      console.error('获取公钥失败:', error);
      throw error;
    }
  };

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      let passwordPayload = values.password;
      let currentPublicKey = publicKey;

      if (!currentPublicKey) {
        try {
          currentPublicKey = await fetchPublicKey();
        } catch {
          // 后端会兼容明文密码（服务端尝试解密失败后回退）
          currentPublicKey = '';
        }
      }

      if (currentPublicKey) {
        const encryptedPassword = encrypt(values.password, currentPublicKey);
        if (encryptedPassword) {
          passwordPayload = encryptedPassword;
        } else {
          console.warn('密码加密失败，回退为明文提交');
        }
      }

      const result = await login({
        username: values.username,
        password: passwordPayload,
      });
      
      localStorage.setItem('token', result.token);
      localStorage.setItem('userInfo', JSON.stringify(result.userInfo));
      
      message.success('登录成功');
      navigate('/');
    } catch (error) {
      message.error('登录失败，请检查用户名和密码');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ 
      height: '100vh', 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }}>
      <Card style={{ width: 400, boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <img src="/vite.svg" alt="Logo" style={{ width: 48, height: 48, marginBottom: 16 }} />
          <h2>Easy Station</h2>
          <p style={{ color: '#666' }}>欢迎登录系统</p>
        </div>
        
        <Form
          name="login"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名!' }]}
          >
            <Input 
              prefix={<UserOutlined />} 
              placeholder="用户名" 
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码!' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
            />
          </Form.Item>

          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit" 
              loading={loading}
              style={{ width: '100%' }}
            >
              登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default Login;
