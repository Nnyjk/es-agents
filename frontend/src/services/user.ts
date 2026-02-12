import request from '../utils/request';
import type { User } from '../types';

export const getUsers = (): Promise<User[]> => request.get('/users');
export const createUser = (data: Partial<User>): Promise<User> => request.post('/users', data);
export const updateUser = (id: string, data: Partial<User>): Promise<User> => request.put(`/users/${id}`, data);
export const deleteUser = (id: string): Promise<void> => request.delete(`/users/${id}`);
export const changeUserStatus = (id: string, status: string): Promise<void> => request.put(`/users/${id}/status/${status}`);
