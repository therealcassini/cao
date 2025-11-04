<template>
  <div class="graph-manager">
    <a-card class="section-card">
      <template #title>
        <div class="section-actions">
          <span>主题管理</span>
          <a-button type="primary" @click="handleAddTheme" style="margin-left: 12px">
            <PlusOutlined /> 添加主题
          </a-button>
        </div>
      </template>
      <a-table
        v-if="themes.length > 0"
        :columns="themeColumns"
        :data-source="themes"
        row-key="id"
        :loading="loadingThemes"
      >
        <template #operation="{ record }">
          <a-space>
            <a-button type="link" @click="handleEditTheme(record)">编辑</a-button>
            <a-button type="link" danger @click="handleDeleteTheme(record.id)">删除</a-button>
          </a-space>
        </template>
      </a-table>
      <div v-else-if="!loadingThemes" class="empty-state">
        <p>暂无主题数据，请点击上方按钮添加</p>
      </div>
    </a-card>

    <a-card class="section-card">
      <template #title>
        <div class="section-actions">
          <span>节点管理</span>
          <a-select
            v-model:value="selectedThemeId"
            style="width: 180px; margin-left: 12px"
            placeholder="选择主题"
          >
            <a-select-option v-for="theme in themes" :key="theme.id" :value="theme.id">
              {{ theme.name }}
            </a-select-option>
          </a-select>
          <a-input
            v-model:value="nodeNameFilter"
            placeholder="输入节点名称搜索"
            style="width: 200px; margin-left: 12px"
          />
          <a-button type="primary" @click="handleAddNode" style="margin-left: 12px" :disabled="!selectedThemeId">
            <PlusOutlined /> 添加节点
          </a-button>
        </div>
      </template>
      <a-table
        v-if="filteredNodes.length > 0"
        :columns="nodeColumns"
        :data-source="filteredNodes"
        row-key="id"
        :loading="loadingNodes"
      >
        <template #operation="{ record }">
          <a-space>
            <a-button type="link" @click="handleEditNode(record)">编辑</a-button>
            <a-button type="link" danger @click="handleDeleteNode(record.id)">删除</a-button>
          </a-space>
        </template>
        <template #color="{ record }">
          <div :style="{ backgroundColor: record.color, width: '20px', height: '20px', borderRadius: '4px' }" />
        </template>
        <template #nodeProperty="{ record }">
          <div v-if="record.properties">
            <a-tooltip title="点击查看详情" placement="top">
              <div @click="showJsonDetail(record.properties, '节点属性详情')" style="cursor: pointer; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                {{ formatJsonPreview(record.properties) }}
              </div>
            </a-tooltip>
          </div>
          <span v-else>-</span>
        </template>
      </a-table>
      <div v-else-if="!loadingNodes" class="empty-state">
        <p v-if="selectedThemeId && nodes.length === 0">当前主题下暂无节点数据</p>
        <p v-else-if="selectedThemeId && filteredNodes.length === 0">未找到匹配的节点</p>
        <p v-else>请先选择主题</p>
      </div>
    </a-card>

    <a-card class="section-card">
      <template #title>
        <div class="section-actions">
          <span>关系管理</span>
          <a-select
            v-model:value="selectedEdgeThemeId"
            style="width: 180px; margin-left: 12px"
            placeholder="选择主题"
          >
            <a-select-option v-for="theme in themes" :key="theme.id" :value="theme.id">
              {{ theme.name }}
            </a-select-option>
          </a-select>
          <a-input
            v-model:value="edgeRelationFilter"
            placeholder="输入关系类型搜索"
            style="width: 200px; margin-left: 12px"
          />
          <a-button type="primary" @click="handleAddEdge" style="margin-left: 12px" :disabled="!selectedEdgeThemeId">
            <plus-outlined /> 添加关系
          </a-button>
        </div>
      </template>
      <a-table
        v-if="filteredEdges.length > 0"
        :columns="edgeColumns"
        :data-source="filteredEdges"
        row-key="id"
        :loading="loadingEdges"
      >
        <template #operation="{ record }">
          <a-space>
            <a-button type="link" @click="handleEditEdge(record)">编辑</a-button>
            <a-button type="link" danger @click="handleDeleteEdge(record.id)">删除</a-button>
          </a-space>
        </template>
        <template #sourceNode="{ record }">
          <span>{{ record.source_node_name || record.sourceNodeName || record.source_node_id || record.sourceNodeId || '未定义' }}</span>
        </template>
        <template #targetNode="{ record }">
          <span>{{ record.target_node_name || record.targetNodeName || record.target_node_id || record.targetNodeId || '未定义' }}</span>
        </template>

        <template #edgeColor="{ record }">
          <div :style="{ backgroundColor: record.color, width: '20px', height: '20px', borderRadius: '4px' }" />
        </template>
        <template #edgeDashed="{ record }">
          <span>{{ record.dashed ? '是' : '否' }}</span>
        </template>
      </a-table>
      <div v-else-if="!loadingEdges" class="empty-state">
        <p v-if="selectedEdgeThemeId && edges.length === 0">当前主题下暂无关系数据</p>
        <p v-else-if="selectedEdgeThemeId && filteredEdges.length === 0">未找到匹配的关系</p>
        <p v-else>请先选择主题</p>
      </div>
    </a-card>

    <!-- 主题编辑模态框 -->
    <a-modal
      v-model:visible="themeModalVisible"
      :title="themeModalTitle"
      @ok="handleThemeModalOk"
      @cancel="handleThemeModalCancel"
      destroyOnClose
      :maskClosable="false"
    >
      <a-form
        ref="themeFormRef"
        :model="currentTheme"
        :rules="themeFormRules"
        layout="vertical"
      >
        <a-form-item name="name" label="主题名称">
          <a-input v-model:value="currentTheme.name" placeholder="请输入主题名称" />
        </a-form-item>
        <a-form-item name="description" label="主题描述">
          <a-input.TextArea v-model:value="currentTheme.description" placeholder="请输入主题描述" rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 节点编辑模态框 -->
    <a-modal
      v-model:visible="nodeModalVisible"
      :title="nodeModalTitle"
      @ok="handleNodeModalOk"
      @cancel="handleNodeModalCancel"
      destroyOnClose
      width="800px"
      :maskClosable="false"
    >
      <a-form
        ref="nodeFormRef"
        :model="currentNode"
        :rules="nodeFormRules"
        layout="vertical"
      >
        <a-form-item name="name" label="节点名称">
          <a-input v-model:value="currentNode.name" placeholder="请输入节点名称" />
        </a-form-item>
        <a-form-item name="color" label="节点颜色">
          <a-input v-model:value="currentNode.color" placeholder="请输入颜色代码，如 #ff0000" />
        </a-form-item>
        <a-form-item name="size" label="节点大小">
          <a-input-number v-model:value="currentNode.size" :min="1" :max="200" placeholder="请输入节点大小" />
        </a-form-item>
        <a-form-item label="节点属性">
          <div v-if="nodePropertiesList.length === 0" class="empty-property">
            <p>暂无属性，点击下方按钮添加</p>
          </div>
          <div v-for="(item, index) in nodePropertiesList" :key="index" class="property-item" style="display: flex; align-items: center; width: 100%;">
            <a-input
              v-model:value="item.key"
              placeholder="属性名称"
              style="width: 180px; margin-right: 8px"
            />
            <a-input
              v-model:value="item.value"
              placeholder="属性值"
              style="flex: 1; min-width: 300px; margin-right: 8px"
            />
            <a-button
                type="text"
                danger
                size="small"
                @click="handleDeleteProperty(index)"
              >
                删除
              </a-button>
          </div>
          <a-button
            type="dashed"
            block
            class="add-property-button"
            @click="handleAddProperty"
          >
            <PlusOutlined /> 添加属性
          </a-button>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 关系编辑模态框 -->
    <a-modal
      v-model:visible="edgeModalVisible"
      :title="edgeModalTitle"
      @ok="handleEdgeModalOk"
      @cancel="handleEdgeModalCancel"
      destroyOnClose
      :maskClosable="false"
    >
      <a-form
        ref="edgeFormRef"
        :model="currentEdge"
        :rules="edgeFormRules"
        layout="vertical"
      >
        <a-form-item name="relation" label="关系类型">
          <a-input v-model:value="currentEdge.relation" placeholder="请输入关系类型" />
        </a-form-item>
        <a-form-item name="sourceNodeName" label="源节点">
          <a-input v-model:value="currentEdge.sourceNodeName" placeholder="请输入源节点名称" />
        </a-form-item>
        <a-form-item name="targetNodeName" label="目标节点">
          <a-input v-model:value="currentEdge.targetNodeName" placeholder="请输入目标节点名称" />
        </a-form-item>
        <a-form-item name="color" label="边颜色">
          <a-input v-model:value="currentEdge.color" placeholder="请输入颜色代码，如 #000000" />
        </a-form-item>
        <a-form-item name="width" label="边宽度">
          <a-input-number v-model:value="currentEdge.width" :min="1" :max="20" placeholder="请输入边宽度" />
        </a-form-item>
        <a-form-item label="是否虚线">
          <a-switch v-model:checked="currentEdge.dashed" />
        </a-form-item>

      </a-form>
    </a-modal>

    <!-- JSON详情弹窗 -->
    <a-modal
      v-model:visible="jsonDetailVisible"
      :title="jsonDetailTitle"
      :footer="null"
      :closable="true"
      :maskClosable="false"
      :width="600"
      @cancel="jsonDetailVisible = false"
      footer=""
    >
      <pre style="white-space: pre-wrap; word-break: break-all; background: #f5f5f5; padding: 16px; border-radius: 4px; max-height: 400px; overflow-y: auto;">{{ jsonDetailContent }}</pre>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { PlusOutlined } from '@ant-design/icons';
