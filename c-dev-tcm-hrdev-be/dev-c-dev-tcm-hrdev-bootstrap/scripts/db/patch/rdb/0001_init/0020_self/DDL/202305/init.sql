
SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for aa_billcode_candidateprop
-- ----------------------------

CREATE TABLE IF NOT EXISTS `aa_billcode_candidateprop` (
    `autoid` bigint(20) NOT NULL  COMMENT '主键',
    `pk_bcr_obj` varchar(200) DEFAULT NULL COMMENT '编码对象主键',
    `en_prop_name` varchar(200) DEFAULT NULL COMMENT '实体属性名',
    `display_name` varchar(200) DEFAULT NULL COMMENT '显示名称',
    `elem_type` varchar(200) DEFAULT NULL COMMENT '类型（时间，字符，参照）',
    `mapping_entity` varchar(200) DEFAULT NULL COMMENT '所属编码映射实体',
    `pubts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    `mapping_entity_name` varchar(200) DEFAULT NULL COMMENT '引用实体名称',
    `display_name2` varchar(200) DEFAULT NULL COMMENT '显示名称',
    `display_name3` varchar(200) DEFAULT NULL COMMENT '显示名称',
    `display_name4` varchar(200) DEFAULT NULL COMMENT '显示名称',
    `display_name5` varchar(200) DEFAULT NULL COMMENT '显示名称',
    `display_name6` varchar(200) DEFAULT NULL COMMENT '显示名称',
    `display_name_ext` varchar(200) DEFAULT NULL COMMENT '显示名称',
    PRIMARY KEY (`autoid`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    ) COMMENT='候选属性表';

-- ----------------------------
-- Table structure for aa_billcode_mapping
-- ----------------------------

CREATE TABLE IF NOT EXISTS `aa_billcode_mapping` (
    `autoid` bigint(20) NOT NULL  COMMENT '主键',
    `name` varchar(200) DEFAULT NULL COMMENT '名称',
    `domain` varchar(200) NOT NULL COMMENT '应用编码',
    `ref_code` varchar(200) NOT NULL COMMENT '参照编码',
    `md_en_id` varchar(200) NOT NULL COMMENT '元数据实体ID',
    `map_length` int(11) NOT NULL COMMENT '映射值长度',
    `fill_style` tinyint(1) NOT NULL DEFAULT '0' COMMENT '补位方式，0不补位 1左补位 2右补位',
    `fill_char` varchar(1) DEFAULT NULL COMMENT '补位符号',
    `map_prop_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '映射的值是mappingchild中的pk，code还是name，pk为0 code为1  name为2',
    `pubts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenantid` varchar(255) NOT NULL,
    `sysid` varchar(255) DEFAULT NULL,
    `default_map_value` varchar(255) DEFAULT NULL COMMENT '默认映射值',
    `label` varchar(255) DEFAULT NULL COMMENT '标签 (为了处理组织类的元数据通过标签来匹配)',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    `ref_parameter` varchar(255) DEFAULT NULL,
    `isvTag` VARCHAR(64) DEFAULT NULL COMMENT 'isv标志',
    PRIMARY KEY (`autoid`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    )COMMENT='编码映射主表';

-- ----------------------------
-- Table structure for aa_billcode_mappingchild
-- ----------------------------

CREATE TABLE IF NOT EXISTS `aa_billcode_mappingchild` (
    `autoid` bigint(20) NOT NULL  COMMENT '主键',
    `pk_map_id` int(11) DEFAULT NULL COMMENT '映射主表主键值',
    `pk_ref` varchar(200) DEFAULT NULL COMMENT '属性值（参照属性主键值）',
    `map_val` varchar(200) DEFAULT NULL COMMENT '编码',
    `ref_code` varchar(255) DEFAULT NULL,
    `ref_name` varchar(255) DEFAULT NULL,
    `pubts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `ref_code_display` varchar(200) DEFAULT NULL,
    `tenantid` varchar(200) DEFAULT NULL,
    `sysid` varchar(200) DEFAULT NULL,
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    PRIMARY KEY (`autoid`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    )COMMENT='aa_billcode_mappingchild';

-- ----------------------------
-- Table structure for aa_billcode_obj
-- ----------------------------

CREATE TABLE IF NOT EXISTS `aa_billcode_obj` (
    `pk_billobj` varchar(40) NOT NULL,
    `code` varchar(40) DEFAULT NULL,
    `name` varchar(40) DEFAULT NULL,
    `name2` varchar(40) DEFAULT NULL,
    `name3` varchar(40) DEFAULT NULL,
    `name4` varchar(40) DEFAULT NULL,
    `name5` varchar(40) DEFAULT NULL,
    `name6` varchar(40) DEFAULT NULL,
    `name_ext` varchar(40) DEFAULT NULL,
    `createdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `tenantid` varchar(64) DEFAULT NULL COMMENT '租户id，当租户值为''system''时，表示该实体为系统级',
    `sysid` varchar(64) DEFAULT NULL,
    `classify_code` varchar(64) DEFAULT NULL COMMENT '该编码实体所属的二级分类树code',
    `service_code` varchar(64) DEFAULT NULL COMMENT '关联diwork的原子服务code',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    PRIMARY KEY (`pk_billobj`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    )COMMENT='aa_billcode_obj';

-- ----------------------------
-- Table structure for aa_billcondition
-- ----------------------------

CREATE TABLE IF NOT EXISTS `aa_billcondition` (
    `autoid` bigint(20) NOT NULL  COMMENT '主键 数据库自增',
    `cbillnum` varchar(64) NOT NULL COMMENT '单据billNum',
    `ccondition` varchar(255) NOT NULL COMMENT '条件字段名称',
    `cconditionresid` varchar(255) DEFAULT NULL COMMENT '多语资源ID',
    `cconditionid` varchar(64) DEFAULT NULL COMMENT '条件ID aa_billprefabricate表预制数据的UUID',
    `cconditiontype` int(11) NOT NULL COMMENT '条件类型 0=字符串 1=日期 2=数字',
    `cconditionvalue` text COMMENT '条件值',
    `csourcename` varchar(255) DEFAULT NULL COMMENT '即datasourceName 取aa_billprefabricate表里的cdatasourceName',
    `cfieldname` varchar(255) NOT NULL COMMENT '即cFieldName 取aa_billprefabricate表里的cFieldName',
    `bMain` int(11) DEFAULT '1' COMMENT '是否主表字段 0=否 1=是',
    `billnumberid` bigint(20) DEFAULT NULL COMMENT '编码规则主键 编码规则主键',
    `billnumberCode` varchar(50) NOT NULL COMMENT '规则编码 关联编码规则主表',
    `orgId` varchar(64) NOT NULL COMMENT '组织ID',
    `tenant_id` bigint(20) DEFAULT NULL COMMENT '租户 系统租户=0',
    `yhtTenantId` varchar(36) DEFAULT NULL COMMENT '友户通租户',
    `logical` varchar(10) DEFAULT NULL COMMENT '逻辑符 &&、||',
    `operator` varchar(10) NOT NULL COMMENT '操作符 ==、<、>、<=、>=',
    `formula` varchar(500) DEFAULT NULL COMMENT '公式',
    `formuladisplay` varchar(500) DEFAULT NULL COMMENT '公式显示',
    `iorder` int(11) NOT NULL COMMENT '顺序 条件顺序',
    `dr` int(11) DEFAULT NULL COMMENT '删除标记 1=已删除，0=未删除',
    `pubts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳 当前时间',
    `refPropType` tinyint(4) DEFAULT NULL COMMENT '属性为参照时，值类型：0=id，1=code，2=name',
    `refDisplayName` varchar(255) DEFAULT NULL COMMENT '参照显示名称',
    `cmdId` varchar(255) DEFAULT NULL COMMENT '参照实体元数据id',
    `refId` text COMMENT '参照id',
    `refCode` text COMMENT '参照编码',
    `refName` text COMMENT '参照名称',
    `refDataCode` text COMMENT '参照数据编码',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    PRIMARY KEY (`autoid`),
    KEY `ix_aa_billcondition_cbillnum_tenantId` (`cbillnum`,`tenant_id`),
    KEY `idx_ytenant_id` (`ytenant_id`),
    KEY `ix_aa_billcondition_cbillnum_yhtTenantId` (`cbillnum`,`yhtTenantId`),
    KEY `i_aa_billcondition_cbillnum_ytenantId` (`cbillnum`,`ytenant_id`)
    )COMMENT='条件数据表';

-- ----------------------------
-- Table structure for aa_billcontrolrule
-- ----------------------------

CREATE TABLE IF NOT EXISTS `aa_billcontrolrule` (
    `cBillNum` varchar(40) DEFAULT NULL COMMENT '表单编码',
    `cChildrenField` varchar(1000) DEFAULT NULL COMMENT '子表集合属性',
    `cItemName` varchar(200) DEFAULT NULL COMMENT '字段别名',
    `cCaption` varchar(255) DEFAULT NULL,
    `bVisible` bit(1) DEFAULT b'0' COMMENT '是否可见',
    `bEditable` bit(1) DEFAULT b'0' COMMENT '是否可编辑',
    `cRegisterType` varchar(20) DEFAULT NULL COMMENT '开通类型',
    `iControlType` smallint(1) DEFAULT NULL COMMENT '管控类型',
    `bCopyData` bit(1) DEFAULT NULL,
    `bCanBatchEdit` bit(1) DEFAULT b'0',
    `id` bigint(20) NOT NULL  COMMENT 'ID',
    `pubts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
    `tenant_id` bigint(20) NOT NULL COMMENT '租户',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `create_date` date DEFAULT NULL COMMENT '创建日期',
    `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
    `modify_date` date DEFAULT NULL COMMENT '修改日期',
    `creator` varchar(255) DEFAULT NULL COMMENT '创建人',
    `modifier` varchar(255) DEFAULT NULL COMMENT '修改人',
    `stopstatus` bit(1) DEFAULT b'0' COMMENT '停用状态',
    `stop_time` datetime DEFAULT NULL COMMENT '停用时间',
    `creatorId` bigint(20) DEFAULT NULL COMMENT '创建人',
    `modifierId` bigint(20) DEFAULT NULL COMMENT '修改人',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    PRIMARY KEY (`id`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    )COMMENT='aa_billcontrolrule';

-- ----------------------------
-- Table structure for aa_billhistory
-- ----------------------------

CREATE TABLE IF NOT EXISTS `aa_billhistory` (
    `autoid` bigint(20) NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint(20) DEFAULT NULL,
    `orgId` varchar(64) DEFAULT '-1',
    `cbillnum` varchar(100) NOT NULL COMMENT '表单编码',
    `ownerorg` bigint(20) DEFAULT NULL COMMENT '所属组织',
    `cglide` varchar(1000) DEFAULT NULL COMMENT '前缀名称',
    `cgliderule` varchar(100) DEFAULT NULL COMMENT '取值规则',
    `cseed` varchar(100) NOT NULL COMMENT '编号前缀',
    `inumber` int(11) DEFAULT NULL COMMENT '流水号',
    `letterNumber` int(11) DEFAULT NULL COMMENT '字母流水号',
    `totalBasis` varchar(100) NOT NULL COMMENT '总依据(依据1|依据2|依据3|...)',
    `billnumberid` bigint(20) DEFAULT NULL,
    `dr` tinyint(1) DEFAULT NULL COMMENT '删除标记',
    `pubts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
    `yhtTenantId` varchar(36) DEFAULT NULL COMMENT '友户通租户',
    `billnumberCode` varchar(150) DEFAULT NULL COMMENT '规则code',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    `cglidename` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '流水依据名称',
    PRIMARY KEY (`autoid`),
    KEY `cbillnum` (`tenant_id`,`cbillnum`,`cseed`),
    KEY `ix_aa_billhistory_cbillnum_cglide` (`cbillnum`,`cglide`(255)),
    KEY `idx_ytenant_id` (`ytenant_id`),
    KEY `ix_aa_billhistory_cbillnum_bnCode_orgId_tenantId` (`cbillnum`,`billnumberCode`,`orgId`,`tenant_id`),
    KEY `i_aa_billhistory_ytenantId_cbillnum_cseed` (`ytenant_id`,`cbillnum`,`cseed`),
    KEY `i_aa_billhistory_cbn_bnCd_org_ytenantId` (`cbillnum`,`billnumberCode`,`orgId`,`ytenant_id`)
    ) COMMENT='流水号';

-- ----------------------------
-- Table structure for aa_billnumber
-- ----------------------------

CREATE TABLE IF NOT EXISTS `aa_billnumber` (
    `autoid` bigint(20) NOT NULL  COMMENT '主键',
    `tenant_id` bigint(20) DEFAULT '0' COMMENT '租户',
    `orgId` varchar(64) DEFAULT '-1',
    `cbillnum` varchar(100) NOT NULL COMMENT '作为规则编码，表单模式下是表单编码，元数据模式下是元数据ID，自定义实体模式下是自定义实体主键ID',
    `cbillname` varchar(50) NOT NULL COMMENT '表单名称',
    `csubid` varchar(64) DEFAULT NULL,
    `ballowhandwork` tinyint(4) NOT NULL COMMENT '允许手动编码',
    `brepeatredo` tinyint(4) NOT NULL COMMENT '允许手工可改',
    `istartnumber` int(11) NOT NULL COMMENT '流水号初始值',
    `iseriallen` tinyint(4) NOT NULL COMMENT '流水号长度',
    `billnumLen` int(11) NOT NULL DEFAULT '8' COMMENT '流水号长度',
    `billnumInit` int(11) NOT NULL DEFAULT '1' COMMENT '流水号初始值',
    `billnumTruncatType` int(11) NOT NULL DEFAULT '0' COMMENT '截断类型0 = 左截断 1 = 右截断',
    `billnumFillType` int(11) NOT NULL DEFAULT '0' COMMENT '补位类型0=不补位 1=左补位 2=右补位',
    `billnumFillMark` varchar(20) NOT NULL DEFAULT '0' COMMENT '补位符',
    `billnumMode` int(11) NOT NULL DEFAULT '0' COMMENT '0=手工编号 1=自动编号 2=自动编号 手工可改',
    `billnumRule` int(11) NOT NULL DEFAULT '0' COMMENT '使用规则 0 企业默认 1 自定义规则',
    `isReuse` tinyint(1) DEFAULT '0' COMMENT '是否开启退号补号',
    `sysid` varchar(40) NOT NULL COMMENT '系统ID',
    `datatype` tinyint(4) NOT NULL DEFAULT '2' COMMENT '编码实体类型，1：表单，2：元数据，3：自定义实体',
    `rulecode` varchar(100) NOT NULL COMMENT '规则编码',
    `rulename` varchar(255) DEFAULT NULL COMMENT '规则名称',
    `dr` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标记',
    `pubts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
    `yhtTenantId` varchar(36) DEFAULT NULL COMMENT '友户通租户',
    `sntype` tinyint(4) NOT NULL DEFAULT '0',
    `isDefault` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否默认，0否 1是',
    `code` varchar(50) DEFAULT NULL,
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    `isvTag` VARCHAR(64) DEFAULT NULL COMMENT 'isv标志',
    `snFillType` tinyint DEFAULT '1' COMMENT '流水号补码类型：0-不补码，1-左边补0',
    `codeTime` tinyint(4) DEFAULT '1' COMMENT '编码时机：1-保存时，2-页面生成',
    `applicationCode` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '应用编码',
    `micro_service_code` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '微服务编码',
    `complexSwitch` int(11) DEFAULT '1' COMMENT '多开关复用的复合开关：1-表示编码验重',
    `excludedCharacters` varchar(64) DEFAULT '' COMMENT '排除字母(字母流水)',
    `billnumberCode` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规则编码 关联编码规则主表',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
    `creator` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
    `modifier` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
    PRIMARY KEY (`autoid`),
    KEY `ix_aa_billnumber_code_orgId_tenantId` (`code`,`orgId`,`yhtTenantId`),
    KEY `ix_aa_billnumber_cbillnum_orgid_tenantId` (`cbillnum`,`orgId`,`tenant_id`),
    KEY `idx_ytenant_id` (`ytenant_id`),
    KEY `ix_aa_billnumber_rulecode` (`rulecode`),
    KEY `ix_aa_billnumber_cbillnum` (`cbillnum`),
    KEY `i_aa_billnumber_cbillnum_orgid_ytenantId` (`cbillnum`,`orgId`,`ytenant_id`),
    KEY `i_aa_billnumber_code_orgId_ytenantId` (`code`,`orgId`,`ytenant_id`)
    )COMMENT='编码规则-主表';

-- ----------------------------
-- Table structure for aa_billprecode
-- ----------------------------

CREATE TABLE IF NOT EXISTS `aa_billprecode` (
    `autoid` bigint(20) NOT NULL  COMMENT '主键',
    `tenant_id` bigint(20) DEFAULT '0' COMMENT '租户',
    `orgId` varchar(64) DEFAULT '-1',
    `cbillnum` varchar(100) DEFAULT NULL COMMENT '表单编码',
    `ownerorg` bigint(20) DEFAULT NULL COMMENT '所属组织',
    `cglide` varchar(250) DEFAULT NULL COMMENT '前缀名称',
    `cgliderule` varchar(250) DEFAULT NULL COMMENT '取值规则',
    `cseed` varchar(250) NOT NULL COMMENT '编号前缀',
    `inumber` int(11) NOT NULL COMMENT '流水号',
    `totalBasis` varchar(500) NOT NULL COMMENT '总依据(依据1|依据2|依据3|...)',
    `billnumberid` bigint(20) DEFAULT NULL COMMENT '规则id（外键）',
    `billcode` varchar(250) DEFAULT NULL COMMENT '编码号',
    `dr` tinyint(1) DEFAULT NULL COMMENT '删除标记',
    `pubts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
    `yhtTenantId` varchar(36) DEFAULT NULL COMMENT '友户通租户',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    PRIMARY KEY (`autoid`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    )COMMENT='预取表';

-- ----------------------------
-- Table structure for aa_billprefabricate
-- ----------------------------

CREATE TABLE IF NOT EXISTS `aa_billprefabricate` (
    `autoid` bigint(20) NOT NULL AUTO_INCREMENT,
    `cbillnum` varchar(100) NOT NULL COMMENT '表单编码',
    `cprefix` varchar(50) NOT NULL COMMENT '前缀名字',
    `cprefixid` varchar(50) NOT NULL COMMENT '唯一uid',
    `cprefixtype` int(11) NOT NULL COMMENT '前缀类型 0=字符串 1=date 2=文本',
    `iprefixtype` tinyint(4) DEFAULT NULL,
    `ipurpose` tinyint(4) DEFAULT NULL,
    `csourcename` varchar(50) DEFAULT NULL,
    `cfieldname` varchar(50) DEFAULT NULL COMMENT '取值字段',
    `carchname` varchar(50) DEFAULT NULL,
    `carchfieldname` varchar(50) DEFAULT NULL,
    `carchclsfieldname` varchar(50) DEFAULT NULL,
    `ckeyword` varchar(50) DEFAULT NULL,
    `ckeywordnamefield` varchar(50) DEFAULT NULL,
    `tenant_id` bigint(20) DEFAULT NULL,
    `yhtTenantId` varchar(36) DEFAULT NULL COMMENT '友户通租户',
    `bMain` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否主表字段，0否 1是',
    `refPropType` tinyint(4) DEFAULT NULL COMMENT '属性为参照时，值类型：0=id，1=code，2=name',
    `refDomain` varchar(255) DEFAULT NULL COMMENT '参照领域',
    `refCode` varchar(255) DEFAULT NULL COMMENT '参照编码',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    `isvTag` VARCHAR(64) DEFAULT NULL COMMENT 'isv标志',
    `applicationCode` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '应用编码',
    `micro_service_code` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '微服务编码',
    PRIMARY KEY (`autoid`),
    KEY `ix_aa_billprefabricate_cbillnum_cprefix` (`cbillnum`,`cprefix`),
    KEY `idx_ytenant_id` (`ytenant_id`),
    KEY `ix_aa_billprefabricate_cbn_field` (`yhtTenantId`,`cbillnum`,`cfieldname`),
    KEY `i_aa_billprefabricate_ytenantId_cbn_field` (`ytenant_id`,`cbillnum`,`cfieldname`)
    )COMMENT='预置表单';

-- ----------------------------
-- Table structure for aa_billprefix
-- ----------------------------

CREATE TABLE IF NOT EXISTS `aa_billprefix` (
    `autoid` bigint(20) NOT NULL  COMMENT '主键',
    `tenant_id` bigint(20) DEFAULT '0' COMMENT '租户',
    `orgId` varchar(64) DEFAULT '-1',
    `cprefix` varchar(500) NOT NULL COMMENT '编码段名称：如门店代码，手工输入等',
    `iprefixlen` int(11) NOT NULL COMMENT '长度',
    `cprefixrule` varchar(50) DEFAULT NULL COMMENT '取值规则(例如yyyymmdd)',
    `cprefixseed` varchar(50) DEFAULT NULL COMMENT '依据参数：如y，ym，ymd',
    `iorder` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
    `bfix` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否依据',
    `cprefixid` varchar(50) DEFAULT NULL COMMENT '关联前缀预设的id',
    `cprefixtype` int(11) NOT NULL DEFAULT '1' COMMENT '前缀类型 0(字符字段)、1（日期字段）、2（常量）、3(系统时间)，4（单据参照属性），5 随机码',
    `cprefixsep` varchar(10) DEFAULT NULL COMMENT '分隔符',
    `cfieldname` varchar(500) DEFAULT NULL COMMENT '取值字段',
    `csourcename` varchar(300) DEFAULT NULL,
    `ipurpose` tinyint(4) DEFAULT NULL,
    `fillstyle` tinyint(4) DEFAULT NULL COMMENT '补位方式（0-不补位，1-左补位，2-右补位）',
    `fillsign` varchar(4) DEFAULT NULL COMMENT '补位符（最长4位）  ',
    `truncateType` tinyint(4) DEFAULT '0' COMMENT '0-左侧截断 1-右侧截断',
    `billnumberid` bigint(20) DEFAULT NULL COMMENT '规则id',
    `dr` tinyint(1) DEFAULT NULL COMMENT '删除标记',
    `pubts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
    `cbillnum` varchar(100) DEFAULT NULL,
    `yhtTenantId` varchar(36) DEFAULT NULL COMMENT '友户通租户',
    `formula` varchar(500) DEFAULT NULL COMMENT '公式',
    `formuladisplay` varchar(500) DEFAULT NULL COMMENT '公式显示',
    `bMain` tinyint(4) DEFAULT '1' COMMENT '是否主表字段，0否 1是',
    `billnumberCode` varchar(50) DEFAULT NULL,
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    `isvTag` VARCHAR(64) DEFAULT NULL COMMENT 'isv标志',
    `applicationCode` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '应用编码',
    `micro_service_code` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '微服务编码',
    `cprefixresid` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '编码元素名称',
    PRIMARY KEY (`autoid`),
    KEY `ix_aa_billprefix_bnCode_orgId_tenantId` (`billnumberCode`,`orgId`,`tenant_id`),
    KEY `idx_ytenant_id` (`ytenant_id`),
    KEY `i_aa_billprefix_bnCode_orgId_ytenantId` (`billnumberCode`,`orgId`,`ytenant_id`),
    KEY `i_aa_billprefix_cbillnum_ytenantId` (`cbillnum`,`ytenant_id`)
    )COMMENT='编码规则子表';

-- ----------------------------
-- Table structure for aa_billreturn
-- ----------------------------

CREATE TABLE IF NOT EXISTS `aa_billreturn` (
    `autoid` bigint(20) NOT NULL  COMMENT '主键',
    `tenant_id` bigint(20) DEFAULT '0' COMMENT '租户',
    `orgId` varchar(64) DEFAULT '-1',
    `cbillnum` varchar(100) DEFAULT NULL COMMENT '表单编码',
    `ownerorg` bigint(20) DEFAULT NULL COMMENT '所属组织',
    `cglide` varchar(500) DEFAULT NULL COMMENT '前缀名称',
    `cgliderule` varchar(250) DEFAULT NULL COMMENT '取值规则',
    `cseed` varchar(250) NOT NULL COMMENT '编号前缀',
    `inumber` int(11) DEFAULT NULL COMMENT '流水号',
    `letterNumber` int(11) DEFAULT NULL COMMENT '字母流水号',
    `totalBasis` varchar(500) NOT NULL COMMENT '总依据(依据1|依据2|依据3|...)',
    `billnumberid` bigint(20) DEFAULT NULL COMMENT '规则id（外键）',
    `dr` tinyint(1) DEFAULT NULL COMMENT '删除标记',
    `pubts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
    `yhtTenantId` varchar(36) DEFAULT NULL COMMENT '友户通租户',
    `billnumberCode` varchar(50) DEFAULT NULL,
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    `cglidename` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '流水依据名称',
    PRIMARY KEY (`autoid`),
    KEY `idx_ytenant_id` (`ytenant_id`),
    KEY `i_aa_billreturn_cbillnum_cseed_ytenantId` (`cbillnum`,`cseed`,`ytenant_id`)
    )COMMENT='退号表';

-- ----------------------------
-- Table structure for billcode_mapping
-- ----------------------------
CREATE TABLE IF NOT EXISTS `billcode_mapping` (
    `billnum` varchar(100) NOT NULL,
    `target` varchar(100) NOT NULL,
    `tenant_id` varchar(64) DEFAULT NULL,
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    PRIMARY KEY (`billnum`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    )COMMENT='billcode_mapping';

-- ----------------------------
-- Table structure for billforeignkey
-- ----------------------------
CREATE TABLE IF NOT EXISTS `billforeignkey` (
    `id` int(11) NOT NULL  COMMENT '主键',
    `type` tinyint(4) NOT NULL DEFAULT '0',
    `entityname` varchar(100) DEFAULT NULL COMMENT '类名',
    `billno` varchar(100) DEFAULT NULL COMMENT '表单',
    `source` varchar(100) DEFAULT NULL COMMENT '来源',
    `sourceattr` varchar(45) DEFAULT NULL,
    `target` varchar(45) DEFAULT NULL COMMENT '转换后的结果',
    `extra` varchar(500) DEFAULT NULL,
    `extraTarget` varchar(500) DEFAULT NULL,
    `extra_condition` varchar(500) DEFAULT NULL COMMENT '翻译自定义扩展条件',
    `tenant_id` varchar(50) DEFAULT NULL COMMENT '租户id',
    `sort` int(4) DEFAULT '0' COMMENT '排序字段',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `id_UNIQUE` (`id`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    )COMMENT='billforeignkey';


CREATE TABLE IF NOT EXISTS `billforeignkey_tenant` (
    `id` int(11) NOT NULL  COMMENT '主键',
    `type` tinyint(4) NOT NULL DEFAULT '0',
    `entityname` varchar(100) DEFAULT NULL COMMENT '类名',
    `billno` varchar(100) DEFAULT NULL COMMENT '表单',
    `source` varchar(45) DEFAULT NULL COMMENT '来源',
    `sourceattr` varchar(45) DEFAULT NULL,
    `target` varchar(45) DEFAULT NULL COMMENT '转换后的结果',
    `extra` varchar(500) DEFAULT NULL,
    `extraTarget` varchar(500) DEFAULT NULL,
    `extra_condition` varchar(500) DEFAULT NULL COMMENT '翻译自定义扩展条件',
    `tenant_id` varchar(50) DEFAULT NULL COMMENT '租户id',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    `sort` int(4) DEFAULT '0' COMMENT '排序字段',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `id_UNIQUE` (`id`) USING BTREE,
    KEY `idx_ytenant_id` (`ytenant_id`)
    )COMMENT='billforeignkey_tenant';

-- Data exporting was unselected.


CREATE TABLE IF NOT EXISTS `ds_schema` (
    `id_` varchar(64) NOT NULL,
    `schema_` varchar(255) DEFAULT NULL,
    `create_time_` datetime DEFAULT NULL,
    `modify_time_` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `current_` char(1) NOT NULL,
    `config_id` int(11) NOT NULL,
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    PRIMARY KEY (`id_`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    )COMMENT='ds_schema';

-- Data exporting was unselected.

CREATE TABLE IF NOT EXISTS `schema_mapping` (
    `id` int(11) NOT NULL,
    `code` varchar(64) DEFAULT NULL,
    `showname` varchar(64) DEFAULT NULL,
    `tenant` varchar(64) DEFAULT NULL,
    `app` varchar(32) DEFAULT NULL,
    `business` varchar(32) DEFAULT NULL,
    `dft` varchar(8) DEFAULT NULL,
    `update_date` bigint(20) DEFAULT NULL,
    `status` char(8) DEFAULT NULL,
    `isdirty` char(8) DEFAULT NULL,
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_ytenant_id` (`ytenant_id`)
    )COMMENT='schema_mapping';


CREATE TABLE  IF NOT EXISTS `ypd_billplugin` (
    `vbilltype` varchar(50) COMMENT '单据类型',
    `vclazzname` varchar(50) COMMENT '单据类型',
    `dr` smallint(4) COMMENT '逻辑删除标记',
    `tenant_id` varchar(60) COMMENT '租户id',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    `create_time` datetime COMMENT '创建时间',
    `modify_time` datetime COMMENT '修改时间',
    `creator` varchar(60) COMMENT '创建人',
    `modifier` varchar(60) COMMENT '修改人',
    `id` varchar(36) NOT NULL COMMENT 'ID',
    `pubts` timestamp COMMENT '时间戳' NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    )COMMENT='ypd_billplugin';

CREATE TABLE IF NOT EXISTS `templatesnapshot` (
    `id` bigint(20) NOT NULL COMMENT 'ID',
    `context` mediumtext COMMENT '模板快照',
    `billTemplateData` bigint(20) NOT NULL COMMENT '表单模版数据',
    `tenant_id`  varchar(36)  NOT NULL COMMENT '租户',
    `pubts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    PRIMARY KEY (`id`),
    KEY `IX_billTemplateData` (`billTemplateData`),
    KEY `templatesnapshot_tenant_id_idx` (`tenant_id`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    )COMMENT='templatesnapshot';

CREATE TABLE IF NOT EXISTS `billtemplatedata` (
    `id` bigint(20) NOT NULL COMMENT 'ID',
    `name` varchar(200) DEFAULT NULL,
    `billno` varchar(100) NOT NULL COMMENT 'BillNo',
    `type` int(11) NOT NULL COMMENT '表单模版类型',
    `org` varchar(200) NOT NULL COMMENT '组织ID',
    `creatorId` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `tenant_id` varchar(36) NOT NULL COMMENT '租户',
    `stopstatus` bit(1) DEFAULT b'0' COMMENT '停用状态',
    `stop_time` datetime DEFAULT NULL COMMENT '停用时间',
    `pubts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    `dataType` varchar(45) DEFAULT '0' COMMENT '数据类型 0-模板 1-草稿',
    `userId` varchar(45) DEFAULT null COMMENT '用户信息',
    `isPersonal` int(1) DEFAULT null COMMENT '是否个人级',
    PRIMARY KEY (`id`),
    KEY `billtemplatedata_billno_idx` (`billno`,`tenant_id`),
    KEY `billtemplatedata_tenant_id_idx` (`tenant_id`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    )COMMENT='billtemplatedata';
CREATE TABLE IF NOT EXISTS `user_masterorg` (
    `id` bigint(20) NOT NULL  COMMENT 'ID',
    `user_id` varchar(45) NOT NULL,
    `billnum` varchar(60) NOT NULL,
    `tenant_id` varchar(36) NOT NULL,
    `org` varchar(60) NOT NULL,
    `pubts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `inx_user_billnum_tenant` (`user_id`,`billnum`,`tenant_id`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    )COMMENT='主组织记录表';

CREATE TABLE IF NOT EXISTS `pub_ref_hot`  (
    `id` bigint(20) NOT NULL COMMENT 'ID',
    `billnum` varchar(200) NOT NULL,
    `refid` varchar(100) NOT NULL,
    `refcode` varchar(100) NOT NULL,
    `hot_count` int(4) NOT NULL DEFAULT 0,
    `hot_type` int(4) NOT NULL DEFAULT 0,
    `tenant_id` varchar(36) NOT NULL DEFAULT '0' COMMENT '友户通租户ID',
    `user_id` varchar(100) NOT NULL DEFAULT '0' COMMENT '友户通用户ID',
    `pubts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
    `ytenant_id` varchar(36) NOT NULL COMMENT '租户ID',
    `orgid` varchar(64) DEFAULT NULL COMMENT '组织',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_ytenant_id`(`ytenant_id`) USING BTREE,
    INDEX `bizindex1`(`refcode`, `tenant_id`, `user_id`) USING BTREE,
    INDEX `biz_refId`(`refid`, `refcode`, `tenant_id`, `user_id`, `hot_type`) USING BTREE
    )COMMENT='参照收藏表';
CREATE TABLE IF NOT EXISTS `aa_billnumber_code` (
    `autoid` bigint(20) NOT NULL,
    `cbillnum` varchar(50) NOT NULL COMMENT '规则编码',
    `orgId` varchar(64) NOT NULL DEFAULT '-1' COMMENT '组织',
    `billCode` varchar(64) NOT NULL COMMENT '编码',
    `codePrefixLeft` varchar(64) DEFAULT '' COMMENT '左侧前缀',
    `codePrefixMiddle` varchar(64) DEFAULT '' COMMENT '中间前缀',
    `codePrefixRight` varchar(64) DEFAULT '' COMMENT '右侧前缀',
    `snRefer` varchar(64) NOT NULL DEFAULT '' COMMENT '流水依据',
    `snReferDesc` varchar(256) NOT NULL DEFAULT '' COMMENT '流水依据描述',
    `snReferNameDesc` varchar(256) NOT NULL DEFAULT '' COMMENT '流水依据名称',
    `sn` varchar(64) DEFAULT NULL COMMENT '流水号',
    `letterSN` varchar(64) DEFAULT NULL COMMENT '字母流水号',
    `ytenant_id` varchar(36) NOT NULL COMMENT '租户',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `applicationCode` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '应用编码',
    `micro_service_code` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '微服务编码',
    `extendInfo` varchar(1024) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '额外信息',
    PRIMARY KEY (`autoid`),
    UNIQUE KEY `idx_cbillnum_billCode_orgId_ytenantId` (`cbillnum`,`billCode`, `orgId`, `ytenant_id`) USING BTREE,
    KEY `idx_ytenant_id` (`ytenant_id`) USING BTREE,
    KEY `idx_create_time` (`create_time`) USING BTREE
    )COMMENT='编码表';

CREATE TABLE IF NOT EXISTS `aa_billprefabricate_bak`(
    `autoid`      bigint(20) NOT NULL,
    `cfieldname`  varchar(500) DEFAULT NULL COMMENT '取值字段',
    `csourcename` varchar(300) DEFAULT NULL COMMENT 'aa_billprefabricate表里的csourcename',
    `ytenant_id`  varchar(36) NOT NULL COMMENT '租户ID',
    PRIMARY KEY (`autoid`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    ) ENGINE=InnoDB  COMMENT='编码规则预置属性表';

CREATE TABLE IF NOT EXISTS `aa_billprefix_bak` (
    `autoid` bigint(20) NOT NULL,
    `cfieldname` varchar(500) DEFAULT NULL COMMENT '取值字段',
    `csourcename` varchar(300) DEFAULT NULL COMMENT '元数据fullName',
    `formula` varchar(500) DEFAULT NULL COMMENT '公式',
    `formuladisplay` varchar(500) DEFAULT NULL COMMENT '公式显示',
    `ytenant_id` varchar(36) NOT NULL COMMENT '友互通租户id',
    PRIMARY KEY (`autoid`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    ) ENGINE=InnoDB COMMENT='编码规则子表';

CREATE TABLE IF NOT EXISTS `aa_billcondition_bak`(
    `autoid`      bigint(20) NOT NULL,
    `cfieldname`  varchar(500) DEFAULT NULL COMMENT '取值字段',
    `csourcename` varchar(300) DEFAULT NULL COMMENT 'aa_billprefabricate表里的csourcename',
    `ytenant_id`  varchar(36) NOT NULL COMMENT '租户ID',
    PRIMARY KEY (`autoid`),
    KEY `idx_ytenant_id` (`ytenant_id`)
    ) ENGINE=InnoDB  COMMENT='编码规则条件表';