import axios from 'axios'

const BASE_URL = 'http://localhost:8080/api/advertisements'

export const getAdvertisements = async () => {
  try {
    const response = await axios.get(BASE_URL)
    console.log('获取广告响应:', response.data)
    return response.data
  } catch (error) {
    console.error('获取广告列表失败', error)
    if (error.response) {
      console.error('响应数据:', error.response.data)
    }
    return { code: '500', msg: '获取广告列表失败' }
  }
}

export const createAdvertisement = async (advertisementData) => {
  try {
    console.log('发送创建广告请求数据:', advertisementData)
    const response = await axios.post(BASE_URL, advertisementData)
    console.log('创建广告响应:', response.data)
    return response.data
  } catch (error) {
    console.error('创建广告失败', error)
    if (error.response) {
      console.error('响应数据:', error.response.data)
    }
    return { code: '500', msg: error.response?.data?.msg || '创建广告失败' }
  }
}

export const updateAdvertisement = async (advertisementData) => {
  try {
    console.log('发送更新广告请求数据:', advertisementData)
    const response = await axios.put(BASE_URL, advertisementData)
    console.log('更新广告响应:', response.data)
    return response.data
  } catch (error) {
    console.error('更新广告失败', error)
    if (error.response) {
      console.error('响应数据:', error.response.data)
    }
    return { code: '500', msg: error.response?.data?.msg || '更新广告失败' }
  }
}

export const deleteAdvertisement = async (id) => {
  try {
    console.log('删除广告ID:', id)
    const response = await axios.delete(`${BASE_URL}/${id}`)
    console.log('删除广告响应:', response.data)
    return response.data
  } catch (error) {
    console.error('删除广告失败', error)
    if (error.response) {
      console.error('响应数据:', error.response.data)
    }
    return { code: '500', msg: error.response?.data?.msg || '删除广告失败' }
  }
} 