import { message } from 'ant-design-vue';
import { themeAPI, nodeAPI, edgeAPI } from '../api/graphService';

// 状态管理
const themes = ref([]);
const nodes = ref([]);
const edges = ref([]);
const allNodes = ref([]); // 所有节点，用于关系选择
const selectedThemeId = ref('');
const selectedEdgeThemeId = ref('');
const loadingThemes = ref(false);
const loadingNodes = ref(false);
const loadingEdges = ref(false);
const nodeNameFilter = ref(''); // 节点名称筛选关键词
const edgeRelationFilter = ref(''); // 关系类型筛选关键词

// 表单和模态框
const themeFormRef = ref();
const nodeFormRef = ref();
const edgeFormRef = ref();
const themeModalVisible = ref(false);
const nodeModalVisible = ref(false);
const edgeModalVisible = ref(false);
const themeModalTitle = ref('');
const nodeModalTitle = ref('');
const edgeModalTitle = ref('');

// 当前操作对象
const currentTheme = reactive({ id: '', name: '', description: '' });
const currentNode = reactive({ id: '', themeId: '', name: '', type: '', color: '#ff0000', size: 100, properties: '{}' });
const currentEdge = reactive({ id: '', themeId: '', sourceNodeName: '', targetNodeName: '', relation: '', color: '#000000', width: 2, dashed: false });

