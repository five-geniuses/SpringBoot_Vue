// src/api/api.js
const API_URL = '/api/user';

export const login = async (username, password) => {
  const formData = new URLSearchParams();
  formData.append('username', username);
  formData.append('password', password);

  const response = await fetch(`${API_URL}/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: formData.toString()
  });

  if (!response.ok) {
    throw new Error('登录失败');
  }

  return { username }; // 模拟返回用户信息
};

export const register = async (user) => {
  const formData = new URLSearchParams();
  formData.append('username', user.username);
  formData.append('password', user.password);
  formData.append('email', user.email || '');
  formData.append('phone', user.phone || '');

  const response = await fetch(`${API_URL}/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: formData.toString()
  });

  if (!response.ok) {
    throw new Error('注册失败');
  }

  return response.ok;
};