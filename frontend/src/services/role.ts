import request from '../utils/request';
import type { Role } from '../types';

export const getRoles = (): Promise<Role[]> => request.get('/roles');
export const getRole = (id: string): Promise<Role> => request.get(`/roles/${id}`);
export const createRole = (data: Partial<Role>): Promise<Role> => request.post('/roles', data);
export const updateRole = (id: string, data: Partial<Role>): Promise<Role> => request.put(`/roles/${id}`, data);
export const deleteRole = (id: string): Promise<void> => request.delete(`/roles/${id}`);
