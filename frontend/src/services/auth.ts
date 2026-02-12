import request from '../utils/request';
import type { LoginResult } from '../types';

export const login = (data: any): Promise<LoginResult> => {
  return request.post('/auth/login', data);
};

export const logout = (): void => {
  localStorage.removeItem('token');
  localStorage.removeItem('userInfo');
  localStorage.removeItem('permissions');
};

export interface RouteItem {
  path: string;
  name: string;
  icon?: string;
  routes?: RouteItem[];
}

export const getRoutes = (): Promise<RouteItem[]> => {
  return request.get('/auth/routes');
};

export const getPublicKey = (): Promise<{ publicKey: string }> => {
  return request.get('/auth/public-key');
};
