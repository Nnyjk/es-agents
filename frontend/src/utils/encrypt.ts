import JSEncrypt from 'jsencrypt';

export const encrypt = (text: string, publicKey: string): string | false => {
  const encryptor = new JSEncrypt();
  encryptor.setPublicKey(publicKey);
  return encryptor.encrypt(text);
};