// 节点属性相关变量
const nodePropertiesList = ref([]);
// 标记哪些key正在被编辑
const editingKeyIndices = ref(new Set());
// 用于存储临时编辑的key
const tempEditingKeys = ref({});

// 操作类型
const currentThemeAction = ref('');
const currentNodeAction = ref('');
const currentEdgeAction = ref('');

// 表单验证规则
const themeFormRules = {
  name: [{ required: true, message: '请输入主题名称', trigger: 'blur' }]
};

const nodeFormRules = {
  name: [{ required: true, message: '请输入节点名称', trigger: 'blur' }],
  color: [{ required: true, message: '请输入节点颜色', trigger: 'blur' }],
  size: [{ required: true, message: '请输入节点大小', trigger: 'blur' }]
};

const edgeFormRules = {
  relation: [{ required: true, message: '请输入关系类型', trigger: 'blur' }],
  sourceNodeName: [{ required: true, message: '请输入源节点名称', trigger: 'blur' }],
  targetNodeName: [{ required: true, message: '请输入目标节点名称', trigger: 'blur' }],
  color: [{ required: false, message: '请输入边颜色', trigger: 'blur' }],
  width: [{ required: false, type: 'number', min: 1, max: 20, message: '边宽度必须在1-20之间', trigger: 'blur' }]
};

// 列定义
const themeColumns = [
  { title: '主题ID', dataIndex: 'id', key: 'id' },
  { title: '主题名称', dataIndex: 'name', key: 'name' },
  { title: '主题描述', dataIndex: 'description', key: 'description' },
  { title: '操作', key: 'operation', slots: { customRender: 'operation' } }
];

