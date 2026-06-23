/**
 * Patched version of @dcloudio/uni-app/dist/uni-app.es.js
 *
 * Original imports `isInSSRComponentSetup` and `injectHook` from 'vue',
 * but these are Vue runtime-core internals NOT exported by the public vue package.
 *
 * This patched version replaces them with equivalents:
 * - isInSSRComponentSetup → always false (PDA is pure client-side)
 * - injectHook → Vue's internal hook injection (extracted from runtime-core)
 */
import { shallowRef, ref, getCurrentInstance } from 'vue'
import { hasOwn } from '@vue/shared'
export { capitalize, extend, hasOwn, isPlainObject } from '@vue/shared'
import { sanitise, UNI_SSR_DATA, UNI_SSR_GLOBAL_DATA, UNI_SSR, ON_SHOW, ON_HIDE, ON_LAUNCH, ON_ERROR, ON_THEME_CHANGE, ON_PAGE_NOT_FOUND, ON_UNHANDLE_REJECTION, ON_LAST_PAGE_BACK_PRESS, ON_EXIT, ON_INIT, ON_LOAD, ON_READY, ON_UNLOAD, ON_RESIZE, ON_BACK_PRESS, ON_PAGE_SCROLL, ON_TAB_ITEM_TAP, ON_REACH_BOTTOM, ON_PULL_DOWN_REFRESH, ON_SAVE_EXIT_STATE, ON_TITLE_CLICK, ON_SHARE_TIMELINE, ON_SHARE_CHAT, ON_ADD_TO_FAVORITES, ON_SHARE_APP_MESSAGE, ON_COPY_URL, ON_UPLOAD_DOUYIN_VIDEO, ON_LIVE_MOUNT, ON_NAVIGATION_BAR_BUTTON_TAP, ON_NAVIGATION_BAR_SEARCH_INPUT_CHANGED, ON_NAVIGATION_BAR_SEARCH_INPUT_CLICKED, ON_NAVIGATION_BAR_SEARCH_INPUT_CONFIRMED, ON_NAVIGATION_BAR_SEARCH_INPUT_FOCUS_CHANGED } from '@dcloudio/uni-shared'

// ── Polyfills for Vue internal APIs ──

// isInSSRComponentSetup: always false in PDA (pure client-side, no SSR)
var isInSSRComponentSetup = false

// injectHook: Vue's internal lifecycle hook registration.
// We use getCurrentInstance() and manually push onto the component's lifecycle array.
function injectHook(type, hook, target) {
  target = target || getCurrentInstance()
  if (target) {
    var hooks = target[type] || (target[type] = [])
    // wrapped hook with error handling
    var wrappedHook = function () {
      var args = []
      for (var _i = 0; _i < arguments.length; _i++) { args[_i] = arguments[_i] }
      try {
        return hook.apply(void 0, args)
      } catch (e) {
        console.error('UniApp lifecycle hook error:', e)
      }
    }
    wrappedHook.__weh = hook.__weh
    hooks.push(wrappedHook)
  }
}

function getSSRDataType() {
  return getCurrentInstance() ? UNI_SSR_DATA : UNI_SSR_GLOBAL_DATA
}
function assertKey(key, shallow) {
  shallow = shallow === undefined ? false : shallow
  if (!key) {
    throw new Error((shallow ? 'shallowSsrRef' : 'ssrRef') + ': You must provide a key.')
  }
}
var ssrClientRef = function (value, key, shallow) {
  shallow = shallow === undefined ? false : shallow
  var valRef = shallow ? shallowRef(value) : ref(value)
  if (typeof window === 'undefined') {
    return valRef
  }
  var __uniSSR = window[UNI_SSR]
  if (!__uniSSR) {
    return valRef
  }
  var type = getSSRDataType()
  assertKey(key, shallow)
  if (hasOwn(__uniSSR[type], key)) {
    valRef.value = __uniSSR[type][key]
    if (type === UNI_SSR_DATA) {
      delete __uniSSR[type][key]
    }
  }
  return valRef
}
var globalData = {}
var ssrRef = function (value, key) {
  return ssrClientRef(value, key)
}
var shallowSsrRef = function (value, key) {
  return ssrClientRef(value, key, true)
}
function getSsrGlobalData() {
  return sanitise(globalData)
}
function getCurrentSubNVue() {
  return uni.getSubNVueById(plus.webview.currentWebview().id)
}
function requireNativePlugin(name) {
  return weex.requireModule(name)
}
function formatAppLog(type, filename) {
  var args = []
  for (var _i = 2; _i < arguments.length; _i++) { args[_i - 2] = arguments[_i] }
  if (uni.__log__) {
    uni.__log__(type, filename, args)
  } else {
    console[type].apply(console, args.concat([filename]))
  }
}
function formatLog(type, filename) {
  var args = []
  for (var _i = 2; _i < arguments.length; _i++) { args[_i - 2] = arguments[_i] }
  if (filename) {
    args.push(filename)
  }
  console[type].apply(console, args)
}
function resolveEasycom(component, easycom) {
  return typeof component === 'string' ? easycom : component
}

