<template>
  <div class="historical-timeline">
    <!-- 年龄柱状图 - 移到时间选择器上方 -->
    <div class="panel age-chart-container">
      <div id="age-chart" style="width: 100%; height: 500px;"></div>
    </div>
    
    <!-- 年份控制器 -->
    <div class="year-controls">
      <h3>年份选择: {{ currentYear }}</h3>
      <input 
        type="range" 
        v-model="currentYear" 
        :min="minYear" 
        :max="maxYear" 
        class="year-slider"
        @input="handleYearChange"
        @mousedown="stopAutoPlay"
      />
      <div class="year-range">
        <span>{{ minYear }}</span>
        <button 
          class="play-button" 
          @click="toggleAutoPlay"
          :class="{ 'playing': isAutoPlaying }"
        >
          {{ isAutoPlaying ? '暂停' : '播放' }}
        </button>
        <span>{{ maxYear }}</span>
      </div>
    </div>
    
    <!-- 主要内容区域 - 使用Tab形式 -->
    <div class="tab-container">
      <div class="tab-header">
        <div 
          class="tab" 
          :class="{ active: activeTab === 'persons' }"
          @click="activeTab = 'persons'"
        >
          历史人物 ({{ alivePersonsInCurrentYear.length }})
        </div>
        <div 
          class="tab" 
          :class="{ active: activeTab === 'events' }"
          @click="activeTab = 'events'"
        >
          历史事件 ({{ currentYearEvents.length }})
        </div>
        <div 
          class="tab" 
          :class="{ active: activeTab === 'lifespan' }"
          @click="activeTab = 'lifespan'"
        >
          寿命分布 ({{ historicalPersons.length }})
        </div>
      </div>
      
      <div class="tab-content">
        <!-- 历史人物列表 - 占满整行 -->
        <div v-if="activeTab === 'persons'" class="panel full-width">
          <div class="person-list">
            <div 
              v-for="person in alivePersonsInCurrentYear" 
              :key="person.id" 
              class="person-item"
              :class="{ 'is-alive': isPersonAlive(person), 'selected': selectedPerson === person.name }"
              @click="selectedPerson = selectedPerson === person.name ? '' : person.name"
            >
              <div class="person-name">{{ person.name }}</div>
              <div class="person-info">
                <div class="person-years">出生日期: {{ formatDate(person.birth_date) }}</div>
                <div class="person-years" v-if="person.death_date">逝世日期: {{ formatDate(person.death_date) }}</div>
                <div class="person-years" v-else>状态: 在世</div>
                <div class="person-age">
                  {{ currentYear }}年: {{ calculateAge(person, currentYear) }}岁 (寿命: {{ person.life_span || '-' }}岁)
                </div>
                <div class="person-bio">简介: {{ person.bio || '暂无简介信息' }}</div>
              </div>
            </div>
            <div v-if="alivePersonsInCurrentYear.length === 0" class="no-persons">
              该年份暂无在世人物
            </div>
          </div>
        </div>
        
        <!-- 历史事件列表 - 占满整行 -->
        <div v-if="activeTab === 'events'" class="panel full-width">
          <div class="events-list" v-if="currentYearEvents.length > 0">
              <div 
                v-for="event in currentYearEvents" 
                :key="event.id" 
                class="event-item"
              >
                <div class="event-date">{{ event.date || `${currentYear}年` }}</div>
                <div class="event-name">{{ event.name }}</div>
                <div class="event-description">{{ event.description }}</div>
                <div class="event-participants">
                  相关人物: 
                  <Tag 
                    v-for="person in event.participants" 
                    :key="person.name" 
                    class="participant-tag"
                    @click="selectedPerson = person.name"
                  >
                    {{ person.name }}
                  </Tag>
                </div>
              </div>
            </div>
            <div class="no-events" v-else>
              该年份暂无历史事件记录
            </div>
        </div>
        
        <!-- 寿命分布气泡图 - 占满整行 -->
        <div v-if="activeTab === 'lifespan'" class="panel full-width">
          <div id="lifespan-chart" style="width: 100%; height: 600px;"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onMounted as vueOnMounted, nextTick } from 'vue';
