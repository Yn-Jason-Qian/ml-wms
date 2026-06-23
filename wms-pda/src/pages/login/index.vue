<template>
  <view class="login-page">
    <!-- Logo 区 -->
    <view class="logo-section">
      <view class="logo-icon">
        <up-icon name="home" size="60" color="#fff" />
      </view>
      <text class="app-title">ML-WMS</text>
      <text class="app-subtitle">仓库管理系统 · PDA</text>
    </view>

    <!-- 表单区 -->
    <view class="form-section">
      <view class="form-card">
        <up-form :model="form" labelWidth="0">
          <up-form-item>
            <up-input
              v-model="form.username"
              placeholder="请输入用户名"
              prefixIcon="account"
              clearable
            />
          </up-form-item>
          <up-form-item>
            <up-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              prefixIcon="lock"
            />
          </up-form-item>
        </up-form>

        <view class="login-btn-wrapper">
          <up-button
            type="primary"
            :loading="loading"
            shape="circle"
            text="登 录"
            @click="handleLogin"
          />
        </view>

        <view class="login-tips">
          <text class="tips-text">首次使用请联系管理员获取账号</text>
        </view>
      </view>
    </view>

    <!-- 底部版权 -->
    <view class="footer">
      <text class="footer-text">ML-WMS v1.0.0</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

async function handleLogin() {
  if (!form.username.trim()) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return
  }
  if (!form.password) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }

  loading.value = true
  try {
    await authStore.login({
      username: form.username.trim(),
      password: form.password
    })
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/home/index' })
    }, 500)
  } catch {
    // 错误由 request 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 48rpx;
}

.logo-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 160rpx;
}

.logo-icon {
  width: 120rpx;
  height: 120rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
}

.app-title {
  font-size: 48rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 8rpx;
  margin-bottom: 12rpx;
}

.app-subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.75);
}

.form-section {
  width: 100%;
  padding-bottom: 80rpx;
}

.form-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 48rpx 36rpx 36rpx;
  box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.12);
}

.login-btn-wrapper {
  margin-top: 40rpx;
  width: 100%;
}

.login-tips {
  margin-top: 24rpx;
  text-align: center;
}

.tips-text {
  font-size: 24rpx;
  color: #c0c4cc;
}

.footer {
  padding-bottom: 48rpx;
}

.footer-text {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.5);
}
</style>
