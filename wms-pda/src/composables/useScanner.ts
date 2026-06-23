/**
 * 扫码 Composable — 封装 uni.scanCode
 * 提供：连续扫描、振动反馈、防重复、结果解析
 */

import { ref } from 'vue'
import { useAppStore } from '@/stores/app'

export interface ScanResult {
  barcodeType: string  // 条码类型：ASN/ORDER/SKU/LOCATION/CONTAINER/UNKNOWN
  barcodeValue: string // 条码原始值
  raw: UniApp.ScanCodeSuccessRes
}

// ── 条码类型识别规则 ──
// 可按业务扩展前缀映射
const BARCODE_PREFIX_MAP: Record<string, string> = {
  ASN: 'ASN',
  ORD: 'ORDER',
  WAV: 'WAVE',
  SKU: 'SKU',
  LOC: 'LOCATION',
  CTN: 'CONTAINER',
  LOT: 'LOT',
  SN: 'SN'
}

function identifyBarcodeType(value: string): string {
  const upper = value.toUpperCase()
  for (const [prefix, type] of Object.entries(BARCODE_PREFIX_MAP)) {
    if (upper.startsWith(prefix)) return type
  }
  return 'UNKNOWN'
}

// ── Composable ──
export function useScanner() {
  const lastScanTime = ref(0)
  const scanning = ref(false)
  const appStore = useAppStore()
  const SCAN_COOLDOWN = 500 // ms 冷却时间

  /**
   * 触发扫码
   * @param options.onlyFromCamera 仅从相机扫码（默认 true）
   * @param options.autoIdentify 自动识别条码类型（默认 true）
   */
  function scan(options?: {
    onlyFromCamera?: boolean
    autoIdentify?: boolean
  }): Promise<ScanResult | null> {
    const { onlyFromCamera = true, autoIdentify = true } = options ?? {}

    return new Promise((resolve) => {
      // 防重复
      const now = Date.now()
      if (scanning.value || now - lastScanTime.value < SCAN_COOLDOWN) {
        resolve(null)
        return
      }

      scanning.value = true
      appStore.lockScanner()

      uni.scanCode({
        onlyFromCamera,
        scanType: ['barCode', 'qrCode', 'datamatrix', 'pdf417'],
        success: (res) => {
          // 振动反馈
          uni.vibrateShort({ type: 'light' })

          const result: ScanResult = {
            barcodeType: autoIdentify ? identifyBarcodeType(res.result) : 'UNKNOWN',
            barcodeValue: res.result,
            raw: res
          }

          lastScanTime.value = Date.now()
          resolve(result)
        },
        fail: (err) => {
          console.log('[Scanner] 扫码取消或失败:', err.errMsg)
          resolve(null)
        },
        complete: () => {
          scanning.value = false
          appStore.unlockScanner()
        }
      })
    })
  }

  /**
   * 连续扫码模式（不关闭扫码界面）
   */
  function scanContinuously(
    onResult: (result: ScanResult) => void,
    interval = 800
  ): void {
    const doScan = () => {
      scan().then((result) => {
        if (result) onResult(result)
        setTimeout(doScan, interval)
      })
    }
    doScan()
  }

  return { scan, scanContinuously, scanning, lastScanTime }
}
