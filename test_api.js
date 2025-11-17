// 测试后端API返回的评分数据
const testProductAPI = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/products');
    const data = await response.json();
    
    console.log('API响应:', data);
    
    if (data.code === '200' && data.data) {
      console.log('商品列表:');
      data.data.forEach(product => {
        console.log(`商品: ${product.title}, 评分: ${product.rate}`);
      });
    }
  } catch (error) {
    console.error('API测试失败:', error);
  }
};

// 在浏览器控制台中运行: testProductAPI()
console.log('请在浏览器控制台中运行: testProductAPI()'); 