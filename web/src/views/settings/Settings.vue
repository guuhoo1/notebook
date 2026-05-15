<script setup lang="ts">
import { ref, computed } from 'vue';
import { useAuthStore } from '@/stores';
import { userApi } from '@/api';

const authStore = useAuthStore();
const user = computed(() => authStore.user);

// 标签页状态
const activeTab = ref<'profile' | 'password' | 'avatar'>('profile');

// 表单数据
const nickname = ref('');
const email = ref('');
const phone = ref('');

// 密码表单
const passwordForm = ref({
 oldPassword: '',
 newPassword: '',
 confirmPassword: ''
});

// 头像上传
const avatarPreview = ref('');
const avatarFile = ref<File | null>(null);
const uploadProgress = ref(0);
const uploadStatus = ref<'idle' | 'uploading' | 'success' | 'error'>('idle');
const uploadMessage = ref('');

// 表单验证错误
const errors = ref({
 nickname: '',
 email: '',
 phone: '',
 oldPassword: '',
 newPassword: '',
 confirmPassword: ''
});

// 保存状态
const saving = ref(false);
const saveSuccess = ref(false);
const passwordSaving = ref(false);
const passwordSuccess = ref(false);

// 初始化用户信息
function initUserInfo() {
 if (user.value) {
 nickname.value = user.value.nickname;
 email.value = user.value.email || '';
 phone.value = user.value.phone || '';
 if (user.value.avatar) {
 avatarPreview.value = user.value.avatar;
 }
 }
}

// 验证邮箱格式
function validateEmail(emailStr: string): boolean {
 const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
 return emailRegex.test(emailStr);
}

// 验证个人信息表单
function validateProfileForm(): boolean {
 errors.value = {
 nickname: '',
 email: '',
 phone: '',
 oldPassword: '',
 newPassword: '',
 confirmPassword: ''
 };
 let isValid = true;
 if (!nickname.value.trim()) {
 errors.value.nickname = '请输入昵称';
 isValid = false;
 }
 if (email.value && !validateEmail(email.value)) {
 errors.value.email = '请输入有效的邮箱地址';
 isValid = false;
 }
 if (phone.value && phone.value.length !== 11) {
 errors.value.phone = '请输入有效的手机号码';
 isValid = false;
 }
 return isValid;
}

// 验证密码表单
function validatePasswordForm(): boolean {
 errors.value = {
 nickname: '',
 email: '',
 phone: '',
 oldPassword: '',
 newPassword: '',
 confirmPassword: ''
 };
 let isValid = true;
 if (!passwordForm.value.oldPassword) {
 errors.value.oldPassword = '请输入旧密码';
 isValid = false;
 }
 if (!passwordForm.value.newPassword) {
 errors.value.newPassword = '请输入新密码';
 isValid = false;
 } else if (passwordForm.value.newPassword.length < 8) {
 errors.value.newPassword = '密码长度至少8位';
 isValid = false;
 }
 if (!passwordForm.value.confirmPassword) {
 errors.value.confirmPassword = '请确认新密码';
 isValid = false;
 }
 else if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
 errors.value.confirmPassword = '两次输入的密码不一致';
 isValid = false;
 }
 return isValid;
}

// 保存个人信息
async function saveProfile() {
 if (!validateProfileForm())
 return;
 saving.value = true;
 try {
 const data: Partial<{
 nickname: string;
 email: string | null;
 phone: string | null;
 }> = {
 nickname: nickname.value.trim()
 };
 if (email.value) {
 data.email = email.value.trim();
 }
 if (phone.value) {
 data.phone = phone.value.trim();
 }
 const res = await userApi.updateInfo(data);
 if (res.code === 200 && res.data) {
 authStore.user = res.data;
 saveSuccess.value = true;
 setTimeout(() => {
 saveSuccess.value = false;
 }, 3000);
 }
 }
 catch (error: any) {
 console.error('保存失败:', error);
 }
 finally {
 saving.value = false;
 }
}