const nodeColumns = [
  { title: '节点ID', dataIndex: 'id', key: 'id' },
  { title: '节点名称', dataIndex: 'name', key: 'name' },
  { title: '节点属性', key: 'type', slots: { customRender: 'nodeProperty' } },
  { title: '颜色', key: 'color', slots: { customRender: 'color' } },
  { title: '大小', dataIndex: 'size', key: 'size' },
  { title: '操作', key: 'operation', slots: { customRender: 'operation' } }
];

const edgeColumns = [
  { title: '关系ID', dataIndex: 'id', key: 'id' },
  { title: '关系类型', dataIndex: 'relation', key: 'relation' },
  { title: '源节点', key: 'sourceNodeName', slots: { customRender: 'sourceNode' } },
  { title: '目标节点', key: 'targetNodeName', slots: { customRender: 'targetNode' } },
  { title: '颜色', key: 'color', slots: { customRender: 'edgeColor' } },
  { title: '宽度', dataIndex: 'width', key: 'width' },
  { title: '是否虚线', dataIndex: 'dashed', key: 'dashed', slots: { customRender: 'edgeDashed' } },
  { title: '操作', key: 'operation', slots: { customRender: 'operation' } }
];

// 工具函数 - 显示错误信息
const showError = (title, error) => {
  console.error(`${title}失败:`, error);
  message.error(`${title}失败，请稍后重试。`);
};

// 工具函数 - 格式化JSON预览
const formatJsonPreview = (jsonStr) => {
  try {
    // 尝试解析JSON字符串
    const obj = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr;
    // 如果是简单对象，显示键值对预览
    if (typeof obj === 'object' && obj !== null) {
      const keys = Object.keys(obj);
      if (keys.length === 0) return '{}';
      if (keys.length === 1) return `${keys[0]}: ${String(obj[keys[0]])}`;
      return `${keys[0]}: ${String(obj[keys[0]])}...`;
    }
    return String(jsonStr);
  } catch (e) {
    // 如果不是有效的JSON，直接返回原字符串
    return jsonStr;
  }
};

// 工具函数 - 显示JSON详情弹窗
const jsonDetailVisible = ref(false);
const jsonDetailTitle = ref('');
const jsonDetailContent = ref('');

const showJsonDetail = (jsonStr, title) => {
  try {
    // 尝试格式化JSON以便更好地显示
    const obj = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr;
    jsonDetailContent.value = JSON.stringify(obj, null, 2);
  } catch (e) {
    // 如果不是有效的JSON，显示原字符串
    jsonDetailContent.value = jsonStr;
  }
  jsonDetailTitle.value = title;
  jsonDetailVisible.value = true;
};

// 准备关系提交数据
const prepareEdgeData = (edge) => {
  // 创建要提交的关系对象，不包含properties字段，注意：后端Edge实体类使用theme对象而不是themeId
  const edgeData = {
    id: edge.id,
    sourceNodeName: edge.sourceNodeName, // 使用正确的字段名匹配后端Edge实体类
    targetNodeName: edge.targetNodeName, // 使用正确的字段名匹配后端Edge实体类
    relation: edge.relation, // 直接使用relation字段
    color: edge.color,
    width: edge.width,
    dashed: edge.dashed
  };
  
  // 为了与后端Edge实体类匹配，添加theme对象字段
  if (edge.themeId) {
    edgeData.theme = { id: edge.themeId };
  }
  
  console.log('准备提交的关系数据:', edgeData);
  return edgeData;
};