import { Tag } from 'ant-design-vue';
import { timelineAPI } from '../api/timelineService';
import * as echarts from 'echarts';

// 状态管理
const currentYear = ref(1900);
const minYear = ref(1800);
const maxYear = ref(2025);
const historicalPersons = ref([]);
const allHistoricalEvents = ref([]); // 存储所有历史事件
const eventsCache = ref([]); // 事件数据缓存（现在存储所有事件）
const cacheTimestamp = ref(0); // 缓存时间戳
const CACHE_DURATION = 30 * 60 * 1000; // 缓存有效期：30分钟
const selectedPerson = ref('');
const activeTab = ref('persons'); // Tab状态
let ageChart = null;
let lifespanChart = null; // 寿命分布图表
let autoPlayTimer = null; // 自动播放定时器
let isAutoPlaying = ref(true); // 是否自动播放
let isEventsLoaded = ref(false); // 标记事件数据是否已加载

// 计算属性：当前年份的事件
const currentYearEvents = computed(() => {
  // 从所有事件中过滤出当前年份的事件
  return allHistoricalEvents.value.filter(event => {
    const eventYear = new Date(event.date || event.start_date).getFullYear();
    return eventYear === currentYear.value;
  });
});

// 计算属性：当前年份在世的人物
const alivePersonsInCurrentYear = computed(() => {
  return historicalPersons.value.filter(person => isPersonAliveInYear(person, currentYear.value));
});

// 计算属性：年龄图表数据
const ageChartData = computed(() => {
  const data = alivePersonsInCurrentYear.value.map(person => {
    const age = calculateAge(person, currentYear.value);
    return {
      name: person.name,
      value: age || 0,
      birthYear: person.birth_date ? formatYear(person.birth_date) : '',
      deathYear: person.death_date ? formatYear(person.death_date) : '至今'
    };
  });
  // 过滤掉10岁以下的节点，然后按年龄从大到小排序
  return data.filter(item => item.value >= 10).sort((a, b) => b.value - a.value);
});

// 计算属性：寿命气泡图数据
const lifespanChartData = computed(() => {
  return historicalPersons.value.map((person, index) => {
    const birthYear = person.birth_date ? new Date(person.birth_date).getFullYear() : null;
    const deathYear = person.death_date ? new Date(person.death_date).getFullYear() : 
                     (new Date().getFullYear() > maxYear.value ? maxYear.value : new Date().getFullYear());
    const lifespan = birthYear ? deathYear - birthYear : 0;
    
    return {
      name: person.name,
      value: [
        index, // x轴：简单使用索引
        lifespan, // y轴：寿命
        lifespan // 气泡大小：与寿命成正比
      ],
      birthYear: birthYear,
      deathYear: person.death_date ? deathYear : '至今',
      lifespan: lifespan
    };
  });
});

