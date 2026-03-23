import request from "../utils/request";
import type { Permission, PermissionQueryParams } from "../types";

export const getPermissions = (
  params?: PermissionQueryParams,
): Promise<Permission[]> => request.get("/permissions", { params });

export const getPermission = (id: string): Promise<Permission> =>
  request.get(`/permissions/${id}`);

export const createPermission = (
  data: Partial<Permission>,
): Promise<Permission> => request.post("/permissions", data);

export const updatePermission = (
  id: string,
  data: Partial<Permission>,
): Promise<Permission> => request.put(`/permissions/${id}`, data);

export const deletePermission = (id: string): Promise<void> =>
  request.delete(`/permissions/${id}`);