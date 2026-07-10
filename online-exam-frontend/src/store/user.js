import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

function readStoredUserInfo() {
  const raw = localStorage.getItem('userInfo')
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (error) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    return null
  }
}
export const useUserStore = defineStore('user', () => {
  const userInfo = ref(readStoredUserInfo())
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role || '')
  const isAdmin = computed(() => role.value === 'ADMIN')
  const isTeacher = computed(() => role.value === 'TEACHER')
  const isStudent = computed(() => role.value === 'STUDENT')
  // 教师或管理员可管理题库/试卷
  const canManage = computed(() => isAdmin.value || isTeacher.value)

  function setLoginInfo(loginVO) {
    token.value = loginVO.token
    userInfo.value = {
      userId: loginVO.userId,
      username: loginVO.username,
      name: loginVO.name,
      role: loginVO.role
    }
    localStorage.setItem('token', loginVO.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    userInfo, token, isLoggedIn, role, isAdmin, isTeacher, isStudent, canManage,
    setLoginInfo, setUserInfo, logout
  }
})
