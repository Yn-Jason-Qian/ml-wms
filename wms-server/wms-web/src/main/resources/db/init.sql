-- ============================================================
-- WMS 仓库管理系统 - 初始化数据库脚本
-- Database: ml_wms
-- Engine: MySQL 8.x, InnoDB
-- Charset: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS ml_wms
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE ml_wms;

-- ============================================================
-- 1. 系统管理模块 (System)
-- ============================================================

-- 租户表
CREATE TABLE wms_sys_tenant (
    id BIGINT NOT NULL,
    tenant_code VARCHAR(32) NOT NULL COMMENT '租户编码',
    tenant_name VARCHAR(128) NOT NULL COMMENT '租户名称',
    contact_person VARCHAR(64) COMMENT '联系人',
    contact_phone VARCHAR(32) COMMENT '联系电话',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_code (tenant_code)
) COMMENT '租户表';

-- 用户表
CREATE TABLE wms_sys_user (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password VARCHAR(256) NOT NULL COMMENT '密码(BCrypt)',
    real_name VARCHAR(64) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(128) COMMENT '邮箱',
    avatar VARCHAR(512) COMMENT '头像URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    last_login_at DATETIME COMMENT '最后登录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_username (tenant_id, username)
) COMMENT '用户表';

-- 角色表
CREATE TABLE wms_sys_role (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(128) NOT NULL COMMENT '角色名称',
    description VARCHAR(256) COMMENT '描述',
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_role_code (tenant_id, role_code)
) COMMENT '角色表';

-- 用户角色关联表
CREATE TABLE wms_sys_user_role (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) COMMENT '用户角色关联表';

-- 菜单权限表
CREATE TABLE wms_sys_menu (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '0表示系统级菜单',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID, 0=顶级',
    menu_type TINYINT NOT NULL COMMENT '1=目录 2=菜单 3=按钮',
    menu_name VARCHAR(64) NOT NULL COMMENT '菜单名称',
    menu_code VARCHAR(128) COMMENT '权限编码',
    path VARCHAR(256) COMMENT '路由路径',
    component VARCHAR(256) COMMENT '组件路径',
    icon VARCHAR(64) COMMENT '图标',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) COMMENT '菜单权限表';

-- 角色菜单关联表
CREATE TABLE wms_sys_role_menu (
    id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) COMMENT '角色菜单关联表';

-- 操作日志表
CREATE TABLE wms_sys_operation_log (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(64) COMMENT '操作用户名',
    module VARCHAR(64) COMMENT '操作模块',
    action VARCHAR(64) COMMENT '操作类型',
    description VARCHAR(1024) COMMENT '操作描述',
    method VARCHAR(256) COMMENT '请求方法',
    request_params TEXT COMMENT '请求参数JSON',
    result VARCHAR(16) COMMENT '结果: SUCCESS/ERROR',
    error_msg VARCHAR(1024) COMMENT '错误信息',
    elapsed_ms INT COMMENT '耗时(毫秒)',
    ip_address VARCHAR(64) COMMENT '请求IP',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_tenant_time (tenant_id, created_at),
    KEY idx_user (user_id)
) COMMENT '操作日志表';

-- ============================================================
-- 2. 基础数据模块 (Master Data)
-- ============================================================

-- 计量单位表
CREATE TABLE wms_masterdata_unit (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    unit_code VARCHAR(32) NOT NULL COMMENT '单位编码',
    unit_name VARCHAR(64) NOT NULL COMMENT '单位名称',
    unit_type VARCHAR(16) NOT NULL COMMENT '类型: QUANTITY/WEIGHT/VOLUME/LENGTH',
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_unit_code (tenant_id, unit_code)
) COMMENT '计量单位表';

-- 仓库表
CREATE TABLE wms_masterdata_warehouse (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    wh_code VARCHAR(32) NOT NULL COMMENT '仓库编码',
    wh_name VARCHAR(128) NOT NULL COMMENT '仓库名称',
    wh_type VARCHAR(32) NOT NULL COMMENT '仓库类型: STANDARD/COLD/HAZARDOUS/BONDED',
    address VARCHAR(512) COMMENT '地址',
    contact_person VARCHAR(64) COMMENT '联系人',
    contact_phone VARCHAR(32) COMMENT '联系电话',
    length DECIMAL(10,2) COMMENT '长(m)',
    width DECIMAL(10,2) COMMENT '宽(m)',
    height DECIMAL(10,2) COMMENT '高(m)',
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_wh_code (tenant_id, wh_code)
) COMMENT '仓库表';

-- 库区表
CREATE TABLE wms_masterdata_area (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    area_code VARCHAR(32) NOT NULL COMMENT '库区编码',
    area_name VARCHAR(128) NOT NULL COMMENT '库区名称',
    area_type VARCHAR(32) NOT NULL COMMENT '库区类型: RECEIVE/SHIPPING/STORAGE/PICKING/RETURN/QC',
    temperature_min DECIMAL(5,2) COMMENT '最低温度(℃)',
    temperature_max DECIMAL(5,2) COMMENT '最高温度(℃)',
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_area_code (tenant_id, warehouse_id, area_code),
    KEY idx_warehouse (warehouse_id)
) COMMENT '库区表';