// 加载数据
const loadData = async () => {
  try {
    console.log('尝试从后端加载数据...');
    // 尝试从后端加载数据
    const personsData = await timelineAPI.getPersons();
    
    console.log('API返回的人物数据:', personsData);
    
    // 处理人物数据 - 优先使用后端数据，即使为空数组
    if (Array.isArray(personsData)) {
      historicalPersons.value = personsData;
      console.log('已使用后端返回的人物数据，数量:', personsData.length);
    } else {
      // 只有在数据格式错误时才使用模拟数据
      console.log('后端返回的人物数据格式错误，使用模拟数据');
      historicalPersons.value = getMockPersons();
    }
    
    // 检查缓存是否有效
    if (isCacheValid()) {
      console.log('使用缓存的事件数据');
      allHistoricalEvents.value = eventsCache.value;
      isEventsLoaded.value = true;
    } else {
      console.log('缓存无效或不存在，加载所有事件数据');
      await loadAllEvents();
    }
    
    // 更新年份范围
    updateYearRange();
    
  } catch (error) {
    console.log('使用模拟数据:', error.message);
    // 使用模拟数据
    historicalPersons.value = getMockPersons();
    
    // 转换模拟数据格式并设置到全局事件数组
    const mockEvents = getMockEvents();
    const allMockEvents = [];
    Object.keys(mockEvents).forEach(year => {
      const yearEvents = mockEvents[year].map(event => ({
        ...event,
        year: parseInt(year)
      }));
      allMockEvents.push(...yearEvents);
    });
    
    allHistoricalEvents.value = allMockEvents;
    eventsCache.value = allMockEvents;
    cacheTimestamp.value = Date.now();
    isEventsLoaded.value = true;
    
    updateYearRange();
  }
};

// 检查缓存是否有效
const isCacheValid = () => {
  // 如果缓存时间戳为0，表示还没有缓存数据
  if (cacheTimestamp.value === 0) return false;
  
  const now = Date.now();
  const isValid = now - cacheTimestamp.value < CACHE_DURATION;
  console.log(`缓存状态: ${isValid ? '有效' : '过期'}，已缓存 ${(now - cacheTimestamp.value) / 1000} 秒`);
  return isValid;
};

// 清除过期缓存
const clearExpiredCache = () => {
  if (!isCacheValid()) {
    console.log('缓存已过期，清除缓存');
    eventsCache.value = [];
    cacheTimestamp.value = 0;
    isEventsLoaded.value = false;
  }
};

// 模拟人物数据 (适配数据库表结构)
const getMockPersons = () => {
  return [
    { id: 1, name: '孙中山', birth_date: '1866-01-01', death_date: '1925-03-12', bio: '中国近代民主革命家', life_span: 59 },
    { id: 2, name: '毛泽东', birth_date: '1893-12-26', death_date: '1976-09-09', bio: '中华人民共和国开国领袖', life_span: 83 },
    { id: 3, name: '周恩来', birth_date: '1898-03-05', death_date: '1976-01-08', bio: '中国革命家、政治家、军事家', life_span: 78 },
    { id: 4, name: '邓小平', birth_date: '1904-08-22', death_date: '1997-02-19', bio: '中国改革开放的总设计师', life_span: 93 },
    { id: 5, name: '鲁迅', birth_date: '1881-09-25', death_date: '1936-10-19', bio: '中国现代文学家、思想家', life_span: 55 },
    { id: 6, name: '李大钊', birth_date: '1889-10-29', death_date: '1927-04-28', bio: '中国共产主义运动的先驱', life_span: 38 },
    { id: 7, name: '陈独秀', birth_date: '1879-10-09', death_date: '1942-05-27', bio: '新文化运动领袖，中国共产党早期领导人', life_span: 63 }
  ];
};

