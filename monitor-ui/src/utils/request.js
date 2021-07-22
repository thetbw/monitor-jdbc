import Axios from "axios"
import { Notification} from 'element-ui'

Axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'
// 创建axios实例
const service = Axios.create({
    // axios中请求配置有baseURL选项，表示请求URL公共部分
    baseURL: "http://127.0.0.1:10086",
    // 超时
    timeout: 1000
})

service.interceptors.response.use(res=>{
    if (res.data.success){
        return res.data.body
    }else {
        Notification.error({
            title: res.data.msg
        })
        return Promise.reject('error')
    }
},(error)=>{
    console.error("连接失败",error)
    Notification.error({
        title: "连接失败"
    })
    return Promise.reject('error')
})

export default service