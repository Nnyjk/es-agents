import React, { useEffect, useState } from 'react';
import { Drawer, Tree, Button, message, Spin, Space } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { getRole, updateRole } from '../../../services/role';
import { getModules } from '../../../services/module';
import type { Module } from '../../../types';

interface TreeNode {
  key: string;
  title: string;
  children: TreeNode[];
}

const Authorization: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  // const [role, setRole] = useState<Role | null>(null); // Unused for now
  const [treeData, setTreeData] = useState<TreeNode[]>([]);
  const [checkedKeys, setCheckedKeys] = useState<React.Key[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(true);

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const loadData = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const [modules, roleData] = await Promise.all([
        getModules(),
        getRole(id)
      ]);
      
      const tree = convertToTree(modules);
      setTreeData(tree);
      
      // setRole(roleData);
      setCheckedKeys(roleData.moduleIds || []);
      
    } catch (error) {
      console.error(error);
      message.error('加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  const convertToTree = (list: Module[]): TreeNode[] => {
      const map: Record<string, number> = {};
      const roots: TreeNode[] = [];
      const nodeList: TreeNode[] = list.map(item => ({ 
          key: item.id, 
          title: item.name, 
          children: [] 
      }));

      nodeList.forEach((item, i) => {
        map[item.key] = i;
      });
      
      list.forEach((item, i) => {
          const node = nodeList[i];
          if (item.parentId && map[item.parentId] !== undefined) {
             nodeList[map[item.parentId]].children.push(node);
          } else {
             roots.push(node);
          }
      });
      return roots;
  };

  const handleSave = async () => {
      if (!id) return;
      try {
          await updateRole(id, { moduleIds: checkedKeys as string[] });
          message.success('权限保存成功');
          navigate('/roles');
      } catch (error) {
          console.error(error);
          message.error('保存失败');
      }
  };

  return (
    <Drawer
      title="角色授权"
      open={open}
      placement="right"
      width={560}
      onClose={() => {
        setOpen(false);
        navigate('/roles');
      }}
      destroyOnClose
      extra={(
        <Space>
          <Button
            onClick={() => {
              setOpen(false);
              navigate('/roles');
            }}
          >
            取消
          </Button>
          <Button type="primary" onClick={handleSave} loading={loading}>
            保存
          </Button>
        </Space>
      )}
    >
      {loading ? <Spin /> : (
        <Tree
          checkable
          treeData={treeData}
          checkedKeys={checkedKeys}
          onCheck={(keys) => setCheckedKeys(keys as React.Key[])}
        />
      )}
    </Drawer>
  );
};

export default Authorization;