// 模拟事件数据 (适配数据库表结构)
const getMockEvents = () => {
  return {
    1911: [
      { id: 1, event_name: '辛亥革命', start_date: '1911-10-10', end_date: '1912-02-12', event_category: '政治', description: '推翻清朝统治的资产阶级民主革命', create_time: new Date(), update_time: new Date() }
    ],
    1919: [
      { id: 2, event_name: '五四运动', start_date: '1919-05-04', end_date: '1919-06-28', event_category: '社会', description: '中国人民彻底的反对帝国主义、封建主义的爱国运动', create_time: new Date(), update_time: new Date() }
    ],
    1921: [
      { id: 3, event_name: '中国共产党成立', start_date: '1921-07-23', end_date: '1921-07-31', event_category: '政治', description: '中国共产党第一次全国代表大会召开', create_time: new Date(), update_time: new Date() }
    ],
    1927: [
      { id: 4, event_name: '南昌起义', start_date: '1927-08-01', end_date: '1927-08-01', event_category: '战争', description: '打响了武装反抗国民党反动派的第一枪', create_time: new Date(), update_time: new Date() }
    ],
    1934: [
      { id: 5, event_name: '长征开始', start_date: '1934-10-10', end_date: '1936-10-22', event_category: '战争', description: '中国工农红军开始战略性转移', create_time: new Date(), update_time: new Date() }
    ],
    1937: [
      { id: 6, event_name: '抗日战争爆发', start_date: '1937-07-07', end_date: '1945-08-15', event_category: '战争', description: '日本发动全面侵华战争', create_time: new Date(), update_time: new Date() }
    ],
    1945: [
      { id: 7, event_name: '抗日战争胜利', start_date: '1945-08-15', end_date: '1945-09-02', event_category: '战争', description: '日本宣布无条件投降', create_time: new Date(), update_time: new Date() }
    ],
    1949: [
      { id: 8, event_name: '中华人民共和国成立', start_date: '1949-10-01', end_date: '1949-10-01', event_category: '政治', description: '毛泽东在天安门城楼上宣布中华人民共和国成立', create_time: new Date(), update_time: new Date() }
    ],
    1978: [
      { id: 9, event_name: '十一届三中全会', start_date: '1978-12-18', end_date: '1978-12-22', event_category: '政治', description: '确立改革开放的重大决策', create_time: new Date(), update_time: new Date() }
    ],
    1992: [
      { id: 10, event_name: '邓小平南方谈话', start_date: '1992-01-18', end_date: '1992-02-21', event_category: '政治', description: '推动改革开放和现代化建设进入新阶段', create_time: new Date(), update_time: new Date() }
    ]
  };
};

// 更新年份范围
const updateYearRange = () => {
  let min = 2025;
  let max = 1800;
  
  // 从人物数据获取年份范围
  historicalPersons.value.forEach(person => {
    if (person.birth_date) {
      const birthYear = new Date(person.birth_date).getFullYear();
      if (birthYear < min) min = birthYear;
    }
    if (person.death_date) {
      const deathYear = new Date(person.death_date).getFullYear();
      if (deathYear > max) max = deathYear;
    }
  });
  
  // 从事件数据获取年份范围
  allHistoricalEvents.value.forEach(event => {
    if (event.start_date || event.date) {
      const eventYear = new Date(event.start_date || event.date).getFullYear();
      if (eventYear < min) min = eventYear;
      if (eventYear > max) max = eventYear;
    }
  });
  
  minYear.value = min;
  maxYear.value = max;
  
  // 确保当前年份在有效范围内
  if (currentYear.value < min || currentYear.value > max) {
    currentYear.value = Math.floor((min + max) / 2);
  }
};

// 计算人物在特定年份的年龄
const calculateAge = (person, year) => {
  if (!person.birth_date) return null;
  
  const birthYear = new Date(person.birth_date).getFullYear();
  
  // 如果有死亡日期且计算年份超过死亡年份，返回null
  if (person.death_date) {
    const deathYear = new Date(person.death_date).getFullYear();
    if (year > deathYear) {
      return null;
    }
  }
  
  return year - birthYear;
};

// 判断人物是否在世
const isPersonAlive = (person) => {
  return !person.death_date;
};

// 判断人物在特定年份是否在世
const isPersonAliveInYear = (person, year) => {
  if (!person.birth_date) return false;
  
  const birthYear = new Date(person.birth_date).getFullYear();
  if (year < birthYear) return false;
  
  // 如果有死亡日期，判断计算年份是否在死亡年份之前或等于
  if (person.death_date) {
    const deathYear = new Date(person.death_date).getFullYear();
    return year <= deathYear;
  }
  
  return true; // 没有死亡日期，默认在世
};

// 格式化日期为YYYY-MM-DD
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return isNaN(date.getTime()) ? dateStr : date.toISOString().split('T')[0];
};

