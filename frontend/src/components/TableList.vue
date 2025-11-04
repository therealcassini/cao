<template>
  <div class="table-list">
    <div class="table-grid">
      <div 
        v-for="table in tables" 
        :key="table.name"
        class="table-item"
        @click="handleSelect(table)"
      >
        <div class="table-name">{{ table.desc || table.name }}</div>
        <div class="table-original-name">{{ table.name }}</div>
        <button 
          class="view-data-btn"
          @click.stop="handleLoadData(table.name)"
        >
          查看数据
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TableList',
  props: {
    tables: {
      type: Array,
      default: () => []
    }
  },
  emits: ['select-table', 'load-data'],
  methods: {
    handleSelect(table) {
      this.$emit('select-table', table);
    },
    handleLoadData(tableName) {
      this.$emit('select-table', { name: tableName });
      this.$emit('load-data', tableName);
    }
  }
};
</script>

<style scoped>
.table-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.table-item {
  background-color: #fff;
  padding: 15px;
  border-radius: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.table-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.table-name {
  font-weight: bold;
  color: #333;
  font-size: 1rem;
}

.table-original-name {
  color: #666;
  font-size: 0.85rem;
  font-style: italic;
}

.view-data-btn {
  background-color: #2196F3;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 3px;
  cursor: pointer;
  font-size: 0.8rem;
  margin-top: 10px;
  transition: background-color 0.3s ease;
  align-self: flex-start;
}

.view-data-btn:hover {
  background-color: #0b7dda;
}
</style>