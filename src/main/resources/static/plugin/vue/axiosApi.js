//拦截器
axios.interceptors.request.use((config) => {
  if (config.method === 'post' && axios.defaults.headers.post['Content-Type'] === 'multipart/form-data;') {
    return config
  }
  if (config.method === 'post' && axios.defaults.headers.post['Content-Type'] === 'application/x-www-form-urlencoded') {
    config.data = JSON.stringify(config.data)
  } else {
    config.data = JSON.stringify(config.data)
  }
  return config

}, (error) => {
  return Promise.reject(error)
})


axios.interceptors.request.use(
  function(config) {
    const { data, headers, cyptedData } = config;
    // authHeaders(headers);
    let newData;
    if (!headers['Content-Type']) {
      // newData = Object.assign({}, data);
      headers['Content-Type'] = 'application/json;charset=UTF-8';
    }
    return { ...config, data: data };
  },
  function(error) {
    return Promise.reject(error);
  }
);