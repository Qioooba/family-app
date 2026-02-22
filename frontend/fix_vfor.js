const fs = require('fs');
const path = require('path');

const filesToFix = [
  'src/pages/ai/voice-input.vue',
  'src/pages/ai/shopping-assistant.vue',
  'src/pages/ai/night-summary.vue',
  'src/pages/ai/nutritionist.vue',
  'src/pages/ai/voice.vue',
  'src/pages/task/reminder.vue',
  'src/pages/task/quickview.vue',
  'src/pages/task/repeat.vue',
  'src/pages/task/create.vue',
  'src/pages/task/dependency.vue',
  'src/pages/task/drag-sort.vue',
  'src/pages/task/mood.vue',
  'src/pages/task/i18n.vue',
  'src/pages/task/timeline.vue',
  'src/pages/task/schedule.vue',
  'src/pages/task/board.vue',
  'src/pages/task/gantt.vue',
  'src/pages/task/heatmap.vue',
  'src/pages/task/detail.vue',
  'src/pages/task/voice.vue',
  'src/pages/food/index.vue',
  'src/pages/food/record.vue',
  'src/pages/nutrition/index.vue',
  'src/pages/nutrition/detail.vue',
];

function fixVFor(content) {
  // 修复 v-for="item in list" 格式
  content = content.replace(
    /v-for="(\w+) in (\w+)"/g,
    'v-for="($1, index) in $2" :key="$1.id || index"'
  );
  
  // 修复 v-for="(item, index) in list" 但缺少:key的情况
  content = content.replace(
    /v-for="\((\w+), (\w+)\) in ([\w.]+)"(?![^>]*:key)/g,
    'v-for="($1, $2) in $3" :key="$1.id || $2"'
  );
  
  // 修复 v-for="n in number" 格式
  content = content.replace(
    /v-for="(\w+) in (\d+)"/g,
    'v-for="($1, index) in $2" :key="index"'
  );
  
  return content;
}

const baseDir = '/Volumes/document/Projects/family-app/frontend';

filesToFix.forEach(file => {
  const filePath = path.join(baseDir, file);
  if (fs.existsSync(filePath)) {
    console.log(`Fixing ${file}...`);
    let content = fs.readFileSync(filePath, 'utf-8');
    const originalContent = content;
    content = fixVFor(content);
    if (content !== originalContent) {
      fs.writeFileSync(filePath, content);
      console.log(`  ✓ Fixed`);
    } else {
      console.log(`  - No changes needed`);
    }
  } else {
    console.log(`✗ File not found: ${file}`);
  }
});

console.log('\nDone!');