// 通用表单提交函数
const submitForm = async (type) => {
  const types = {
    theme: {
      formRef: themeFormRef,
      modalVisible: themeModalVisible,
      action: currentThemeAction,
      currentObj: currentTheme,
      createAPI: themeAPI.create,
      updateAPI: (id, data) => themeAPI.update(id, data),
      reloadActions: [loadThemes, loadAllNodes]
    },
    node: {
      formRef: nodeFormRef,
      modalVisible: nodeModalVisible,
      action: currentNodeAction,
      currentObj: currentNode,
      createAPI: nodeAPI.create,
      updateAPI: (id, data) => nodeAPI.update(id, data),
      prepareData: prepareNodeData,
      reloadActions: [loadAllNodes, () => loadNodesByThemeId(selectedThemeId.value)]
    },
    edge: {
      formRef: edgeFormRef,
      modalVisible: edgeModalVisible,
      action: currentEdgeAction,
      currentObj: currentEdge,
      createAPI: edgeAPI.create,
      updateAPI: (id, data) => edgeAPI.update(id, data),
      prepareData: prepareEdgeData,
      reloadActions: [() => loadEdgesByThemeId(selectedEdgeThemeId.value)]
    }
  };
  
  const config = types[type];
  if (!config) return;
  
  try {
    // 验证表单
    if (config.formRef.value) {
      await config.formRef.value.validate();
    }
    
    // 验证节点属性
    if (type === 'node') {
      validateNodeProperties(currentNode);
    }
    
    // 验证关系属性
    if (type === 'edge') {
      validateEdgeProperties(currentEdge);
    }
    
    // 准备数据
    const data = config.prepareData ? config.prepareData(config.currentObj) : { ...config.currentObj };
    
    // 执行操作
    if (config.action.value === 'add') {
      await config.createAPI(data);
    } else if (config.action.value === 'edit') {
      await config.updateAPI(config.currentObj.id, data);
    }
    
    // 关闭模态框
    config.modalVisible.value = false;
    
    // 刷新数据
    for (const action of config.reloadActions) {
      await action();
    }
    
    message.success(`${config.action.value === 'add' ? '添加' : '编辑'}${type === 'theme' ? '主题' : type === 'node' ? '节点' : '关系'}成功`);
  } catch (error) {
    showError(`${config.action.value === 'add' ? '添加' : '编辑'}${type === 'theme' ? '主题' : type === 'node' ? '节点' : '关系'}`, error);
  }
};

// 验证节点属性
const validateNodeProperties = (node) => {
  try {
    JSON.parse(node.properties);
    return true;
  } catch (e) {
    message.error('属性必须是有效的JSON格式');
    return false;
  }
};

// 准备节点提交数据
const prepareNodeData = (node) => {
  // 解析properties字符串为对象
  let properties = {};
  try {
    properties = JSON.parse(node.properties || '{}');
  } catch (e) {
    console.error('解析节点属性失败:', e);
  }
  
  // 确保properties是一个对象
  if (typeof properties !== 'object' || properties === null) {
    properties = {};
  }
  
  // 创建要提交的节点对象 - color和size作为独立字段提交
  const nodeData = {
    ...node,
    properties: JSON.stringify(properties)
  };
  
  console.log('准备提交的节点数据:', nodeData);
  return nodeData;
};

// 验证关系属性（不再验证properties字段，因为已从界面移除）
const validateEdgeProperties = (edge) => {
  return true;
};

// 关闭模态框
const closeModal = (type) => {
  const modals = {
    theme: themeModalVisible,
    node: nodeModalVisible,
    edge: edgeModalVisible
  };
  
  if (modals[type]) {
    modals[type].value = false;
  }
};

// 监听主题选择变化
watch(selectedThemeId, (newVal) => {
  loadNodesByThemeId(newVal);
  // 当主题变化时，更新当前节点的themeId
  if (newVal) {
    currentNode.themeId = newVal;
  }
});

watch(selectedEdgeThemeId, (newVal) => {
  loadEdgesByThemeId(newVal);
  // 当主题变化时，更新当前关系的themeId
  if (newVal) {
    currentEdge.themeId = newVal;
  }
});

// 初始化数据
onMounted(async () => {
  await loadThemes();
  await loadAllNodes();
  
  // 如果有主题数据，自动加载第一个主题的节点数据
  if (themes.value.length > 0 && !selectedThemeId.value) {
    selectedThemeId.value = themes.value[0].id;
    currentNode.themeId = selectedThemeId.value;
    await loadNodesByThemeId(selectedThemeId.value);
  }
  
  // 如果有主题数据，自动设置关系的主题
  if (themes.value.length > 0 && !selectedEdgeThemeId.value) {
    selectedEdgeThemeId.value = themes.value[0].id;
    currentEdge.themeId = selectedEdgeThemeId.value;
    await loadEdgesByThemeId(selectedEdgeThemeId.value);
  }
});

