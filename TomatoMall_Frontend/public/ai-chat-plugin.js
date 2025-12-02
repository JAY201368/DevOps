// AI 聊天插件
(function () {
  const _Ai = {
    config: {
      model: 'Qwen/Qwen3-14B',
      key: 'sk-szrfqqlzjbkbysppmurhkqjufcxuswzgoewuocxdmxlqjjfq',
      apiUrl: 'https://api.siliconflow.cn/v1/chat/completions',
      img: '',
      backendUrl: null,
      userId: null // 添加用户ID字段
    },

    // // 添加与图书相关的关键词库和屏蔽词库
    // bookKeywords: [
    //   "书", "图书", "小说", "文学", "作者", "出版", "阅读", "书籍", "故事", "章节",
    //   "书店", "bookstore", "book", "novel", "literature", "author", "publisher", "reader",
    //   "出版社", "杂志", "期刊", "文集", "丛书", "书系", "系列", "文章", "散文", "诗歌",
    //   "绘本", "漫画", "电子书", "有声书", "教材", "教辅", "工具书", "词典", "百科全书",
    //   "畅销书", "经典", "名著", "传记", "自传", "科幻", "奇幻", "悬疑", "推理", "言情",
    //   "武侠", "历史", "哲学", "心理", "艺术", "摄影", "建筑", "设计", "料理", "旅行",
    //   "生活", "健康", "经济", "管理", "科学", "技术", "编程", "计算机", "医学", "法律",
    //   "教育", "考试", "考研", "考证", "考公", "textbook", "dictionary", "encyclopedia",
    //   "novel", "fiction", "nonfiction", "biography", "autobiography", "sci-fi", "fantasy",
    //   "romance", "history", "philosophy", "psychology", "art", "photography", "architecture",
    //   "design", "cooking", "travel", "lifestyle", "health", "economics", "management",
    //   "science", "technology", "programming", "computer", "medicine", "law", "education",
    //   "test", "exam", "certification", "public service", "comic", "manga", "audiobook",
    //   "ebook", "digital book", "printed book", "hardcover", "paperback", "poetry", "essay",
    //   "anthology", "collection", "series", "trilogy", "sequel", "prequel", "adaptation",
    //   "translation", "edition", "publication", "copyright", "library", "bibliography",
    //   "reference", "index", "contents", "chapter", "page", "bookshelf", "bookmark",
    //   "recommended reading", "bestseller", "classic", "prize", "award", "review", "rating",
    //   "comment", "isbn", "book cover", "illustration", "bookstore", "书店", "新书", "二手书",
    //   "租书", "借书", "图书馆", "藏书", "绝版书", "古籍", "书评", "读后感", "读书笔记",
    //   "购书", "藏书票", "书签", "书架", "书柜", "书房", "书单", "书目", "书号", "定价",
    //   "折扣", "特价", "促销", "封面", "封底", "书脊", "装帧", "开本", "字体", "排版",
    //   "书摘", "书虫", "书迷", "书友", "读者", "作家", "编辑", "校对", "书稿", "书商",
    //   "番茄书城", "下单", "购买", "购物车", "收藏", "愿望单", "热门图书", "推荐", "评分",
    //   "评论", "书评", "bestselling", "new release", "book club", "reading list",
    //   "bookworm", "bookish", "bibliophile", "publisher", "imprint", "print run",
    //   "edition", "title", "subtitle", "table of contents", "appendix", "glossary",
    //   "note", "footnote", "bibliography", "index", "preface", "foreword", "introduction",
    //   "conclusion", "epilogue", "prologue", "abstract", "synopsis"
    // ],

    // 添加系统提示词
    systemPrompt: `你是番茄书城的专业图书助手，你的主要职责是：

    1. 回答用户关于图书的问题，包括但不限于：
      - 书城内图书的信息、评价、价格、库存等
      - 图书相关的知识（如作者、出版社、文学流派等）
      - 阅读建议和图书推荐（包括书城内外的图书）
      - 文学、文化、历史等相关知识

    2. 根据用户需求推荐合适的图书，包括：
      - 优先推荐书城内的相关图书
      - 如果书城内没有合适的图书，可以推荐其他相关图书
      - 解释推荐理由，分享相关的文学知识

    3. 提供图书的详细信息，包括：
      - 书城内图书的完整信息（书名、描述、标签、评分等）
      - 书城外图书的基本信息（如作者、出版社、出版年份等）
      - 相关的文学背景和知识

    4. 解答关于阅读、作者、出版等相关问题

    在回答时，请注意：
    1. 对于书城内图书的问题：
      - 优先使用图书的基本信息（书名、描述、评分、价格、标签）
      - 参考该图书的评论信息
      - 告知当前可用的优惠券信息
      - 提供具体的库存数量
      - 提供详细的规格信息

    2. 对于书城外图书的问题：
      - 提供准确的图书信息（如作者、出版社、出版年份等）
      - 分享相关的文学知识或背景信息
      - 如果书城有类似主题的图书，可以适当推荐
      - 解释推荐理由，帮助用户做出选择

    3. 对于一般性的图书问题：
      - 提供专业的阅读建议
      - 分享相关的文学知识
      - 结合书城内的图书进行举例说明
      - 根据用户兴趣推荐合适的图书

    4. 如果用户的问题与图书完全无关，请礼貌地告知用户您主要回答与图书相关的问题

    请基于以上信息，灵活地回答用户的问题。在回答时，既要准确提供书城内图书的信息，也要能够分享更广泛的图书知识。`,

    Init: function (options) {
      // 动态设置后端URL
      this.config.backendUrl = (window.TOMATO_MALL_CONFIG?.apiBaseUrl + '/api') || '';
      this.config.model = options.model || '';
      this.config.key = options.key || '';
      this.config.img = options.img || '';
      this.config.userId = options.userId || null; // 初始化用户ID
      this.createUI();
      this.bindEvents(); 
      return this;
    },

    // 添加方法检查消息是否与图书相关
    isBookRelated: function (message) {
      message = message.toLowerCase();
      return this.bookKeywords.some(keyword => message.includes(keyword.toLowerCase()));
    },

    createUI: function () {
      // 创建样式
      const style = document.createElement('style');
      style.textContent = `
        .ai-chat-icon {
          position: fixed;
          bottom: 10px;
          right: 5px;
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background-color: transparent;
          border: 1.5px solid rgba(255, 126, 179, 0.6);
          box-shadow: 0 2px 8px rgba(255, 126, 179, 0.2);
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          z-index: 10001;
          transition: all 0.3s;
          backdrop-filter: blur(2px);
        }
        
        .ai-chat-icon:hover {
          transform: scale(1.1);
          border-color: rgba(255, 126, 179, 0.8);
          box-shadow: 0 2px 12px rgba(255, 126, 179, 0.4);
        }
        
        .ai-chat-icon svg {
          width: 24px;
          height: 24px;
          fill: rgba(255, 126, 179, 1);
        }
        
        .ai-chat-container {
          position: fixed;
          bottom: 90px;
          right: 300px;
          width: 350px;
          height: 500px;
          background-color: rgba(255, 245, 250, 0.95);
          border-radius: 10px;
          box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
          display: flex;
          flex-direction: column;
          z-index: 10001;
          overflow: hidden;
          transition: all 0.3s;
          opacity: 0;
          transform: translateY(20px);
          pointer-events: none;
        }
        
        .ai-chat-container.active {
          opacity: 1;
          transform: translateY(0);
          pointer-events: all;
        }
        
        .ai-chat-header {
          padding: 15px;
          background-color: rgba(2, 175, 255, 0);
          color: white;
          font-weight: bold;
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        
        .ai-chat-close {
          cursor: pointer;
        }
        
        .ai-chat-messages {
          flex: 1;
          padding: 15px;
          overflow-y: auto;
          display: flex;
          flex-direction: column;
        }
        
        .ai-chat-message {
          margin-bottom: 10px;
          max-width: 80%;
          word-wrap: break-word;
        }
        
        .ai-chat-message-user {
          align-self: flex-end;
          background-color: #ffe6f0;
          padding: 10px;
          border-radius: 10px 10px 0 10px;
        }
        
        .ai-chat-message-ai {
          align-self: flex-start;
          background-color: white;
          padding: 10px;
          border-radius: 10px 10px 10px 0;
        }
        
        .ai-chat-message-error {
          color: #ff4d7e;
          font-size: 0.9em;
          margin-top: 5px;
        }
        
        .ai-chat-input-container {
          padding: 15px;
          border-top: 1px solid #eee;
          display: flex;
        }
        
        .ai-chat-input {
          flex: 1;
          padding: 10px;
          border: 1px solid #ddd;
          border-radius: 20px;
          outline: none;
        }
        
        .ai-chat-input:focus {
          border-color: #ff7eb3;
          box-shadow: 0 0 0 2px rgba(255, 126, 179, 0.2);
        }
        
        .ai-chat-send {
          margin-left: 10px;
          padding: 10px 15px;
          background-color: #ff7eb3;
          color: white;
          border: none;
          border-radius: 20px;
          cursor: pointer;
        }
        
        .ai-chat-send:hover {
          background-color: #ff69b4;
        }

        .ai-chat-loading {
          display: inline-block;
          width: 20px;
          height: 20px;
          border: 2px solid #f3f3f3;
          border-radius: 50%;
          border-top: 2px solid #ff7eb3;
          animation: ai-spin 1s linear infinite;
          margin-right: 10px;
        }
        
        @keyframes ai-spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }

        .ai-chat-message h1 {
          font-size: 1.8em;
          margin: 0.5em 0;
          color: #333;
          border-bottom: 2px solid #eee;
          padding-bottom: 0.3em;
        }
        
        .ai-chat-message h2 {
          font-size: 1.5em;
          margin: 0.5em 0;
          color: #444;
          border-bottom: 1px solid #eee;
          padding-bottom: 0.3em;
        }
        
        .ai-chat-message h3 {
          font-size: 1.3em;
          margin: 0.5em 0;
          color: #555;
        }
        
        .ai-chat-message pre {
          background-color: #f6f8fa;
          padding: 1em;
          border-radius: 4px;
          overflow-x: auto;
        }
        
        .ai-chat-message code {
          font-family: Consolas, Monaco, 'Andale Mono', monospace;
          background-color: #f6f8fa;
          padding: 0.2em 0.4em;
          border-radius: 3px;
          font-size: 0.9em;
        }
        
        .ai-chat-message strong {
          font-weight: 600;
        }
        
        .ai-chat-message em {
          font-style: italic;
        }

        /* 修改下拉菜单样式 */
        .el-dropdown-menu {
          border-radius: 8px !important;
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1) !important;
          padding: 8px 0 !important;
          background: rgba(255, 255, 255, 0.95) !important;
          backdrop-filter: blur(10px) !important;
        }
        
        .el-dropdown-menu__item {
          display: flex !important;
          align-items: center !important;
          padding: 10px 20px !important;
          transition: all 0.3s ease !important;
        }
        
        .el-dropdown-menu__item:hover {
          background: linear-gradient(to right, rgba(255, 126, 179, 0.1), rgba(255, 182, 193, 0.1)) !important;
          color: #ff7eb3 !important;
        }
        
        .el-dropdown-menu__item .el-icon {
          margin-right: 10px !important;
          font-size: 18px !important;
        }

        @media (max-width: 768px) {
          .ai-chat-container {
            right: 10px;
            width: calc(100% - 20px);
          }
        }
      `;
      document.head.appendChild(style);

      // 创建图标
      const iconContainer = document.createElement('div');
      iconContainer.className = 'ai-chat-icon';

      if (this.config.img) {
        iconContainer.innerHTML = `<img src="${this.config.img}" style="width:100%; height:100%; object-fit:cover; border-radius:50%;" alt="AI助手">`;
      } else {
        iconContainer.innerHTML = `
        <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM17 16.5C17 17.33 16.33 18 15.5 18H8.5C7.67 18 7 17.33 7 16.5V15H17V16.5ZM17 14H7V9C7 8.17 7.67 7.5 8.5 7.5H15.5C16.33 7.5 17 8.17 17 9V14ZM10 11.5C10 12.05 9.55 12.5 9 12.5C8.45 12.5 8 12.05 8 11.5C8 10.95 8.45 10.5 9 10.5C9.55 10.5 10 10.95 10 11.5ZM16 11.5C16 12.05 15.55 12.5 15 12.5C14.45 12.5 14 12.05 14 11.5C14 10.95 14.45 10.5 15 10.5C15.55 10.5 16 10.95 16 11.5Z"/>
        </svg>
      `;
      }

      // 创建聊天容器
      const chatContainer = document.createElement('div');
      chatContainer.className = 'ai-chat-container';
      chatContainer.innerHTML = `
        <div class="ai-chat-header">
          <div></div>
          <div class="ai-chat-close" style="color: #606266;">✕</div>
        </div>
        <div class="ai-chat-messages"></div>
        <div class="ai-chat-input-container">
          <input type="text" class="ai-chat-input" placeholder="请输入您的问题...">
          <button class="ai-chat-send">发送</button>
        </div>
      `;

      document.body.appendChild(iconContainer);
      document.body.appendChild(chatContainer);

      // 存储DOM引用
      this.elements = {
        icon: iconContainer,
        container: chatContainer,
        messages: chatContainer.querySelector('.ai-chat-messages'),
        input: chatContainer.querySelector('.ai-chat-input'),
        sendButton: chatContainer.querySelector('.ai-chat-send'),
        closeButton: chatContainer.querySelector('.ai-chat-close')
      };

      // 添加欢迎消息
      this.addMessage('你好！我是番茄书城的AI助手，有关于图书的问题都可以向我咨询哦！', 'ai');
    },

    bindEvents: function () {
      // 点击图标打开聊天
      this.elements.icon.addEventListener('click', () => {
        this.elements.container.classList.add('active');
      });

      // 点击关闭按钮
      this.elements.closeButton.addEventListener('click', () => {
        this.elements.container.classList.remove('active');
      });

      // 发送消息
      const sendMessage = () => {
        const message = this.elements.input.value.trim();
        if (message) {
          this.addMessage(message, 'user');
          this.elements.input.value = '';
          this.sendToAI(message);
        }
      };

      this.elements.sendButton.addEventListener('click', sendMessage);

      this.elements.input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
          sendMessage();
        }
      });
    },

    addMessage: function (text, sender) {
      const messageElement = document.createElement('div');
      messageElement.className = `ai-chat-message ai-chat-message-${sender}`;

      // 处理Markdown语法
      let formattedText = text;

      // 去除开头的换行符（针对AI回复）
      if (sender === 'ai') {
        formattedText = formattedText.replace(/^\n+/, '');
      }

      // 处理标题 (### 标题)
      formattedText = formattedText.replace(/^### (.*$)/gm, '<h3>$1</h3>');
      formattedText = formattedText.replace(/^## (.*$)/gm, '<h2>$1</h2>');
      formattedText = formattedText.replace(/^# (.*$)/gm, '<h1>$1</h1>');

      // 处理加粗文本 (**text** 或 __text__)
      formattedText = formattedText.replace(/\*\*(.*?)\*\*|__(.*?)__/g, '<strong>$1$2</strong>');

      // 处理斜体文本 (*text* 或 _text_)
      formattedText = formattedText.replace(/\*(.*?)\*|_(.*?)_/g, '<em>$1$2</em>');

      // 处理代码块 (```text```)
      formattedText = formattedText.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>');

      // 处理行内代码 (`text`)
      formattedText = formattedText.replace(/`(.*?)`/g, '<code>$1</code>');

      // 处理换行符
      formattedText = formattedText.replace(/\n/g, '<br>');

      messageElement.innerHTML = formattedText;
      this.elements.messages.appendChild(messageElement);
      this.elements.messages.scrollTop = this.elements.messages.scrollHeight;
    },

    addLoadingIndicator: function () {
      const loadingElement = document.createElement('div');
      loadingElement.className = 'ai-chat-message ai-chat-message-ai';
      loadingElement.innerHTML = '<div class="ai-chat-loading"></div>正在思考...';
      loadingElement.id = 'ai-loading-indicator';

      this.elements.messages.appendChild(loadingElement);
      this.elements.messages.scrollTop = this.elements.messages.scrollHeight;
    },

    removeLoadingIndicator: function () {
      const loadingElement = document.getElementById('ai-loading-indicator');
      if (loadingElement) {
        loadingElement.remove();
      }
    },

    // 添加获取图书上下文的方法
    getBookContext: async function () {
      try {
        const contextUrl = this.config.userId 
          ? `${this.config.backendUrl}/ai-chat/context?userId=${encodeURIComponent(this.config.userId)}`
          : `${this.config.backendUrl}/ai-chat/context`;

        const response = await fetch(contextUrl);

        if (!response.ok) {
          return null;
        }
        const result = await response.json();

        if (result.code === '200' && result.data) {
          return result.data;
        } else {
          return null;
        }
      } catch (error) {
        return null;
      }
    },

    sendToAI: async function (message) {
      // 检查配置
      if (!this.config.model || !this.config.key) {
        this.addMessage('错误：请先设置 model 和 key', 'ai');
        return;
      }

      this.addLoadingIndicator();

      try {
        // 获取图书上下文和对话历史
        const contextUrl = this.config.userId 
          ? `${this.config.backendUrl}/ai-chat/context?userId=${encodeURIComponent(this.config.userId)}`
          : `${this.config.backendUrl}/ai-chat/context`;
        
        const contextResponse = await fetch(contextUrl);
        if (!contextResponse.ok) {
          throw new Error('获取上下文失败');
        }
        const contextResult = await contextResponse.json();
        
        if (contextResult.code !== '200' && contextResult.code !== 200) {
          throw new Error('获取上下文失败: ' + contextResult.message);
        }

        const systemPrompt = contextResult.data;

        // 发送消息到AI服务
        const response = await fetch(this.config.apiUrl, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.config.key}`
          },
          body: JSON.stringify({
            model: this.config.model,
            messages: [
              {
                role: 'system',
                content: systemPrompt
              },
              {
                role: 'user',
                content: message
              }
            ]
          })
        });

        const data = await response.json();
        this.removeLoadingIndicator();

        if (data.error) {
          const errorMsg = `错误：${data.error.message || '请求失败'}\n请检查您选择的模型类型，部分模型不支持赠送金额`;
          this.addMessage(errorMsg, 'ai');
          return;
        }

        if (data.choices && data.choices.length > 0) {
          const reply = data.choices[0].message.content;
          this.addMessage(reply, 'ai');

          // 如果有用户ID，保存对话历史
          if (this.config.userId) {
            try {
              await fetch(`${this.config.backendUrl}/ai-chat/conversation`, {
                method: 'POST',
                headers: {
                  'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({
                  userId: this.config.userId,
                  question: message,
                  answer: reply
                })
              });
            } catch (error) {
              console.error('保存对话历史失败:', error);
            }
          }
        } else {
          this.addMessage('抱歉，我无法处理您的请求\n请检查您选择的模型类型，部分模型不支持赠送金额', 'ai');
        }
      } catch (error) {
        this.removeLoadingIndicator();
        this.addMessage(`错误：${error.message || '请求失败'}\n请检查您选择的模型类型，部分模型不支持赠送金额`, 'ai');
      }
    },

    // 设置用户ID的方法
    setUserId: function(userId) {
      this.config.userId = userId;
    },

    // 清除对话历史的方法
    clearHistory: async function() {
      if (!this.config.userId) {
        return;
      }

      try {
        const response = await fetch(
          `${this.config.backendUrl}/ai-chat/conversation/history?userId=${encodeURIComponent(this.config.userId)}`,
          { method: 'DELETE' }
        );
        
        if (!response.ok) {
          throw new Error('清除历史记录失败');
        }
        
        // 清空聊天界面
        this.elements.messages.innerHTML = '';
        // 重新添加欢迎消息
        this.addMessage('你好！我是番茄书城的AI助手，有关于图书的问题都可以向我咨询哦！', 'ai');
      } catch (error) {
        console.error('清除历史记录失败:', error);
        this.addMessage('清除历史记录失败，请稍后重试', 'ai');
      }
    }
  };

  // 暴露到全局
  window._Ai = _Ai;
})(); 