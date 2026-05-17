package com.lovelycatv.crystalframework.system.controller.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SystemInitializeDTO(
    @field:NotBlank(message = "Username is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @field:Size(max = 64, message = "Username length cannot exceed 64 characters")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$", message = "Password must be at least 8 characters and contain both letters and numbers")
    @field:Size(max = 128, message = "Password length cannot exceed 128 characters")
    val password: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @field:Size(max = 256, message = "Email length cannot exceed 256 characters")
    val email: String,

    @field:NotBlank(message = "SMTP host is required")
    val smtpHost: String,

    @Min(value = 0, message = "SMTP Port must be at least 1 or greater than 0")
    @Max(value = 65535, message = "SMTP Port must be less than or equal to 65535")
    val smtpPort: Int,

    @field:NotBlank(message = "SMTP username is required")
    val smtpUsername: String,

    @field:NotBlank(message = "SMTP password is required")
    val smtpPassword: String,

    @field:NotBlank(message = "From email is required")
    @field:Email(message = "Invalid from email format")
    val fromEmail: String,

    @field:NotBlank(message = "From name is required")
    val fromName: String,
)