// 格式化年份为YYYY
const formatYear = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return isNaN(date.getTime()) ? dateStr : date.getFullYear();
};

// 初始化年龄柱状图
const initAgeChart = () => {
  if (!ageChart) {
    const chartDom = document.getElementById('age-chart');
    if (chartDom) {
      ageChart = echarts.init(chartDom);
    }
  }
  updateAgeChart();
};

// 更新年龄柱状图
const updateAgeChart = () => {
  if (!ageChart) return;
  
  const chartData = ageChartData.value;
  
  const option = {
    title: {
      text: `${currentYear.value}年历史人物年龄分布`,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        const data = params[0];
        const personData = chartData.find(d => d.name === data.name);
        return `${data.name}<br/>
                年龄: ${data.value}岁<br/>
                生卒年: ${personData.birthYear}-${personData.deathYear}`;
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%', // 增加底部空间显示滚动条
      top: '15%',
      containLabel: true
    },
    dataZoom: [
      {
        type: 'slider',
        show: true,
        xAxisIndex: [0],
        start: 0,
        end: Math.min(100, 40 / chartData.length * 100), // 初始显示40个数据
        handleStyle: {
          color: '#1890ff'
        },
        textStyle: {
          color: '#666'
        }
      },
      {
        type: 'inside',
        xAxisIndex: [0],
        start: 0,
        end: Math.min(100, 40 / chartData.length * 100)
      }
    ],
    xAxis: {
      type: 'category',
      data: chartData.map(d => d.name),
      axisLabel: {
        rotate: 0, // 不旋转标签以便显示更多数据
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '年龄',
      min: 0,
      max: 120,
      interval: 20
    },
    series: [{
      name: '年龄',
      type: 'bar',
      data: chartData.map(d => d.value),
      itemStyle: {
        color: '#1890ff'
      },
      barWidth: 20,
      barGap: '0%',
      barCategoryGap: '10%',
      label: {
        show: true,
        position: 'top',
        formatter: '{c}岁'
      }
    }]
  };
  
  ageChart.setOption(option);
};

// 监听数据变化，更新图表
watch(() => currentYear.value, () => {
  nextTick(() => {
    updateAgeChart();
  });
});

// 监听Tab切换，初始化或更新寿命图表
watch(() => activeTab.value, (newTab, oldTab) => {
  nextTick(() => {
    if (newTab === 'lifespan') {
      // 强制重新渲染，确保DOM元素可用
      setTimeout(() => {
        initLifespanChart();
      }, 100); // 短暂延迟确保DOM已经更新
    }
  });
});

watch(() => historicalPersons.value, () => {
  nextTick(() => {
    updateAgeChart();
  });
}, { deep: true });

// 处理窗口大小变化
const handleResize = () => {
  if (ageChart) {
    ageChart.resize();
  }
  if (lifespanChart) {
    lifespanChart.resize();
  }
};

// 处理年份变化
const handleYearChange = () => {
  // 重置选中状态
  selectedPerson.value = '';
  // 用户交互后停止自动播放
  stopAutoPlay();
  
  // 不需要再按年份加载事件，前端会自动过滤
  console.log(`切换到${currentYear.value}年，从已加载的${allHistoricalEvents.value.length}个事件中过滤`);
};

// 加载所有事件数据
const loadAllEvents = async () => {
  try {
    console.log('尝试加载所有历史事件数据...');
    
    // 直接使用getEvents方法，因为timelineAPI已经定义了这个方法
    let allEventsData;
    
    try {
      console.log('使用getEvents获取所有事件数据');
      allEventsData = await timelineAPI.getEvents();
      console.log('API返回的所有事件数据数量:', allEventsData?.length || 0);
    } catch (e) {
      // 即使API调用失败，timelineAPI.getEvents也会返回空数组，这里是额外保障
      console.error('getEvents调用失败:', e);
      allEventsData = [];
    }
    
    // 即使获取到空数组也正常处理，不再判断长度
    if (Array.isArray(allEventsData)) {
      // 处理并规范化所有事件数据
      const normalizedEvents = allEventsData.map(event => ({
        id: event.id,
        name: event.event_name || event.name || '未命名事件',
        date: event.start_date || event.date || '1900-01-01',
        description: event.description || '暂无描述',
        participants: event.participants || [],
        category: event.event_category || '其他'
      }));
      
      // 检查是否有有效数据，如果没有，使用模拟数据
      if (normalizedEvents.length === 0) {
        console.log('API返回的数据为空，使用模拟数据');
        const mockEvents = getMockEvents();
        const allMockEvents = [];
        Object.keys(mockEvents).forEach(year => {
          const yearEvents = mockEvents[year].map(event => ({
            id: event.id,
            name: event.event_name || event.name || '未命名事件',
            date: event.start_date || `${year}-01-01`,
            description: event.description || '暂无描述',
            participants: [],
            category: event.event_category || '其他',
            year: parseInt(year)
          }));
          allMockEvents.push(...yearEvents);
        });
        
        allHistoricalEvents.value = allMockEvents;
        eventsCache.value = allMockEvents;
        console.log('已使用模拟事件数据，共', allMockEvents.length, '个事件');
      } else {
        // 更新全局事件数据并缓存
        allHistoricalEvents.value = normalizedEvents;
        eventsCache.value = normalizedEvents;
        console.log(`已加载并缓存所有事件数据，共${normalizedEvents.length}个事件`);
      }
      
      cacheTimestamp.value = Date.now();
      isEventsLoaded.value = true;
    } else {
      // 数据格式错误时使用模拟数据
      console.log('API返回的数据格式错误，使用模拟数据');
      const mockEvents = getMockEvents();
      const allMockEvents = [];
      Object.keys(mockEvents).forEach(year => {
        const yearEvents = mockEvents[year].map(event => ({
          id: event.id,
          name: event.event_name || event.name || '未命名事件',
          date: event.start_date || `${year}-01-01`,
          description: event.description || '暂无描述',
          participants: [],
          category: event.event_category || '其他',
          year: parseInt(year)
        }));
        allMockEvents.push(...yearEvents);
      });
      
      allHistoricalEvents.value = allMockEvents;
      eventsCache.value = allMockEvents;
      cacheTimestamp.value = Date.now();
      isEventsLoaded.value = true;
    }
  } catch (error) {
    console.error('加载所有事件数据发生异常:', error);
    
    // 发生任何异常时，使用模拟数据
    console.log('使用模拟事件数据');
    const mockEvents = getMockEvents();
    const allMockEvents = [];
    Object.keys(mockEvents).forEach(year => {
      const yearEvents = mockEvents[year].map(event => ({
        id: event.id,
        name: event.event_name || event.name || '未命名事件',
        date: event.start_date || `${year}-01-01`,
        description: event.description || '暂无描述',
        participants: [],
        category: event.event_category || '其他',
        year: parseInt(year)
      }));
      allMockEvents.push(...yearEvents);
    });
    
    allHistoricalEvents.value = allMockEvents;
    eventsCache.value = allMockEvents;
    cacheTimestamp.value = Date.now();
    isEventsLoaded.value = true;
  }
};

// 预加载附近年份的事件数据 - 现在不再需要，因为已加载所有事件
// const preloadNearbyYears = async () => {
//   // 不再需要预加载，因为我们已经一次性加载了所有事件
// };


// 开始自动播放
const startAutoPlay = () => {
  // 清除可能存在的旧定时器
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer);
    autoPlayTimer = null;
  }
  
  isAutoPlaying.value = true;
  
  // 保存当前年份作为起始点，确保从当前位置继续
  const currentStartYear = currentYear.value;
  
  autoPlayTimer = setInterval(() => {
    if (isAutoPlaying.value) {
      // 自动增加年份，到达最大值后重置到最小值
      currentYear.value = currentYear.value >= maxYear.value ? minYear.value : currentYear.value + 1;
    }
  }, 1000); // 每秒移动一次
};