-- 库位表
CREATE TABLE wms_masterdata_location (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    area_id BIGINT NOT NULL COMMENT '库区ID',
    location_code VARCHAR(64) NOT NULL COMMENT '库位编码(条码)',
    location_name VARCHAR(128) COMMENT '库位名称',
    location_type VARCHAR(32) NOT NULL COMMENT '库位类型: FLOOR/RACK/SHELF/BIN',
    aisle VARCHAR(16) COMMENT '巷道号',
    shelf VARCHAR(16) COMMENT '货架号',
    tier VARCHAR(16) COMMENT '层号',
    depth_pos VARCHAR(16) COMMENT '深度位',
    max_weight DECIMAL(12,4) COMMENT '最大承重(kg)',
    max_volume DECIMAL(12,4) COMMENT '最大容积(m³)',
    max_qty INT COMMENT '最大存放数量',
    length DECIMAL(10,2) COMMENT '长(cm)',
    width DECIMAL(10,2) COMMENT '宽(cm)',
    height DECIMAL(10,2) COMMENT '高(cm)',
    roadway VARCHAR(32) COMMENT '通道/作业面',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=空闲 2=占用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_loc_code (tenant_id, warehouse_id, location_code),
    KEY idx_area (area_id),
    KEY idx_warehouse (warehouse_id)
) COMMENT '库位表';

-- 货主表
CREATE TABLE wms_masterdata_owner (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    owner_code VARCHAR(32) NOT NULL COMMENT '货主编码',
    owner_name VARCHAR(128) NOT NULL COMMENT '货主名称',
    contact_person VARCHAR(64) COMMENT '联系人',
    contact_phone VARCHAR(32) COMMENT '联系电话',
    address VARCHAR(512) COMMENT '地址',
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_owner_code (tenant_id, owner_code)
) COMMENT '货主表';

-- SKU 主表
CREATE TABLE wms_masterdata_sku (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL COMMENT '货主ID',
    sku_code VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    sku_name VARCHAR(256) NOT NULL COMMENT 'SKU名称',
    sku_desc VARCHAR(1024) COMMENT '描述',
    barcode VARCHAR(128) COMMENT '主条码',
    category VARCHAR(64) COMMENT '品类',
    brand VARCHAR(128) COMMENT '品牌',
    spec VARCHAR(256) COMMENT '规格型号',
    base_unit_id BIGINT COMMENT '基础计量单位ID',
    base_length DECIMAL(10,2) COMMENT '长(cm)',
    base_width DECIMAL(10,2) COMMENT '宽(cm)',
    base_height DECIMAL(10,2) COMMENT '高(cm)',
    base_weight DECIMAL(12,4) COMMENT '毛重(kg)',
    base_volume DECIMAL(12,4) COMMENT '体积(m³)',
    shelf_life INT COMMENT '保质期(天)',
    shelf_life_type VARCHAR(16) COMMENT '保质期类型: PRODUCTION/RECEIVE',
    batch_managed TINYINT NOT NULL DEFAULT 0 COMMENT '批次管理: 0=否 1=是',
    sn_managed TINYINT NOT NULL DEFAULT 0 COMMENT '序列号管理: 0=否 1=是',
    lot_attrs VARCHAR(512) COMMENT '批次属性模板JSON: [{name,type,must}]',
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_sku_code (tenant_id, sku_code),
    KEY idx_owner (owner_id)
) COMMENT 'SKU表';

-- SKU 包装规格表
CREATE TABLE wms_masterdata_sku_package (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    package_level VARCHAR(16) NOT NULL COMMENT '包装层级: EA/CASE/PALLET',
    package_name VARCHAR(64) NOT NULL COMMENT '包装名称',
    unit_id BIGINT NOT NULL COMMENT '单位ID',
    qty_per_parent DECIMAL(12,4) NOT NULL COMMENT '上级包装含本级的数量',
    barcode VARCHAR(128) COMMENT '条码',
    length DECIMAL(10,2) COMMENT '长(cm)',
    width DECIMAL(10,2) COMMENT '宽(cm)',
    height DECIMAL(10,2) COMMENT '高(cm)',
    weight DECIMAL(12,4) COMMENT '重量(kg)',
    is_default_receive TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认收货包装',
    is_default_pick TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认拣货包装',
    is_default_storage TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认存储包装',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_sku (sku_id)
) COMMENT 'SKU包装规格表';

-- 字典表
CREATE TABLE wms_masterdata_dictionary (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    dict_type VARCHAR(64) NOT NULL COMMENT '字典类型',
    dict_code VARCHAR(64) NOT NULL COMMENT '字典编码',
    dict_name VARCHAR(128) NOT NULL COMMENT '字典名称',
    parent_code VARCHAR(64) DEFAULT '0' COMMENT '上级字典编码',
    sort_order INT NOT NULL DEFAULT 0,
    extra VARCHAR(512) COMMENT '扩展字段JSON',
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_dict (tenant_id, dict_type, dict_code)
) COMMENT '字典表';

