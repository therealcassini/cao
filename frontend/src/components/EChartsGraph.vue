<template>
  <div class="echarts-graph-container">
    <!-- 图表容器 -->
    <div ref="chart" class="chart" :style="chartStyle"></div>
    
    <!-- 控制面板 -->
    <div class="controls">
      <select v-model="selectedTheme" @change="loadGraphByTheme" class="theme-select">
        <option value="0">所有主题</option>
        <option v-for="theme in themes" :key="theme.id" :value="theme.id">{{ theme.name }}</option>
      </select>
      <button @click="refreshGraph" class="refresh-btn">刷新</button>
      <div class="graph-stats">
        节点: {{ currentNodeCount }} | 边: {{ currentEdgeCount }}
      </div>
    </div>
    
    <!-- 节点属性详情弹窗 -->
    <a-modal
      v-model:open="isModalVisible"
      :title="null"
      :footer="null"
      @cancel="handleCancel"
      width="500px"
      :closable="false"
      :maskClosable="true"
    >
      <div class="node-property-content">
        <div class="node-name">{{ selectedNode?.name || '节点详情' }}</div>
        <div class="properties-table">
          <template v-for="item in getNodePropertiesData()" :key="item.rowKey">
            <div class="property-item">
              <div class="property-key">{{ item.key }}:</div>
              <div class="property-value">{{ item.value }}</div>
            </div>
          </template>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { graphAPI, themeAPI } from '../api/graphService';
import { ref, onMounted, onUnmounted, watch } from 'vue';

