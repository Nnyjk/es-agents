import request from '../utils/request';
import type { Module } from '../types';

export const getModules = (): Promise<Module[]> => request.get('/modules');
export const createModule = (data: Partial<Module>): Promise<Module> => request.post('/modules', data);
export const updateModule = (id: string, data: Partial<Module>): Promise<Module> => request.put(`/modules/${id}`, data);
export const deleteModule = (id: string): Promise<void> => request.delete(`/modules/${id}`);