-- ============================================================
-- 3. 入库模块 (Inbound)
-- ============================================================

-- ASN单头 (预到货通知)
CREATE TABLE wms_inbound_asn_header (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    owner_id BIGINT NOT NULL COMMENT '货主ID',
    asn_no VARCHAR(64) NOT NULL COMMENT 'ASN单号',
    asn_type VARCHAR(32) NOT NULL COMMENT '类型: PURCHASE/RETURN/TRANSFER/ADJUST',
    source_no VARCHAR(64) COMMENT '来源单号(采购单/退货单)',
    expected_arrive_time DATETIME COMMENT '预计到货时间',
    carrier_name VARCHAR(128) COMMENT '承运商',
    carrier_phone VARCHAR(32) COMMENT '承运商电话',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/RECEIVING/PARTIAL_RECEIVED/RECEIVED/CANCELLED/CLOSED',
    remark VARCHAR(512) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_asn_no (tenant_id, asn_no),
    KEY idx_warehouse (warehouse_id),
    KEY idx_owner (owner_id),
    KEY idx_status (status)
) COMMENT 'ASN单头表';

-- ASN单行
CREATE TABLE wms_inbound_asn_line (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    asn_header_id BIGINT NOT NULL COMMENT 'ASN单头ID',
    line_no INT NOT NULL COMMENT '行号',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    sku_code VARCHAR(64) NOT NULL COMMENT 'SKU编码(冗余)',
    sku_name VARCHAR(256) NOT NULL COMMENT 'SKU名称(冗余)',
    expected_qty DECIMAL(18,4) NOT NULL COMMENT '预计数量(EA)',
    received_qty DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '已收货数量',
    unit_id BIGINT COMMENT '单位ID',
    batch_no VARCHAR(64) COMMENT '生产批次号',
    lot_attrs VARCHAR(1024) COMMENT '批次属性JSON',
    production_date DATE COMMENT '生产日期',
    expiry_date DATE COMMENT '失效日期',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_asn_header (asn_header_id),
    KEY idx_sku (sku_id)
) COMMENT 'ASN单行表';

-- 收货单头
CREATE TABLE wms_inbound_receive_header (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    receive_no VARCHAR(64) NOT NULL COMMENT '收货单号',
    asn_header_id BIGINT COMMENT '关联ASN单头',
    receive_type VARCHAR(32) NOT NULL COMMENT '类型: ASN/BLIND',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/RECEIVING/DONE/CANCELLED',
    received_by BIGINT COMMENT '收货人(用户ID)',
    received_at DATETIME COMMENT '收货完成时间',
    remark VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_receive_no (tenant_id, receive_no),
    KEY idx_asn_header (asn_header_id)
) COMMENT '收货单头表';

-- 收货单行
CREATE TABLE wms_inbound_receive_line (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    receive_header_id BIGINT NOT NULL COMMENT '收货单头ID',
    asn_line_id BIGINT COMMENT '关联ASN行',
    line_no INT NOT NULL COMMENT '行号',
    sku_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    sku_name VARCHAR(256) NOT NULL,
    receive_qty DECIMAL(18,4) NOT NULL COMMENT '收货数量(EA)',
    receive_package VARCHAR(16) COMMENT '收货包装层级',
    unit_id BIGINT COMMENT '单位ID',
    receive_location_id BIGINT COMMENT '收货暂存库位',
    batch_no VARCHAR(64) COMMENT '生产批次号',
    lot_attrs VARCHAR(1024) COMMENT '批次属性JSON',
    production_date DATE COMMENT '生产日期',
    expiry_date DATE COMMENT '失效日期',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_receive_header (receive_header_id),
    KEY idx_sku (sku_id),
    KEY idx_asn_line (asn_line_id)
) COMMENT '收货单行表';

-- 质检单头
CREATE TABLE wms_inbound_qc_header (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    qc_no VARCHAR(64) NOT NULL COMMENT '质检单号',
    receive_header_id BIGINT COMMENT '关联收货单头',
    qc_type VARCHAR(32) NOT NULL COMMENT '类型: FULL/SAMPLE/NONE',
    sample_ratio DECIMAL(5,4) COMMENT '抽样比例',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/QCING/PASS/REJECT',
    qc_by BIGINT COMMENT '质检人',
    qc_at DATETIME COMMENT '质检完成时间',
    remark VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_qc_no (tenant_id, qc_no)
) COMMENT '质检单头表';

-- 质检单行
CREATE TABLE wms_inbound_qc_line (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    qc_header_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    sku_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    sku_name VARCHAR(256) NOT NULL,
    inspect_qty DECIMAL(18,4) NOT NULL COMMENT '检验数量',
    pass_qty DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '合格数量',
    reject_qty DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '不合格数量',
    reject_reason VARCHAR(256) COMMENT '不合格原因',
    batch_no VARCHAR(64),
    lot_attrs VARCHAR(1024),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_qc_header (qc_header_id),
    KEY idx_sku (sku_id)
) COMMENT '质检单行表';