// 处理添加主题
const handleAddTheme = () => {
  currentThemeAction.value = 'add';
  Object.assign(currentTheme, { id: '', name: '', description: '' });
  themeModalTitle.value = '添加主题';
  themeModalVisible.value = true;
};

// 处理编辑主题
const handleEditTheme = (record) => {
  currentThemeAction.value = 'edit';
  Object.assign(currentTheme, { ...record });
  themeModalTitle.value = '编辑主题';
  themeModalVisible.value = true;
};

// 处理删除主题
const handleDeleteTheme = async (id) => {
  try {
    await themeAPI.delete(id);
    message.success('主题删除成功');
    await loadThemes();
    await loadAllNodes();
  } catch (error) {
    showError('删除主题', error);
  }
};

// 处理添加节点
const handleAddNode = () => {
  currentNodeAction.value = 'add';
  Object.assign(currentNode, { id: '', themeId: selectedThemeId.value, name: '', type: '', color: '#ff0000', size: 100, properties: '{}' });
  nodePropertiesList.value = [];
  nodeModalTitle.value = '添加节点';
  nodeModalVisible.value = true;
};

// 处理编辑节点
const handleEditNode = (record) => {
  currentNodeAction.value = 'edit';
  Object.assign(currentNode, { ...record });
  // 初始化properties列表
  try {
    const properties = JSON.parse(record.properties || '{}');
    nodePropertiesList.value = Object.entries(properties).map(([key, value]) => ({ key, value }));
  } catch (e) {
    nodePropertiesList.value = [];
  }
  nodeModalTitle.value = '编辑节点';
  nodeModalVisible.value = true;
};

// 处理删除节点
const handleDeleteNode = async (id) => {
  try {
    await nodeAPI.delete(id);
    message.success('节点删除成功');
    await loadAllNodes();
    await loadNodesByThemeId(selectedThemeId.value);
  } catch (error) {
    showError('删除节点', error);
  }
};

// 处理添加关系
const handleAddEdge = () => {
  currentEdgeAction.value = 'add';
  Object.assign(currentEdge, { id: '', themeId: selectedEdgeThemeId.value, sourceNodeName: '', targetNodeName: '', relation: '', color: '#000000', width: 2, dashed: false, properties: '{}' });
  edgeModalTitle.value = '添加关系';
  edgeModalVisible.value = true;
};

// 处理编辑关系
const handleEditEdge = (record) => {
  currentEdgeAction.value = 'edit';
  // 正确复制关系记录数据到currentEdge对象
  Object.assign(currentEdge, { ...record });
  edgeModalTitle.value = '编辑关系';
  edgeModalVisible.value = true;
};

// 处理删除关系
const handleDeleteEdge = async (id) => {
  try {
    await edgeAPI.delete(id);
    message.success('关系删除成功');
    await loadEdgesByThemeId(selectedEdgeThemeId.value);
  } catch (error) {
    showError('删除关系', error);
  }
};

// 处理主题模态框确认
const handleThemeModalOk = () => {
  submitForm('theme');
};

// 处理主题模态框取消
const handleThemeModalCancel = () => {
  closeModal('theme');
};

// 处理添加属性
const handleAddProperty = () => {
  nodePropertiesList.value.push({ key: '新属性', value: '' });
};

// 处理删除属性
const handleDeleteProperty = (index) => {
  nodePropertiesList.value.splice(index, 1);
};

// 处理节点模态框确认
const handleNodeModalOk = () => {
  // 将属性列表转换回JSON字符串
  const properties = {};
  nodePropertiesList.value.forEach(item => {
    if (item.key && item.key.trim()) {
      properties[item.key.trim()] = item.value;
    }
  });
  currentNode.properties = JSON.stringify(properties);
  submitForm('node');
};

// 处理节点模态框取消
const handleNodeModalCancel = () => {
  closeModal('node');
};

// 处理关系模态框确认
const handleEdgeModalOk = () => {
  submitForm('edge');
};

// 处理关系模态框取消
const handleEdgeModalCancel = () => {
  closeModal('edge');
};

// 根据主题ID获取节点选项
const getNodeOptionsByThemeId = (themeId) => {
  return allNodes.value.filter(node => node.themeId === themeId);
};

