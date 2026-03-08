package com.lovelycatv.crystalframework.mail.constants

import com.lovelycatv.crystalframework.mail.types.MailTemplateCategoryDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateTypeDeclaration

object SystemMailDeclaration {
    const val VARIABLE_EMAIL_CODE = "code"

    private val systemCategory = MailTemplateCategoryDeclaration(
        name = "system",
        description = "System mail templates",
    )

    val categories = listOf(
        systemCategory
    )

    val systemUserRegisterTemplateType = MailTemplateTypeDeclaration(
        name = "system_user_register",
        description = "User registration mail templates",
        variables = arrayOf(VARIABLE_EMAIL_CODE),
        categoryDeclaration = systemCategory,
        allowMultiple = false
    )

    val systemResetPasswordTemplateType =  MailTemplateTypeDeclaration(
        name = "system_reset_password",
        description = "User reset password mail templates",
        variables = arrayOf(VARIABLE_EMAIL_CODE),
        categoryDeclaration = systemCategory,
        allowMultiple = false
    )

    val systemResetEmailAddressTemplateType = MailTemplateTypeDeclaration(
        name = "system_reset_email_address",
        description = "User reset email address mail templates",
        variables = arrayOf(VARIABLE_EMAIL_CODE),
        categoryDeclaration = systemCategory,
        allowMultiple = false
    )

    val types = listOf(
        systemUserRegisterTemplateType,
        systemResetPasswordTemplateType,
        systemResetEmailAddressTemplateType
    )

    val defaultSystemUserRegisterTemplate = MailTemplateDeclaration(
        name = "default_system_user_register",
        description = "User registration mail templates",
        title = "Register",
        content = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Email Code</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f7f9; color: #333333;">
            <table border="0" cellpadding="0" cellspacing="0" width="100%" style="table-layout: fixed;">
                <tr>
                    <td align="center" style="padding: 40px 0;">
                        <table border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.05);">
            
                            <tr>
                                <td style="background-color: #007bff; height: 6px;"></td>
                            </tr>
            
                            <tr>
                                <td style="padding: 40px 30px;">
                                    <h2 style="margin: 0 0 20px; color: #007bff; font-size: 24px; font-weight: 600;">SakuraChat Account Registration</h2>
            
                                    <p style="margin: 0 0 15px; font-size: 16px; line-height: 1.6; color: #555555;">
                                        Hello! Thank you for registering~
                                    </p>
            
                                    <p style="margin: 0 0 30px; font-size: 16px; line-height: 1.6; color: #555555;">
                                        Please enter the verification code below on the registration page to complete identity verification. This code will expire in <span style="font-weight: bold; color: #333;">5 minutes</span>:
                                    </p>
            
                                    <div style="background-color: #f0f7ff; border: 1px dashed #007bff; border-radius: 4px; padding: 25px; text-align: center; margin-bottom: 30px;">
                                            <span style="font-size: 36px; font-family: 'Courier New', Courier, monospace; font-weight: bold; color: #007bff; letter-spacing: 10px;">
                                                {{code}}
                                            </span>
                                    </div>
            
                                    <p style="margin: 0 0 15px; font-size: 14px; line-height: 1.6; color: #888888;">
                                        If you did not attempt to register, please ignore this email. For your account security, do not share or disclose this verification code to others.
                                    </p>
            
                                    <div style="border-top: 1px solid #eeeeee; padding-top: 25px; margin-top: 30px;">
                                        <p style="margin: 5px 0 0; font-size: 12px; color: #999999;">
                                            This email is automatically sent by the system. Please do not reply directly.
                                        </p>
                                    </div>
                                </td>
                            </tr>
            
