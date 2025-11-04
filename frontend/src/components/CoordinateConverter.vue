<template>
  <div class="coordinate-converter">
    <h2 style="margin-bottom: 20px;">经纬度转换工具</h2>
    
    <div class="input-section">
      <label for="input-coordinates" style="display: block; margin-bottom: 8px; font-weight: 500;">输入经纬度数据：</label>
      <textarea
        id="input-coordinates"
        v-model="inputCoordinates"
        placeholder="请输入经纬度数据，支持多种格式：\n东经102°37′34″北纬28°20′52″\n东经103.1955841 北纬27.6631839\n东经102°50′49.7508″\n北纬27°31′58.0836″\n东经100.964375E\n北纬28.0917N"
        rows="10"
        style="width: 100%; padding: 12px; border: 1px solid #d9d9d9; border-radius: 6px; font-size: 14px; resize: vertical;"
      ></textarea>
    </div>
    
    <div style="margin: 20px 0;">
      <button
        @click="convertCoordinates"
        style="padding: 10px 24px; background-color: #1890ff; color: white; border: none; border-radius: 6px; font-size: 16px; cursor: pointer;"
      >
        转换
      </button>
      <button
        @click="clearAll"
        style="margin-left: 12px; padding: 10px 24px; background-color: #f5f5f5; color: #333; border: 1px solid #d9d9d9; border-radius: 6px; font-size: 16px; cursor: pointer;"
      >
        清空
      </button>
    </div>
    
    <div class="output-section">
      <label for="output-coordinates" style="display: block; margin-bottom: 8px; font-weight: 500;">转换结果：</label>
      <textarea
        id="output-coordinates"
        v-model="outputCoordinates"
        placeholder="转换后的经纬度将显示在这里..."
        rows="10"
        style="width: 100%; padding: 12px; border: 1px solid #d9d9d9; border-radius: 6px; font-size: 14px; resize: vertical; background-color: #fafafa;"
        readonly
      ></textarea>
    </div>
    
    <div class="conversion-info" v-if="conversionCount > 0">
      <p style="color: #52c41a; margin-top: 12px;">成功转换了 {{ conversionCount }} 个坐标</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CoordinateConverter',
  data() {
    return {
      inputCoordinates: '',
      outputCoordinates: '',
      conversionCount: 0
    };
  },
  methods: {
    convertCoordinates() {
      if (!this.inputCoordinates.trim()) {
        this.outputCoordinates = '请输入经纬度数据！';
        this.conversionCount = 0;
        return;
      }
      
      const lines = this.inputCoordinates.trim().split('\n');
      const results = [];
      let count = 0;
      
      lines.forEach(line => {
        const trimmedLine = line.trim();
        if (!trimmedLine) return;
        
        // 处理一行中包含经纬度的情况
        if (trimmedLine.includes('经') && trimmedLine.includes('纬')) {
          // 处理类似 "东经102°37′34″北纬28°20′52″" 的格式
          const eastResult = this.extractAndConvertLatLng(trimmedLine, /东\s*经\s*([\d\.°′″'"]+)/, 'E');
          const northResult = this.extractAndConvertLatLng(trimmedLine, /北\s*纬\s*([\d\.°′″'"]+)/, 'N');
          
          if (eastResult !== null && northResult !== null) {
            results.push(`经度: ${eastResult.toFixed(6)}, 纬度: ${northResult.toFixed(6)}`);
            count += 2;
          } else {
            results.push(`无法解析: ${trimmedLine}`);
          }
        } else if (trimmedLine.includes('经')) {
          // 只包含经度
          const eastResult = this.extractAndConvertLatLng(trimmedLine, /(?:东|西)\s*经\s*([\d\.°′″'"]+)/, 'E');
          const decimalResult = this.extractDecimalDegree(trimmedLine, 'E');
          
          if (eastResult !== null) {
            results.push(`经度: ${eastResult.toFixed(6)}`);
            count++;
          } else if (decimalResult !== null) {
            results.push(`经度: ${decimalResult.toFixed(6)}`);
            count++;
          } else {
            results.push(`无法解析经度: ${trimmedLine}`);
          }
        } else if (trimmedLine.includes('纬')) {
          // 只包含纬度
          const northResult = this.extractAndConvertLatLng(trimmedLine, /(?:北|南)\s*纬\s*([\d\.°′″'"]+)/, 'N');
          const decimalResult = this.extractDecimalDegree(trimmedLine, 'N');
          
          if (northResult !== null) {
            results.push(`纬度: ${northResult.toFixed(6)}`);
            count++;
          } else if (decimalResult !== null) {
            results.push(`纬度: ${decimalResult.toFixed(6)}`);
            count++;
          } else {
            results.push(`无法解析纬度: ${trimmedLine}`);
          }
        } else if (trimmedLine.includes('E') || trimmedLine.includes('W')) {
          // 处理类似 "100.964375E" 的格式
          const decimalResult = this.extractDecimalDegree(trimmedLine, 'E');
          if (decimalResult !== null) {
            results.push(`经度: ${decimalResult.toFixed(6)}`);
            count++;
          } else {
            results.push(`无法解析: ${trimmedLine}`);
          }
        } else if (trimmedLine.includes('N') || trimmedLine.includes('S')) {
          // 处理类似 "28.0917N" 的格式
          const decimalResult = this.extractDecimalDegree(trimmedLine, 'N');
          if (decimalResult !== null) {
            results.push(`纬度: ${decimalResult.toFixed(6)}`);
            count++;
          } else {
            results.push(`无法解析: ${trimmedLine}`);
          }
        } else if (/^\s*[\d\.]+\s+[\d\.]+\s*$/.test(trimmedLine)) {
          // 处理类似 "103.1955841 27.6631839" 的格式
          const parts = trimmedLine.trim().split(/\s+/);
          if (parts.length >= 2) {
            const lon = parseFloat(parts[0]);
            const lat = parseFloat(parts[1]);
            if (!isNaN(lon) && !isNaN(lat)) {
              results.push(`经度: ${lon.toFixed(6)}, 纬度: ${lat.toFixed(6)}`);
              count += 2;
            } else {
              results.push(`无法解析: ${trimmedLine}`);
            }
          }
        }
      });
      
      this.outputCoordinates = results.join('\n');
      this.conversionCount = count;
    },
    
    extractAndConvertLatLng(text, regex, type) {
      const match = text.match(regex);
      if (!match || !match[1]) return null;
      
      const coordStr = match[1];
      // 处理度分秒格式
      const dmsMatch = coordStr.match(/(\d+)°\s*(\d+)[′']\s*([\d\.]+)[″"]?/);
      if (dmsMatch) {
        const degrees = parseFloat(dmsMatch[1]);
        const minutes = parseFloat(dmsMatch[2]);
        const seconds = parseFloat(dmsMatch[3]);
        
        if (!isNaN(degrees) && !isNaN(minutes) && !isNaN(seconds)) {
          let decimalDegrees = degrees + minutes / 60 + seconds / 3600;
          // 根据方向调整正负
          if (text.includes('西') || text.includes('南')) {
            decimalDegrees = -decimalDegrees;
          }
          return decimalDegrees;
        }
      }
      
      // 处理度分格式
      const dmMatch = coordStr.match(/(\d+)°\s*(\d+\.\d+)[′']?/);
      if (dmMatch) {
        const degrees = parseFloat(dmMatch[1]);
        const minutes = parseFloat(dmMatch[2]);
        
        if (!isNaN(degrees) && !isNaN(minutes)) {
          let decimalDegrees = degrees + minutes / 60;
          if (text.includes('西') || text.includes('南')) {
            decimalDegrees = -decimalDegrees;
          }
          return decimalDegrees;
        }
      }
      
      // 处理度格式
      const dMatch = coordStr.match(/(\d+\.\d+)°/);
      if (dMatch) {
        const degrees = parseFloat(dMatch[1]);
        if (!isNaN(degrees)) {
          if (text.includes('西') || text.includes('南')) {
            return -degrees;
          }
          return degrees;
        }
      }
      
      return null;
    },
    
    extractDecimalDegree(text, type) {
      // 提取带方向标识的小数度
      const pattern = type === 'E' ? /([\d\.]+)[EW]/i : /([\d\.]+)[NS]/i;
      const match = text.match(pattern);
      if (match && match[1]) {
        let value = parseFloat(match[1]);
        if (!isNaN(value)) {
          // 处理西经和南纬的负值
          if (text.match(/[WS]/i)) {
            value = -value;
          }
          return value;
        }
      }
      
      // 尝试提取纯数字
      const numMatch = text.match(/^\s*([\d\.]+)\s*$/);
      if (numMatch && numMatch[1]) {
        const value = parseFloat(numMatch[1]);
        if (!isNaN(value)) {
          return value;
        }
      }
      
      return null;
    },
    
    clearAll() {
      this.inputCoordinates = '';
      this.outputCoordinates = '';
      this.conversionCount = 0;
    }
  }
};
</script>

<style scoped>
.coordinate-converter {
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.input-section,
.output-section {
  margin-bottom: 16px;
}

button:hover {
  opacity: 0.8;
}

button:active {
  opacity: 0.9;
}
</style>