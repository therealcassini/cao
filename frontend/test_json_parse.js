// 测试 GraphManager.vue 中的 JSON 解析逻辑

// 模拟数据
const testData = [
  // 有效的 JSON 字符串
  '{"name": "测试节点", "value": 100}',
  // 可能带有额外引号的 JSON 字符串
  '"{\"name\": \"测试节点\", \"value\": 100}"',
  "'{\"name\": \"测试节点\", \"value\": 100}'",
  // 空对象
  '{}',
  // 非 JSON 字符串
  '这不是一个 JSON 字符串',
  // null 值
  null,
  // undefined 值
  undefined
];

console.log('开始测试 JSON 解析逻辑...');

// 测试解析逻辑
function testJsonParse(value) {
  try {
    let properties = {};
    
    console.log('\n测试值:', value);
    console.log('值类型:', typeof value);
    
    // 处理 JSON 字符串
    if (typeof value === 'string') {
      try {
        // 尝试解析 JSON 字符串
        properties = JSON.parse(value);
        console.log('首次解析成功:', properties);
      } catch (e1) {
        console.warn('首次解析 JSON 失败:', e1.message);
        try {
          // 尝试清理字符串中的特殊字符
          const cleanedStr = value.replace(/^"|"$/g, '').replace(/^'|'$/g, '');
          console.log('清理后的字符串:', cleanedStr);
          properties = JSON.parse(cleanedStr);
          console.log('清理后解析成功:', properties);
        } catch (e2) {
          console.warn('清理后解析仍失败:', e2.message);
          // 如果两次解析都失败，将整个字符串作为单个属性值
          properties = { '属性': value };
          console.log('使用整个字符串作为属性值:', properties);
        }
      }
    } 
    // 处理对象类型
    else if (typeof value === 'object' && value !== null) {
      properties = value;
      console.log('直接使用对象:', properties);
    } else {
      // 处理其他类型
      properties = { '属性': String(value) };
      console.log('转换为字符串属性值:', properties);
    }
    
    // 确保 properties 是有效的对象
    if (typeof properties === 'object' && properties !== null) {
      // 将 properties 对象转换为数组格式
      const entries = Array.isArray(properties) ? 
        properties.map((item, index) => [`属性${index + 1}`, item]) : 
        Object.entries(properties);
      
      // 过滤和转换属性数组
      const filteredEntries = entries
        .filter(([key, value]) => {
          // 确保键是有效的字符串
          const keyIsValid = typeof key === 'string' && key.trim() !== '';
          // 跳过 color 和 size 属性
          const isNotReservedKey = keyIsValid && key.trim() !== 'color' && key.trim() !== 'size';
          return keyIsValid && isNotReservedKey;
        })
        .map(([key, value]) => {
          // 确保值是字符串类型
          const stringValue = value !== undefined && value !== null ? String(value) : '';
          return {
            key: key.trim(),
            value: stringValue
          };
        });
      
      console.log('处理后的属性数组:', filteredEntries);
      return filteredEntries;
    }
  } catch (e) {
    console.error('解析过程中发生严重错误:', e);
    return [{ key: '属性1', value: '' }];
  }
}

// 运行所有测试
for (let i = 0; i < testData.length; i++) {
  console.log('\n--- 测试', i + 1, '---');
  testJsonParse(testData[i]);
}

console.log('\n测试完成!');