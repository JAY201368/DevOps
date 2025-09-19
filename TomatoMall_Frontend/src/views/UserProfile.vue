<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>个人信息</h2>
          <el-button type="primary" @click="handleEdit" v-if="!isEditing"
            >编辑</el-button
          >
        </div>
      </template>

      <el-form
        :model="userForm"
        :rules="rules"
        ref="userFormRef"
        label-width="100px"
        :disabled="!isEditing"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" disabled />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="isEditing">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="不修改请留空"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="telephone">
          <el-input v-model="userForm.telephone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="所在地" prop="location">
          <el-input v-model="userForm.location" />
        </el-form-item>

        <el-form-item v-if="isEditing">
          <el-button type="primary" @click="handleSave" :loading="loading"
            >保存</el-button
          >
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>

      <div class="logout-section">
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { getUserInfo, updateUserInfo } from "../api/user";

const router = useRouter();
const userFormRef = ref(null);
const loading = ref(false);
const isEditing = ref(false);

const userForm = reactive({
  username: "",
  password: "",
  name: "",
  role: "",
  telephone: "",
  email: "",
  location: "",
});

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[0-9]{10}$/.test(value)) {
    callback(new Error("请输入正确的手机号"));
  } else {
    callback();
  }
};

const validateEmail = (rule, value, callback) => {
  if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error("请输入正确的邮箱地址"));
  } else {
    callback();
  }
};

const rules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  role: [{ required: true, message: "请选择角色", trigger: "change" }],
  telephone: [{ validator: validatePhone, trigger: "blur" }],
  email: [{ validator: validateEmail, trigger: "blur" }],
};

const fetchUserInfo = async () => {
  getUserInfo(userForm.username)
    .then((res) => {
      if (res.data.code === "200") {
        Object.assign(userForm, res.data.data);
      } else if (res.data.code === "400") {
        ElMessage.error(res.data.msg || "获取用户信息失败");
      }
    });
    // .catch((error) => {
    //   ElMessage.error("获取用户信息失败，请稍后重试");
    // });
};

const handleEdit = () => {
  isEditing.value = true;
};

const handleCancel = () => {
  isEditing.value = false;
  fetchUserInfo();
};

const handleSave = async () => {
  if (!userFormRef.value) return;

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      updateUserInfo(userForm)
        .then((res) => {
          if (res.data.code === "200") {
            ElMessage.success("更新成功");
            isEditing.value = false;
          } else if (res.data.code === "400") {
            ElMessage.error(res.data.msg || "更新失败");
          }
          loading.value = false;
        });
        // .catch((error) => {
        //   ElMessage.error("更新失败，请稍后重试");
        //   loading.value = false;
        // });
    }
  });
};

const handleLogout = () => {
  localStorage.removeItem("token");
  router.push("/login");
};

onMounted(() => {
  // 从localStorage获取用户名
  const username = localStorage.getItem("username");
  if (username) {
    userForm.username = username;
    fetchUserInfo();
  } else {
    router.push("/login");
  }
});
</script>

<style scoped>
.profile-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px);
  padding: 20px;
}

.profile-card {
  width: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logout-section {
  margin-top: 20px;
  text-align: center;
}
</style>
