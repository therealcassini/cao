// 简单的节点数据调试脚本
// 这个脚本会在浏览器控制台中执行，用于检查节点数据的加载情况

// 首先检查allNodes数组是否存在且有数据
console.log('===== 节点数据调试 =====');

// 检查节点加载情况
try {
  // 获取GraphManager组件实例
  const graphManager = document.querySelector('.graph-manager').__vueParentComponent;
  
  if (graphManager) {
    console.log('找到GraphManager组件');
    
    // 查找allNodes引用
    let allNodes = null;
    let nodes = null;
    let edges = null;
    
    // 遍历组件的响应式数据
    for (const key in graphManager.proxy) {
      if (key === 'allNodes' && Array.isArray(graphManager.proxy[key])) {
        allNodes = graphManager.proxy[key];
      }
      if (key === 'nodes' && Array.isArray(graphManager.proxy[key])) {
        nodes = graphManager.proxy[key];
      }
      if (key === 'edges' && Array.isArray(graphManager.proxy[key])) {
        edges = graphManager.proxy[key];
      }
    }
    
    console.log('allNodes数组长度:', allNodes ? allNodes.length : '未找到');
    console.log('nodes数组长度:', nodes ? nodes.length : '未找到');
    console.log('edges数组长度:', edges ? edges.length : '未找到');
    
    // 显示前几个节点数据
    if (allNodes && allNodes.length > 0) {
      console.log('前5个节点数据:', allNodes.slice(0, 5));
    }
    
    // 检查edges中引用的节点ID是否在allNodes中存在
    if (edges && edges.length > 0 && allNodes) {
      const missingNodes = [];
      edges.forEach(edge => {
        const sourceNode = allNodes.find(n => n.id === edge.sourceNodeId);
        const targetNode = allNodes.find(n => n.id === edge.targetNodeId);
        
        if (!sourceNode) {
          missingNodes.push(`源节点ID: ${edge.sourceNodeId} (关系ID: ${edge.id})`);
        }
        if (!targetNode) {
          missingNodes.push(`目标节点ID: ${edge.targetNodeId} (关系ID: ${edge.id})`);
        }
      });
      
      if (missingNodes.length > 0) {
        console.warn('未找到的节点引用:', missingNodes);
      } else {
        console.log('所有关系引用的节点ID都能在allNodes中找到');
      }
    }
    
    // 检查节点ID的类型（可能存在类型不匹配问题）
    if (allNodes && allNodes.length > 0) {
      console.log('节点ID类型:', typeof allNodes[0].id);
      if (edges && edges.length > 0) {
        console.log('关系中引用的节点ID类型:', typeof edges[0].sourceNodeId);
      }
    }
  } else {
    console.log('未找到GraphManager组件');
  }
} catch (error) {
  console.error('调试脚本执行错误:', error);
}

console.log('===== 调试结束 =====');