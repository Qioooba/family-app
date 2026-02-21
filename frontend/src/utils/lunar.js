/**
 * 公历转农历工具函数
 * 简化版实现，支持1900-2100年
 */

// 农历数据（1900-2100）
// 每个元素代表一年的数据，格式：闰月信息+各月天数
const LUNAR_DATA = [
  0x04bd8,0x04ae0,0x0a570,0x054d5,0x0d260,0x0d950,0x16554,0x056a0,0x09ad0,0x055d2,
  0x04ae0,0x0a5b6,0x0a4d0,0x0d250,0x1d255,0x0b540,0x0d6a0,0x0ada2,0x095b0,0x14977,
  0x04970,0x0a4b0,0x0b4b5,0x06a50,0x06d40,0x1ab54,0x02b60,0x09570,0x052f2,0x04970,
  0x06566,0x0d4a0,0x0ea50,0x06e95,0x05ad0,0x02b60,0x186e3,0x092e0,0x1c8d7,0x0c950,
  0x0d4a0,0x1d8a6,0x0b550,0x056a0,0x1a5b4,0x025d0,0x092d0,0x0d2b2,0x0a950,0x0b557,
  0x06ca0,0x0b550,0x15355,0x04da0,0x0a5d0,0x14573,0x052d0,0x0a9a8,0x0e950,0x06aa0,
  0x0aea6,0x0ab50,0x04b60,0x0aae4,0x0a570,0x05260,0x0f263,0x0d950,0x05b57,0x056a0,
  0x096d0,0x04dd5,0x04ad0,0x0a4d0,0x0d4d4,0x0d250,0x0d558,0x0b540,0x0b5a0,0x195a6,
  0x095b0,0x049b0,0x0a974,0x0a4b0,0x0b27a,0x06a50,0x06d40,0x0af46,0x0ab60,0x09570,
  0x04af5,0x04970,0x064b0,0x074a3,0x0ea50,0x06b58,0x055c0,0x0ab60,0x096d5,0x092e0,
  0x0c960,0x0d954,0x0d4a0,0x0da50,0x07552,0x056a0,0x0abb7,0x025d0,0x092d0,0x0cab5,
  0x0a950,0x0b4a0,0x0baa4,0x0ad50,0x055d9,0x04ba0,0x0a5b0,0x15176,0x052b0,0x0a930,
  0x07954,0x06aa0,0x0ad50,0x05b52,0x04b60,0x0a6e6,0x0a4e0,0x0d260,0x0ea65,0x0d530,
  0x05aa0,0x076a3,0x096d0,0x04bd7,0x04ad0,0x0a4d0,0x1d0b6,0x0d250,0x0d520,0x0dd45,
  0x0b5a0,0x056d0,0x055b2,0x049b0,0x0a577,0x0a4b0,0x0aa50,0x1b255,0x06d20,0x0ada0
]

// 天干
const TIAN_GAN = ['甲','乙','丙','丁','戊','己','庚','辛','壬','癸']
// 地支
const DI_ZHI = ['子','丑','寅','卯','辰','巳','午','未','申','酉','戌','亥']
// 生肖
const ZODIAC = ['鼠','牛','虎','兔','龙','蛇','马','羊','猴','鸡','狗','猪']
// 农历月份
const LUNAR_MONTHS = ['正月','二月','三月','四月','五月','六月','七月','八月','九月','十月','冬月','腊月']
// 农历日期
const LUNAR_DAYS = [
  '初一','初二','初三','初四','初五','初六','初七','初八','初九','初十',
  '十一','十二','十三','十四','十五','十六','十七','十八','十九','二十',
  '廿一','廿二','廿三','廿四','廿五','廿六','廿七','廿八','廿九','三十'
]

/**
 * 判断是否是闰年
 */
function isLeapYear(year) {
  return (year % 4 === 0 && year % 100 !== 0) || (year % 400 === 0)
}

/**
 * 获取公历某年的总天数
 */
