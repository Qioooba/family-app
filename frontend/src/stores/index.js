import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { taskApi, wishApi, recipeApi, dietApi, anniversaryApi, voteApi } from '../api'

// 任务Store
export const useTaskStore = defineStore('task', () => {
  const tasks = ref([])
  const todayTasks = ref([])
  const loading = ref(false)
  
  const completedTasks = computed(() => tasks.value.filter(t => t.status === 2))
  const pendingTasks = computed(() => tasks.value.filter(t => t.status !== 2))
  
  const fetchTasks = async (familyId, params = {}) => {
    loading.value = true
    try {
      const res = await taskApi.getList(familyId, params)
      tasks.value = res
      return res
    } finally {
      loading.value = false
    }
  }
  
  const fetchTodayTasks = async (familyId) => {
    const res = await taskApi.getTodayTasks(familyId)
    todayTasks.value = res
    return res
  }
  
  const completeTask = async (id) => {
    await taskApi.complete(id)
    const task = tasks.value.find(t => t.id === id)
    if (task) task.status = 2
  }
  
  return {
    tasks,
    todayTasks,
    loading,
    completedTasks,
    pendingTasks,
    fetchTasks,
    fetchTodayTasks,
    completeTask
  }
})

// 心愿Store
export const useWishStore = defineStore('wish', () => {
  const wishes = ref([])
  const loading = ref(false)
  
  const completedWishes = computed(() => wishes.value.filter(w => w.status === 2))
  const inProgressWishes = computed(() => wishes.value.filter(w => w.status === 1))
  
  const fetchWishes = async (familyId, params = {}) => {
    loading.value = true
    try {
      const res = await wishApi.getList(familyId, params)
      wishes.value = res
      return res
    } finally {
      loading.value = false
    }
  }
  
  const claimWish = async (wishId, userId) => {
    await wishApi.claim(wishId, userId)
    const wish = wishes.value.find(w => w.id === wishId)
    if (wish) {
      wish.claimantId = userId
      wish.status = 1
    }
  }
  
  return {
    wishes,
    loading,
    completedWishes,
    inProgressWishes,
    fetchWishes,
    claimWish
  }
})

// 菜谱Store
export const useRecipeStore = defineStore('recipe', () => {
  const recipes = ref([])
  const aiRecipes = ref([])
  const loading = ref(false)
  
  const fetchRecipes = async (familyId) => {
    loading.value = true
    try {
      const res = await recipeApi.getFamilyRecipes(familyId)
      recipes.value = res
      return res
    } finally {
      loading.value = false
    }
  }
  
  const searchRecipes = async (params) => {
    loading.value = true
    try {
      const res = await recipeApi.search(params)
      recipes.value = res
      return res
    } finally {
      loading.value = false
    }
  }
  
  const getAIRecommend = async (ingredients) => {
    const res = await recipeApi.recommend(ingredients)
    aiRecipes.value = res
    return res
  }
  
  return {
    recipes,
    aiRecipes,
    loading,
    fetchRecipes,
    searchRecipes,
    getAIRecommend
  }
})

// 饮食Store
export const useDietStore = defineStore('diet', () => {
  const todayRecords = ref([])
  const todayStats = ref({
    intake: 0,
    target: 2000,
    protein: 0,
    carbs: 0,
    fat: 0
  })
  
  const fetchTodayRecords = async (userId, date) => {
    const res = await dietApi.getDayRecords(userId, date)
    todayRecords.value = res
    return res
  }
  
  const fetchTodayStats = async (userId, date) => {
    const res = await dietApi.getDayStatistics(userId, date)
    todayStats.value = { ...todayStats.value, ...res }
    return res
  }
  
  const addRecord = async (data) => {
    const res = await dietApi.addRecord(data)
    todayRecords.value.push(res)
    return res
  }
  
  return {
    todayRecords,
    todayStats,
    fetchTodayRecords,
    fetchTodayStats,
    addRecord
  }
})

// 纪念日Store
export const useAnniversaryStore = defineStore('anniversary', () => {
  const anniversaries = ref([])
  const upcomingList = ref([])
  const todayCountdown = ref(null)
  
  const fetchAnniversaries = async (familyId) => {
    const res = await anniversaryApi.getList(familyId)
    anniversaries.value = res
    return res
  }
  
  const fetchUpcoming = async (familyId, days = 30) => {
    const res = await anniversaryApi.getUpcoming(familyId, days)
    upcomingList.value = res
    return res
  }
  
  const fetchTodayCountdown = async (familyId) => {
    const res = await anniversaryApi.getToday(familyId)
    todayCountdown.value = res[0] || null
    return res
  }
  
  return {
    anniversaries,
    upcomingList,
    todayCountdown,
    fetchAnniversaries,
    fetchUpcoming,
    fetchTodayCountdown
  }
})

// 投票Store
export const useVoteStore = defineStore('vote', () => {
  const votes = ref([])
  const activeVotes = computed(() => votes.value.filter(v => v.status === 0))
  const endedVotes = computed(() => votes.value.filter(v => v.status === 1))
  
  const fetchVotes = async (familyId, status) => {
    const res = await voteApi.getList(familyId, status)
    votes.value = res
    return res
  }
  
  const doVote = async (voteId, userId, options) => {
    await voteApi.doVote(voteId, userId, options)
    const vote = votes.value.find(v => v.id === voteId)
    if (vote) vote.hasVoted = true
  }
  
  return {
    votes,
    activeVotes,
    endedVotes,
    fetchVotes,
    doVote
  }
})