// 修改密码
async function changePassword() {
 if (!validatePasswordForm())
 return;
 passwordSaving.value = true;
 try {
 const res = await userApi.updatePassword({
 oldPassword: passwordForm.value.oldPassword,
 newPassword: passwordForm.value.newPassword
 });
 if (res.code === 200) {
 passwordSuccess.value = true;
 passwordForm.value = {
 oldPassword: '',
 newPassword: '',
 confirmPassword: ''
 };
 setTimeout(() => {
 passwordSuccess.value = false;
 }, 3000);
 }
 }
 catch (error: any) {
 console.error('修改密码失败:', error);
 if (error.message) {
 errors.value.oldPassword = error.message;
 }
 }
 finally {
 passwordSaving.value = false;
 }
}

// 处理头像文件选择
function handleAvatarSelect(event: Event) {
 const target = event.target as HTMLInputElement;
 const file = target.files?.[0];
 if (!file)
 return;
 // 验证文件类型
 const allowedTypes = ['image/jpeg', 'image/png'];
 if (!allowedTypes.includes(file.type)) {
 uploadStatus.value = 'error';
 uploadMessage.value = '只支持JPG和PNG格式的图片';
 return;
 }
 // 验证文件大小 (2MB)
 const maxSize = 2 * 1024 * 1024;
 if (file.size > maxSize) {
 uploadStatus.value = 'error';
 uploadMessage.value = '图片大小不能超过2MB';
 return;
 }
 avatarFile.value = file;
 // 预览图片
 const reader = new FileReader();
 reader.onload = (e) => {
 avatarPreview.value = e.target?.result as string;
 };
 reader.readAsDataURL(file);
 uploadStatus.value = 'idle';
 uploadMessage.value = '';
}

// 上传头像
async function uploadAvatar() {
 if (!avatarFile.value)
 return;
 uploadStatus.value = 'uploading';
 uploadProgress.value = 0;
 uploadMessage.value = '';
 try {
 const res = await userApi.uploadAvatar(avatarFile.value);
 if (res.code === 200 && res.data) {
 authStore.user = res.data;
 if (res.data.avatar) {
 avatarPreview.value = res.data.avatar;
 }
 uploadStatus.value = 'success';
 uploadMessage.value = '头像上传成功';
 avatarFile.value = null;
 setTimeout(() => {
 uploadStatus.value = 'idle';
 uploadMessage.value = '';
 }, 3000);
 }
 }
 catch (error: any) {
 uploadStatus.value = 'error';
 uploadMessage.value = error.message || '上传失败，请重试';
 console.error('上传头像失败:', error);
 }
}

// 取消头像预览
function cancelAvatarPreview() {
 if (user.value?.avatar) {
 avatarPreview.value = user.value.avatar;
 }
 else {
 avatarPreview.value = '';
 }
 avatarFile.value = null;
 uploadStatus.value = 'idle';
 uploadMessage.value = '';
}

// 获取性别显示
function getGenderText(gender: number | null): string {
 switch (gender) {
 case 1: return '男';
 case 2: return '女';
 default: return '未设置';
 }
}

// 获取状态显示
function getStatusText(status: number): string {
 switch (status) {
 case 0: return '未激活';
 case 1: return '正常';
 case 2: return '已禁用';
 default: return '未知';
 }
}

initUserInfo();
</script>