-- 上架单头
CREATE TABLE wms_inbound_putaway_header (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    putaway_no VARCHAR(64) NOT NULL COMMENT '上架单号',
    receive_header_id BIGINT COMMENT '关联收货单',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/PUTAWAYING/PARTIAL_DONE/DONE/CANCELLED',
    strategy_id BIGINT COMMENT '使用的上架策略ID',
    remark VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_putaway_no (tenant_id, putaway_no)
) COMMENT '上架单头表';

-- 上架单行（混上架：单行可分配多个库位）
CREATE TABLE wms_inbound_putaway_line (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    putaway_header_id BIGINT NOT NULL COMMENT '上架单头ID',
    line_no INT NOT NULL COMMENT '行号',
    sku_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    sku_name VARCHAR(256) NOT NULL,
    putaway_qty DECIMAL(18,4) NOT NULL COMMENT '上架数量',
    done_qty DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '已上架数量',
    from_location_id BIGINT NOT NULL COMMENT '来源库位(暂存区)',
    to_location_id BIGINT NOT NULL COMMENT '目标库位',
    batch_no VARCHAR(64),
    lot_attrs VARCHAR(1024),
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    putaway_by BIGINT COMMENT '上架人',
    putaway_at DATETIME COMMENT '上架完成时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_putaway_header (putaway_header_id),
    KEY idx_sku (sku_id),
    KEY idx_to_location (to_location_id)
) COMMENT '上架单行表';

-- ============================================================
-- 4. 出库模块 (Outbound)
-- ============================================================

-- 订单头
CREATE TABLE wms_outbound_order_header (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL COMMENT '货主ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    order_type VARCHAR(32) NOT NULL COMMENT '类型: SALE/TRANSFER/RETURN_SUPPLIER/SAMPLE',
    source_no VARCHAR(64) COMMENT '来源单号',
    wave_header_id BIGINT COMMENT '关联波次ID',
    customer_name VARCHAR(128) COMMENT '客户名称',
    customer_address VARCHAR(512) COMMENT '客户地址',
    expected_ship_time DATETIME COMMENT '预计发货时间',
    priority INT NOT NULL DEFAULT 5 COMMENT '优先级: 1(最高)-9(最低)',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/ALLOCATED/PICKING/PICKED/CHECKING/CHECKED/SHIPPED/CANCELLED/CLOSED',
    remark VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_order_no (tenant_id, order_no),
    KEY idx_warehouse (warehouse_id),
    KEY idx_owner (owner_id),
    KEY idx_wave (wave_header_id),
    KEY idx_status (status)
) COMMENT '订单头表';

-- 订单行
CREATE TABLE wms_outbound_order_line (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    order_header_id BIGINT NOT NULL COMMENT '订单头ID',
    line_no INT NOT NULL COMMENT '行号',
    sku_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    sku_name VARCHAR(256) NOT NULL,
    order_qty DECIMAL(18,4) NOT NULL COMMENT '订单数量(EA)',
    allocated_qty DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '已分配数量',
    picked_qty DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '已拣货数量',
    shipped_qty DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '已发货数量',
    unit_id BIGINT,
    batch_no VARCHAR(64) COMMENT '指定批次号',
    lot_attrs VARCHAR(1024) COMMENT '指定批次属性',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_order_header (order_header_id),
    KEY idx_sku (sku_id)
) COMMENT '订单行表';

-- 波次头
CREATE TABLE wms_outbound_wave_header (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    wave_no VARCHAR(64) NOT NULL COMMENT '波次号',
    wave_type VARCHAR(32) NOT NULL COMMENT '类型: ORDER_POOL/PICK_ZONE/PRIORITY/TIME_WINDOW',
    wave_status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/RELEASED/PICKING/PICKED/CHECKING/DONE',
    order_count INT NOT NULL DEFAULT 0 COMMENT '订单数',
    total_lines INT NOT NULL DEFAULT 0 COMMENT '总行数',
    total_qty DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '总数量',
    strategy_id BIGINT COMMENT '使用的波次策略ID',
    released_by BIGINT COMMENT '释放人',
    released_at DATETIME COMMENT '释放时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_wave_no (tenant_id, wave_no),
    KEY idx_warehouse (warehouse_id),
    KEY idx_status (wave_status)
) COMMENT '波次头表';

-- 波次明细（波次-订单关联）
CREATE TABLE wms_outbound_wave_line (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    wave_header_id BIGINT NOT NULL,
    order_header_id BIGINT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0 COMMENT '波次内排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wave_order (wave_header_id, order_header_id),
    KEY idx_order (order_header_id)
) COMMENT '波次明细表';

