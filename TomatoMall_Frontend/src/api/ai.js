import request from '../utils/request';

// AI查询接口
export function aiQuery(query) {
  return request({
    url: '/api/ai/query',
    method: 'post',
    data: { query }
  });
}

// 语音识别接口
export function speechToText(audioData) {
  return request({
    url: '/api/ai/speech',
    method: 'post',
    data: { audio: audioData },
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
} 