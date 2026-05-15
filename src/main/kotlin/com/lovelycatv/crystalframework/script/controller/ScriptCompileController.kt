package com.lovelycatv.crystalframework.script.controller

import com.lovelycatv.crystalframework.script.service.KotlinCompileService
import com.lovelycatv.crystalframework.script.types.CompileResult
import com.lovelycatv.crystalframework.shared.annotations.Unauthorized
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class CompileCheckRequest(
    val sourceCode: String
)

@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/script")
class ScriptCompileController(
    private val kotlinCompileService: KotlinCompileService
) {
    @Unauthorized
    @PostMapping("/compile-check", version = "1")
    fun compileCheck(@RequestBody request: CompileCheckRequest): ApiResponse<CompileResult> {
        val result = kotlinCompileService.checkCompilation(request.sourceCode)
        return ApiResponse.success(result)
    }
}