function getSolarDays(year, month) {
  const daysInMonth = [31, isLeapYear(year) ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
  return daysInMonth[month - 1]
}

/**
 * 公历转农历
 * @param {number} year - 公历年
 * @param {number} month - 公历月
 * @param {number} day - 公历日
 * @returns {object|null} 农历日期对象
 */
export function solarToLunar(year, month, day) {
  if (year < 1900 || year > 2100) return null

  // 计算从1900年1月31日（农历正月初一）到目标日期的天数
  let offset = 0
  for (let y = 1900; y < year; y++) {
    offset += isLeapYear(y) ? 366 : 365
  }
  for (let m = 1; m < month; m++) {
    offset += getSolarDays(year, m)
  }
  offset += day - 1

  // 计算农历
  let lunarYear = 1900
  let daysInLunarYear = 0

  while (lunarYear <= 2100) {
    daysInLunarYear = getLunarYearDays(lunarYear)
    if (offset < daysInLunarYear) break
    offset -= daysInLunarYear
    lunarYear++
  }

  if (lunarYear > 2100) return null

  // 计算农历月
  let lunarMonth = 1
  let isLeap = false
  const leapMonth = getLeapMonth(lunarYear)
  let daysInMonth = 0

  for (let m = 1; m <= 12; m++) {
    // 判断是否有闰月
    if (leapMonth > 0 && m === leapMonth + 1 && !isLeap) {
      m--
      isLeap = true
      daysInMonth = getLeapDays(lunarYear)
    } else {
      daysInMonth = getMonthDays(lunarYear, m)
    }

    if (offset < daysInMonth) break

    offset -= daysInMonth

    if (isLeap && m === leapMonth + 1) {
      isLeap = false
    }

    if (!isLeap) lunarMonth++
  }

  const lunarDay = offset + 1

  return {
    year: lunarYear,
    month: LUNAR_MONTHS[lunarMonth - 1],
    day: LUNAR_DAYS[lunarDay - 1],
    monthNum: lunarMonth,
    dayNum: lunarDay,
    isLeap: isLeap,
    ganZhi: getGanZhi(lunarYear),
    zodiac: ZODIAC[(lunarYear - 4) % 12]
  }
}

/**
 * 农历转公历
 * @param {number} year - 农历年
 * @param {number} month - 农历月（1-12）
 * @param {number} day - 农历日（1-30）
 * @param {boolean} isLeapMonth - 是否是闰月
 * @returns {object|null} 公历日期对象
 */
export function lunarToSolar(year, month, day, isLeapMonth = false) {
  if (year < 1900 || year > 2100) return null
  if (month < 1 || month > 12) return null
  if (day < 1 || day > 30) return null

  // 计算从1900年到目标农历年的总天数
  let offset = 0
  for (let y = 1900; y < year; y++) {
    offset += getLunarYearDays(y)
  }

  // 加上目标年之前的月份天数
  const leapMonth = getLeapMonth(year)
  for (let m = 1; m < month; m++) {
    if (leapMonth > 0 && m === leapMonth) {
      offset += getLeapDays(year)
    }
    offset += getMonthDays(year, m)
  }

  // 如果是闰月
  if (isLeapMonth && leapMonth === month) {
    offset += getMonthDays(year, month)
  }

  // 加上当月天数
  offset += day - 1

  // 从1900年1月31日开始推算公历日期
  const startDate = new Date(1900, 0, 31)
  const targetDate = new Date(startDate.getTime() + offset * 24 * 60 * 60 * 1000)

  return {
    year: targetDate.getFullYear(),
    month: targetDate.getMonth() + 1,
    day: targetDate.getDate()
  }
}

/**
 * 获取农历年的总天数
 */
function getLunarYearDays(year) {
  let sum = 0
  for (let i = 0x8000; i > 0x8; i >>= 1) {
    sum += (LUNAR_DATA[year - 1900] & i) ? 30 : 29
  }
  return sum + getLeapDays(year)
}

/**
 * 获取闰月的天数
 */
function getLeapDays(year) {
  if (getLeapMonth(year)) {
    return (LUNAR_DATA[year - 1900] & 0x10000) ? 30 : 29
  }
  return 0
}

/**
 * 获取某月的天数
 */
function getMonthDays(year, month) {
  return (LUNAR_DATA[year - 1900] & (0x10000 >> month)) ? 30 : 29
}

/**
 * 获取闰月（0表示无闰月，1-12表示闰几月）
 */
function getLeapMonth(year) {
  return LUNAR_DATA[year - 1900] & 0xf
}

/**
 * 获取干支纪年
 */
function getGanZhi(year) {
  const gan = TIAN_GAN[(year - 4) % 10]
  const zhi = DI_ZHI[(year - 4) % 12]
  return gan + zhi
}

/**
 * 获取农历日期字符串
 */
export function getLunarDateString(year, month, day) {
  const lunar = solarToLunar(year, month, day)
  if (!lunar) return ''
  return `${lunar.year}年${lunar.month}${lunar.day}`
}

/**
 * 获取公历日期字符串
 */
export function getSolarDateString(date) {
  return `${date.year}-${String(date.month).padStart(2, '0')}-${String(date.day).padStart(2, '0')}`
}

/**
 * 判断是否是今天（农历）
 */
export function isToday(year, month, day) {
  const today = new Date()
  const lunar = solarToLunar(today.getFullYear(), today.getMonth() + 1, today.getDate())
  if (!lunar) return false
  return lunar.year === year && lunar.monthNum === month && lunar.dayNum === day
}

/**
 * 获取生肖
 */
export function getZodiac(year) {
  return ZODIAC[(year - 4) % 12]
}

/**
 * 获取节气（简化版，返回主要节气）
 */
export function getJieQi(year, month, day) {
  // 简化节气计算，返回空或使用更精确的算法
  const jieQiList = [
    '立春','雨水','惊蛰','春分','清明','谷雨',
    '立夏','小满','芒种','夏至','小暑','大暑',
    '立秋','处暑','白露','秋分','寒露','霜降',
    '立冬','小雪','大雪','冬至','小寒','大寒'
  ]
  // 这里简化处理，实际节气需要精确计算
  return ''
}
