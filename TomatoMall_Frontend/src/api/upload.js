import request from './request'
import axios from "axios";

/**
 * 上传图片
 * @param file 图片文件
 * @returns 返回图片的URL
 */
export const uploadImage = (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post(`api/upload/image`, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
        .then(res => {
            return res
        })
}