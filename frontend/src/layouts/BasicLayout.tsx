import React, { useState, useEffect } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { ProLayout, PageContainer } from '@ant-design/pro-components';
import { Dropdown } from 'antd';
import { 
  LogoutOutlined, 
  UserOutlined, 
  TeamOutlined, 
  AppstoreOutlined, 
  SettingOutlined, 
  CloudServerOutlined 
} from '@ant-design/icons';
import { logout, getRoutes, RouteItem } from '../services/auth';

// Icon mapper
const IconMap: Record<string, React.ReactNode> = {
  UserOutlined: <UserOutlined />,
  TeamOutlined: <TeamOutlined />,
  AppstoreOutlined: <AppstoreOutlined />,
  SettingOutlined: <SettingOutlined />,
  CloudServerOutlined: <CloudServerOutlined />,
};

const loopMenuItem = (menus: RouteItem[]): any[] =>
  menus.map(({ icon, routes, ...item }) => ({
    ...item,
    icon: icon && IconMap[icon],
    children: routes && loopMenuItem(routes),
  }));

const BasicLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [pathname, setPathname] = useState(location.pathname);
  const [userInfo, setUserInfo] = useState<any>(null);

  useEffect(() => {
    const userStr = localStorage.getItem('userInfo');
    if (!userStr) {
      navigate('/login');
    } else {
      setUserInfo(JSON.parse(userStr));
    }
  }, [navigate]);

  return (
    <ProLayout
      title="Easy Station"
      logo="/vite.svg"
      location={{
        pathname,
      }}
      menu={{
        request: async () => {
          try {
            const routes = await getRoutes();
            return loopMenuItem(routes);
          } catch (e) {
            console.error('Fetch menu failed', e);
            return [];
          }
        },
      }}
      layout="mix"
      splitMenus={false}
      avatarProps={{
        src: 'https://gw.alipayobjects.com/zos/antfincdn/efFD%24IOql2/weixintupian_20170331104822.jpg',
        title: userInfo?.username || 'User',
        render: (_props, dom) => {
          return (
            <Dropdown
              menu={{
                items: [
                  {
                    key: 'logout',
                    icon: <LogoutOutlined />,
                    label: '退出登录',
                    onClick: () => {
                      logout();
                      navigate('/login');
                    },
                  },
                ],
              }}
            >
              {dom}
            </Dropdown>
          );
        },
      }}
      menuItemRender={(item, dom) => (
        <a
          onClick={() => {
            setPathname(item.path || '/');
            navigate(item.path || '/');
          }}
        >
          {dom}
        </a>
      )}
    >
      <PageContainer>
        <Outlet />
      </PageContainer>
    </ProLayout>
  );
};

export default BasicLayout;