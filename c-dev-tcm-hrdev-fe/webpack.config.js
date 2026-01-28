const common = require('@mdf/create-app/lib/webpack.common');
const parts = require('@mdf/create-app/lib/webpack.parts');

const configEnv = require("./src/common/config.env");

const PRIVATE_HOST = "https://bip-dev.namkwong.com.mo";
const domainDefine = parts.domainDefine({
  domainKey: configEnv.DOMAIN_KEY, // 领城 domainKey
  // enableTNS: false, // 关闭 TNS
  tnsOptions: {
    // libraryEnv;'online', // TNS 参数//自定义远程插件服务环境为专属化环境
    remotePluginEnv:PRIVATE_HOST,
    remoteProviderEnv:PRIVATE_HOST
  }
})
module.exports = parts.uniquePlugin(common, domainDefine)