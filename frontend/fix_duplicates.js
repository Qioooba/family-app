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

// 修复重复:key
function fixDuplicateKeys(content) {
  // 修复在同一行有两个:key的情况
  // 模式: :key="..." ... :key="..."
  content = content.replace(/(:key="[^"]+"[^>]*)\s*:key="[^"]+"/g, '$1');
  
  return content;
}

// 修复重复的其他属性
function fixDuplicateAttrs(content) {
  const lines = content.split('\n');
  const fixedLines = [];
  
  for (const line of lines) {
    // 检测是否有重复的属性名
    const attrRegex = /\s(\w+(?::\w+)?)=/g;
    const attrs = [];
    let match;
    
    while ((match = attrRegex.exec(line)) !== null) {
      attrs.push(match[1]);
    }
    
    // 检查是否有重复
    const uniqueAttrs = [...new Set(attrs)];
    if (attrs.length !== uniqueAttrs.length) {
      console.log('Found duplicate attrs in line:', line.trim().substring(0, 80));
      // 保留第一个出现的属性
      let fixedLine = line;
      const seen = new Set();
      for (const attr of attrs) {
        if (seen.has(attr)) {
          // 移除重复的
          const regex = new RegExp(`\\s${attr}="[^"]*"`, 'g');
          let first = true;
          fixedLine = fixedLine.replace(regex, (match) => {
            if (first) {
              first = false;
              return match;
            }
            return '';
          });
        } else {
          seen.add(attr);
        }
      }
      fixedLines.push(fixedLine);
    } else {
      fixedLines.push(line);
    }
  }
  
  return fixedLines.join('\n');
}

const baseDir = '/Volumes/document/Projects/family-app/frontend/src';
const vueFiles = getVueFiles(baseDir);

let fixedCount = 0;

vueFiles.forEach(file => {
  let content = fs.readFileSync(file, 'utf-8');
  const originalContent = content;
  
  content = fixDuplicateKeys(content);
  content = fixDuplicateAttrs(content);
  
  if (content !== originalContent) {
    fs.writeFileSync(file, content);
    console.log(`✓ Fixed: ${file.replace(baseDir, 'src')}`);
    fixedCount++;
  }
});

console.log(`\nFixed ${fixedCount} files`);
