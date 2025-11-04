<template>
  <div class="test-page">
    <h1>编辑弹窗测试页面</h1>
    
    <!-- 调试信息显示 -->
    <div class="debug-info">
      <p>showModal状态: {{ showModal }}</p>
      <p>formData数据: {{ JSON.stringify(formData) }}</p>
      <p>组件挂载状态: {{ isComponentMounted ? '已挂载' : '未挂载' }}</p>
    </div>
    
    <!-- 控制按钮 -->
    <div class="control-buttons">
      <button @click="openModal" class="open-btn">打开编辑弹窗</button>
      <button @click="closeModal" class="close-btn">关闭编辑弹窗</button>
    </div>
    
    <!-- 直接使用SimpleEditModal组件 -->
    <SimpleEditModal
      :visible="showModal"
      :initial-data="formData"
      :disabled-fields="['id']"
      @ok="handleSave"
      @cancel="handleCancel"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import SimpleEditModal from './components/SimpleEditModal.vue';

// 控制模态框显示的状态
const showModal = ref(false);
const isComponentMounted = ref(false);

// 测试数据
const formData = ref({
  id: 1,
  name: '测试记录',
  description: '这是一条用于测试编辑弹窗的数据',
  create_time: new Date().toISOString()
});

// 监听showModal状态变化
watch(showModal, (newValue) => {
  console.log('[TestPage] showModal状态变化为:', newValue);
  console.log('[TestPage] 当前formData数据:', formData.value);
});

// 打开模态框
const openModal = () => {
  console.log('[TestPage] 点击了打开按钮');
  showModal.value = true;
  isComponentMounted.value = true;
  console.log('[TestPage] 设置showModal为true后的值:', showModal.value);
};

// 关闭模态框
const closeModal = () => {
  console.log('[TestPage] 点击了关闭按钮');
  showModal.value = false;
  console.log('[TestPage] 设置showModal为false后的值:', showModal.value);
};

// 处理保存
const handleSave = (values) => {
  console.log('[TestPage] 保存成功，返回的值:', values);
  formData.value = { ...values };
  showModal.value = false;
};

// 处理取消
const handleCancel = () => {
  console.log('[TestPage] 点击了取消按钮');
  showModal.value = false;
};

// 组件加载完成
console.log('[TestPage] 测试页面已加载');
</script>

<style>
.test-page {
  padding: 40px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.debug-info {
  background-color: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-family: monospace;
}

.control-buttons {
  margin-bottom: 20px;
}

.open-btn,
.close-btn {
  padding: 10px 20px;
  margin-right: 10px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.open-btn {
  background-color: #1890ff;
  color: white;
}

.open-btn:hover {
  background-color: #40a9ff;
}

.close-btn {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #d9d9d9;
}

.close-btn:hover {
  border-color: #40a9ff;
  color: #40a9ff;
}
</style>