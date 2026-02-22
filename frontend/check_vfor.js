const fs = require('fs');
const path = require('path');

// 递归获取所有vue文件
function getVueFiles(dir, files = []) {
  const items = fs.readdirSync(dir);
  
  for (const item of items) {
    const fullPath = path.join(dir, item);
    const stat = fs.statSync(fullPath);
    
    if (stat.isDirectory()) {
      getVueFiles(fullPath, files);
    } else if (item.endsWith('.vue')) {
      files.push(fullPath);
    }
  }
  
  return files;
}

// 检查v-for是否缺少key
function checkVFor(content, filePath) {
  const lines = content.split('\n');
  const issues = [];
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const vforMatch = line.match(/v-for="([^"]+)"/);
    
    if (vforMatch) {
      // 检查同一行是否有:key
      const hasKeyOnSameLine = line.match(/:key="[^"]+"/);
      
      // 检查下一行是否有:key（如果当前行以>结尾或下一行是属性）
      let hasKeyOnNextLine = false;
      if (!line.includes('>') && i < lines.length - 1) {
        const nextLine = lines[i + 1];
        if (nextLine.match(/^\s*:key="[^"]+"/)) {
          hasKeyOnNextLine = true;
        }
      }
      
      if (!hasKeyOnSameLine && !hasKeyOnNextLine) {
        issues.push({
          line: i + 1,
          content: line.trim(),
          vfor: vforMatch[1]
        });
      }
    }
  }
  
  return issues;
}

const baseDir = '/Volumes/document/Projects/family-app/frontend/src';
const vueFiles = getVueFiles(baseDir);

let totalIssues = 0;
const fileIssues = [];

vueFiles.forEach(file => {
  const content = fs.readFileSync(file, 'utf-8');
  const issues = checkVFor(content, file);
  
  if (issues.length > 0) {
    totalIssues += issues.length;
    fileIssues.push({
      file: file.replace(baseDir, 'src'),
      issues
    });
  }
});

console.log(`找到 ${totalIssues} 个缺少key的v-for\n`);

fileIssues.forEach(({ file, issues }) => {
  console.log(`${file}:`);
  issues.forEach(issue => {
    console.log(`  第${issue.line}行: ${issue.vfor}`);
  });
});