-- 拣货单头
CREATE TABLE wms_outbound_pick_header (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    pick_no VARCHAR(64) NOT NULL COMMENT '拣货单号',
    wave_header_id BIGINT COMMENT '关联波次ID',
    pick_type VARCHAR(32) NOT NULL COMMENT '类型: PIECE/PALLET/PAPER/RF',
    pick_zone VARCHAR(64) COMMENT '拣货区域编码',
    assign_to BIGINT COMMENT '分配给用户ID',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/ASSIGNED/PICKING/PICKED/DONE/CANCELLED',
    start_time DATETIME COMMENT '开始拣货时间',
    end_time DATETIME COMMENT '完成拣货时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_pick_no (tenant_id, pick_no),
    KEY idx_wave (wave_header_id),
    KEY idx_assign_to (assign_to)
) COMMENT '拣货单头表';

-- 拣货单行
CREATE TABLE wms_outbound_pick_line (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    pick_header_id BIGINT NOT NULL COMMENT '拣货单头ID',
    line_no INT NOT NULL COMMENT '行号',
    order_header_id BIGINT COMMENT '来源订单ID',
    order_line_id BIGINT COMMENT '来源订单行ID',
    sku_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    sku_name VARCHAR(256) NOT NULL,
    pick_qty DECIMAL(18,4) NOT NULL COMMENT '应拣数量',
    picked_qty DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '实拣数量',
    location_id BIGINT NOT NULL COMMENT '拣货库位',
    from_stock_id BIGINT COMMENT '来源库存ID',
    batch_no VARCHAR(64),
    lot_attrs VARCHAR(1024),
    to_container VARCHAR(64) COMMENT '目标容器(周转箱号)',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    pick_by BIGINT COMMENT '拣货人',
    pick_at DATETIME COMMENT '拣货时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_pick_header (pick_header_id),
    KEY idx_order_header (order_header_id),
    KEY idx_sku (sku_id),
    KEY idx_location (location_id)
) COMMENT '拣货单行表';

-- 复核单头
CREATE TABLE wms_outbound_check_header (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    check_no VARCHAR(64) NOT NULL COMMENT '复核单号',
    wave_header_id BIGINT COMMENT '关联波次ID',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/CHECKING/PASS/REJECT/DONE',
    check_by BIGINT COMMENT '复核人',
    start_time DATETIME,
    end_time DATETIME,
    remark VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_check_no (tenant_id, check_no)
) COMMENT '复核单头表';

-- 复核单行
CREATE TABLE wms_outbound_check_line (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    check_header_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    order_header_id BIGINT,
    sku_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    sku_name VARCHAR(256) NOT NULL,
    order_qty DECIMAL(18,4) COMMENT '订单数量',
    check_qty DECIMAL(18,4) NOT NULL COMMENT '复核数量',
    is_match TINYINT NOT NULL DEFAULT 1 COMMENT '是否一致: 0=差异 1=一致',
    diff_reason VARCHAR(256) COMMENT '差异原因',
    from_container VARCHAR(64) COMMENT '来源容器',
    to_container VARCHAR(64) COMMENT '发货容器',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_check_header (check_header_id),
    KEY idx_sku (sku_id)
) COMMENT '复核单行表';

-- 发货单头
CREATE TABLE wms_outbound_ship_header (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    ship_no VARCHAR(64) NOT NULL COMMENT '发货单号',
    wave_header_id BIGINT COMMENT '关联波次ID',
    delivery_method VARCHAR(32) NOT NULL COMMENT '发货方式: EXPRESS/LTL/FTL/SELF_PICKUP',
    carrier_name VARCHAR(128) COMMENT '承运商',
    tracking_no VARCHAR(128) COMMENT '快递单号/运单号',
    package_count INT NOT NULL DEFAULT 0 COMMENT '包裹数',
    gross_weight DECIMAL(12,4) COMMENT '毛重(kg)',
    volume DECIMAL(12,4) COMMENT '体积(m³)',
    ship_by BIGINT COMMENT '发货人',
    ship_at DATETIME COMMENT '发货时间',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/SHIPPING/DONE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_ship_no (tenant_id, ship_no)
) COMMENT '发货单头表';

-- 发货单行
CREATE TABLE wms_outbound_ship_line (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    ship_header_id BIGINT NOT NULL,
    order_header_id BIGINT,
    line_no INT NOT NULL,
    sku_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    sku_name VARCHAR(256) NOT NULL,
    ship_qty DECIMAL(18,4) NOT NULL COMMENT '发货数量',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_ship_header (ship_header_id)
) COMMENT '发货单行表';

-- ============================================================
-- 5. 库存模块 (Inventory)
-- ============================================================

