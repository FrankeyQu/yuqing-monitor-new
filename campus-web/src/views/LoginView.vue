<template>
  <main class="login-screen">
    <section class="login-visual" aria-hidden="true">
      <img src="../assets/campus-mark.svg" alt="" />
      <div class="login-visual-copy">
        <span>{{ englishName }}</span>
        <strong>{{ productName }}</strong>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-heading">
        <h1>{{ productName }}</h1>
        <p>{{ productSubtitle }} · {{ companyName }}</p>
      </div>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />

      <el-form class="login-form" :model="form" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="账号">
          <el-input v-model.trim="form.telephone" autocomplete="username" size="large">
            <template #prefix><UserRound :size="18" /></template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" show-password size="large">
            <template #prefix><LockKeyhole :size="18" /></template>
          </el-input>
        </el-form-item>

        <el-button class="login-button" type="primary" size="large" :loading="submitting" @click="handleLogin">
          <LogIn :size="18" />
          登录
        </el-button>
      </el-form>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { LockKeyhole, LogIn, UserRound } from 'lucide-vue-next';
import { COMPANY_NAME, PRODUCT_EN_NAME, PRODUCT_NAME, PRODUCT_SUBTITLE } from '../config/brand';
import { login } from '../services/auth';

const router = useRouter();
const companyName = COMPANY_NAME;
const englishName = PRODUCT_EN_NAME;
const productName = PRODUCT_NAME;
const productSubtitle = PRODUCT_SUBTITLE;
const submitting = ref(false);
const errorMessage = ref('');

const form = reactive({
  telephone: '',
  password: ''
});

async function handleLogin() {
  errorMessage.value = '';
  if (!form.telephone || !form.password) {
    errorMessage.value = '账号和密码不能为空';
    return;
  }
  submitting.value = true;
  try {
    await login(form);
    router.replace('/');
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败';
  } finally {
    submitting.value = false;
  }
}
</script>
