// 注册扩展脚本
const businessContext = require.context("business");
cb.extend.registerScripts(process.env.__DOMAINKEY__, businessContext);

// 本地调试和普通打包时加载扩展组件，组件单独打包时扩展组件不在此处注册
if (!process.env.__EXTENDCOMP__) {
  // 注册扩展组件
  const extendComponents = require("./components").default;
  cb.extend.registerComponents(process.env.__DOMAINKEY__, extendComponents);
}

// 注册reducer
const extendReducers = require("./redux/reducers").default;
cb.extend.registerReducers(process.env.__DOMAINKEY__, extendReducers);

// 注册router
const extendRoutes = require("./routes").default;
cb.extend.registerRoutes(process.env.__DOMAINKEY__, extendRoutes);

// 注册扩展action
const extendBizAction = require("../common/biz/actions").default;
cb.extend.registerBizAction(process.env.__DOMAINKEY__, extendBizAction);

// 注册变量（框架使用的变量前后添加__）此方式和在Consul中配置等效且优先级大于Consul
cb.extend.registerEnv(process.env.__DOMAINKEY__, { // registerEnv的第2个参数可以是个方法，接收一个当前环境env参数
  currentEnv: "daily" // 领域自定义变量示例（小驼峰规范），变量使用：viewmodel.getEnv('currentEnv')
});

// 注册多语
cb.lang.registerLang(process.env.__DOMAINKEY__, require("../../pack"), "");