var createLifeCycleHook = function (lifecycle, _flag) {
  _flag = _flag === undefined ? 0 : _flag
  return function (hook, target) {
    target = target || getCurrentInstance()
    !isInSSRComponentSetup && injectHook(lifecycle, hook, target)
  }
}
var onShow = createLifeCycleHook(ON_SHOW, 1 | 2)
var onHide = createLifeCycleHook(ON_HIDE, 1 | 2)
var onLaunch = createLifeCycleHook(ON_LAUNCH, 1)
var onError = createLifeCycleHook(ON_ERROR, 1)
var onThemeChange = createLifeCycleHook(ON_THEME_CHANGE, 1)
var onPageNotFound = createLifeCycleHook(ON_PAGE_NOT_FOUND, 1)
var onUnhandledRejection = createLifeCycleHook(ON_UNHANDLE_REJECTION, 1)
var onLastPageBackPress = createLifeCycleHook(ON_LAST_PAGE_BACK_PRESS, 1)
var onExit = createLifeCycleHook(ON_EXIT, 1)
var onInit = createLifeCycleHook(ON_INIT, 2 | 4)
var onLoad = createLifeCycleHook(ON_LOAD, 2)
var onReady = createLifeCycleHook(ON_READY, 2)
var onUnload = createLifeCycleHook(ON_UNLOAD, 2)
var onResize = createLifeCycleHook(ON_RESIZE, 2)
var onBackPress = createLifeCycleHook(ON_BACK_PRESS, 2)
var onPageScroll = createLifeCycleHook(ON_PAGE_SCROLL, 2)
var onTabItemTap = createLifeCycleHook(ON_TAB_ITEM_TAP, 2)
var onReachBottom = createLifeCycleHook(ON_REACH_BOTTOM, 2)
var onPullDownRefresh = createLifeCycleHook(ON_PULL_DOWN_REFRESH, 2)
var onSaveExitState = createLifeCycleHook(ON_SAVE_EXIT_STATE, 2)
var onTitleClick = createLifeCycleHook(ON_TITLE_CLICK, 2)
var onShareTimeline = createLifeCycleHook(ON_SHARE_TIMELINE, 2)
var onShareChat = createLifeCycleHook(ON_SHARE_CHAT, 2)
var onAddToFavorites = createLifeCycleHook(ON_ADD_TO_FAVORITES, 2)
var onShareAppMessage = createLifeCycleHook(ON_SHARE_APP_MESSAGE, 2)
var onCopyUrl = createLifeCycleHook(ON_COPY_URL, 2)
var onUploadDouyinVideo = createLifeCycleHook(ON_UPLOAD_DOUYIN_VIDEO, 2)
var onLiveMount = createLifeCycleHook(ON_LIVE_MOUNT, 2)
var onNavigationBarButtonTap = createLifeCycleHook(ON_NAVIGATION_BAR_BUTTON_TAP, 2)
var onNavigationBarSearchInputChanged = createLifeCycleHook(ON_NAVIGATION_BAR_SEARCH_INPUT_CHANGED, 2)
var onNavigationBarSearchInputClicked = createLifeCycleHook(ON_NAVIGATION_BAR_SEARCH_INPUT_CLICKED, 2)
var onNavigationBarSearchInputConfirmed = createLifeCycleHook(ON_NAVIGATION_BAR_SEARCH_INPUT_CONFIRMED, 2)
var onNavigationBarSearchInputFocusChanged = createLifeCycleHook(ON_NAVIGATION_BAR_SEARCH_INPUT_FOCUS_CHANGED, 2)
var onPageHide = onHide
var onPageShow = onShow
var onAppHide = onHide
var onAppShow = onShow

function renderComponentSlot(slots, name, props) {
  props = props === undefined ? null : props
  if (slots[name]) {
    return slots[name](props)
  }
  return null
}

export {
  formatAppLog, formatLog, getCurrentSubNVue, getSsrGlobalData,
  onAddToFavorites, onAppHide, onAppShow, onBackPress, onCopyUrl,
  onError, onExit, onHide, onInit, onLastPageBackPress, onLaunch,
  onLiveMount, onLoad, onNavigationBarButtonTap,
  onNavigationBarSearchInputChanged, onNavigationBarSearchInputClicked,
  onNavigationBarSearchInputConfirmed, onNavigationBarSearchInputFocusChanged,
  onPageHide, onPageNotFound, onPageScroll, onPageShow,
  onPullDownRefresh, onReachBottom, onReady, onResize,
  onSaveExitState, onShareAppMessage, onShareChat, onShareTimeline,
  onShow, onTabItemTap, onThemeChange, onTitleClick,
  onUnhandledRejection, onUnload, onUploadDouyinVideo,
  renderComponentSlot, requireNativePlugin, resolveEasycom,
  shallowSsrRef, ssrRef
}
