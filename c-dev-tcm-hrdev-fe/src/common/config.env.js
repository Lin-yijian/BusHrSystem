const packageJson = require('../../package.json');

/**
 * 全局配置的环境变量
 * 1. DOMAIN_KEY 领域应用的domainKey
 * 2. HTTP_SERVICE_BASEURL Java服务地址(仅本地调试和独立Node需要配置)
 * 3. HTTP_MAIN_ORIGIN 主站域名，即业务中台域名（仅专属化无外网本地调试需要配置）
 */
const config = {
  DOMAIN_KEY: packageJson.domainKey || '',
  // HTTP_SERVICE_BASEURL: '', // 本地调试和独立Node时必输，如：https://bip-daily.yyuap.com/yonbip-xxx-xxx
  // HTTP_MAIN_ORIGIN: '', // 专属化无外网本地调试必输
  
  /**
   * 其它常用可选配置（详细说明查看《MDF服务配置项说明文档》）
   */
  // MDF_RESOURCE_VERSION: 'release-3.1', // MDF框架Web端版本
  // MDF_MOBILE_RESOURCE_VERSION: 'release-3.1', // MDF框架移动端版本
  // dependencies: ['developplatform'], // 依赖的其它领域前端扩展资源
  // customRoutes: ['/demo'], // 扩展路由
  // proxyRoutes: [], // 代理转发
  // AUTH_WHITELIST: ['/demo'], // 免授权白名单
  // MOBILE_SHOW_VCONSOLE: 'true' // 移动端是否显示vConsole
}
module.exports = config;