<template>
  <div class="settings-page">
    <div class="max-w-4xl mx-auto px-4 py-6">
      <header class="mb-8">
        <h1 class="text-2xl font-bold text-ink mb-2">个人中心</h1>
        <p class="text-muted">管理您的个人信息和账户设置</p>
      </header>

      <div class="grid gap-6 md:grid-cols-3">
        <!-- 左侧导航 -->
        <div class="md:col-span-1">
          <nav class="space-y-1">
            <button
              class="w-full text-left px-4 py-3 rounded-lg transition-colors"
              :class="activeTab === 'profile' ? 'bg-primary text-on-primary' : 'text-body hover:bg-surface-soft'"
              @click="activeTab = 'profile'"
            >
              <svg class="w-5 h-5 inline-block mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              个人信息
            </button>
            <button
              class="w-full text-left px-4 py-3 rounded-lg transition-colors"
              :class="activeTab === 'password' ? 'bg-primary text-on-primary' : 'text-body hover:bg-surface-soft'"
              @click="activeTab = 'password'"
            >
              <svg class="w-5 h-5 inline-block mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
              修改密码
            </button>
            <button
              class="w-full text-left px-4 py-3 rounded-lg transition-colors"
              :class="activeTab === 'avatar' ? 'bg-primary text-on-primary' : 'text-body hover:bg-surface-soft'"
              @click="activeTab = 'avatar'"
            >
              <svg class="w-5 h-5 inline-block mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              头像设置
            </button>
          </nav>
        </div>

        <!-- 右侧内容区 -->
        <div class="md:col-span-2">
          <!-- 个人信息卡片 -->
          <div v-show="activeTab === 'profile'" class="card">
            <h2 class="text-lg font-semibold text-ink mb-6">基本信息</h2>

            <div class="space-y-6">
              <div>
                <label class="block text-sm font-medium text-body mb-2">昵称</label>
                <input
                  v-model="nickname"
                  type="text"
                  class="input-base"
                  placeholder="请输入昵称"
                  :class="{ 'border-red-500': errors.nickname }"
                />
                <p v-if="errors.nickname" class="mt-1 text-sm text-red-500">{{ errors.nickname }}</p>
              </div>

              <div>
                <label class="block text-sm font-medium text-body mb-2">邮箱</label>
                <input
                  v-model="email"
                  type="email"
                  class="input-base"
                  placeholder="请输入邮箱（可选）"
                  :class="{ 'border-red-500': errors.email }"
                />
                <p v-if="errors.email" class="mt-1 text-sm text-red-500">{{ errors.email }}</p>
              </div>

              <div>
                <label class="block text-sm font-medium text-body mb-2">手机号</label>
                <input
                  v-model="phone"
                  type="tel"
                  class="input-base"
                  placeholder="请输入手机号（可选）"
                  :class="{ 'border-red-500': errors.phone }"
                />
                <p v-if="errors.phone" class="mt-1 text-sm text-red-500">{{ errors.phone }}</p>
              </div>

              <div class="pt-4 border-t border-hairline">
                <div class="flex items-center justify-between py-2">
                  <span class="text-muted">性别</span>
                  <span class="text-body">{{ getGenderText(user?.gender || null) }}</span>
                </div>
                <div class="flex items-center justify-between py-2">
                  <span class="text-muted">账户状态</span>
                  <span :class="user?.status === 1 ? 'text-green-600' : 'text-red-500'">
                    {{ getStatusText(user?.status || 0) }}
                  </span>
                </div>
                <div class="flex items-center justify-between py-2">
                  <span class="text-muted">注册时间</span>
                  <span class="text-body">{{ user?.createTime || '-' }}</span>
                </div>
              </div>

              <div class="flex gap-3 pt-4">
                <button
                  class="btn-primary flex-1"
                  :disabled="saving"
                  @click="saveProfile"
                >
                  <svg v-if="saving" class="w-5 h-5 mr-2 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  {{ saving ? '保存中...' : '保存修改' }}
                </button>
              </div>

              <Transition name="fade">
                <div v-if="saveSuccess" class="flex items-center gap-2 text-green-600 bg-green-50 px-4 py-3 rounded-lg">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                  保存成功
                </div>
              </Transition>
            </div>
          </div>

          <!-- 修改密码卡片 -->
          <div v-show="activeTab === 'password'" class="card">
            <h2 class="text-lg font-semibold text-ink mb-6">修改密码</h2>

            <div class="space-y-6">
              <div>
                <label class="block text-sm font-medium text-body mb-2">旧密码</label>
                <input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  class="input-base"
                  placeholder="请输入当前密码"
                  :class="{ 'border-red-500': errors.oldPassword }"
                />
                <p v-if="errors.oldPassword" class="mt-1 text-sm text-red-500">{{ errors.oldPassword }}</p>
              </div>

              <div>
                <label class="block text-sm font-medium text-body mb-2">新密码</label>
                <input
                  v-model="passwordForm.newPassword"
                  type="password"
                  class="input-base"
                  placeholder="请输入新密码（至少8位）"
                  :class="{ 'border-red-500': errors.newPassword }"
                />
                <p v-if="errors.newPassword" class="mt-1 text-sm text-red-500">{{ errors.newPassword }}</p>
              </div>

              <div>
                <label class="block text-sm font-medium text-body mb-2">确认新密码</label>
                <input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  class="input-base"
                  placeholder="请再次输入新密码"
                  :class="{ 'border-red-500': errors.confirmPassword }"
                />
                <p v-if="errors.confirmPassword" class="mt-1 text-sm text-red-500">{{ errors.confirmPassword }}</p>
              </div>

              <div class="flex gap-3">
                <button
                  class="btn-primary flex-1"
                  :disabled="passwordSaving"
                  @click="changePassword"
                >
                  <svg v-if="passwordSaving" class="w-5 h-5 mr-2 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  {{ passwordSaving ? '修改中...' : '修改密码' }}
                </button>
              </div>

              <Transition name="fade">
                <div v-if="passwordSuccess" class="flex items-center gap-2 text-green-600 bg-green-50 px-4 py-3 rounded-lg">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                  密码修改成功，请重新登录
                </div>
              </Transition>
            </div>
          </div>

          <!-- 头像设置卡片 -->
          <div v-show="activeTab === 'avatar'" class="card">
            <h2 class="text-lg font-semibold text-ink mb-6">头像设置</h2>

            <div class="space-y-6">
              <!-- 头像预览 -->
              <div class="flex flex-col items-center">
                <div class="relative w-32 h-32 mb-4">
                  <div
                    class="w-full h-full rounded-full overflow-hidden bg-surface-soft border-2 border-hairline"
                    :class="{ 'border-primary': avatarFile }"
                  >
                    <img
                      v-if="avatarPreview"
                      :src="avatarPreview"
                      alt="头像预览"
                      class="w-full h-full object-cover"
                    />
                    <div v-else class="w-full h-full flex items-center justify-center text-muted">
                      <svg class="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                    </div>
                  </div>

                  <!-- 上传按钮覆盖层 -->
                  <div class="absolute inset-0 flex items-center justify-center bg-black/50 opacity-0 hover:opacity-100 transition-opacity rounded-full">
                    <label class="cursor-pointer">
                      <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                      <input
                        type="file"
                        accept="image/jpeg,image/png"
                        class="hidden"
                        @change="handleAvatarSelect"
                      />
                    </label>
                  </div>
                </div>

                <p class="text-sm text-muted">点击头像更换</p>
              </div>

              <!-- 上传状态 -->
              <Transition name="fade">
                <div v-if="uploadStatus !== 'idle'" class="rounded-lg p-4"
                  :class="{
                    'bg-yellow-50 text-yellow-800': uploadStatus === 'uploading',
                    'bg-green-50 text-green-800': uploadStatus === 'success',
                    'bg-red-50 text-red-800': uploadStatus === 'error'
                  }"
                >
                  <div class="flex items-center gap-2">
                    <svg v-if="uploadStatus === 'uploading'" class="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    <svg v-else-if="uploadStatus === 'success'" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                    <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                    <span>{{ uploadMessage }}</span>
                  </div>

                  <!-- 上传进度条 -->
                  <div v-if="uploadStatus === 'uploading'" class="mt-3">
                    <div class="h-2 bg-surface-soft rounded-full overflow-hidden">
                      <div
                        class="h-full bg-primary transition-all duration-300"
                        :style="{ width: `${uploadProgress}%` }"
                      ></div>
                    </div>
                  </div>
                </div>
              </Transition>

              <!-- 操作按钮 -->
              <div class="flex gap-3">
                <button
                  v-if="avatarFile"
                  class="btn-secondary flex-1"
                  @click="cancelAvatarPreview"
                >
                  取消
                </button>
                <button
                  class="btn-primary flex-1"
                  :disabled="!avatarFile || uploadStatus === 'uploading'"
                  @click="uploadAvatar"
                >
                  {{ avatarFile ? '上传头像' : '选择图片' }}
                </button>
              </div>

              <!-- 提示信息 -->
              <div class="text-sm text-muted bg-surface-soft p-3 rounded-lg">
                <p class="mb-1">支持格式：JPG、PNG</p>
                <p>文件大小：不超过 2MB</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.settings-page {
  min-height: calc(100vh - 64px);
}

.card {
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 1.5rem;
}

@media (max-width: 768px) {
  .card {
    padding: 1rem;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
