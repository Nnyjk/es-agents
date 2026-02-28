// 暂时剔除登录加密功能
// 此文件保留仅作兼容，实际不再使用

export const encrypt = (text: string, _publicKey: string): string | false => {
  // 加密功能已暂时剔除，直接返回原文
  console.warn('登录加密功能已暂时剔除，使用明文密码');
  return text;
};
