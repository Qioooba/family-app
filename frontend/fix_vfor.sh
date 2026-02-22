#!/bin/bash
# 修复v-for缺少key的脚本

cd /Volumes/document/Projects/family-app/frontend

# 定义修复函数
fix_vfor() {
  local file=$1
  echo "Fixing $file..."
  
  # 使用sed修复常见的v-for模式
  # 模式1: v-for="item in list" -> v-for="(item, index) in list" :key="index"
  sed -i '' -E 's/v-for="([a-zA-Z_]+) in ([a-zA-Z_.]+)"/v-for="(\1, index) in \2" :key="index"/g' "$file"
  
  # 模式2: v-for="(item, idx) in list" 没有key -> 添加:key
  sed -i '' -E 's/v-for="\(([a-zA-Z_]+), ([a-zA-Z_]+)\) in ([a-zA-Z_.]+)"([^:][^k][^e][^y]|$)/v-for="(\1, \2) in \3" :key="\2"/g' "$file"
}

# 要修复的文件列表
files=(
  "src/pages/ai/voice-input.vue"
  "src/pages/ai/shopping-assistant.vue"
  "src/pages/ai/night-summary.vue"
  "src/pages/ai/nutritionist.vue"
  "src/pages/ai/voice.vue"
  "src/pages/task/reminder.vue"
  "src/pages/task/quickview.vue"
  "src/pages/task/repeat.vue"
  "src/pages/task/create.vue"
  "src/pages/task/dependency.vue"
  "src/pages/task/drag-sort.vue"
  "src/pages/task/mood.vue"
  "src/pages/task/i18n.vue"
  "src/pages/task/timeline.vue"
  "src/pages/task/schedule.vue"
  "src/pages/task/board.vue"
  "src/pages/task/gantt.vue"
  "src/pages/task/heatmap.vue"
  "src/pages/task/detail.vue"
  "src/pages/task/voice.vue"
  "src/pages/food/index.vue"
  "src/pages/food/record.vue"
  "src/pages/nutrition/index.vue"
  "src/pages/nutrition/detail.vue"
)

for file in "${files[@]}"; do
  if [ -f "$file" ]; then
    fix_vfor "$file"
  else
    echo "File not found: $file"
  fi
done

echo "Done!"
