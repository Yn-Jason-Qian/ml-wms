<template>
  <div class="rule-editor">
    <!-- 条件 -->
    <div class="editor-section">
      <div class="section-header">
        <span class="section-label">匹配条件</span>
        <el-tooltip content="所有条件需同时满足（AND）" placement="top">
          <el-icon style="cursor:help;color:#909399"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <div v-for="(cond, i) in conditions" :key="i" class="rule-item">
        <span class="item-index">{{ i + 1 }}</span>
        <el-select :model-value="cond.attr" @update:model-value="(v: string) => updateAttr(i, v)" placeholder="属性" style="width: 160px">
          <el-option v-for="a in availableAttrs" :key="a.value" :label="a.label" :value="a.value" />
        </el-select>
        <el-select v-model="cond.op" placeholder="运算符" style="width: 110px">
          <el-option v-for="o in getOps(cond.attr)" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
        <el-input
          v-if="!isMultiOp(cond.op)"
          v-model="cond.value"
          :placeholder="getValuePlaceholder(cond.attr)"
          style="width: 200px"
        />
        <el-select
          v-else
          v-model="cond.valueList"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="输入后回车"
          style="width: 240px"
        />
        <el-button
          @click="removeCondition(i)"
          :disabled="conditions.length <= 1"
          type="danger"
          :icon="Delete"
          circle
          size="small"
        />
      </div>
      <el-button link type="primary" :icon="Plus" @click="addCondition">添加条件</el-button>
    </div>

    <!-- 动作 -->
    <div class="editor-section">
      <div class="section-header"><span class="section-label">执行动作</span></div>
      <div v-for="(act, i) in actions" :key="i" class="rule-item">
        <span class="item-index">{{ i + 1 }}</span>
        <el-select v-model="act.type" @change="(v: string) => onActionTypeChange(i, v)" placeholder="动作类型" style="width: 200px">
          <el-option v-for="a in actionTypes" :key="a.value" :label="a.label" :value="a.value" />
        </el-select>
        <template v-for="p in getActionFieldDefs(act.type)" :key="p.key">
          <el-input
            v-model="act.params[p.key]"
            :placeholder="p.label"
            style="width: 150px"
          />
        </template>
        <el-button
          @click="removeAction(i)"
          :disabled="actions.length <= 1"
          type="danger"
          :icon="Delete"
          circle
          size="small"
        />
      </div>
      <el-button link type="primary" :icon="Plus" @click="addAction">添加动作</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { Delete, Plus, InfoFilled } from '@element-plus/icons-vue'

export interface ConditionItem {
  attr: string
  op: string
  value: string
  valueList?: string[]
}

export interface ActionItem {
  type: string
  params: Record<string, string>
}

const props = defineProps<{
  modelValue: { conditionsJson: string; actionsJson: string }
}>()

const emit = defineEmits<{ 'update:modelValue': [v: { conditionsJson: string; actionsJson: string }] }>()

// ── 属性定义 ──
const availableAttrs = [
  // SKU 属性
  { label: 'SKU 编码', value: 'sku.code' },
  { label: 'SKU 名称', value: 'sku.name' },
  { label: 'SKU 重量(g)', value: 'sku.weight' },
  { label: 'SKU 体积(m³)', value: 'sku.volume' },
  { label: 'SKU 类别', value: 'sku.category' },
  { label: 'SKU 批次', value: 'sku.batchNo' },
  { label: '效期剩余天数', value: 'sku.expiryWithin' },
  // 库位属性
  { label: '库位编码', value: 'location.code' },
  { label: '库位类型', value: 'location.type' },
  { label: '库位区域', value: 'location.zone' },
  { label: '库位温层', value: 'location.tempZone' },
  { label: '库位容量', value: 'location.capacity' },
  // 订单属性
  { label: '订单优先级', value: 'order.priority' },
  { label: '订单类型', value: 'order.type' },
  { label: '客户名称', value: 'order.customerName' },
  { label: '订单行数', value: 'order.lineCount' },
  // 上架属性
  { label: '来源库位', value: 'putaway.fromLocationId' },
  { label: '上架类型', value: 'putaway.type' },
] as const

// 数字型属性 → 使用比较运算符 (gt/lt/gte/lte)
const numericAttrs = new Set([
  'sku.weight', 'sku.volume', 'sku.expiryWithin',
  'location.capacity', 'order.priority', 'order.lineCount',
])

const allOps = [
  { label: '等于', value: 'eq' },
  { label: '不等于', value: 'ne' },
  { label: '大于', value: 'gt' },
  { label: '小于', value: 'lt' },
  { label: '大于等于', value: 'gte' },
  { label: '小于等于', value: 'lte' },
  { label: '包含于', value: 'in' },
  { label: '不包含于', value: 'notIn' },
  { label: '包含文字', value: 'contains' },
  { label: '正则匹配', value: 'regex' },
]

const stringOnlyOps = ['eq', 'ne', 'in', 'notIn', 'contains', 'regex']
const multiOps = new Set(['in', 'notIn'])

function getOps(attr: string) {
  return allOps.filter(o => numericAttrs.has(attr) || stringOnlyOps.includes(o.value))
}

function isMultiOp(op: string) { return multiOps.has(op) }

