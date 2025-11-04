<template>
  <div class="database-list">
    <div class="database-grid">
      <div 
        v-for="database in databases" 
        :key="database.name"
        class="database-item"
        @click="handleSelect(database)"
      >
        <div class="database-name">{{ database.name }}</div>
        <button 
          class="load-tables-btn"
          @click.stop="handleLoadTables(database.name)"
        >
          查看表
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DatabaseList',
  props: {
    databases: {
      type: Array,
      default: () => []
    }
  },
  emits: ['select-database', 'load-tables'],
  methods: {
    handleSelect(database) {
      this.$emit('select-database', database);
    },
    handleLoadTables(databaseName) {
      this.$emit('select-database', { name: databaseName });
      this.$emit('load-tables', databaseName);
    }
  }
};
</script>

<style scoped>
.database-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.database-item {
  background-color: #fff;
  padding: 20px;
  border-radius: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.database-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.database-name {
  font-weight: bold;
  color: #333;
}

.load-tables-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 3px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: background-color 0.3s ease;
}

.load-tables-btn:hover {
  background-color: #45a049;
}
</style>