-- 库存表（核心：库位+SKU+批次+序列号粒度）
CREATE TABLE wms_inventory_stock (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL COMMENT '货主ID',
    location_id BIGINT NOT NULL COMMENT '库位ID',
    sku_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    sku_name VARCHAR(256) NOT NULL,
    batch_no VARCHAR(64) COMMENT '批次号',
    lot_attrs VARCHAR(1024) COMMENT '批次属性JSON',
    production_date DATE,
    expiry_date DATE,
    qty_on_hand DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '在手数量',
    qty_allocated DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '已分配数量',
    qty_available DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '可用数量(在手-已分配)',
    qty_frozen DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '冻结数量',
    unit_id BIGINT COMMENT '单位ID',
    first_in_time DATETIME COMMENT '首次入库时间',
    last_in_time DATETIME COMMENT '最后入库时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=冻结 1=正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_key (tenant_id, warehouse_id, location_id, sku_id, batch_no),
    KEY idx_sku (sku_id),
    KEY idx_location (location_id),
    KEY idx_owner (owner_id),
    KEY idx_expiry (expiry_date)
) COMMENT '库存表';

-- 库存流水表（审计日志：所有库存变更记录）
CREATE TABLE wms_inventory_stock_transaction (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL COMMENT '库存ID',
    sku_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    batch_no VARCHAR(64),
    txn_type VARCHAR(32) NOT NULL COMMENT '事务类型: RECEIVE/PUTAWAY/ALLOCATE/DEALLOCATE/PICK/SHIP/ADJUST/MOVE/STOCKTAKE/FREEZE/UNFREEZE',
    txn_direction VARCHAR(8) NOT NULL COMMENT '方向: IN/OUT',
    txn_qty DECIMAL(18,4) NOT NULL COMMENT '变更数量',
    qty_before DECIMAL(18,4) COMMENT '变更前在手数量',
    qty_after DECIMAL(18,4) COMMENT '变更后在手数量',
    ref_no VARCHAR(64) COMMENT '关联单据号',
    ref_id BIGINT COMMENT '关联单据ID',
    ref_line_id BIGINT COMMENT '关联单据行ID',
    from_location_id BIGINT COMMENT '来源库位',
    to_location_id BIGINT COMMENT '目标库位',
    remark VARCHAR(512),
    txn_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '事务时间',
    created_by BIGINT COMMENT '操作人',
    PRIMARY KEY (id),
    KEY idx_stock (stock_id),
    KEY idx_sku (sku_id),
    KEY idx_txn_type (txn_type),
    KEY idx_ref (ref_id),
    KEY idx_txn_time (txn_time)
) COMMENT '库存流水表';

-- 盘点单头
CREATE TABLE wms_inventory_stocktake_header (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    stocktake_no VARCHAR(64) NOT NULL COMMENT '盘点单号',
    stocktake_type VARCHAR(32) NOT NULL COMMENT '类型: FULL/AREA/LOCATION/SKU/BLIND',
    stocktake_mode VARCHAR(16) NOT NULL COMMENT '模式: PLAN/BLIND',
    location_range VARCHAR(1024) COMMENT '库位范围(JSON)',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/COUNTING/COUNTED/DIFF_REVIEW/ADJUSTED/DONE/CANCELLED',
    plan_start_time DATETIME COMMENT '计划开始时间',
    plan_end_time DATETIME COMMENT '计划结束时间',
    start_time DATETIME COMMENT '实际开始时间',
    end_time DATETIME COMMENT '实际结束时间',
    total_lines INT NOT NULL DEFAULT 0,
    first_count_lines INT NOT NULL DEFAULT 0 COMMENT '一盘点行数',
    second_count_lines INT NOT NULL DEFAULT 0 COMMENT '二盘点行数',
    diff_lines INT NOT NULL DEFAULT 0 COMMENT '差异行数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_stocktake_no (tenant_id, stocktake_no),
    KEY idx_warehouse (warehouse_id)
) COMMENT '盘点单头表';

-- 盘点单行
CREATE TABLE wms_inventory_stocktake_line (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    stocktake_header_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    location_id BIGINT NOT NULL COMMENT '库位ID',
    location_code VARCHAR(64) NOT NULL COMMENT '库位编码(冗余, 方便PDA扫码)',
    sku_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    sku_name VARCHAR(256) NOT NULL,
    batch_no VARCHAR(64),
    book_qty DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '账面数量',
    first_count_qty DECIMAL(18,4) COMMENT '一盘点数量',
    first_count_by BIGINT COMMENT '一盘点人',
    first_count_at DATETIME COMMENT '一盘点时间',
    second_count_qty DECIMAL(18,4) COMMENT '二盘点数量',
    second_count_by BIGINT COMMENT '二盘点人',
    second_count_at DATETIME COMMENT '二盘点时间',
    diff_qty DECIMAL(18,4) COMMENT '差异数量(盘点-账面)',
    adj_qty DECIMAL(18,4) COMMENT '调整数量',
    adj_reason VARCHAR(256) COMMENT '调整原因',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_stocktake_header (stocktake_header_id),
    KEY idx_location (location_id),
    KEY idx_sku (sku_id)
) COMMENT '盘点单行表';

-- 移库单头
CREATE TABLE wms_inventory_move_header (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    move_no VARCHAR(64) NOT NULL COMMENT '移库单号',
    move_type VARCHAR(32) NOT NULL COMMENT '类型: MANUAL/REPLENISH/PUTAWAY_FIX/RETURN',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/MOVING/DONE/CANCELLED',
    remark VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_move_no (tenant_id, move_no)
) COMMENT '移库单头表';

