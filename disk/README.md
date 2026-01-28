1：目录说明
   log：脚本执行过程中产生的日志文件。
   output：最终生成的产品盘存放目录，目录下存在产品盘的源文件和压缩文件。
   package: 微服务制品存放目录，下级目录名使用微服务编码命名，目录下存放本地打包的微服务构建产物。
   setup：存放的是产品级的配置，nginx模板、安装模式配置地址、redis清理规则文件,分别对应的目录名如下
          nginx模板:  nginx
		  安装模式配置地址: deploy
		  redis清理规则文件: redis
   application.properties: 工具的配置文件，主要配置产品盘的相关信息
   iuap-ypr-ctl.jar   脚本执行工具
 
2: 启动命令:
    切换到iuap-ypr-ctl.jar所在目录，执行如下命令：

    制作产品盘: java -jar iuap-ypr-ctl.jar product

    制作QP补丁: java -jar iuap-ypr-ctl.jar patch --baseline-product-serialNum 20240826143219 (可简写为:java -jar iuap-ypr-ctl.jar patch 20240826143219)
	
	
3：使用注意事项
   a) 本地构建产物最好包含module.xml文件，尤其是yms服务，本地打包后会生成  xxx.war和 xxx-module.war两个文件，这里需要放
       xxx-module.war文件。
	   
   
	
	
	
	
	