export default {
  name: 'EChartsGraph',
  props: {
    height: {
      type: Number,
      default: null
    }
  },
  setup(props) {
    // 基础引用和状态
    const chart = ref(null);
    const chartInstance = ref(null);
    const themes = ref([]);
    const selectedTheme = ref('0');
    // 设置图表容器样式
    const chartStyle = ref({ width: '100%', height: props.height !== null && props.height !== undefined ? props.height + 'px' : '100%' });
    
    // 节点详情弹窗状态
    const isModalVisible = ref(false);
    const selectedNode = ref(null);
    
    // 节点选择状态管理
    const selectedNodes = ref(new Set()); // 用于存储被选中的节点ID集合
    const nodeSelectionEnabled = ref(true); // 是否启用节点选择功能
    
    // 统计信息
    const currentNodeCount = ref(0);
    const currentEdgeCount = ref(0);
    
    // 缓存当前图谱数据，用于调试
    const currentGraphData = ref({ nodes: [], edges: [] });

    // 窗口大小改变时重新调整图表高度和宽度
    const handleResize = () => {
      if (chartInstance.value) {
        // 更新高度 - 只有当props.height明确为null或undefined时才使用100%
        chartStyle.value.height = props.height !== null && props.height !== undefined ? props.height + 'px' : '100%';
        // 调整图表大小
        chartInstance.value.resize();
      }
    };

    // 处理节点点击事件
    const handleNodeClick = (params) => {
      if (params.dataType === 'node') {
        console.log('节点点击:', params.data);
        
        // 如果启用了节点选择功能
        if (nodeSelectionEnabled.value) {
          toggleNodeSelection(params.data);
        }
        
        // 显示节点详情
        selectedNode.value = params.data;
        isModalVisible.value = true;
      }
    };
    
    // 切换节点选择状态
    const toggleNodeSelection = (node) => {
      const nodeId = node.id;
      
      if (selectedNodes.value.has(nodeId)) {
        // 取消选择节点
        selectedNodes.value.delete(nodeId);
        console.log(`取消选择节点: ${node.name} (ID: ${nodeId})`);
      } else {
        // 选择节点
        selectedNodes.value.add(nodeId);
        console.log(`选择节点: ${node.name} (ID: ${nodeId})`);
      }
      
      // 更新图表显示，过滤边
      updateGraphDisplay();
    };
    
    // 根据节点选择状态更新图表显示
    const updateGraphDisplay = () => {
      if (!chartInstance.value || !currentGraphData.value.nodes || !currentGraphData.value.edges) {
        return;
      }
      
      console.log(`更新图表显示，当前选中节点数: ${selectedNodes.value.size}`);
      
      // 如果有选中的节点，过滤边只显示与选中节点相关的边
      let filteredEdges = [];
      
      if (selectedNodes.value.size > 0) {
        // 创建节点ID到名称的映射，用于边过滤
        const nodeIdToNameMap = {};
        currentGraphData.value.nodes.forEach(node => {
          nodeIdToNameMap[node.id] = node.name;
        });
        
        // 创建节点名称到ID的映射，用于边过滤
        const nodeNameToIdMap = {};
        currentGraphData.value.nodes.forEach(node => {
          nodeNameToIdMap[node.name] = node.id;
        });
        
        // 过滤边，只保留与选中节点相关的边
        filteredEdges = currentGraphData.value.edges.filter(edge => {
          const sourceId = nodeNameToIdMap[edge.sourceNodeName];
          const targetId = nodeNameToIdMap[edge.targetNodeName];
          
          // 检查边的源节点或目标节点是否在选中的节点集合中
          return selectedNodes.value.has(sourceId) || selectedNodes.value.has(targetId);
        });
        
        console.log(`过滤后显示的边数: ${filteredEdges.length}`);
      } else {
        // 如果没有选中的节点，显示所有边
        filteredEdges = currentGraphData.value.edges;
      }
      
      // 更新图表配置
      const option = getChartOption(currentGraphData.value.nodes, filteredEdges);
      
      // 为选中的节点添加特殊样式
      if (selectedNodes.value.size > 0) {
        option.series[0].data = option.series[0].data.map(node => {
          if (selectedNodes.value.has(node.id)) {
            return {
              ...node,
              itemStyle: {
                ...node.itemStyle,
                shadowBlur: 15,
                shadowColor: 'rgba(255, 102, 0, 0.8)',
                borderColor: '#ff6600',
                borderWidth: 2
              }
            };
          }
          return node;
        });
      }
      
      // 应用配置更新图表
      chartInstance.value.setOption(option, true);
    };
    
    // 清空所有节点选择
    const clearNodeSelection = () => {
      selectedNodes.value.clear();
      console.log('清空所有节点选择');
      updateGraphDisplay();
    };

    // 初始化图表
    const initChart = () => {
      if (chart.value) {
        chartInstance.value = echarts.init(chart.value);
        chartInstance.value.setOption(getChartOption());
        
        // 添加节点点击事件监听
        chartInstance.value.on('click', handleNodeClick);
        
        // 添加全局错误处理
        chartInstance.value.on('error', (error) => {
          console.error('ECharts 错误:', error);
        });
        
        // 窗口大小改变时重新调整图表大小
        window.addEventListener('resize', handleResize);
      }
    };

    // 创建节点名称到节点对象的映射
    const createNodeNameToObjectMap = (nodes) => {
      const nodeMap = new Map();
      nodes.forEach(node => {
        if (node.name) {
          // 确保节点名称是字符串类型
          const nodeName = typeof node.name === 'string' ? node.name : String(node.name);
          // 确保节点ID是字符串类型
          const nodeId = typeof node.id === 'string' ? node.id : String(node.id);
          // 更新节点对象的ID为字符串
          node.id = nodeId;
          nodeMap.set(nodeName, node);
        } else {
          console.warn('发现没有名称的节点:', node);
        }
      });
      console.log(`创建节点映射: 共 ${nodeMap.size} 个节点`);
      return nodeMap;
    };

    // 专门处理与"韩立"相关的边，进行特殊调试
    const debugHanliEdges = (edges, nodeMap) => {
      const hanliEdges = edges.filter(edge => 
        edge.sourceNodeName === '韩立' || edge.targetNodeName === '韩立'
      );
      
      if (hanliEdges.length > 0) {
        console.log('\n===== 调试"韩立"相关的边 =====');
        console.log(`找到 ${hanliEdges.length} 条与"韩立"相关的边`);
        hanliEdges.forEach((edge, index) => {
          const sourceExists = nodeMap.has(edge.sourceNodeName);
          const targetExists = nodeMap.has(edge.targetNodeName);
          console.log(`边${index + 1}: ${edge.sourceNodeName} -[${edge.relation}]-> ${edge.targetNodeName}`);
          console.log(`  源节点存在: ${sourceExists}, 目标节点存在: ${targetExists}`);
          console.log(`  源节点类型: ${typeof edge.sourceNodeName}, 目标节点类型: ${typeof edge.targetNodeName}`);
          const sourceNode = nodeMap.get(edge.sourceNodeName);
          const targetNode = nodeMap.get(edge.targetNodeName);
          console.log(`  源节点: 名称='${edge.sourceNodeName}', ID=${sourceNode?.id || '未找到'}`);
          console.log(`  目标节点: 名称='${edge.targetNodeName}', ID=${targetNode?.id || '未找到'}`);
        });
        console.log('==============================\n');
      }
    };

    // 验证边数据的有效性
    const validateEdgeData = (edges) => {
      console.log('\n===== 边数据有效性验证 =====');
      let validCount = 0;
      let invalidCount = 0;
      const invalidReasons = new Map();
      
      edges.forEach((edge, index) => {
        const reasons = [];
        
        if (!edge.sourceNodeName) reasons.push('缺少源节点名称');
        if (!edge.targetNodeName) reasons.push('缺少目标节点名称');
        if (!edge.relation) reasons.push('缺少关系类型');
        
        if (reasons.length > 0) {
          invalidCount++;
          reasons.forEach(reason => {
            const count = invalidReasons.get(reason) || 0;
            invalidReasons.set(reason, count + 1);
          });
        } else {
          validCount++;
        }
      });
      
      console.log(`总边数: ${edges.length}, 有效: ${validCount}, 无效: ${invalidCount}`);
      if (invalidCount > 0) {
        console.log('无效原因统计:');
        invalidReasons.forEach((count, reason) => {
          console.log(`  ${reason}: ${count}条`);
        });
      }
      console.log('============================\n');
    };
    
    // 获取图表配置选项
    const getChartOption = (nodes = [], edges = []) => {
      console.log('开始处理图谱数据:', { nodes: nodes.length, edges: edges.length });
      
      // 添加详细的节点主题分布统计
      const themeStats = {};
      nodes.forEach(node => {
        const themeId = node.theme?.id || node.themeId || '未知';
        themeStats[themeId] = (themeStats[themeId] || 0) + 1;
      });
      console.log('图表渲染前的节点主题分布:', themeStats);
      
      // 检查theme_id=4的节点是否存在于输入数据中
      const theme4Nodes = nodes.filter(node => node.theme?.id === 4 || node.themeId === 4);
      console.log('getChartOption中theme_id=4的节点数量:', theme4Nodes.length);
      if (theme4Nodes.length > 0) {
        console.log('getChartOption中theme_id=4的节点示例:', theme4Nodes.slice(0, 5));
      } else {
        console.warn('getChartOption中未找到theme_id=4的节点');
      }
      
      // 验证边数据有效性
      validateEdgeData(edges);
      
      // 更新统计信息
      currentNodeCount.value = nodes.length;
      currentEdgeCount.value = edges.length;
      
      // 保存当前数据用于调试
      currentGraphData.value = { nodes, edges };
      
      // 打印数据示例，包括主题ID信息
      console.log('节点数据示例:', nodes.slice(0, 10).map(n => ({
        id: n.id, 
        name: n.name,
        themeId: n.theme?.id || n.themeId || '未知'
      })));
      console.log('边数据示例:', edges.slice(0, 5).map(e => ({ 
        id: e.id, 
        sourceNodeName: e.sourceNodeName, 
        targetNodeName: e.targetNodeName 
      })));
      
      // 准备分类数据
      const categories = themes.value.map(theme => ({
        name: theme.name,
        itemStyle: {
          color: theme.defaultNodeColor
        }
      }));
      console.log('分类数据:', categories);
      
      // 创建主题ID到分类索引的映射
      const themeIdToCategoryIndex = {};
      themes.value.forEach((theme, index) => {
        themeIdToCategoryIndex[theme.id] = index;
      });
      console.log('主题ID到分类索引的映射:', themeIdToCategoryIndex);
      
      // 转换节点数据格式以适应ECharts
      const echartsNodes = nodes.map(node => {
        const themeId = node.theme?.id || 0;
        const categoryIndex = themeIdToCategoryIndex[themeId] !== undefined ? themeIdToCategoryIndex[themeId] : 0;
        
        console.log(`节点 ${node.name} (ID:${node.id}) - 主题ID:${themeId}, 分类索引:${categoryIndex}`);
        
        return {
          id: node.id,
          name: node.name,
          value: node.name,
          category: categoryIndex,
          itemStyle: {
            color: node.color || node.theme?.defaultNodeColor || '#4285F4'
          },
          symbolSize: node.size || node.theme?.defaultNodeSize || 20,
          // 如果有预存的坐标，使用它
          x: node.x,
          y: node.y,
          // 扩展属性
          properties: node.properties
        };
      });

      // 创建节点名称到ID的映射，用于边的关联
      const nodeMap = createNodeNameToObjectMap(nodes);
      const nodeNameToIdMap = {};
      
      // 为了确保类型一致性，我们将所有ID转换为字符串
      nodeMap.forEach((node, name) => {
        // 确保ID是字符串类型，防止类型不匹配问题
        const stringId = typeof node.id === 'string' ? node.id : String(node.id);
        nodeNameToIdMap[name] = stringId;
        // 同时也更新节点对象的ID，确保类型一致
        node.id = stringId;
      });
      
      // 打印节点名称映射的详细示例和ID类型信息
      console.log('节点名称到ID映射示例 (包含ID类型):');
      Object.keys(nodeNameToIdMap).slice(0, 5).forEach(name => {
        const id = nodeNameToIdMap[name];
        console.log(`  '${name}': ${id} (类型: ${typeof id})`);
      });
      
      // 打印完整的节点映射数量
      console.log(`总节点映射数: ${Object.keys(nodeNameToIdMap).length}`);

      // 专门调试"韩立"相关的边
      debugHanliEdges(edges, nodeMap);

      // 转换边数据，使用节点ID进行关联
      // 先打印所有边数据的详细信息，帮助调试
      console.log('\n===== 原始边数据详情 =====');
      edges.forEach((edge, index) => {
        console.log(`边${index + 1}: ID=${edge.id}, 源节点="${edge.sourceNodeName}", 目标节点="${edge.targetNodeName}", 关系="${edge.relation}"`);
      });
      console.log('==========================\n');
      
      const echartsEdges = edges.map(edge => {
        // 检查边的源节点和目标节点是否存在于映射中
        const sourceId = nodeNameToIdMap[edge.sourceNodeName];
        const targetId = nodeNameToIdMap[edge.targetNodeName];
        
        // 打印每条边的关联状态和类型信息
        console.log(`边ID: ${edge.id}, 关系: "${edge.relation}"`);
        console.log(`  源节点: "${edge.sourceNodeName}" -> ID=${sourceId || '未找到'} (类型: ${typeof sourceId})`);
        console.log(`  目标节点: "${edge.targetNodeName}" -> ID=${targetId || '未找到'} (类型: ${typeof targetId})`);
        
        if (!sourceId) {
          console.warn(`警告: 找不到对应的源节点 '${edge.sourceNodeName}'，边ID: ${edge.id}`);
        }
        if (!targetId) {
          console.warn(`警告: 找不到对应的目标节点 '${edge.targetNodeName}'，边ID: ${edge.id}`);
        }
        
        return {
          source: sourceId,
          target: targetId,
          name: edge.relation,
          sourceName: edge.sourceNodeName,  // 添加源节点名称用于tooltip显示
          targetName: edge.targetNodeName,  // 添加目标节点名称用于tooltip显示
          label: {
            show: true,
            formatter: edge.relation,
            fontSize: 12,
            color: '#333',
            backgroundColor: 'rgba(255, 255, 255, 0.7)',
            padding: [2, 5],
            borderRadius: 3
          },
          lineStyle: {
            color: edge.color || '#666666',
            width: edge.width || 2.5,  // 稍微加粗线条以提高可见性
            type: edge.dashed ? 'dashed' : 'solid',
            opacity: 1  // 确保线条不透明
          }
        };
      });
      
      // 统计有效和无效的边
      const validEdges = echartsEdges.filter(edge => {
        const isValid = edge.source !== undefined && edge.source !== null && 
                      edge.target !== undefined && edge.target !== null;
        
        if (!isValid) {
          console.log(`过滤无效边: source=${edge.source}, target=${edge.target}, 关系="${edge.name}"`);
        }
        
        return isValid;
      });
      
      const invalidEdges = echartsEdges.filter(edge => 
        edge.source === undefined || edge.source === null || 
        edge.target === undefined || edge.target === null
      );
      
      console.log('\n===== 边处理统计 =====');
      console.log(`总边数: ${edges.length}`);
      console.log(`有效边数: ${validEdges.length}`);
      console.log(`无效边数: ${invalidEdges.length}`);
      
      if (invalidEdges.length > 0) {
        console.log('无效边详情:');
        invalidEdges.slice(0, 5).forEach((edge, index) => {
          console.log(`  ${index + 1}. 源节点ID=${edge.source || '未找到'}, 目标节点ID=${edge.target || '未找到'}, 关系="${edge.name}"`);
        });
      }
      console.log('=====================\n');
      
      // 显示处理后的边的完整列表
      console.log('处理后的边数据:', validEdges);
      
      console.log('处理后的节点数:', echartsNodes.length, '处理后的边数:', validEdges.length);
      console.log('最终使用的分类数据:', categories);

      // 返回ECharts配置
      return {
        tooltip: {
          trigger: 'item',
          formatter: function(params) {
            if (params.dataType === 'edge') {
              return `${params.data.name}`;
            } else {
              let html = `<div style="font-weight: bold;">${params.name}</div>`;
              if (params.data.properties) {
                const props = params.data.properties;
                if (typeof props === 'object') {
                  for (const key in props) {
                    html += `<div>${key}: ${props[key]}</div>`;
                  }
                }
              }
              return html;
            }
          },
          backgroundColor: 'rgba(50, 50, 50, 0.8)',
          borderColor: '#ddd',
          textStyle: {
            color: '#fff'
          }
        },
        legend: {
          show: false
        },
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [
          {
            type: 'graph',
            layout: 'force',
            force: {
              repulsion: 300,
              edgeLength: 100,
              gravity: 0.1
            },
            roam: true,
            label: {
              show: true,
              position: 'right',
              fontSize: 12
            },
            draggable: true,
            data: echartsNodes,
            links: validEdges,
            categories: categories,
            emphasis: {
              focus: 'adjacency',
              lineStyle: {
                width: 4
              },
              itemStyle: {
                shadowBlur: 10,
                shadowColor: 'rgba(0, 0, 0, 0.3)'
              }
            },
            lineStyle: {
              opacity: 1,  // 确保线条不透明
              width: 3,    // 增加线条宽度，提高可见性
              curveness: 0
            }
          }
        ]
      };
    };

    // 加载主题列表
    const loadThemes = async () => {
      try {
        console.log('开始加载主题列表');
        const data = await themeAPI.getAll();
        themes.value = data;
        console.log('主题列表加载成功:', data);
      } catch (error) {
        console.error('加载主题失败:', error);
        // 显示错误提示
        alert('加载主题列表失败，请检查后端服务是否正常运行');
      }
    };

    // 加载整个知识图谱
    const loadFullGraph = async () => {
      try {
        console.log('开始加载完整知识图谱');
        // 加载新数据前清空选择状态
        selectedNodes.value.clear();
        const data = await graphAPI.getFullGraph();
        
        // 添加详细的调试日志
        console.log('完整图谱数据统计:', {
          总节点数: data.nodes?.length || 0,
          总边数: data.edges?.length || 0,
          主题ID分布: data.nodes?.reduce((acc, node) => {
            const themeId = node.theme?.id || node.themeId || '未知';
            acc[themeId] = (acc[themeId] || 0) + 1;
            return acc;
          }, {}) || {}
        });
        
        // 显示所有不同的主题ID
        const themeIds = new Set();
        if (data.nodes && Array.isArray(data.nodes)) {
          data.nodes.forEach(node => {
            const themeId = node.theme?.id || node.themeId;
            if (themeId) {
              themeIds.add(themeId);
            }
          });
          console.log('数据中包含的主题ID集合:', Array.from(themeIds));
          
          // 打印theme_id=4的节点示例
          const theme4Nodes = data.nodes.filter(node => 
            node.theme?.id === 4 || node.themeId === 4
          );
          console.log('theme_id=4的节点数量:', theme4Nodes.length);
          if (theme4Nodes.length > 0) {
            console.log('theme_id=4的节点示例:', theme4Nodes.slice(0, 5));
          }
        }
        
        if (chartInstance.value) {
          // 在设置新配置前清空画布，避免画面残留
          chartInstance.value.clear();
          chartInstance.value.setOption(getChartOption(data.nodes, data.edges));
        }
        console.log('完整知识图谱加载成功');
      } catch (error) {
        console.error('加载知识图谱失败:', error);
        // 显示错误提示
        alert('加载知识图谱失败，请检查后端服务是否正常运行');
      }
    };

    // 根据主题加载知识图谱
    const loadGraphByTheme = async () => {
      try {
        console.log(`主题选择变化: ${selectedTheme.value}`);
        
        // 确保图表实例存在
        if (!chartInstance.value) {
          console.error('图表实例不存在，尝试重新初始化');
          initChart();
          if (!chartInstance.value) {
            console.error('重新初始化图表失败');
            return;
          }
        }
        
        // 加载新数据前清空选择状态
        selectedNodes.value.clear();
        let data;
        if (selectedTheme.value === '0') {
          console.log('加载所有主题的知识图谱');
          data = await graphAPI.getFullGraph();
        } else {
          console.log(`加载主题ID为 ${selectedTheme.value} 的知识图谱`);
          data = await graphAPI.getGraphByTheme(selectedTheme.value);
        }
        
        // 验证数据
        console.log(`接收到的数据: 节点数量=${data.nodes?.length || 0}, 边数量=${data.edges?.length || 0}`);
        
        if (!data.nodes || !Array.isArray(data.nodes)) {
          console.error('节点数据格式错误:', data.nodes);
          alert('节点数据格式错误，请联系管理员');
          return;
        }
        
        if (!data.edges || !Array.isArray(data.edges)) {
          console.error('边数据格式错误:', data.edges);
          alert('边数据格式错误，请联系管理员');
          return;
        }
        
        // 更新统计信息
        currentNodeCount.value = data.nodes.length;
        currentEdgeCount.value = data.edges.length;
        
        // 生成并应用图表配置
        const option = getChartOption(data.nodes, data.edges);
        console.log('生成的图表配置:', option);
        
        // 先清空画布，再设置新配置，避免画面残留
        chartInstance.value.clear();
        chartInstance.value.setOption(option, true);
        
        // 调整图表大小确保正确显示
        setTimeout(() => {
          if (chartInstance.value) {
            chartInstance.value.resize();
          }
        }, 100);
        
        console.log(`主题ID为 ${selectedTheme.value} 的知识图谱加载成功并应用`);
      } catch (error) {
        console.error('加载主题知识图谱失败:', error);
        // 显示错误提示
        alert('加载知识图谱失败，请检查后端服务是否正常运行');
      }
    };

    // 刷新图表
    const refreshGraph = () => {
      console.log('刷新图表数据');
      loadGraphByTheme();
    };

    // 转换节点属性为表格数据格式
    const getNodePropertiesData = () => {
      if (!selectedNode.value) {
        return [];
      }
      
      const result = [];
      
      // 记录节点数据用于调试
      console.log('当前选中节点数据:', selectedNode.value);
      
      // 首先尝试从properties字段获取属性
      let properties = selectedNode.value.properties;
      
      // 处理可能是JSON字符串的情况
      if (typeof properties === 'string') {
        try {
          properties = JSON.parse(properties);
        } catch (e) {
          // 如果不是有效的JSON，将其视为普通字符串
          result.push({
            key: 'properties',
            value: properties,
            rowKey: 'properties_string'
          });
          // 同时添加节点基本信息
          result.push({
            key: 'ID',
            value: selectedNode.value.id || '未知',
            rowKey: 'id'
          });
          result.push({
            key: '名称',
            value: selectedNode.value.name || '未知',
            rowKey: 'name'
          });
          return result;
        }
      }
      
      // 如果没有properties或properties为空，尝试检查其他可能包含属性的字段
      if (!properties || (typeof properties === 'object' && Object.keys(properties).length === 0)) {
        // 创建一个包含节点基本信息的属性集合
        properties = {
          'ID': selectedNode.value.id || '未知',
          '名称': selectedNode.value.name || '未知',
          '类型': selectedNode.value.category?.toString() || '未知',
          '大小': selectedNode.value.symbolSize || '默认',
          '颜色': selectedNode.value.itemStyle?.color || '默认'
        };
      }
      
      // 处理属性对象
      if (typeof properties === 'object') {
        for (const key in properties) {
          // 确保key和value都有值
          if (key !== undefined && properties[key] !== undefined) {
            // 处理嵌套对象
            let displayValue = properties[key];
            if (typeof displayValue === 'object' && displayValue !== null) {
              displayValue = JSON.stringify(displayValue, null, 2);
            }
            result.push({
              key,
              value: displayValue,
              rowKey: key + '_' + Math.random().toString(36).substr(2, 9) // 使用随机字符串确保唯一性
            });
          }
        }
      }
      
      // 如果仍然没有数据，显示提示信息
      if (result.length === 0) {
        result.push({
          key: '提示',
          value: '该节点暂无属性信息',
          rowKey: 'empty'
        });
      }
      
      console.log('处理后的属性数据:', result);
      return result;
    };
    
    // 关闭弹窗
    const handleCancel = () => {
      isModalVisible.value = false;
    };
    
    // 监听高度变化
    watch(() => props.height, (newHeight) => {
      chartStyle.value.height = newHeight + 'px';
      // 延迟调整图表大小，确保DOM已经更新
      setTimeout(() => {
        if (chartInstance.value) {
          chartInstance.value.resize();
        }
      }, 100);
    });

    // 组件挂载时初始化
    onMounted(() => {
      console.log('EChartsGraph组件挂载，开始初始化');
      initChart();
      loadThemes();
      loadFullGraph();
    });

    // 组件卸载时清理
    onUnmounted(() => {
      console.log('EChartsGraph组件卸载，开始清理');
      if (chartInstance.value) {
        // 移除点击事件监听器
        chartInstance.value.off('click', handleNodeClick);
        chartInstance.value.dispose();
      }
      window.removeEventListener('resize', handleResize);
    });
    
    return {
      chart,
      chartStyle,
      themes,
      selectedTheme,
      currentNodeCount,
      currentEdgeCount,
      loadGraphByTheme,
      refreshGraph,
      isModalVisible,
      selectedNode,
      handleCancel,
      getNodePropertiesData,
      // 节点选择相关功能
      clearNodeSelection,
      selectedNodes,
      nodeSelectionEnabled
    };
  }
};
</script>

