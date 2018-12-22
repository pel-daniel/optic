package com.opticdev.runtime

import play.api.libs.json.JsValue

case class RuntimeValueFragment(modelHash: String, runtimeComponentId: String, value: JsValue)