// 停止自动播放
const stopAutoPlay = () => {
  isAutoPlaying.value = false;
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer);
    autoPlayTimer = null;
  }
};

// 切换自动播放状态
const toggleAutoPlay = () => {
  if (isAutoPlaying.value) {
    stopAutoPlay();
  } else {
    startAutoPlay();
  }
};

// 获取事件类型颜色
const getEventColor = (category) => {
  const colorMap = {
    '政治': 'red',
    '文化': 'blue',
    '科技': 'green',
    '战争': 'purple',
    '经济': 'orange',
    '社会': 'cyan',
    '军事': 'magenta',
    '外交': 'lime',
    '教育': 'yellow',
    '体育': 'volcano'
  };
  return colorMap[category] || 'default';
};

// 初始化寿命气泡图
const initLifespanChart = () => {
  // 先销毁旧图表，确保每次都重新初始化
  if (lifespanChart) {
    lifespanChart.dispose();
    lifespanChart = null;
  }
  
  // 重新获取DOM元素并初始化
  const chartDom = document.getElementById('lifespan-chart');
  if (chartDom) {
    lifespanChart = echarts.init(chartDom);
    updateLifespanChart();
  }
};

// 更新寿命气泡图
const updateLifespanChart = () => {
  if (!lifespanChart) return;
  
  const chartData = lifespanChartData.value;
  const maxLifespan = Math.max(...chartData.map(d => d.lifespan), 100);
  
  const option = {
    title: {
      text: '历史人物寿命分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: function(params) {
        const data = params.data;
        return `${data.name}<br/>
                寿命: ${data.lifespan}岁<br/>
                生卒年: ${data.birthYear}-${data.deathYear}`;
      }
    },
    grid: {
      left: '3%',
      right: '7%',
      bottom: '10%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '人物索引',
      splitLine: {
        show: true
      }
    },
    yAxis: {
      type: 'value',
      name: '寿命（岁）',
      min: 0,
      max: maxLifespan * 1.1,
      splitLine: {
        show: true
      }
    },
    series: [{
      name: '寿命分布',
      type: 'scatter',
      symbolSize: function(val) {
        // 气泡大小与寿命成正比，但设置最小和最大值
        const size = val[2] * 1.5;
        return Math.max(10, Math.min(size, 60));
      },
      data: chartData,
      itemStyle: {
        color: function(params) {
          // 根据寿命长短设置不同颜色
          const lifespan = params.value[2];
          if (lifespan >= 90) return '#52c41a'; // 长寿者：绿色
          if (lifespan >= 70) return '#1890ff'; // 中长寿者：蓝色
          if (lifespan >= 50) return '#faad14'; // 中等寿命：橙色
          return '#f5222d'; // 较短寿命：红色
        },
        opacity: 0.8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        position: 'top',
        formatter: function(params) {
          // 只显示姓名，避免气泡过大时文字重叠
          return params.name;
        },
        fontSize: 12
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  };
  
  lifespanChart.setOption(option);
};

// 监听人物数据变化，更新寿命图表
watch(() => historicalPersons.value, () => {
  nextTick(() => {
    if (activeTab.value === 'lifespan') {
      updateLifespanChart();
    }
  });
}, { deep: true });

// 组件挂载时加载数据
vueOnMounted(() => {
  loadData();
  nextTick(() => {
    initAgeChart();
    window.addEventListener('resize', handleResize);
    // 启动自动播放
    startAutoPlay();
  });
});

// 组件卸载时清理
const unmount = () => {
  if (ageChart) {
    ageChart.dispose();
    ageChart = null;
  }
  if (lifespanChart) {
    lifespanChart.dispose();
    lifespanChart = null;
  }
  // 清理定时器
  stopAutoPlay();
  window.removeEventListener('resize', handleResize);
};
</script>

<style scoped>
.historical-timeline {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 128px);
}

h1 {
  color: #333;
  margin-bottom: 30px;
  text-align: center;
  font-size: 28px;
}

h3 {
  color: #555;
  margin-bottom: 15px;
  font-size: 18px;
}

.year-controls {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
  text-align: center;
}

.year-slider {
  width: 80%;
  max-width: none;
  margin: 15px auto;
  cursor: pointer;
}

.year-range {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #666;
  font-size: 14px;
  margin-top: 10px;
}

.play-button {
  padding: 6px 16px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.play-button:hover {
  background-color: #40a9ff;
}

.play-button.playing {
  background-color: #f5222d;
}

.play-button.playing:hover {
  background-color: #ff4d4f;
}

.main-content {
  display: block;
}

.panel {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

/* Tab样式 */
.tab-container {
  margin-top: 20px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tab-header {
  display: flex;
  background-color: #f0f2f5;
  border-bottom: 1px solid #d9d9d9;
}

.tab {
  flex: 1;
  padding: 12px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.tab:hover {
  background-color: #e6f7ff;
}

.tab.active {
  background-color: #1890ff;
  color: white;
  border-bottom: 2px solid #096dd9;
}

.tab-content {
  background-color: white;
  min-height: 400px;
}

.full-width {
  width: 100%;
  margin: 0;
  border-radius: 0;
  box-shadow: none;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .tab {
    padding: 10px 16px;
    font-size: 14px;
  }
  
  .age-chart-container {
    margin-top: 10px;
  }
  
  #age-chart {
    height: 300px !important;
  }
}

.person-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  padding: 0;
}

.person-item {
  width: calc(33.333% - 10px); /* 调整宽度计算，考虑gap间距 */
  box-sizing: border-box;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .person-item {
    width: calc(50% - 7.5px); /* 中等屏幕显示2列 */
  }
}

@media (max-width: 768px) {
  .person-item {
    width: 100%; /* 移动设备显示1列 */
  }
  
  .person-list {
    margin: 0;
  }
}

.person-item {
  padding: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.person-item:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24,144,255,0.1);
}

.person-item.selected {
  border-color: #1890ff;
  background-color: #e6f7ff;
}

.person-item.is-alive {
  border-left: 4px solid #52c41a;
}

.person-name {
  font-weight: bold;
  color: #333;
  font-size: 18px;
  margin-bottom: 8px;
}

.person-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.person-years {
  color: #666;
  font-size: 14px;
  line-height: 1.4;
}

.person-age {
  color: #1890ff;
  font-size: 14px;
  font-style: italic;
  font-weight: 500;
}

.person-bio {
  color: #444;
  font-size: 14px;
  line-height: 1.5;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-top: 4px;
  border-left: 3px solid #1890ff;
}

.age-bar-container {
  width: 100%;
  height: 10px;
  background-color: #f0f0f0;
  border-radius: 5px;
  overflow: hidden;
  margin-top: 5px;
}

.age-bar {
  height: 100%;
  background-color: #1890ff;
  transition: width 0.3s ease;
  border-radius: 5px;
}

.no-persons {
  text-align: center;
  color: #999;
  padding: 40px;
  font-style: italic;
}

.age-chart-container {
  margin-top: 20px;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.event-item {
  padding: 15px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  background-color: #fafafa;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.event-date {
  color: #666;
  font-size: 14px;
}

.event-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
  font-size: 16px;
}

.event-description {
  color: #555;
  line-height: 1.5;
}

.no-events {
  text-align: center;
  color: #999;
  padding: 40px;
  font-style: italic;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }
  
  .panel {
    min-width: 100%;
  }
}
</style>