<style scoped>
.echarts-graph-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 180px); /* 减去Header和Footer的高度 */
}

.chart {
  background-color: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  width: 100%;
  flex: 1;
  min-height: 0; /* 允许flex子元素收缩 */
}

.controls {
  margin-top: 12px;
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
}

.theme-select {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #fff;
  cursor: pointer;
  transition: border-color 0.3s;
}

.theme-select:hover {
  border-color: #4285F4;
}

.refresh-btn {
  padding: 6px 16px;
  background-color: #4285F4;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.refresh-btn:hover {
  background-color: #3367d6;
}

.refresh-btn:active {
  background-color: #2c5bb5;
}

.graph-stats {
  margin-left: auto;
  font-size: 14px;
  color: #666;
  padding: 4px 12px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

/* 节点属性弹窗样式 */
:deep(.ant-modal-content) {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: none;
  overflow: hidden;
}

:deep(.ant-modal-header) {
  display: none;
}

:deep(.ant-modal-body) {
  padding: 0;
  background-color: #ffffff;
}

/* 自定义节点属性内容样式 */
.node-property-content {
  padding: 16px;
}

.node-name {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.properties-table {
  max-height: 300px;
  overflow-y: auto;
}

.property-item {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.property-key {
  width: 25%;
  font-weight: 500;
  color: #666;
  flex-shrink: 0;
  padding-right: 8px;
}

.property-value {
  width: 75%;
  color: #333;
  word-break: break-all;
  white-space: pre-wrap;
}

/* 滚动条样式 */
.properties-table::-webkit-scrollbar {
  width: 6px;
}

.properties-table::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.properties-table::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

.properties-table::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>