-- 移库单行
CREATE TABLE wms_inventory_move_line (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    move_header_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    sku_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    sku_name VARCHAR(256) NOT NULL,
    move_qty DECIMAL(18,4) NOT NULL COMMENT '移动数量',
    from_location_id BIGINT NOT NULL COMMENT '来源库位',
    from_stock_id BIGINT COMMENT '来源库存ID',
    to_location_id BIGINT NOT NULL COMMENT '目标库位',
    to_stock_id BIGINT COMMENT '目标库存ID',
    batch_no VARCHAR(64),
    lot_attrs VARCHAR(1024),
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    move_by BIGINT COMMENT '移库人',
    move_at DATETIME COMMENT '移库时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_move_header (move_header_id),
    KEY idx_sku (sku_id)
) COMMENT '移库单行表';

-- 库存冻结记录表
CREATE TABLE wms_inventory_freeze (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    freeze_type VARCHAR(32) NOT NULL COMMENT '类型: MANUAL/LOT_EXPIRY/QC_HOLD/DAMAGE',
    stock_id BIGINT COMMENT '关联库存ID(为空则按条件冻结)',
    sku_id BIGINT COMMENT 'SKU范围(空=不限)',
    location_id BIGINT COMMENT '库位范围(空=不限)',
    batch_no VARCHAR(64) COMMENT '批次范围(空=不限)',
    freeze_qty DECIMAL(18,4) COMMENT '冻结数量(空=全部)',
    reason VARCHAR(256) COMMENT '冻结原因',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/RELEASED',
    freeze_by BIGINT COMMENT '冻结人',
    freeze_at DATETIME COMMENT '冻结时间',
    release_by BIGINT COMMENT '解冻人',
    release_at DATETIME COMMENT '解冻时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_stock (stock_id),
    KEY idx_sku (sku_id),
    KEY idx_status (status)
) COMMENT '库存冻结记录表';

-- ============================================================
-- 6. 策略模块 (Strategy)
-- ============================================================

-- 策略配置表
CREATE TABLE wms_strategy_config (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    strategy_code VARCHAR(64) NOT NULL COMMENT '策略编码',
    strategy_name VARCHAR(128) NOT NULL COMMENT '策略名称',
    strategy_type VARCHAR(32) NOT NULL COMMENT '类型: PUTAWAY/ALLOCATION/WAVE/PICKING/REPLENISH',
    description VARCHAR(512) COMMENT '描述',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '优先级(越小越优先)',
    is_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_strategy_code (tenant_id, strategy_code)
) COMMENT '策略配置表';

-- 策略规则表
CREATE TABLE wms_strategy_rule (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    strategy_id BIGINT NOT NULL COMMENT '策略ID',
    rule_no INT NOT NULL COMMENT '规则序号',
    rule_name VARCHAR(128) COMMENT '规则名称',
    conditions_json VARCHAR(2048) NOT NULL DEFAULT '[]' COMMENT '条件列表JSON: [{"attr":"","op":"","value":""}]',
    actions_json VARCHAR(2048) NOT NULL DEFAULT '[]' COMMENT '动作列表JSON: [{"action":"","params":{}}]',
    is_enabled TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_strategy (strategy_id)
) COMMENT '策略规则表';

-- ============================================================
-- 7. 任务模块 (Task)
-- ============================================================

-- 任务头
CREATE TABLE wms_task_header (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    task_no VARCHAR(64) NOT NULL COMMENT '任务号',
    task_type VARCHAR(32) NOT NULL COMMENT '类型: PUTAWAY/PICK/MOVE/REPLENISH/STOCKTAKE',
    task_source_id BIGINT COMMENT '来源单据ID',
    task_source_no VARCHAR(64) COMMENT '来源单据号',
    total_lines INT NOT NULL DEFAULT 0,
    completed_lines INT NOT NULL DEFAULT 0,
    priority INT NOT NULL DEFAULT 5 COMMENT '优先级: 1(最高)-9',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/RELEASED/ASSIGNED/EXECUTING/PARTIAL_DONE/DONE/CANCELLED',
    assign_to BIGINT COMMENT '分配给用户',
    assign_rule VARCHAR(32) COMMENT '分配规则: BY_ZONE/BY_CAPACITY/ROUND_ROBIN/MANUAL',
    start_time DATETIME,
    end_time DATETIME,
    deadline DATETIME COMMENT '截止时间',
    remark VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_task_no (tenant_id, task_no),
    KEY idx_warehouse (warehouse_id),
    KEY idx_assign_to (assign_to),
    KEY idx_status (status)
) COMMENT '任务头表';

