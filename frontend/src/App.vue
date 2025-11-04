<script setup>
import { ref, provide } from 'vue';
import EChartsGraph from './components/EChartsGraph.vue';
import DatabaseManager from './components/DatabaseManager.vue';
import TableDataManager from './components/TableDataManager.vue';
import SearchConfigManager from './components/SearchConfigManager.vue';
import GraphManager from './components/GraphManager.vue';
import MySqlToPgConverter from './components/MySqlToPgConverter.vue';
import HistoricalTimeline from './components/HistoricalTimeline.vue';
import CoordinateConverter from './components/CoordinateConverter.vue';
import { Tabs, Layout, Typography } from 'ant-design-vue';

const { Header, Content, Footer } = Layout;
const { Title, Text } = Typography;
const { TabPane } = Tabs;

const activeTab = ref('database');

const switchTab = (tab) => {
  activeTab.value = tab;
};

// 提供切换标签页的方法给子组件
provide('switchTab', switchTab);
</script>

<template>
  <Layout class="app-layout">
    <Header>
      <Title :level="3" style="color: white; margin: 0; line-height: 64px;">全域数据治理</Title>
    </Header>
    <Content style="padding: 24px; min-height: 280px;">
      <Tabs 
          v-model:activeKey="activeTab"
          @change="switchTab"
          type="card"
          style="width: 100%"
        >
        <TabPane tab="数据库管理" key="database">
          <DatabaseManager />
        </TabPane>
        <TabPane  tab="数据表管理"  key="table-data" >
          <TableDataManager />
        </TabPane>
        <TabPane tab="搜索配置" key="search-config">
          <SearchConfigManager />
        </TabPane>
        <TabPane tab="SQL转换工具" key="sql-converter">
          <MySqlToPgConverter />
        </TabPane>
        <TabPane tab="经纬度转换" key="coordinate-converter">
          <CoordinateConverter />
        </TabPane>
                <TabPane tab="图谱可视化" key="graph">
          <EChartsGraph />
        </TabPane>
        <TabPane tab="图谱管理" key="graph-manager">
          <GraphManager />
        </TabPane>
        <TabPane tab="历史人物时间轴" key="historical-timeline">
          <HistoricalTimeline />
        </TabPane>
      </Tabs>
    </Content>
    <Footer style="text-align: center;">
      <Text type="secondary">全域数据治理 - 基于Spring Boot和Vue3开发</Text>
    </Footer>
  </Layout>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
}

/* 自定义样式以增强Ant Design组件 */
:deep(.ant-layout-header) {
  background-color: #4285F4;
  padding: 0 24px;
}

:deep(.ant-tabs-card) {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
}

:deep(.ant-tabs-card .ant-tabs-content) {
  padding: 24px;
}

/* 解决下拉框显示问题的全局样式 */
:deep(.ant-select-dropdown) {
  color: #333;
  background-color: white;
}

:deep(.ant-select-item) {
  color: #333;
}

:deep(.ant-select-item-option-selected) {
  background-color: #e6f7ff;
  color: #1890ff;
}
</style>
