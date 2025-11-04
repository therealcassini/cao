<template>
  <div class="table-editor-overlay" @click="handleOverlayClick">
    <div class="table-editor-dialog" @click.stop>
      <div class="dialog-header">
        <h3>{{ isEdit ? '编辑记录' : '添加记录' }}</h3>
        <button class="close-btn" @click="handleCancel">×</button>
      </div>
      
      <div class="dialog-body">
        <form class="record-form" @submit.prevent="handleSubmit">
          <div 
            v-for="field in getEditableFields()" 
            :key="field.Field"
            class="form-group"
          >
            <label :for="field.Field">{{ field.Field }}
              <span v-if="field.Null === 'NO' && !isPrimaryKey(field)" class="required-mark">*</span>
              <span v-if="isPrimaryKey(field) && isEdit" class="readonly-mark">(只读)</span>
            </label>
            <input
              :id="field.Field"
              v-model="formData[field.Field]"
              :type="getFieldType(field.Type)"
              :readonly="isPrimaryKey(field) && isEdit"
              :required="field.Null === 'NO' && !isPrimaryKey(field)"
              :placeholder="`请输入${field.Field}`"
            />
          </div>
        </form>
      </div>
      
      <div class="dialog-footer">
        <button class="cancel-btn" @click="handleCancel">取消</button>
        <button class="save-btn" @click="handleSubmit">保存</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue';

export default {
  name: 'TableEditor',
  props: {
    tableName: {
      type: String,
      default: ''
    },
    tableStructure: {
      type: Array,
      default: () => []
    },
    record: {
      type: Object,
      default: () => ({})
    },
    isEdit: {
      type: Boolean,
      default: false
    }
  },
  emits: ['save', 'cancel'],
  setup(props, { emit }) {
    // 表单数据
    const formData = ref({});
    
    // 初始化表单数据
    const initFormData = () => {
      const data = { ...props.record };
      // 为所有字段设置默认值（如果记录中不存在）
      props.tableStructure.forEach(field => {
        if (data[field.Field] === undefined || data[field.Field] === null) {
          data[field.Field] = '';
        } else if (typeof data[field.Field] === 'string' && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}.\d{3}Z$/.test(data[field.Field])) {
          // 格式化日期时间
          data[field.Field] = new Date(data[field.Field]).toISOString().slice(0, 16);
        }
      });
      formData.value = data;
    };
    
    // 检查是否为主键
    const isPrimaryKey = (field) => {
      return field.isPrimaryKey;
    };
    
    // 获取可编辑的字段（添加模式下不包含自增主键）
    const getEditableFields = computed(() => {
      return props.tableStructure.filter(field => {
        if (!props.isEdit && isPrimaryKey(field) && field.Extra && field.Extra.includes('auto_increment')) {
          // 添加模式下，自增主键不显示
          return false;
        }
        return true;
      });
    });
    
    // 获取字段类型
    const getFieldType = (type) => {
      const lowerType = type.toLowerCase();
      if (lowerType.includes('int') || lowerType.includes('double') || lowerType.includes('float') || lowerType.includes('decimal')) {
        return 'number';
      } else if (lowerType.includes('date') || lowerType.includes('time')) {
        return 'datetime-local';
      } else if (lowerType.includes('boolean')) {
        return 'checkbox';
      } else {
        return 'text';
      }
    };
    
    // 处理表单提交
    const handleSubmit = () => {
      // 构建提交数据
      const submitData = { ...formData.value };
      
      // 处理特殊类型的字段
      props.tableStructure.forEach(field => {
        const fieldName = field.Field;
        const fieldType = field.Type.toLowerCase();
        
        // 数字类型转换
        if ((fieldType.includes('int') || fieldType.includes('double') || 
             fieldType.includes('float') || fieldType.includes('decimal')) && 
            submitData[fieldName] !== '' && submitData[fieldName] !== null) {
          submitData[fieldName] = Number(submitData[fieldName]);
        }
        // 空字符串转换为null
        if (submitData[fieldName] === '') {
          submitData[fieldName] = null;
        }
      });
      
      emit('save', submitData);
    };
    
    // 处理取消
    const handleCancel = () => {
      emit('cancel');
    };
    
    // 点击遮罩层关闭
    const handleOverlayClick = () => {
      emit('cancel');
    };
    
    // 监听props变化，重新初始化表单
    const watchProps = () => {
      initFormData();
    };
    
    // 组件挂载时初始化
    watchProps();
    
    return {
      formData,
      isPrimaryKey,
      getEditableFields,
      getFieldType,
      handleSubmit,
      handleCancel,
      handleOverlayClick
    };
  }
};
</script>

<style scoped>
.table-editor-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.table-editor-dialog {
  background-color: white;
  border-radius: 5px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.dialog-header {
  padding: 15px 20px;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.3s ease;
}

.close-btn:hover {
  background-color: #f5f5f5;
}

.dialog-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.record-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group label {
  font-weight: bold;
  color: #555;
  display: flex;
  align-items: center;
  gap: 5px;
}

.required-mark {
  color: #f44336;
  font-weight: bold;
}

.readonly-mark {
  color: #999;
  font-size: 0.8rem;
  font-weight: normal;
}

.form-group input {
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 3px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #2196F3;
}

.form-group input[readonly] {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.dialog-footer {
  padding: 15px 20px;
  border-top: 1px solid #ddd;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.cancel-btn, .save-btn {
  padding: 8px 15px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.3s ease;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #333;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}

.save-btn {
  background-color: #4CAF50;
  color: white;
}

.save-btn:hover {
  background-color: #45a049;
}
</style>