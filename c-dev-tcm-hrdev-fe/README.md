# MDF 4.1

20250521


- 文档地址：https://gfwiki.yyrd.com/x/JJBy
- 新工程请使用此 MDF4.1 脚手架，原有旧工程升级统一 Node 和去 IFrame 请移步：[《性能优化去除 iframe 脚手架升级方案》](https://www.yuque.com/docs/share/8ec6edc7-20a4-42ca-8c33-1dae26cd52ed?#)




   ```bash
   $ npm install ynpm-tool -g
   ```


   ```bash
   $ ynpm install
   ```

   > 确保安装器中已安装中间件 YonBuilderProNPMRegistry

   ```bash
   $ ynpm install --registry=${<域名>}/artifactory/api/npm/ynpm-all/
   ```


`src/common/config.env.js` 配置全局配置的环境变量
   - DOMAIN_KEY 领域应用的domainKey
   - HTTP_SERVICE_BASEURL Java服务地址(仅本地调试和独立Node需要配置)
   - HTTP_MAIN_ORIGIN 主站域名，即业务中台域名（仅专属化无外网本地调试需要配置）


- MDF4.1 启动两个服务、前端和后端 node.js 部分，命令整合之后可以启动一套命令；
- MDF4.1 部署方式有两种，一种是独立 Node、另一种则是统一 Node，官方`强烈建议`使用统一 Node；
- 如果流水线和统一 Node 启用了扩展资源上传 OSS，则只需要启动前端 debug:extend 命令即可，详细请移步：[《领域前端构建过程优化指南》](https://www.yuque.com/docs/share/54e291d8-8681-4777-bea6-5a4f34b61b11?#)
- 统一 Node 中内置了大量的变量可配置，如：是否使用异步导出、是否开启缓存、是否开启 UI 模板，详细请移步：[《统一 Node 配置项说明》](https://gfwiki.yyrd.com/x/O-v4AQ)；

**用户可根据部署环境不同，自由扩展；需要在`src/common/config.env.js`中配置对应的服务地址**

本地调试启动：

```bash
# 1. 启动默认调试，会开启前后端服务，默认接口为src/web/common/config.env.js中的daily
npm run debug

# 2. 单独启动前端工程
npm run debug:extend

# 3. 单独启动后端node.js服务
npm run debug:server
```

部署上线服务：

```bash
# 1. 构建web端部署
npm run build

# 2. 启动服务
npm run start
```



MDF 脚手架默认是没有开启代码检查的，升级到 MDF 4.1 版本脚手架后可以执行 npx init-lint 命令初始化代码检查（如有报错请拉取最新的依赖）
初始化后会在每次代码提交时会针对变更的文件进行 eslint 规范检查并自动修复
如需对全部文件进行代码检查可以手动执行 npm run lint 和 npm run lint:fix 命令

```bash
npx init-lint
```


package.json 文件中的构建命令中添加 ANALYZE 参数，如：

```bash
"build:extend": "cross-env BABEL_ENV=production NODE_ENV=production ANALYZE=true ZIP=true MDF_LANG=true node --max-old-space-size=4096 node_modules/webpack/bin/webpack.js --config webpack.config.js --progress && echo '前端扩展：编译完成'",
```


专属化默认屏蔽了所有 sourcemap，公有云默认开放了低品质的 sourcemap，如需调试进行以下操作：

https://xxx/mdf-node/sourcemap/mdfjs/666666 浏览器访问开启源码（包含框架和领域的）
https://xxx/?debug=true 开启调试模式和高品质源码




为减小构建产物体量，组件包不再单独引入，如果需要使用 metaui-web 或 metaui-mobile 中的组件，可通过控制台打印 cb.components 查看支持的内容，引入示例如下：

```js
1）组件依赖
PC端：
const { Form, Button, Card } = window.YonuiYs; // 不需要单独引入yonui-ys
const { TreeRefer, Label, Input } = require('mdf-metaui-web').basic; // 不需要单独引入@mdf/metaui-web
移动端：
const { Button, DatePicker } = window.AntdMobile; // 需要单独引入@mdf/baseui-mobile
const { Refer } = require('mdf-metaui-mobile').basic; // 不需要单独引入@mdf/metaui-mobile

2）脚本依赖
const { setMode, getWebUrl } = viewmodel.biz.action().common; // 不需要单独引入@mdf/cube
```


修改 package.json 中的 domainKey 为本领域，由专业版引擎创建的 domainKey 无需修改；


```bash
移动端访问简易门户（PC无）：
环境地址：用 cb.utils.getNodeServerUrl() 在控制台打印出来就是

访问统一Node单据页面（PC和移动通用）：
示例：https://<工作台域名>/mdf-node/meta/voucherlist/st_purchaseorderlist?domainKey=upu

访问本地单据页面（PC和移动通用）：
示例：http://local.xxx.com:3003/meta/voucherlist/st_purchaseorderlist
```


- 方式 1：在 url 上添加 `scriptUrl=http://localhost:3004/static` 参数即可加载本地扩展代码
- 方式 2：通过`Resource Override`替换线上资源，步骤如下：
  - 谷歌浏览器安装 Resource Override，[下载地址](https://github.com/kylepaulsen/ResourceOverride)
  - 导入 resource_override_rules.json 配置文件


```bash
process.env.MDF_RESOURCE_VERSION
process.env.MDF_MOBILE_RESOURCE_VERSION
```

独立 Node 需要在 package.json 或流水线环境变量中配置框架版本，以上具体变量值请向 MDF 框架开发团队索取


需要区分 MDF 脚手架和 MDF 框架版本是不同的概念。MDF 脚手架版本是指 yonyou-mdf-server-app 的版本，主要体现在@mdf/create-app 包的版本；MDF 框架的版本是 yonyou-mdf-framework 的版本。


常见的脚手架版本：

- 1.0 早期 yxyweb 的版本（未分包时期）
- 2.0 19 年-20 的脚手架版本（分包后）
- 3.0 21 年的版本（统一去 iframe 之后的版本）
- 4.1 22 年的版本（统一接入 TNS 后的版本）


DMF 框架版本比较复杂，详见：《[MDF 框架版本说明](https://gfwiki.yyrd.com/x/O-v4AQ)》



1. 扩展脚本在`src/business`中扩展

2. 扩展组件在`src/client/web|mobile/components`中扩展

3. 扩展 reducers 在`src/client/mobile/redux/reducers.jsx`中扩展

4. 扩展路由在`src/client/mobile/routes/index.jsx`中扩展

5. 扩展样式在`src/client/mobile/styles`中扩展

6. 扩展 Action`src/client/common/biz/actions.js`中扩展

7. 在`src/client/web|mobile.jsx`中还可以注入多语资源和变量

详见：《[领域前端扩展开发](https://gfwiki.yyrd.com/x/Osft)》


《[MDF 4.1 脚手架升级指南#Webpack 配置](https://gfwiki.yyrd.com/x/ffg8AQ)》
《[统一三方包TNS接入指南](https://docs.yonyoucloud.com/l/4cde7dc9E79f)》



方案一：服务中使用的第三方资源 或 租户级扩展资源可以放到脚手架/static/public/resources 目录下，此方式无版本处理，使用者可在 resources 目录下创建 1.0.0、1.0.1 多个目录用来管理版本

方案二：服务中使用的第三方资源 或 租户级扩展资源可以放到脚手架/static/public/<domainKey>目录下，此方式资源随扩展脚本资源一起放到对应的版本目录下，使用时需要使用 cb.cache.baseUrls.get(domainKey)获取版本号（前提是打开过当前领域的单据）


方案一：

查看项目流水线的镜像构建的日志,镜像构建环节日志中，找到静态资源上传的地址

方案二：
const resourceUrl = cb.cache.baseUrls.get(domainKey) + '/resource/1.0.0/index.html'

更多环境，如专属化环境的资源管理，详见《[MDF 扩展资源统一管理](https://gfwiki.yyrd.com/x/1oNXAQ)》


1. 可自行编写 webpack 或 shell 代码将想要资源构建到以上目录下
2. 如果 resources 目录被 git 忽略，可更新.gitignore



如果控制台信息中报 call...字样的错误，则说明是 MANIFEST 版本不对
此时应注意区分，调试本地 Node 中的代码用 npm run debug:extend，调试线上 Node 中的代码应该用 npm run debug:extend:prod
两个命名的区分就是 MANIFEST 不一样

注意：确保扩展文件的文件夹和文件名正确，曾经有人因为文件夹的名称前面多了个空格导致加载不到


扩展脚本路径中的版本号丢失，如：https://<origin>/mdf-wh/<domainKey>//javascripts/mobile.extend.min.js

这种问题一般只有 2 个原因： 1. nginx 的 mdf-webhome 目录下如果没有<domainKey>对应的目录，则是对应服务的制品安装有问题（服务制品本身有问题、安装器有问题） 2. nginx 的 mdf-webhome 目录下如果有<domainKey>对应的目录，则是 nginx 转发有问题

详见：《[MDF 扩展资源统一管理](https://gfwiki.yyrd.com/x/1oNXAQ)》


此问题应先排查是否问题 1 导致的，判断依据就是刷新整个浏览器，再打开单据看控制台有无 call...字样的错误
如果不是上面的问题，则就一定是扩展代码有真正的语法错误，此时还是找控制台报错信息，一定会有其它的报错信息
再有就是排查扩展脚本第一行 cb.define 的第一个参数是否 process.env.**DOMAINKEY**



如果有`Uncaught (in promise) Error: Module xxx at tns3nd.js`错误，则需要更新 TNS 补丁


如果接口有报错，则看错误信息，大概率是 redis 问题


在 https 下加载了 http 的资源，联系运维修复 yms 中 domain.url 配置为 https 的


如果控制台中有`Uncaught (in promise) ReferenceError: TinperNext is not defined`错误:
方法一：锁定@mdf/create-app 的版本为当前环境版本（通过业务中台控制台输入 mdfjs.version 查看）；如 mdfjs.version 输出的 branch 为 release/3.9，则把脚手架 package.json 中@mdf/create-app 后面的版本号改成 3.9
方法二：更新 TNS 补丁，补丁中心搜索对应版本的 TNS 补丁安装
