<template>
  <!-- 确保组件总是渲染，不管visible是什么值 -->
  <div class="simple-edit-modal" v-show="true">
    <div v-if="true" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>简单编辑弹窗 - 测试版本</h3>
          <p>visible状态: {{ visible }}</p>
          <p>表单数据字段数量: {{ Object.keys(formData).length }}</p>
          <button @click="handleCancel" class="close-button">×</button>
        </div>
        
        <div class="modal-body">
          <div v-if="Object.keys(formData).length > 0">
            <div class="form-grid">
              <div v-for="(value, key) in formData" :key="key" class="form-item">
                <label>{{ key }}</label>
                <input 
                  v-model="formData[key]"
                  :disabled="disabledFields.includes(key)"
                  :placeholder="`请输入${key}`"
                />
              </div>
            </div>
          </div>
          <div v-else>
            <p>示例数据:</p>
            <div class="form-item">
              <label>id</label>
              <input value="1" disabled />
            </div>
            <div class="form-item">
              <label>name</label>
              <input value="测试名称" />
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="handleCancel" class="btn-cancel">取消</button>
          <button @click="handleSave" class="btn-save">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  initialData: {
    type: Object,
    default: () => ({})
  },
  disabledFields: {
    type: Array,
    default: () => ['id']
  }
});

const emit = defineEmits(['ok', 'cancel']);

// 表单数据
const formData = ref({});

// 监听visible变化，添加调试日志
watch(() => props.visible, (newVisible) => {
  console.log('[SimpleEditModal] visible状态变化为:', newVisible);
}, { immediate: true });

// 监听初始数据变化
watch(() => props.initialData, (newData) => {
  console.log('[SimpleEditModal] 接收到的初始数据:', newData);
  formData.value = { ...newData };
}, { deep: true, immediate: true });

// 处理保存
const handleSave = () => {
  console.log('[SimpleEditModal] 保存按钮被点击，表单数据:', formData.value);
  emit('ok', { ...formData.value });
};

// 处理取消
const handleCancel = () => {
  console.log('[SimpleEditModal] 取消按钮被点击');
  emit('cancel');
};

// 组件挂载时
onMounted(() => {
  console.log('[SimpleEditModal] 组件已成功挂载');
  // 强制渲染
  setTimeout(() => {
    console.log('[SimpleEditModal] 强制渲染检查');
  }, 100);
});
</script>

<style scoped>
.simple-edit-modal {
  position: relative;
  z-index: 1001;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  width: 700px;
  max-width: 90vw;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-button {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.3s;
}

.close-button:hover {
  background-color: #f5f5f5;
  color: #333;
}

.modal-body {
  margin-bottom: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
}

.form-item label {
  margin-bottom: 4px;
  font-size: 14px;
  color: #333;
}

.form-item input {
  padding: 6px 11px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
}

.form-item input:focus {
  border-color: #40a9ff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-item input:disabled {
  background-color: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.btn-cancel,
.btn-save {
  padding: 6px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cancel {
  background-color: white;
  color: #333;
}

.btn-cancel:hover {
  border-color: #40a9ff;
  color: #40a9ff;
}

.btn-save {
  background-color: #1890ff;
  border-color: #1890ff;
  color: white;
}

.btn-save:hover {
  background-color: #40a9ff;
  border-color: #40a9ff;
}
</style>