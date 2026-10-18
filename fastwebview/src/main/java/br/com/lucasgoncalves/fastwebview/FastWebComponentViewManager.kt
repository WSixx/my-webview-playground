/*
 * Copyright (C) 2025 Lucas S Goncalves
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.lucasgoncalves.fastwebview

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

/**
 * React Native compatibility ViewManager bridge for [FastWebView].
 * Exposes FastWebView as a React Native view component, allowing WebComponents to be mounted and controlled from JS.
 */
class FastWebComponentViewManager : SimpleViewManager<FastWebView>() {

    override fun getName(): String = REACT_CLASS

    override fun createViewInstance(reactContext: ThemedReactContext): FastWebView {
        val webView = WebViewWarmupPool.acquireFastWebView(reactContext)
        webView.onNativeMessageReceived = { message ->
            // Native message callback handling for React Native bridge
        }
        return webView
    }

    override fun onDropViewInstance(view: FastWebView) {
        super.onDropViewInstance(view)
        WebViewWarmupPool.releaseFastWebView(view)
    }

    @ReactProp(name = "componentTag")
    fun setComponentTag(view: FastWebView, tag: String?) {
        if (tag != null) {
            view.mountComponent(tag, "{}")
        }
    }

    @ReactProp(name = "propsJson")
    fun setPropsJson(view: FastWebView, propsJson: String?) {
        if (propsJson != null) {
            view.updateProps(propsJson)
        }
    }

    override fun getCommandsMap(): Map<String, Int> {
        return MapBuilder.of(
            COMMAND_MOUNT, COMMAND_MOUNT_ID,
            COMMAND_UPDATE_PROPS, COMMAND_UPDATE_PROPS_ID,
            COMMAND_SEND_MESSAGE, COMMAND_SEND_MESSAGE_ID
        )
    }

    override fun receiveCommand(root: FastWebView, commandId: Int, args: ReadableArray?) {
        when (commandId) {
            COMMAND_MOUNT_ID -> {
                val tag = args?.getString(0) ?: return
                val props = if (args.size() > 1) args.getString(1) else "{}"
                root.mountComponent(tag, props ?: "{}")
            }
            COMMAND_UPDATE_PROPS_ID -> {
                val props = args?.getString(0) ?: "{}"
                root.updateProps(props)
            }
            COMMAND_SEND_MESSAGE_ID -> {
                val msg = args?.getString(0) ?: return
                root.sendDirectMessage(msg)
            }
        }
    }

    companion object {
        const val REACT_CLASS = "FastWebComponentView"

        const val COMMAND_MOUNT = "mountComponent"
        const val COMMAND_MOUNT_ID = 1

        const val COMMAND_UPDATE_PROPS = "updateProps"
        const val COMMAND_UPDATE_PROPS_ID = 2

        const val COMMAND_SEND_MESSAGE = "sendMessage"
        const val COMMAND_SEND_MESSAGE_ID = 3
    }
}
