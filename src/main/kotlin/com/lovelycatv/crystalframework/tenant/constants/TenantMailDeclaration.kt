package com.lovelycatv.crystalframework.tenant.constants

import com.lovelycatv.crystalframework.mail.types.MailTemplateCategoryDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateTypeDeclaration

object TenantMailDeclaration {
    const val VARIABLE_USERNAME = "username"
    const val VARIABLE_NICKNAME = "nickname"
    const val VARIABLE_REAL_NAME = "real_name"
    const val VARIABLE_PHONE_NUMBER = "phone_number"

    val tenantCategory = MailTemplateCategoryDeclaration(
        name = "tenant",
        description = "Tenant mail templates",
    )

    val tenantMemberJoinReviewTemplateType = MailTemplateTypeDeclaration(
        name = "tenant_member_join_review",
        description = "Notification to tenant admins about new member join request",
        variables = arrayOf(
            VARIABLE_USERNAME,
            VARIABLE_NICKNAME,
            VARIABLE_REAL_NAME,
            VARIABLE_PHONE_NUMBER
        ),
        categoryDeclaration = tenantCategory,
        allowMultiple = false
    )

    val defaultTenantMemberJoinReviewTemplate = MailTemplateDeclaration(
        name = "default_tenant_member_join_review",
        description = "Notification to tenant admins about new member join request",
        title = "New Tenant Member Join Request",
        content = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Tenant Join Request</title>
        </head>
        <body style="margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f7f9; color: #333333;">
        <table border="0" cellpadding="0" cellspacing="0" width="100%" style="table-layout: fixed;">
            <tr>
                <td align="center" style="padding: 40px 0;">
                    <table border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.05);">
        
                        <tr>
                            <td style="background-color: #28a745; height: 6px;"></td>
                        </tr>
        
                        <tr>
                            <td style="padding: 40px 30px;">
                                <h2 style="margin: 0 0 20px; color: #28a745; font-size: 24px; font-weight: 600;">New Tenant Join Request</h2>
        
                                <p style="margin: 0 0 15px; font-size: 16px; line-height: 1.6; color: #555555;">
                                    Hello,
                                </p>
        
                                <p style="margin: 0 0 20px; font-size: 16px; line-height: 1.6; color: #555555;">
                                    A new user has requested to join your tenant. Please review the applicant details below:
                                </p>
        
                                <div style="background-color: #f8f9fa; border-left: 4px solid #28a745; border-radius: 4px; padding: 20px; margin-bottom: 30px;">
                                    <table style="width: 100%; font-size: 15px; line-height: 1.8; color: #555555;">
                                        <tr>
                                            <td style="padding: 5px 0; width: 120px; color: #777777;">Username:</td>
                                            <td style="padding: 5px 0; font-weight: 500; color: #333333;">{{username}}</td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 5px 0; color: #777777;">Nickname:</td>
                                            <td style="padding: 5px 0; font-weight: 500; color: #333333;">{{nickname}}</td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 5px 0; color: #777777;">Real Name:</td>
                                            <td style="padding: 5px 0; font-weight: 500; color: #333333;">{{real_name}}</td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 5px 0; color: #777777;">Phone Number:</td>
                                            <td style="padding: 5px 0; font-weight: 500; color: #333333;">{{phone_number}}</td>
                                        </tr>
                                    </table>
                                </div>
        
                                <p style="margin: 0 0 25px; font-size: 16px; line-height: 1.6; color: #555555;">
                                    Please log in to the admin console to approve or decline this request.</span>.
                                </p>
        
                                <p style="margin: 0 0 15px; font-size: 14px; line-height: 1.6; color: #888888;">
                                    If you are not expecting this request, you can safely ignore this email or contact support if you have any concerns.
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
        type = tenantMemberJoinReviewTemplateType
    )
}