// 根据节点ID获取节点名称
const getNodeNameById = (nodeId) => {
  const node = allNodes.value.find(n => n.id === nodeId);
  return node ? node.name : `节点ID: ${nodeId}`;
};

// 加载所有主题
const loadThemes = async () => {
  loadingThemes.value = true;
  try {
    // 注意：API响应拦截器已经直接返回了response.data
    const data = await themeAPI.getAll();
    themes.value = Array.isArray(data) ? data : [];
  } catch (error) {
    showError('加载主题', error);
    themes.value = [];
  } finally {
    loadingThemes.value = false;
  }
};

// 加载所有节点（用于关系选择器）
const loadAllNodes = async () => {
  try {
    // 注意：API响应拦截器已经直接返回了response.data
    const data = await nodeAPI.getAll();
    allNodes.value = Array.isArray(data) ? data : [];
  } catch (error) {
    showError('加载所有节点', error);
    allNodes.value = [];
  }
};

// 根据主题ID加载节点
const loadNodesByThemeId = async (themeId) => {
  if (!themeId) {
    nodes.value = [];
    return;
  }
  
  loadingNodes.value = true;
  try {
    // 注意：API响应拦截器已经直接返回了response.data
    const data = await nodeAPI.getByThemeId(themeId);
    nodes.value = Array.isArray(data) ? data : [];
  } catch (error) {
    showError('加载节点', error);
    nodes.value = [];
  } finally {
    loadingNodes.value = false;
  }
};

// 根据主题ID加载关系
const loadEdgesByThemeId = async (themeId) => {
  if (!themeId) {
    edges.value = [];
    return;
  }
  
  loadingEdges.value = true;
  try {
    // 注意：API响应拦截器已经直接返回了response.data
    const data = await edgeAPI.getByThemeId(themeId);
    // 调试：打印获取到的关系数据
    console.log('从后端获取的关系数据:', data);
    if (Array.isArray(data) && data.length > 0) {
      console.log('关系数据的第一个元素结构:', data[0]);
      console.log('可用字段:', Object.keys(data[0]));
    }
    // 转换数据，处理各种可能的字段名变体
    edges.value = Array.isArray(data) ? data.map(edge => ({
      id: edge.id,
      theme_id: edge.theme_id || edge.themeId,
      relation: edge.relation || edge.type, // 支持relation或type字段
      // 确保所有可能的源节点和目标节点字段名都被处理
      source_node_name: edge.source_node_name || edge.sourceNodeName || edge.source_node_id || edge.sourceNodeId || '',
      target_node_name: edge.target_node_name || edge.targetNodeName || edge.target_node_id || edge.targetNodeId || '',
      sourceNodeName: edge.source_node_name || edge.sourceNodeName || edge.source_node_id || edge.sourceNodeId || '',
      targetNodeName: edge.target_node_name || edge.targetNodeName || edge.target_node_id || edge.targetNodeId || '',
      color: edge.color,
      width: edge.width,
      dashed: edge.dashed,
      properties: edge.properties
    })) : [];
  } catch (error) {
    showError('加载关系', error);
    edges.value = [];
  } finally {
    loadingEdges.value = false;
  }
};

// 计算属性 - 筛选后的节点
const filteredNodes = computed(() => {
  if (!nodeNameFilter.value.trim()) {
    return nodes.value;
  }
  
  const keyword = nodeNameFilter.value.trim().toLowerCase();
  return nodes.value.filter(node => 
    node.name.toLowerCase().includes(keyword)
  );
});

// 计算属性 - 筛选后的关系
const filteredEdges = computed(() => {
  if (!edgeRelationFilter.value.trim()) {
    return edges.value;
  }
  
  const keyword = edgeRelationFilter.value.trim().toLowerCase();
  return edges.value.filter(edge => 
    edge.relation?.toLowerCase().includes(keyword)
  );
});
</script>

<style scoped>
.graph-manager {
  padding: 16px;
}

.section-card {
  margin-bottom: 16px;
}

.section-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
  color: #999;
}

.json-editor {
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 8px;
  min-height: 100px;
  font-family: monospace;
}

.property-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.property-key {
  flex: 0 0 120px;
  font-weight: bold;
}

.property-value {
  flex: 1;
  word-break: break-all;
}

.edit-button {
  margin-left: 8px;
}

.add-property-button {
  margin-top: 12px;
}
</style>