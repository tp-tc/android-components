/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.samples.browser

import android.app.Application
import mozilla.components.browser.session.Session
import mozilla.components.support.base.facts.Facts
import mozilla.components.support.base.facts.processor.LogFactProcessor
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.base.log.sink.AndroidLogSink
import mozilla.components.support.ktx.android.content.isMainProcess
import mozilla.components.support.webextensions.WebExtensionSupport

class SampleApplication : Application() {
    val components by lazy { Components(this) }

    override fun onCreate() {
        super.onCreate()

        Log.addSink(AndroidLogSink())

        if (!isMainProcess()) {
            return
        }

        Facts.registerProcessor(LogFactProcessor())

        components.engine.warmUp()

        try {
            WebExtensionSupport.initialize(
                components.engine,
                components.store,
                onNewTabOverride = {
                    _, engineSession, url -> components.sessionManager.add(Session(url), true, engineSession)
                }
            )
        } catch (e: UnsupportedOperationException) {
            // Web extension support is only available for engine gecko
            Logger.error("Failed to initialize web extension support", e)
        }
    }
}