-- 任务行
CREATE TABLE wms_task_line (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    task_header_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    ref_id BIGINT COMMENT '关联操作单据行ID(putaway_line_id/pick_line_id等)',
    sku_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    sku_name VARCHAR(256) NOT NULL,
    task_qty DECIMAL(18,4) NOT NULL COMMENT '任务数量',
    done_qty DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '完成数量',
    from_location_id BIGINT,
    to_location_id BIGINT,
    batch_no VARCHAR(64),
    lot_attrs VARCHAR(1024),
    container_code VARCHAR(64) COMMENT '容器/周转箱号',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/EXECUTING/DONE/SKIPPED',
    exec_by BIGINT COMMENT '执行人',
    exec_start_time DATETIME COMMENT '开始执行时间',
    exec_end_time DATETIME COMMENT '结束执行时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_task_header (task_header_id),
    KEY idx_sku (sku_id),
    KEY idx_status (status)
) COMMENT '任务行表';

-- ============================================================
-- 8. 打印模块 (Print)
-- ============================================================

-- 标签模板表
CREATE TABLE wms_print_template (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    template_code VARCHAR(64) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(128) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(32) NOT NULL COMMENT '类型: SKU_LABEL/LOCATION_LABEL/PALLET_LABEL/INBOUND_LABEL/SHIPPING_LABEL',
    paper_width DECIMAL(6,1) NOT NULL COMMENT '纸张宽度(mm)',
    paper_height DECIMAL(6,1) NOT NULL COMMENT '纸张高度(mm)',
    content_json TEXT NOT NULL COMMENT '模板内容JSON(ZPL指令/JSON布局)',
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_template_code (tenant_id, template_code)
) COMMENT '标签模板表';

-- 打印记录表
CREATE TABLE wms_print_record (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL COMMENT '模板ID',
    printer_name VARCHAR(128) COMMENT '打印机名称',
    print_content TEXT COMMENT '实际打印内容',
    print_count INT NOT NULL DEFAULT 1 COMMENT '打印份数',
    ref_type VARCHAR(32) COMMENT '关联单据类型',
    ref_id BIGINT COMMENT '关联单据ID',
    print_by BIGINT COMMENT '打印人',
    print_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打印时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_template (template_id),
    KEY idx_ref (ref_type, ref_id)
) COMMENT '打印记录表';

-- ============================================================
-- 9. 初始化默认数据
-- ============================================================

-- 默认租户
INSERT INTO wms_sys_tenant (id, tenant_code, tenant_name) VALUES
(1, 'DEFAULT', '默认租户');

-- 默认管理员用户 (密码: admin123, BCrypt加密)
-- BCrypt hash of "admin123"
INSERT INTO wms_sys_user (id, tenant_id, username, password, real_name, status) VALUES
(1, 1, 'admin', '$2a$10$0mVRNe1FTZBcXo4u5g2uC.5QqBFe/iCO2/9NkMhxyBCrUyzVqHZPm', '系统管理员', 1);

-- 默认角色
INSERT INTO wms_sys_role (id, tenant_id, role_code, role_name) VALUES
(1, 1, 'ADMIN', '超级管理员'),
(2, 1, 'MANAGER', '仓库经理'),
(3, 1, 'OPERATOR', '操作员'),
(4, 1, 'PICKER', '拣货员');

-- 管理员角色关联
INSERT INTO wms_sys_user_role (id, tenant_id, user_id, role_id) VALUES
(1, 1, 1, 1);

-- 常用字典数据
INSERT INTO wms_masterdata_dictionary (id, tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(1, 1, 'WH_TYPE', 'STANDARD', '标准仓', 1),
(2, 1, 'WH_TYPE', 'COLD', '冷库', 2),
(3, 1, 'WH_TYPE', 'HAZARDOUS', '危化品仓', 3),
(4, 1, 'AREA_TYPE', 'RECEIVE', '收货区', 1),
(5, 1, 'AREA_TYPE', 'SHIPPING', '发货区', 2),
(6, 1, 'AREA_TYPE', 'STORAGE', '存储区', 3),
(7, 1, 'AREA_TYPE', 'PICKING', '拣货区', 4),
(8, 1, 'AREA_TYPE', 'RETURN', '退货区', 5),
(9, 1, 'AREA_TYPE', 'QC', '质检区', 6),
(10, 1, 'LOCATION_TYPE', 'FLOOR', '地堆位', 1),
(11, 1, 'LOCATION_TYPE', 'RACK', '货架位', 2),
(12, 1, 'LOCATION_TYPE', 'SHELF', '隔板位', 3),
(13, 1, 'LOCATION_TYPE', 'BIN', '周转箱位', 4),
(14, 1, 'PACKAGE_LEVEL', 'EA', '件(each)', 1),
(15, 1, 'PACKAGE_LEVEL', 'CASE', '箱(case)', 2),
(16, 1, 'PACKAGE_LEVEL', 'PALLET', '托盘(pallet)', 3),
(17, 1, 'ORDER_TYPE', 'SALE', '销售订单', 1),
(18, 1, 'ORDER_TYPE', 'TRANSFER', '调拨订单', 2),
(19, 1, 'ORDER_TYPE', 'RETURN_SUPPLIER', '退货供应商', 3);