function getValuePlaceholder(attr: string) {
  if (attr === 'location.type') return '例: RACK,SHELF'
  if (attr === 'location.zone') return '例: A01'
  return '输入值'
}

// ── 动作定义 ──
const actionTypes = [
  { label: '推荐库位', value: 'assign_location' },
  { label: '优先分配', value: 'priority_allocate' },
  { label: '波次分组', value: 'wave_group' },
  { label: '拣货模式', value: 'pick_mode' },
  { label: '打印标签', value: 'print_label' },
]

const actionFieldDefs: Record<string, { key: string; label: string }[]> = {
  assign_location: [
    { key: 'zone', label: '区域' },
    { key: 'type', label: '库位类型' },
    { key: 'prefix', label: '编码前缀' },
    { key: 'locationId', label: '指定库位ID' },
  ],
  priority_allocate: [
    { key: 'zone', label: '优先区域' },
    { key: 'sort', label: '排序(FEFO/FIFO)' },
  ],
  wave_group: [
    { key: 'maxOrders', label: '最大订单数' },
    { key: 'sortDesc', label: '排序字段' },
  ],
  pick_mode: [
    { key: 'mode', label: '模式(FEFO/FIFO/AREA)' },
    { key: 'maxLines', label: '最大行数' },
  ],
  print_label: [
    { key: 'templateId', label: '模板ID' },
    { key: 'copies', label: '打印份数' },
  ],
}

function getActionFieldDefs(type: string | undefined) {
  return type ? (actionFieldDefs[type] || []) : []
}

// ── 内部状态 ──
const conditions = ref<ConditionItem[]>([{ attr: '', op: 'eq', value: '' }])
const actions = ref<ActionItem[]>([{ type: '', params: {} }])

// ── 从/到 JSON 转换 ──
function parseJson(jsonStr: string, fallback: any[]) {
  try { return JSON.parse(jsonStr) } catch { return fallback }
}

function loadFromJson(condJson: string, actJson: string) {
  const rawConds = parseJson(condJson, [])
  conditions.value = rawConds.length > 0
    ? rawConds.map((c: any) => ({
        attr: c.attr || '',
        op: c.op || 'eq',
        value: multiOps.has(c.op) ? '' : (c.value || ''),
        valueList: multiOps.has(c.op) ? (typeof c.value === 'string' ? c.value.split(',') : (Array.isArray(c.value) ? c.value : [])) : undefined,
      }))
    : [{ attr: '', op: 'eq', value: '' }]

  const rawActs = parseJson(actJson, [])
  actions.value = rawActs.length > 0
    ? rawActs.map((a: any) => ({
        type: a.type || '',
        params: a.params || {},
      }))
    : [{ type: '', params: {} }]
}

function toCondJson(): string {
  const arr = conditions.value
    .filter(c => c.attr && c.op)
    .map(c => ({
      attr: c.attr,
      op: c.op,
      value: multiOps.has(c.op) && c.valueList?.length
        ? c.valueList.join(',')
        : c.value,
    }))
  return arr.length > 0 ? JSON.stringify(arr) : '[]'
}

function toActJson(): string {
  const arr = actions.value
    .filter(a => a.type)
    .map(a => ({
      type: a.type,
      params: Object.fromEntries(
        Object.entries(a.params).filter(([_, v]) => v !== '' && v !== undefined)
      ),
    }))
  return arr.length > 0 ? JSON.stringify(arr) : '[]'
}

function emitUpdate() {
  emit('update:modelValue', {
    conditionsJson: toCondJson(),
    actionsJson: toActJson(),
  })
}

// ── 操作 ──
function addCondition() { conditions.value.push({ attr: '', op: 'eq', value: '' }); emitUpdate() }
function removeCondition(i: number) { conditions.value.splice(i, 1); if (conditions.value.length === 0) addCondition(); emitUpdate() }

function addAction() { actions.value.push({ type: '', params: {} }); emitUpdate() }
function removeAction(i: number) { actions.value.splice(i, 1); if (actions.value.length === 0) addAction(); emitUpdate() }

function updateAttr(i: number, attr: string) {
  const c = conditions.value[i]
  c.attr = attr
  // 属性切到非数字时重置不合适运算符
  if (!numericAttrs.has(attr) && !stringOnlyOps.includes(c.op)) {
    c.op = 'eq'
  }
  emitUpdate()
}

function onActionTypeChange(i: number, _type: string) {
  actions.value[i].params = {}
  emitUpdate()
}

watch([conditions, actions], () => { emitUpdate() }, { deep: true })

onMounted(() => {
  loadFromJson(props.modelValue.conditionsJson, props.modelValue.actionsJson)
})
</script>

<style scoped>
.rule-editor { background: #f8f9fb; border-radius: 8px; padding: 16px 16px 12px; }
.editor-section { margin-bottom: 16px; }
.editor-section:last-child { margin-bottom: 0; }
.section-header { display: flex; align-items: center; gap: 6px; margin-bottom: 10px; }
.section-label { font-size: 13px; font-weight: 600; color: #303133; }
.rule-item { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; flex-wrap: wrap; }
.item-index { display: inline-flex; width: 22px; height: 22px; align-items: center; justify-content: center; border-radius: 50%; background: #e6f0ff; color: #409eff; font-size: 12px; font-weight: 600; flex-shrink: 0; }
</style>
