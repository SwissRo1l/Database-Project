<<<<<<< HEAD
import axios from 'axios'

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

instance.interceptors.response.use(
  res => res.data,
  err => Promise.reject(err)
)

export default instance
=======
import axios from 'axios'

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

instance.interceptors.response.use(
  res => res.data,
  err => Promise.reject(err)
)

export default instance
>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