                            <tr>
                                <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center;">
                                    <p style="margin: 0; font-size: 12px; color: #aaaaaa;">
                                        &copy; 2026 CrystalFramework (LovelyCat). All rights reserved.
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
            </body>
            </html>
        """.trimIndent(),
        active = true,
        type = systemUserRegisterTemplateType
    )

    val defaultSystemResetPasswordTemplate = MailTemplateDeclaration(
        name = "default_system_reset_password",
        description = "Reset password mail templates",
        title = "Reset Password - Verify Your Identity",
        content = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Reset Password Verification</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f7f9; color: #333333;">
            <table border="0" cellpadding="0" cellspacing="0" width="100%" style="table-layout: fixed;">
                <tr>
                    <td align="center" style="padding: 40px 0;">
                        <table border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.05);">
                            <tr>
                                <td style="background-color: #007bff; height: 6px;"></td>
                            </tr>
                            <tr>
                                <td style="padding: 40px 30px;">
                                    <h2 style="margin: 0 0 20px; color: #007bff; font-size: 24px; font-weight: 600;">Password Reset</h2>
                                    <p style="margin: 0 0 15px; font-size: 16px; line-height: 1.6; color: #555555;">
                                        Hello! We received a request to reset your password.
                                    </p>
                                    <p style="margin: 0 0 30px; font-size: 16px; line-height: 1.6; color: #555555;">
                                        Please enter the verification code below on the password reset page to complete identity verification. This code will expire in <span style="font-weight: bold; color: #333;">5 minutes</span>:
                                    </p>
                                    <div style="background-color: #f0f7ff; border: 1px dashed #007bff; border-radius: 4px; padding: 25px; text-align: center; margin-bottom: 30px;">
                                            <span style="font-size: 36px; font-family: 'Courier New', Courier, monospace; font-weight: bold; color: #007bff; letter-spacing: 10px;">
                                                {{code}}
                                            </span>
                                    </div>
                                    <p style="margin: 0 0 15px; font-size: 14px; line-height: 1.6; color: #888888;">
                                        If you did not request a password reset, please ignore this email and check your account security. For your account safety, do not share this verification code with anyone.
                                    </p>
                                    <div style="border-top: 1px solid #eeeeee; padding-top: 25px; margin-top: 30px;">
                                        <p style="margin: 5px 0 0; font-size: 12px; color: #999999;">
                                            This email is automatically sent by the system. Please do not reply directly.
                                        </p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center;">
                                    <p style="margin: 0; font-size: 12px; color: #aaaaaa;">
                                        &copy; 2026 CrystalFramework (LovelyCat). All rights reserved.
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
            </body>
            </html>
        """.trimIndent(),
        active = true,
        type = systemResetPasswordTemplateType
    )

    val defaultSystemResetEmailAddressTemplate = MailTemplateDeclaration(
        name = "default_system_reset_email_address",
        description = "Reset email address mail templates",
        title = "Change Email - Verify New Email Address",
        content = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Change Email Verification</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f7f9; color: #333333;">
            <table border="0" cellpadding="0" cellspacing="0" width="100%" style="table-layout: fixed;">
                <tr>
                    <td align="center" style="padding: 40px 0;">
                        <table border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.05);">
                            <tr>
                                <td style="background-color: #007bff; height: 6px;"></td>
                            </tr>
                            <tr>
                                <td style="padding: 40px 30px;">
                                    <h2 style="margin: 0 0 20px; color: #007bff; font-size: 24px; font-weight: 600;">Change Email Address</h2>
                                    <p style="margin: 0 0 15px; font-size: 16px; line-height: 1.6; color: #555555;">
                                        Hello! We received a request to change your email address.
                                    </p>
                                    <p style="margin: 0 0 30px; font-size: 16px; line-height: 1.6; color: #555555;">
                                        Please enter the verification code below on the change email page to verify your new email address. This code will expire in <span style="font-weight: bold; color: #333;">5 minutes</span>:
                                    </p>
                                    <div style="background-color: #f0f7ff; border: 1px dashed #007bff; border-radius: 4px; padding: 25px; text-align: center; margin-bottom: 30px;">
                                            <span style="font-size: 36px; font-family: 'Courier New', Courier, monospace; font-weight: bold; color: #007bff; letter-spacing: 10px;">
                                                {{code}}
                                            </span>
                                    </div>
                                    <p style="margin: 0 0 15px; font-size: 14px; line-height: 1.6; color: #888888;">
                                        If you did not request to change your email, please ignore this email and ensure your account security. Do not share this verification code with anyone to protect your information.
                                    </p>
                                    <div style="border-top: 1px solid #eeeeee; padding-top: 25px; margin-top: 30px;">
                                        <p style="margin: 5px 0 0; font-size: 12px; color: #999999;">
                                            This email is automatically sent by the system. Please do not reply directly.
                                        </p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center;">
                                    <p style="margin: 0; font-size: 12px; color: #aaaaaa;">
                                        &copy; 2026 CrystalFramework (LovelyCat). All rights reserved.
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
            </body>
            </html>
        """.trimIndent(),
        active = true,
        type = systemResetEmailAddressTemplateType
    )
}