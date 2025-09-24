<template>
  <div class="live2d-container">
    <canvas id="live2d-canvas"></canvas>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import * as live2d from 'live2d-render';


onMounted(async () => {
  await live2d.initializeLive2D({
    // live2d 所在区域的背景颜色
    BackgroundRGBA: [0.0, 0.0, 0.0, 0.0],

    // live2d 的 model3.json 文件的相对 根目录 的路径
    ResourcesPath: './rabbit/psd_b.model3.json',

    // live2d 的大小
    CanvasSize: {
      height: 500,
      width: 400
    },

    // 展示工具箱（可以控制 live2d 的展出隐藏，使用特定表情）
    ShowToolBox: true,

    // 是否使用 indexDB 进行缓存优化，这样下一次载入就不会再发起网络请求了
    LoadFromCache: true,
    
    // 指定要使用的Canvas元素ID
    CanvasID: 'live2d-canvas'
  });

  console.log('finish loading');
});

</script>

<style scoped>
.live2d-container {
  width: 400px;
  height: 500px;
  position: relative;
}

#live2d-canvas {
  width: 100%;
  height: 100%;
}
</style>
