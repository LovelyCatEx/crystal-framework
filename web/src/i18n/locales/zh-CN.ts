import type {I18nRules} from "@/i18n/i18n-rules.ts";

export const zhCN: I18nRules = {
  pages: {
    systemInitializePage: {
      title: '系统初始化',
      loading: '加载中...',
      steps: {
        adminAccount: '设置管理员',
        adminAccountDesc: '创建管理员账号',
        mailServer: '配置邮件',
        mailServerDesc: '设置邮件服务器',
        complete: '完成初始化',
        completeDesc: '开始使用系统'
      },
      adminAccount: {
        title: '设置管理员账号',
        subtitle: '请设置系统管理员的账号和密码',
        form: {
          initializationToken: {
            placeholder: '一次性初始化令牌',
            required: '请输入一次性初始化令牌',
            help: '请从后端启动日志中复制令牌'
          },
          username: {
            placeholder: '管理员用户名',
            required: '请输入管理员用户名',
            pattern: '用户名只能包含字母、数字和下划线',
            max: '用户名最多64个字符'
          },
          email: {
            placeholder: '管理员邮箱',
            required: '请输入管理员邮箱',
            type: '请输入有效的邮箱地址',
            max: '邮箱最多256个字符'
          },
          password: {
            placeholder: '密码',
            required: '请输入密码',
            pattern: '密码至少8位，包含字母和数字',
            max: '密码最多128个字符'
          },
          confirmPassword: {
            placeholder: '确认密码',
            required: '请确认密码',
            mismatch: '两次输入的密码不一致'
          }
        },
        messages: {
          success: '管理员账号设置成功',
          failed: '设置失败，请重试'
        }
      },
      mailServer: {
        title: '配置邮件服务器',
        subtitle: '设置系统邮件发送服务（可选）',
        form: {
          host: {
            label: '服务器地址',
            placeholder: 'smtp.example.com',
            required: '请输入邮件服务器地址'
          },
          port: {
            label: '端口',
            placeholder: '587'
          },
          username: {
            label: '用户名',
            placeholder: 'your-email@example.com',
            required: '请输入邮件用户名'
          },
          password: {
            label: '密码/授权码',
            placeholder: '邮件密码或授权码',
            required: '请输入邮件密码或授权码'
          },
          fromEmail: {
            label: '发件人邮箱',
            placeholder: 'noreply@example.com',
            required: '请输入发件人邮箱',
            type: '请输入有效的邮箱地址'
          },
          fromName: {
            label: '发件人名称',
            required: '请输入发件人名称'
          }
        },
        messages: {
          success: '邮件服务器配置成功',
          failed: '配置失败，请重试'
        }
      },
      complete: {
        title: '初始化完成',
        subtitle: '系统已成功初始化，您可以开始使用了',
        nextSteps: '后续步骤',
        nextStepsList: {
          login: '使用管理员账号登录系统',
          settings: '配置系统参数和邮件模板',
          tenant: '创建租户并邀请成员'
        },
        button: '进入登录页',
        message: '系统初始化完成',
        messages: {
          failed: '系统初始化失败，请重试'
        }
      }
    },
    auth: {
      // AuthorizationPage
      authorization: {
        allRightsReserved: '© 2026 {{projectName}}. All rights reserved.'
      },

      // AuthCardLayout
      authCardLayout: {
        footer: {
          linkText: '返回登录'
        }
      },

      // LoginPage
      login: {
        title: '欢迎回来',
        subtitle: '请输入您的凭据以访问控制台',
        footerText: '还没有账号?',
        footerLink: '现在注册',
        form: {
          username: {
            placeholder: '用户名或邮箱',
            required: '请输入用户名或邮箱',
            pattern: '只能包含英文字母、数字、下划线和横线或邮箱地址'
          },
          password: {
            placeholder: '密码',
            required: '请输入密码'
          },
          remember: '记住我',
          forgotPassword: '忘记密码?',
          agreement: {
            text: '我已阅读并同意',
            privacyPolicy: '隐私政策',
            and: '和',
            termsOfService: '服务条款',
            required: '请阅读并同意服务条款和隐私政策'
          },
          submit: '立即登录'
        },
        messages: {
          success: '登录成功',
          failed: '登录失败',
          unknownError: '登录失败 未知错误'
        },
        divider: '或者通过以下方式',
        joinedTenant: {
          title: '选择组织',
          noTenant: '你不属于任何组织',
          skip: '跳过',
          confirm: '确认登录',
          loginAsNonTenant: '以非组织身份登录'
        }
      },

      // RegisterPage
      register: {
        title: '创建账号',
        subtitle: '开启您的全新旅程',
        footerText: '已经有账号了?',
        footerLink: '返回登录',
        form: {
          username: {
            placeholder: '用户名',
            required: '请输入用户名',
            pattern: '用户名只能包含数字、字母和下划线',
            max: '用户名长度不能超过64个字符'
          },
          email: {
            placeholder: '电子邮箱',
            required: '请输入邮箱',
            type: '邮箱格式不正确',
            max: '邮箱长度不能超过256个字符'
          },
          emailCode: {
            placeholder: '验证码',
            required: '请输入验证码',
            send: '发送验证码',
            retry: '{{count}}s后重试'
          },
          password: {
            placeholder: '密码',
            required: '请输入密码',
            pattern: '密码至少8位，且包含数字和字母',
            max: '密码长度不能超过128个字符'
          },
          confirmPassword: {
            placeholder: '确认密码',
            required: '请确认密码',
            mismatch: '两次输入的密码不一致'
          },
          agreement: {
            text: '我已阅读并同意',
            privacyPolicy: '隐私政策',
            and: '和',
            termsOfService: '服务条款',
            required: '请阅读并同意服务条款和隐私政策'
          },
          submit: '注册账号'
        },
        messages: {
          emailRequired: '请先输入邮箱',
          codeSendSuccess: '验证码发送成功，请注意查收',
          codeSendFailed: '验证码发送失败，请稍后重试',
          registerSuccess: '注册成功！',
          registerFailed: '注册失败，请稍后重试'
        }
      },

      // ForgotPasswordPage
      forgotPassword: {
        title: '忘记密码',
        subtitle: '通过邮箱重置您的密码',
        footerText: '想起密码了?',
        footerLink: '返回登录',
        form: {
          email: {
            placeholder: '电子邮箱',
            required: '请输入邮箱',
            type: '邮箱格式不正确',
            max: '邮箱长度不能超过256个字符'
          },
          emailCode: {
            placeholder: '验证码',
            required: '请输入验证码',
            send: '发送验证码',
            retry: '{{count}}s后重试'
          },
          newPassword: {
            placeholder: '新密码',
            required: '请输入新密码',
            pattern: '密码至少8位，且包含数字和字母',
            max: '密码长度不能超过128个字符'
          },
          confirmPassword: {
            placeholder: '确认新密码',
            required: '请确认新密码',
            mismatch: '两次输入的密码不一致'
          },
          submit: '重置密码'
        },
        messages: {
          emailRequired: '请先输入邮箱',
          codeSendSuccess: '验证码发送成功，请注意查收',
          codeSendFailed: '验证码发送失败，请稍后重试',
          resetSuccess: '密码重置成功！',
          resetFailed: '密码重置失败，请稍后重试'
        }
      },

      // OAuth2CodePage
      oauth2: {
        title: '第三方登录验证',
        subtitle: '请点击下方按钮完成登录验证',
        button: {
          processing: '验证中...',
          confirm: '确认登录'
        },
        messages: {
          invalidLoginInfo: '无效的登录信息',
          success: '登录成功',
          failed: '登录失败',
          unknownError: '登录失败，未知错误'
        },
        bind: {
          title: '绑定账号',
          subtitle: '该第三方账号尚未绑定，请选择操作',
          tabs: {
            current: '绑定当前账号',
            register: '注册新账号',
            bind: '绑定已有账号'
          },
          currentUser: {
            label: '当前登录用户',
            button: '绑定到当前账号'
          },
          register: {
            username: {
              placeholder: '用户名',
              required: '请输入用户名',
              pattern: '只能包含英文字母、数字、下划线和横线'
            },
            password: {
              placeholder: '密码',
              required: '请输入密码'
            },
            confirmPassword: {
              placeholder: '确认密码',
              required: '请确认密码',
              mismatch: '两次输入的密码不一致'
            },
            nickname: {
              placeholder: '昵称',
              required: '请输入昵称'
            },
            submit: '注册并绑定'
          },
          bindExisting: {
            username: {
              placeholder: '用户名',
              required: '请输入用户名或邮箱',
              pattern: '只能包含英文字母、数字、下划线和横线或邮箱地址'
            },
            password: {
              placeholder: '密码',
              required: '请输入密码'
            },
            submit: '绑定账号'
          },
          messages: {
            bindSuccess: '绑定成功',
            bindFailed: '绑定失败',
            bindRetry: '绑定失败，请重试',
            registerBindSuccess: '注册并绑定成功',
            registerFailed: '注册失败'
          }
        }
      },

      // OAuth2BindPage
      oauth2Bind: {
        title: '绑定账号',
        subtitle: '正在绑定您的第三方账号',
        processing: '绑定中...',
        success: '账号绑定成功',
        failed: '绑定失败',
        successTitle: '绑定成功',
        failedTitle: '绑定失败',
        invalidParams: '绑定参数无效',
        return: '返回'
      },
    },

    userProfile: {
      title: '个人中心',
      subtitle: '查看/编辑你的个人资料',
      tabs: {
        basicInfo: '基本信息',
        security: '账号安全',
        oauth: '第三方账号'
      },
      basicInfo: {
        username: '用户名',
        nickname: '昵称',
        email: '电子邮箱',
        emailHint: '若要修改电子邮箱，请前往账号安全设置页',
        save: '保存',
        updateSuccess: '用户资料更新成功',
        updateFailed: '用户资料更新失败'
      },
      security: {
        accountPassword: {
          title: '账户密码',
          desc: '建议设置高强度复杂密码以保障账号安全',
          status: '安全性：高',
          action: '修改'
        },
        email: {
          title: '电子邮箱',
          statusBound: '已绑定',
          statusUnbound: '未绑定',
          action: '修改'
        },
        passwordModal: {
          title: '修改密码',
          email: '电子邮箱',
          verificationCode: '验证码',
          sendCode: '发送验证码',
          resendCode: '{{seconds}}s后重试',
          newPassword: '新密码',
          confirmPassword: '确认密码',
          passwordHint: '密码至少8位，且包含数字和字母',
          confirm: '确认修改',
          updateSuccess: '密码修改成功！',
          emailRequired: '请先输入邮箱',
          codeSendSuccess: '验证码发送成功，请注意查收',
          codeSendFailed: '验证码发送失败'
        },
        emailModal: {
          title: '修改邮箱',
          currentEmail: '当前邮箱',
          newEmail: '新邮箱',
          verificationCode: '验证码',
          sendCode: '发送验证码',
          resendCode: '{{seconds}}s后重试',
          confirm: '确认修改',
          updateSuccess: '邮箱修改成功！',
          newEmailRequired: '请输入新邮箱',
          codeSendSuccess: '验证码发送成功，请注意查收',
          codeSendFailed: '验证码发送失败'
        }
      },
      oauth: {
        unbindTitle: '解绑第三方账号',
        unbindConfirm: '是否要解绑第三方账号 {{nickname}}',
        unbindSuccess: '账号解绑成功',
        unbindFailed: '账号解绑失败',
        unbind: '解绑',
        bind: '绑定',
        availablePlatforms: '可绑定的平台',
        bindSuccess: '已绑定',
        bindFailed: '绑定失败',
        alreadyBoundToUser: '该第三方账号已被绑定至某账户'
      },
      card: {
        unbound: '未绑定',
        registeredAt: '注册时间'
      },
      avatar: {
        cropTitle: '裁剪头像',
        confirmUpload: '确认上传',
        cancel: '取消',
        uploadSuccess: '头像上传成功',
        uploadFailed: '头像上传失败',
        invalidType: '请上传 JPG、PNG 或 WebP 格式的图片',
        maxSize: '图片大小不能超过 5MB'
      }
    },

    myTenantDashboard: {
      title: '我的组织',
      subtitle: '查看您当前的组织信息',
      noTenants: '您还没有加入任何组织',
      tenantId: '租户ID',
      basicInfo: '基本信息',
      basicInfoDesc: '查看组织的详细资料信息',
      contact: {
        name: '联系人',
        email: '联系邮箱',
        phone: '联系电话',
        address: '联系地址',
        notSet: '未设置',
        title: '联系信息'
      },
      subscription: {
        subscribedTime: '订阅时间',
        expiresTime: '过期时间',
        daysLeft: '剩余 {{days}} 天',
        expired: '已过期',
        nearExpire: '即将过期'
      },
      member: {
        title: '成员信息',
        status: '成员状态',
        joinedAt: '加入时间'
      },
      owner: {
        title: '所有者信息',
        username: '用户名',
        email: '邮箱',
        nickname: '昵称'
      },
      stats: {
        totalMembers: '总成员数',
        totalRoles: '角色数',
        totalDepartments: '部门数',
        totalInvitations: '邀请码数'
      },
      timeInfo: '时间信息',
      time: {
        createdTime: '创建时间',
        updatedTime: '更新时间'
      },
      joinedTenants: {
        title: '已加入的组织',
        subtitle: '查看您已加入的所有组织',
        current: '当前',
        unknown: '未知'
      }
    },

    myTenantSettings: {
      title: '组织设置',
      subtitle: '查看和管理租户组织资料',
      loading: '加载中...',
      loadFailed: '加载租户资料失败',
      updateSuccess: '租户资料更新成功',
      updateFailed: '租户资料更新失败',
      basicInfo: '基本信息',
      basicInfoDesc: '编辑您的租户组织资料信息',
      form: {
        name: '租户名称',
        contactName: '联系人姓名',
        contactEmail: '联系人邮箱',
        contactPhone: '联系人电话',
        address: '联系地址',
        description: '描述'
      },
      placeholders: {
        name: '请输入租户名称',
        contactName: '请输入联系人姓名',
        contactEmail: '请输入联系人邮箱',
        contactPhone: '请输入联系人电话',
        address: '请输入联系地址',
        description: '请输入租户描述'
      },
      validation: {
        nameRequired: '请输入租户名称',
        nameMax: '租户名称长度不能超过64个字符',
        contactNameRequired: '请输入联系人姓名',
        contactNameMax: '联系人姓名长度不能超过64个字符',
        emailRequired: '请输入联系人邮箱',
        emailInvalid: '邮箱格式不正确',
        emailMax: '邮箱长度不能超过256个字符',
        phoneRequired: '请输入联系人电话',
        phoneMax: '电话长度不能超过32个字符',
        addressRequired: '请输入联系地址',
        addressMax: '地址长度不能超过256个字符',
        descriptionMax: '描述长度不能超过512个字符'
      },
      buttons: {
        save: '保存修改',
        reset: '重置'
      },
      avatar: {
        cropTitle: '裁剪头像',
        confirmUpload: '确认上传',
        cancel: '取消',
        uploadSuccess: '头像上传成功',
        uploadFailed: '头像上传失败',
        invalidType: '请上传 JPG、PNG 或 WebP 格式的图片',
        maxSize: '图片大小不能超过 5MB'
      },
      segments: {
        profile: '基本信息',
        settings: '设置'
      }
    },

    // Manager Pages
    userManager: {
      title: '用户管理',
      subtitle: '管理系统用户列表',
      modal: {
        username: {
          label: '用户名',
          required: '请输入用户名',
          maxLength: '用户名长度不能超过64个字符'
        },
        nickname: {
          label: '昵称',
          required: '请输入昵称',
          maxLength: '昵称长度不能超过32个字符'
        },
        email: {
          label: '邮箱',
          maxLength: '邮箱长度不能超过256个字符'
        },
        password: {
          label: '密码',
          required: '请输入密码'
        },
        enabled: {
          label: '账号状态',
          hint: '禁用后用户将被强制下线且无法登录。',
          on: '已启用',
          off: '已禁用'
        }
      },
      filter: {
        username: '用户名',
        usernamePlaceholder: '按用户名筛选',
        email: '邮箱',
        emailPlaceholder: '按邮箱筛选',
        nickname: '昵称',
        nicknamePlaceholder: '按昵称筛选',
        id: '用户ID',
        idPlaceholder: '输入用户 ID',
      },
      action: {
        refreshAuthority: '刷新权限',
        refreshAuthorityConfirm: '确定刷新该用户的权限缓存吗？\n下一次请求会重新拉取权限。',
        refreshAuthorityBatchConfirm: '确定刷新已选用户的权限缓存吗？\n他们下一次请求会重新拉取权限。',
        forceLogout: '强制下线',
        forceLogoutConfirm: '确定强制该用户下线吗？\n其已签发的令牌将立即失效，需重新登录。',
        forceLogoutBatchConfirm: '确定强制已选用户下线吗？\n他们已签发的令牌将立即失效，需重新登录。',
        ban: '封禁',
        unban: '解封',
        unbanConfirm: '确定解除该用户的封禁吗？\n解除后其可立即重新登录。',
      },
      ban: {
        title: '封禁用户',
        reasonLabel: '封禁原因',
        reasonRequired: '请输入封禁原因',
        reasonPlaceholder: '被封禁时向用户展示的原因',
        banUntilLabel: '封禁至',
        banUntilPlaceholder: '选择到期时间',
        banUntilHint: '留空表示永久封禁',
      },
      messages: {
        refreshAuthoritySuccess: '用户权限缓存已刷新',
        refreshAuthorityFailed: '刷新用户权限缓存失败',
        forceLogoutSuccess: '已强制用户下线',
        forceLogoutFailed: '强制用户下线失败',
        banSuccess: '已封禁用户',
        banFailed: '封禁用户失败',
        unbanSuccess: '已解封用户',
        unbanFailed: '解封用户失败',
      }
    },
    userBanRecordManager: {
      title: '用户封禁记录',
      subtitle: '查看历史与生效中的用户登录封禁',
      filter: {
        id: '记录ID',
        idPlaceholder: '输入记录 ID',
        userId: '用户ID',
        userIdPlaceholder: '输入用户 ID',
        operatorUserId: '操作人ID',
        operatorUserIdPlaceholder: '输入操作人用户 ID',
      }
    },
    oauthAccountManager: {
      title: 'OAuth 账号管理',
      subtitle: '管理系统 OAuth 账号绑定',
      modal: {
        userId: {
          label: '系统用户'
        },
        platform: {
          label: '平台',
          required: '请选择平台',
          placeholder: '选择平台'
        },
        identifier: {
          label: '平台标识',
          required: '请输入平台标识',
          maxLength: '平台标识长度不能超过256个字符',
          placeholder: '请输入平台标识'
        },
        nickname: {
          label: '昵称',
          placeholder: '用户昵称',
          maxLength: '昵称长度不能超过128个字符'
        },
        avatar: {
          label: '头像URL',
          placeholder: '头像链接',
          maxLength: '头像URL长度不能超过256个字符'
        }
      },
      filter: {
        platform: '平台',
        all: '全部',
        id: '账号ID',
        idPlaceholder: '输入账号 ID'
      }
    },
    tenantManager: {
      title: '租户管理',
      subtitle: '管理系统租户信息',
      filter: {
        status: '状态',
        all: '全部',
        id: '租户ID',
        idPlaceholder: '输入租户 ID'
      },
      modal: {
        name: {
          label: '租户名称',
          required: '请输入租户名称',
          maxLength: '租户名称长度不能超过64个字符',
          placeholder: '租户名称'
        },
        ownerUserId: {
          label: '所有者用户',
          required: '请选择所有者用户'
        },
        tireTypeId: {
          label: '套餐类型',
          required: '请选择套餐类型',
          placeholder: '选择套餐类型'
        },
        status: {
          label: '状态',
          placeholder: '选择状态'
        },
        subscribedTime: {
          label: '订阅时间',
          required: '请选择订阅时间',
          placeholder: '选择订阅时间'
        },
        expiresTime: {
          label: '过期时间',
          required: '请选择过期时间',
          placeholder: '选择过期时间'
        },
        contactName: {
          label: '联系人姓名',
          required: '请输入联系人姓名',
          maxLength: '联系人姓名长度不能超过64个字符',
          placeholder: '联系人姓名'
        },
        contactEmail: {
          label: '联系人邮箱',
          required: '请输入联系人邮箱',
          maxLength: '邮箱长度不能超过256个字符',
          placeholder: '联系人邮箱'
        },
        contactPhone: {
          label: '联系人电话',
          required: '请输入联系人电话',
          maxLength: '电话长度不能超过32个字符',
          placeholder: '联系人电话'
        },
        address: {
          label: '联系地址',
          required: '请输入联系地址',
          maxLength: '联系地址不能超过256个字符',
          placeholder: '联系地址'
        },
        config: {
          label: '配置 (JSON)',
          placeholder: '输入JSON格式的配置'
        },
        description: {
          label: '描述',
          maxLength: '描述长度不能超过512个字符',
          placeholder: '租户描述'
        }
      }
    },
    tenantTireTypeManager: {
      title: '套餐类型管理',
      subtitle: '管理系统套餐类型',
      filter: {
        id: '套餐ID',
        idPlaceholder: '输入套餐 ID',
        name: '名称',
        description: '描述'
      },
      modal: {
        name: {
          label: '名称',
          required: '请输入名称',
          maxLength: '名称长度不能超过32个字符',
          placeholder: '套餐类型名称'
        },
        description: {
          label: '描述',
          maxLength: '描述长度不能超过512个字符',
          placeholder: '套餐类型描述'
        }
      }
    },
    tenantTireBenefitFeatureManager: {
      title: '套餐权益配置',
      subtitle: '管理系统套餐权益项',
      filter: {
        featureKey: '权益标识',
        name: '权益名称',
        description: '描述',
        featureType: '权益类型',
        featureTypePlaceholder: '选择权益类型',
        useI18n: '显示来源',
        useI18nDb: '数据库',
        useI18nLocale: '翻译'
      },
      modal: {
        featureKey: {
          label: '权益标识',
          required: '请输入权益标识',
          maxLength: '权益标识长度不能超过64个字符',
          placeholder: '例如 invitation.max_count'
        },
        name: {
          label: '名称',
          required: '请输入名称',
          maxLength: '名称长度不能超过128个字符',
          placeholder: '权益名称'
        },
        featureType: {
          label: '权益类型',
          required: '请选择权益类型',
          placeholder: '选择权益类型'
        },
        defaultValue: {
          label: '默认值',
          placeholder: '权益默认值',
          placeholderBoolean: '选择默认值',
          placeholderLimit: '输入默认数值',
          placeholderEnum: '输入选项后回车',
          requiredEnum: '请至少输入一个选项'
        },
        description: {
          label: '描述',
          maxLength: '描述长度不能超过512个字符',
          placeholder: '权益描述'
        }
      }
    },
    tenantTireBenefitValueManager: {
      title: '套餐权益取值',
      subtitle: '为各套餐配置权益的具体取值',
      switch: {
        planBenefits: '套餐权益',
        crossOverview: '权益概览',
        management: '详细管理',
      },
      overview: {
        title: '套餐权益概览',
        subtitle: '查看所有权益项在各套餐下的当前取值',
        selectTireType: '选择套餐类型',
        selectTireTypePlaceholder: '请选择套餐类型',
        name: '权益名称',
        featureKey: '权益标识',
        featureType: '权益类型',
        defaultValue: '默认值',
        currentValue: '当前值',
        default: '默认',
        save: '保存',
        cancel: '取消',
      },
      crossOverview: {
        title: '权益概览',
        subtitle: '查看所有套餐在各权益下的当前取值对比',
        name: '权益名称',
        description: '说明',
        featureKey: '标识',
        featureType: '类型',
        readOnly: '只读',
        edit: '编辑',
        showDefaultValue: '显示默认值',
      },
      filter: {
        tireTypeId: '套餐类型',
        tireTypeIdPlaceholder: '选择套餐类型',
        featureId: '权益项',
        featureIdPlaceholder: '选择权益项'
      },
      modal: {
        tireTypeId: {
          label: '套餐类型',
          required: '请选择套餐类型',
          placeholder: '选择套餐类型'
        },
        featureId: {
          label: '权益项',
          required: '请选择权益项',
          placeholder: '选择权益项'
        },
        featureValue: {
          label: '权益值',
          required: '请输入权益值',
          placeholder: '权益的取值',
          placeholderEnum: '选择枚举值',
          booleanTrue: '开启',
          booleanFalse: '关闭'
        }
      },
      keys: {
        'invitation.enabled': {
          name: '创建邀请码',
          description: '租户是否可以创建邀请码',
        },
        'invitation.max_count': {
          name: '邀请码创建上限',
          description: '租户可创建的邀请码总数上限',
        },
        'invitation.per_day_count': {
          name: '每日邀请码创建上限',
          description: '租户每日可创建的邀请码数量',
        },
        'invitation.per_code_usage_limit': {
          name: '单码使用上限',
          description: '单个邀请码最多可被使用的次数',
        },
        'invitation.max_validity_days': {
          name: '邀请码最长有效期',
          description: '邀请码最长有效期（天）',
        },
        'member.max_count': {
          name: '成员数量上限',
          description: '租户可拥有的成员数量上限',
        },
        'department.max_count': {
          name: '部门数量上限',
          description: '租户可创建的部门数量上限',
        },
      },
      groups: {
        'invitation': '邀请码',
        'member': '成员',
        'department': '部门',
      },
    },
    tenantRoleManager: {
      title: '租户角色管理',
      subtitle: '管理系统租户角色',
      filter: {
        id: '角色ID',
        idPlaceholder: '输入角色 ID'
      },
      action: {
        addNew: '新增租户角色',
        assignPermission: '分配权限',
      },
      modal: {
        name: {
          label: '角色名称',
          required: '请输入角色名称',
          placeholder: '输入角色名称'
        },
        parentId: {
          label: '父角色'
        },
        description: {
          label: '描述',
          placeholder: '输入描述（可选）'
        }
      },
      permissionModal: {
        title: '为角色 "{{name}}" 分配权限',
        titles: {
          available: '可用权限',
          assigned: '已分配权限'
        }
      },
      messages: {
        fetchPermissionsFailed: '无法获取权限列表',
        fetchRolePermissionsFailed: '无法获取角色权限',
        assignSuccess: '权限分配成功',
        assignFailed: '权限分配失败'
      }
    },
    tenantPermissionManager: {
      title: '租户权限管理',
      subtitle: '管理系统租户权限',
      switch: {
        overview: '树形概览',
        management: '权限管理'
      },
      action: {
        addNew: '新增权限'
      },
      filter: {
        type: '权限类型',
        all: '全部',
        id: '权限ID',
        idPlaceholder: '输入权限 ID'
      },
      modal: {
        name: {
          label: '权限名称',
          required: '请输入权限名称',
          placeholder: '输入权限名称'
        },
        type: {
          label: '权限类型',
          required: '请选择权限类型',
          placeholder: '选择权限类型'
        },
        path: {
          label: '路径',
          placeholder: '输入路径（可选）'
        },
        description: {
          label: '描述',
          placeholder: '输入描述（可选）'
        }
      }
    },
    permissionCatalog: {
      source: {
        db: '数据库描述',
        i18n: '翻译描述',
        label: '描述来源',
      },
      byName: {
        // System permissions - Menus (system layer)
        'system.permission': '用户权限菜单',
        'system.role': '用户角色菜单',
        'system.user': '用户菜单',
        'system.user.role': '用户-角色关系菜单',
        'system.settings': '系统设置菜单',
        'system.oauth.account': 'OAuth 账号菜单',
        'system.file.resource': '文件资源菜单',
        'system.storage.provider': '存储供应商菜单',
        'system.mail.template.category': '邮件模板分类菜单',
        'system.mail.template.type': '邮件模板类型菜单',
        'system.mail.template': '邮件模板菜单',
        'system.tenant': '租户菜单',
        'system.tenant.tire.type': '租户档位类型菜单',
        'system.tenant.tire.benefit.feature': '租户档位权益功能菜单',
        'system.tenant.tire.benefit.value': '租户档位权益值菜单',
        'system.message.channel': '系统消息通道菜单',
        'system.audit.log': '审计日志菜单',
        'system.mail.send.log': '邮件发送日志菜单',
        'system.user.login.log': '用户登录日志菜单',
        'system.monitor.sessions': '会话监控菜单',
        'system.announcement': '公告菜单',
        'system.broadcast': '站内信公告菜单',
        'system.approval.flow.definition': '审批流程定义菜单',
        'system.dict.type': '系统字典类型菜单',
        'system.dict.item': '系统字典项菜单',
        // System permissions - Menus (tenantAdmin layer, cross-tenant)
        'tenant.department': '租户部门菜单',
        'tenant.role': '租户角色菜单',
        'tenant.permission': '租户权限菜单',
        'tenant.member': '租户成员菜单',
        'tenant.department.member': '租户部门成员菜单',
        'tenant.member.role': '租户成员角色菜单',
        'tenant.role.permission': '租户角色权限菜单',
        'tenant.invitation': '租户邀请菜单',
        'tenant.message.channel': '租户消息通道菜单',
        'tenant.approval.flow.definition': '租户审批流程定义菜单',
        'tenant.approval.flow.instance': '租户审批流程实例菜单',
        'tenant.dict.type': '租户字典类型菜单',
        'tenant.dict.item': '租户字典项菜单',
        // System permissions - Menus (super/cross-scope layer)
        'x.approval.flow.instance': '审批流程实例菜单',
        // System permissions - Components (system layer)
        'system.dashboard.business.statistics': '仪表盘业务统计组件',
        'system.dashboard.system.metrics': '仪表盘系统指标组件',
        'system.dashboard.tenant.joined': '仪表盘已加入租户组件',
        'system.dashboard.announcements': '仪表盘公告组件',
        // System permissions - Actions (system layer: user permission / role / user)
        'system.permission.create': '创建用户权限',
        'system.permission.read': '读取用户权限',
        'system.permission.update': '更新用户权限',
        'system.permission.delete': '删除用户权限',
        'system.role.create': '创建用户角色',
        'system.role.read': '读取用户角色',
        'system.role.update': '更新用户角色',
        'system.role.delete': '删除用户角色',
        'system.user.create': '创建用户',
        'system.user.read': '读取用户',
        'system.user.update': '更新用户',
        'system.user.delete': '删除用户',
        'system.user.refreshAuthority': '刷新用户的权限缓存',
        'system.user.forceLogout': '强制用户下线',
        'system.user.ban': '封禁用户登录',
        'system.user.unban': '解除用户登录封禁',
        'system.user.setEnabled': '启用或禁用用户账号',
        'system.user.banRecord': '用户封禁记录菜单',
        'system.user.banRecord.read': '读取用户封禁记录',
        'system.role.permission.read': '读取角色的权限分配',
        'system.role.permission.update': '更新角色的权限分配',
        'system.user.role.read': '读取用户的角色分配',
        'system.user.role.update': '更新用户的角色分配',
        // System settings / maintenance
        'system.settings.read': '读取系统设置',
        'system.settings.update': '更新系统设置',
        'system.settings.test.sendEmail': '通过系统设置发送测试邮件',
        'system.settings.test.sendMessage': '通过系统设置发送测试消息',
        'system.maintenance.access': '访问维护操作',
        'system.maintenance.update': '更新维护操作',
        // OAuth account
        'system.oauth.account.create': '创建 OAuth 账号',
        'system.oauth.account.read': '读取 OAuth 账号',
        'system.oauth.account.update': '更新 OAuth 账号',
        'system.oauth.account.delete': '删除 OAuth 账号',
        // File resource / storage
        'system.file.resource.create': '创建文件资源',
        'system.file.resource.read': '读取文件资源',
        'system.file.resource.update': '更新文件资源',
        'system.file.resource.delete': '删除文件资源',
        'system.storage.provider.create': '创建存储供应商',
        'system.storage.provider.read': '读取存储供应商',
        'system.storage.provider.update': '更新存储供应商',
        'system.storage.provider.delete': '删除存储供应商',
        // Mail template
        'system.mail.template.category.create': '创建邮件模板分类',
        'system.mail.template.category.read': '读取邮件模板分类',
        'system.mail.template.category.update': '更新邮件模板分类',
        'system.mail.template.category.delete': '删除邮件模板分类',
        'system.mail.template.type.create': '创建邮件模板类型',
        'system.mail.template.type.read': '读取邮件模板类型',
        'system.mail.template.type.update': '更新邮件模板类型',
        'system.mail.template.type.delete': '删除邮件模板类型',
        'system.mail.template.create': '创建邮件模板',
        'system.mail.template.read': '读取邮件模板',
        'system.mail.template.update': '更新邮件模板',
        'system.mail.template.delete': '删除邮件模板',
        // Tenant top-level (system layer, manages tenants themselves)
        'system.tenant.create': '创建租户',
        'system.tenant.read': '读取租户',
        'system.tenant.update': '更新租户',
        'system.tenant.delete': '删除租户',
        'system.tenant.lifecycle.update': '更新租户所有权、档位、订阅窗口、状态或设置',
        // Tenant tire type / benefit (system layer)
        'system.tenant.tire.type.create': '创建租户档位类型',
        'system.tenant.tire.type.read': '读取租户档位类型',
        'system.tenant.tire.type.update': '更新租户档位类型',
        'system.tenant.tire.type.delete': '删除租户档位类型',
        'system.tenant.tire.benefit.feature.create': '创建租户档位权益功能',
        'system.tenant.tire.benefit.feature.read': '读取租户档位权益功能',
        'system.tenant.tire.benefit.feature.update': '更新租户档位权益功能',
        'system.tenant.tire.benefit.feature.delete': '删除租户档位权益功能',
        'system.tenant.tire.benefit.value.create': '创建租户档位权益值',
        'system.tenant.tire.benefit.value.read': '读取租户档位权益值',
        'system.tenant.tire.benefit.value.update': '更新租户档位权益值',
        'system.tenant.tire.benefit.value.delete': '删除租户档位权益值',
        // Tenant admin scope (tenantAdmin layer, cross-tenant)
        'tenant.department.create': '跨租户创建租户部门',
        'tenant.department.read': '跨租户读取租户部门',
        'tenant.department.update': '跨租户更新租户部门',
        'tenant.department.delete': '跨租户删除租户部门',
        'tenant.role.create': '跨租户创建租户角色',
        'tenant.role.read': '跨租户读取租户角色',
        'tenant.role.update': '跨租户更新租户角色',
        'tenant.role.delete': '跨租户删除租户角色',
        'tenant.permission.create': '跨租户创建租户权限',
        'tenant.permission.read': '跨租户读取租户权限',
        'tenant.permission.update': '跨租户更新租户权限',
        'tenant.permission.delete': '跨租户删除租户权限',
        'tenant.member.create': '跨租户创建租户成员',
        'tenant.member.read': '跨租户读取租户成员',
        'tenant.member.update': '跨租户更新租户成员',
        'tenant.member.delete': '跨租户删除租户成员',
        'tenant.department.member.create': '跨租户为部门分配成员',
        'tenant.department.member.read': '跨租户读取部门成员',
        'tenant.department.member.update': '跨租户更新部门成员',
        'tenant.department.member.delete': '跨租户移除部门成员',
        'tenant.member.role.read': '跨租户读取成员角色分配',
        'tenant.member.role.update': '跨租户更新成员角色分配',
        'tenant.role.permission.read': '跨租户读取角色权限分配',
        'tenant.role.permission.update': '跨租户更新角色权限分配',
        'tenant.invitation.create': '跨租户创建租户邀请',
        'tenant.invitation.read': '跨租户读取租户邀请',
        'tenant.invitation.update': '跨租户更新租户邀请',
        'tenant.invitation.delete': '跨租户删除租户邀请',
        // Message channel (multi-layer)
        'tenant.message.channel.create': '跨租户创建租户级消息通道',
        'tenant.message.channel.read': '跨租户读取租户级消息通道',
        'tenant.message.channel.update': '跨租户更新租户级消息通道',
        'tenant.message.channel.delete': '跨租户删除租户级消息通道',
        'system.message.channel.create': '创建系统级消息通道',
        'system.message.channel.read': '读取系统级消息通道',
        'system.message.channel.update': '更新系统级消息通道',
        'system.message.channel.delete': '删除系统级消息通道',
        'x.message.channel.create': '在任意作用域创建消息通道',
        'x.message.channel.read': '在任意作用域读取消息通道',
        'x.message.channel.update': '在任意作用域更新消息通道',
        'x.message.channel.delete': '在任意作用域删除消息通道',
        // Logs (system layer)
        'system.audit.log.create': '创建审计日志',
        'system.audit.log.read': '读取审计日志',
        'system.audit.log.update': '更新审计日志',
        'system.audit.log.delete': '删除审计日志',
        'system.mail.send.log.read': '读取邮件发送日志',
        'system.user.login.log.read': '读取用户登录日志',
        // Dashboard / monitor (system layer)
        'system.dashboard.business.statistics.read': '读取仪表盘业务统计',
        'system.dashboard.system.metrics.read': '读取仪表盘系统指标',
        'system.monitor.sessions.read': '读取活跃会话监控数据',
        'system.monitor': '系统性能监控菜单',
        'system.monitor.read': '查看系统性能指标数据',
        // Announcement (system layer)
        'system.announcement.create': '创建公告',
        'system.announcement.read': '读取公告',
        'system.announcement.update': '更新公告',
        'system.announcement.delete': '删除公告',
        'system.announcement.list': '查看已发布公告',
        // Approval flow definition (multi-layer)
        'x.approval.flow.definition.create': '在任意作用域创建审批流程定义',
        'x.approval.flow.definition.read': '在任意作用域读取审批流程定义',
        'x.approval.flow.definition.update': '在任意作用域更新审批流程定义',
        'x.approval.flow.definition.delete': '在任意作用域删除审批流程定义',
        'system.approval.flow.definition.create': '创建系统级审批流程定义',
        'system.approval.flow.definition.read': '读取系统级审批流程定义',
        'system.approval.flow.definition.update': '更新系统级审批流程定义',
        'system.approval.flow.definition.delete': '删除系统级审批流程定义',
        'system.approval.flow.instance.read': '读取系统级审批流程实例',
        'tenant.approval.flow.definition.create': '跨租户创建租户级审批流程定义',
        'tenant.approval.flow.definition.read': '跨租户读取租户级审批流程定义',
        'tenant.approval.flow.definition.update': '跨租户更新租户级审批流程定义',
        'tenant.approval.flow.definition.delete': '跨租户删除租户级审批流程定义',
        // Approval flow instance (multi-layer)
        'x.approval.flow.instance.read': '在任意作用域读取审批流程实例',
        'tenant.approval.flow.instance.read': '跨租户读取租户级审批流程实例',
        // Dictionary type (multi-layer)
        'x.dict.type.create': '在任意作用域创建字典类型',
        'x.dict.type.read': '在任意作用域读取字典类型',
        'x.dict.type.update': '在任意作用域更新字典类型',
        'x.dict.type.delete': '在任意作用域删除字典类型',
        'system.dict.type.create': '创建系统级字典类型',
        'system.dict.type.read': '读取系统级字典类型',
        'system.dict.type.update': '更新系统级字典类型',
        'system.dict.type.delete': '删除系统级字典类型',
        'tenant.dict.type.create': '跨租户创建租户级字典类型',
        'tenant.dict.type.read': '跨租户读取租户级字典类型',
        'tenant.dict.type.update': '跨租户更新租户级字典类型',
        'tenant.dict.type.delete': '跨租户删除租户级字典类型',
        // Dictionary item (multi-layer)
        'x.dict.item.create': '在任意作用域创建字典项',
        'x.dict.item.read': '在任意作用域读取字典项',
        'x.dict.item.update': '在任意作用域更新字典项',
        'x.dict.item.delete': '在任意作用域删除字典项',
        'system.dict.item.create': '创建系统级字典项',
        'system.dict.item.read': '读取系统级字典项',
        'system.dict.item.update': '更新系统级字典项',
        'system.dict.item.delete': '删除系统级字典项',
        'tenant.dict.item.create': '跨租户创建租户级字典项',
        'tenant.dict.item.read': '跨租户读取租户级字典项',
        'tenant.dict.item.update': '跨租户更新租户级字典项',
        'tenant.dict.item.delete': '跨租户删除租户级字典项',
        'x.broadcast.create': '在任意作用域创建广播',
        'x.broadcast.read': '在任意作用域读取广播',
        'x.broadcast.update': '在任意作用域更新广播',
        'x.broadcast.delete': '在任意作用域删除广播',
        'system.broadcast.create': '创建系统级广播',
        'system.broadcast.read': '读取系统级广播',
        'system.broadcast.update': '更新系统级广播',
        'system.broadcast.delete': '删除系统级广播',
        'tenant.broadcast.create': '跨租户创建租户级广播',
        'tenant.broadcast.read': '跨租户读取租户级广播',
        'tenant.broadcast.update': '跨租户更新租户级广播',
        'tenant.broadcast.delete': '跨租户删除租户级广播',
        'tenant.file.resource.create': '跨租户创建租户级文件资源',
        'tenant.file.resource.read': '跨租户读取租户级文件资源',
        'tenant.file.resource.update': '跨租户更新租户级文件资源',
        'tenant.file.resource.delete': '跨租户删除租户级文件资源',
        // Tenant permissions - Menus (tenantPem layer, own tenant)
        'i.tenant.dashboard': '我的租户仪表盘菜单',
        'i.tenant.profile': '我的租户资料菜单',
        'i.tenant.personal.profile': '我的租户个人资料菜单',
        'i.tenant.member': '我的租户成员菜单',
        'i.tenant.invitation': '我的租户邀请菜单',
        'i.tenant.role': '我的租户角色菜单',
        'i.tenant.member.role': '我的租户成员角色菜单',
        'i.tenant.department': '我的租户部门菜单',
        'i.tenant.message.channel': '我的租户消息通道菜单',
        'i.tenant.dict.type': '我的租户字典类型菜单',
        'i.tenant.dict.item': '我的租户字典项菜单',
        'i.tenant.approval.flow.definition': '我的租户审批流程定义菜单',
        'i.tenant.approval.flow.instance': '我的租户审批流程实例菜单',
        // Tenant permissions - Actions (tenantPem layer)
        'i.tenant.profile.read.basic': '读取租户基础资料',
        'i.tenant.profile.read': '读取租户资料',
        'i.tenant.profile.update': '更新租户资料',
        'i.tenant.personal.profile.oauth.read': '读取自己的 OAuth 绑定',
        'i.tenant.personal.profile.oauth.bind': '绑定 OAuth 账号',
        'i.tenant.personal.profile.oauth.unbind': '解绑 OAuth 账号',
        'i.tenant.member.read': '读取本租户成员',
        'i.tenant.member.update': '更新本租户成员',
        'i.tenant.member.delete': '移除本租户成员',
        'i.tenant.invitation.create': '为本租户创建邀请',
        'i.tenant.invitation.read': '读取本租户邀请',
        'i.tenant.invitation.update': '更新本租户邀请',
        'i.tenant.invitation.delete': '删除本租户邀请',
        'i.tenant.role.create': '创建本租户角色',
        'i.tenant.role.read': '读取本租户角色',
        'i.tenant.role.update': '更新本租户角色',
        'i.tenant.role.delete': '删除本租户角色',
        'i.tenant.member.role.read': '读取本租户成员角色分配',
        'i.tenant.member.role.update': '更新本租户成员角色分配',
        'i.tenant.role.permission.read': '读取本租户角色权限分配',
        'i.tenant.role.permission.update': '更新本租户角色权限分配',
        'i.tenant.department.create': '创建本租户部门',
        'i.tenant.department.read': '读取本租户部门',
        'i.tenant.department.update': '更新本租户部门',
        'i.tenant.department.delete': '删除本租户部门',
        'i.tenant.department.member.create': '为本租户部门分配成员',
        'i.tenant.department.member.read': '读取本租户部门成员',
        'i.tenant.department.member.update': '更新本租户部门成员',
        'i.tenant.department.member.delete': '从本租户部门移除成员',
        'i.tenant.mail.member.join': '接收租户成员加入审核邮件',
        'i.tenant.settings.read': '读取本租户设置',
        'i.tenant.settings.update': '更新本租户设置',
        'i.tenant.message.channel.create': '创建本租户消息通道',
        'i.tenant.message.channel.read': '读取本租户消息通道',
        'i.tenant.message.channel.update': '更新本租户消息通道',
        'i.tenant.message.channel.delete': '删除本租户消息通道',
        'i.tenant.dict.type.create': '创建本租户字典类型',
        'i.tenant.dict.type.read': '读取本租户字典类型',
        'i.tenant.dict.type.update': '更新本租户字典类型',
        'i.tenant.dict.type.delete': '删除本租户字典类型',
        'i.tenant.dict.item.create': '创建本租户字典项',
        'i.tenant.dict.item.read': '读取本租户字典项',
        'i.tenant.dict.item.update': '更新本租户字典项',
        'i.tenant.dict.item.delete': '删除本租户字典项',
        'i.tenant.broadcast.create': '创建本租户广播',
        'i.tenant.broadcast.read': '读取本租户广播',
        'i.tenant.broadcast.update': '更新本租户广播',
        'i.tenant.broadcast.delete': '删除本租户广播',
        'i.tenant.file.resource.create': '创建本租户文件资源',
        'i.tenant.file.resource.read': '读取本租户文件资源',
        'i.tenant.file.resource.update': '更新本租户文件资源',
        'i.tenant.file.resource.delete': '删除本租户文件资源',
        'i.tenant.message.reception.handle': '查看并回复用户发给本租户的客服会话',
        'i.tenant.approval.flow.definition.create': '创建本租户审批流程定义',
        'i.tenant.approval.flow.definition.read': '读取本租户审批流程定义',
        'i.tenant.approval.flow.definition.update': '更新本租户审批流程定义',
        'i.tenant.approval.flow.definition.delete': '删除本租户审批流程定义',
        'i.tenant.approval.flow.instance.read': '读取本租户审批流程实例',
      },
    },
    tenantMemberManager: {
      title: '租户成员管理',
      subtitle: '管理系统租户成员',
      action: {
        addNew: '新增租户成员'
      },
      filter: {
        status: '状态',
        all: '全部',
        id: '成员 ID',
        idPlaceholder: '输入成员 ID',
        memberUserId: '用户 ID',
        memberUserIdPlaceholder: '输入用户 ID'
      },
      modal: {
        memberUserId: {
          label: '成员用户（仅创建时有效）',
          required: '请选择成员用户'
        },
        status: {
          label: '状态',
          required: '请选择状态',
          placeholder: '选择状态'
        }
      }
    },
    tenantInvitationManager: {
      title: '邀请码管理',
      subtitle: '管理系统租户邀请码',
      filter: {
        id: '邀请码ID',
        idPlaceholder: '输入邀请码 ID'
      },
      addInvitationCode: '新增邀请码',
      copyInvitationLink: '复制邀请码链接',
      copySuccess: '已将邀请链接复制到剪切板',
      copyFailed: '复制失败，请手动复制',
      action: {
        addNew: '新增邀请码'
      },
      modal: {
        creatorMemberId: {
          label: '创建者成员',
          required: '请选择创建者成员',
          placeholder: '选择创建者成员'
        },
        departmentId: {
          label: '部门（可选）',
          placeholder: '选择部门（可选）'
        },
        invitationCount: {
          label: '可邀请次数',
          required: '请输入可邀请次数',
          placeholder: '输入可邀请次数'
        },
        expiresTime: {
          label: '过期时间（可选）',
          placeholder: '选择过期时间（可选）'
        },
        requiresReviewing: {
          label: '需要审核'
        }
      },
      messages: {
        copySuccess: '已将邀请链接复制到剪切板',
        copyFailed: '复制失败，请手动复制'
      }
    },
    tenantMessageChannelManager: {
      title: '消息渠道管理',
      subtitle: '管理系统租户的消息渠道',
      addChannel: '新增渠道',
      filter: {
        id: '渠道ID',
        idPlaceholder: '输入渠道 ID',
        type: '渠道类型',
        all: '全部'
      },
      modal: {
        channelType: {
          label: '渠道类型',
          required: '请选择渠道类型',
          placeholder: '选择渠道类型',
          switchConfirmTitle: '切换渠道类型',
          switchConfirmContent: '切换类型会重置当前配置，确认继续？'
        },
        name: {
          label: '渠道名称',
          required: '请输入渠道名称',
          maxLength: '渠道名称不能超过 64 个字符',
          placeholder: '输入渠道名称'
        },
        enabled: {
          label: '是否启用'
        },
        config: {
          label: '渠道配置',
          required: '请填写渠道配置',
          placeholder: '填写渠道配置（JSON）',
          encryptedHint: '出于安全考虑，敏感字段（如密码、密钥）不会回显，编辑时请重新填写'
        }
      },
      messages: {
        statusUpdateSuccess: '状态更新成功',
        statusUpdateFailed: '状态更新失败'
      }
    },
    systemMessageChannelManager: {
      title: '系统消息渠道',
      subtitle: '管理系统级消息渠道',
      addChannel: '新增渠道',
      filter: {
        id: '渠道ID',
        idPlaceholder: '输入渠道 ID',
        type: '渠道类型',
        all: '全部'
      },
      modal: {
        channelType: {
          label: '渠道类型',
          required: '请选择渠道类型',
          placeholder: '选择渠道类型',
          switchConfirmTitle: '切换渠道类型',
          switchConfirmContent: '切换类型会重置当前配置，确认继续？'
        },
        name: {
          label: '渠道名称',
          required: '请输入渠道名称',
          maxLength: '渠道名称不能超过 64 个字符',
          placeholder: '输入渠道名称'
        },
        enabled: {
          label: '是否启用'
        },
        config: {
          label: '渠道配置',
          required: '请填写渠道配置',
          placeholder: '填写渠道配置（JSON）',
          encryptedHint: '出于安全考虑，敏感字段（如密码、密钥）不会回显，编辑时请重新填写'
        }
      },
      messages: {
        statusUpdateSuccess: '状态更新成功',
        statusUpdateFailed: '状态更新失败'
      }
    },
    tenantDepartmentManager: {
      title: '部门管理',
      subtitle: '管理系统租户部门',
      action: {
        addNew: '新增部门',
        addDepartment: '添加部门',
        edit: '编辑',
        delete: '删除',
        addMember: '添加成员',
        editRole: '编辑角色',
        remove: '移除'
      },
      card: {
        departmentList: '部门列表',
        members: '部门成员'
      },
      info: {
        description: '描述',
        parentDepartment: '父部门',
        createdTime: '创建时间'
      },
      empty: {
        noDepartments: '暂无部门',
        selectDepartment: '请从左侧选择一个部门'
      },
      modal: {
        add: {
          title: '新增部门'
        },
        edit: {
          title: '编辑部门'
        },
        delete: {
          title: '删除部门',
          content: '确定要删除部门 "{{name}}" 吗？'
        },
        removeMember: {
          title: '移除成员',
          content: '确定要将成员 "{{name}}" 从部门中移除吗？'
        },
        name: {
          label: '部门名称',
          required: '请输入部门名称',
          placeholder: '输入部门名称'
        },
        parentId: {
          label: '父部门'
        },
        description: {
          label: '描述',
          placeholder: '输入描述（可选）'
        }
      },
      memberSelectorModal: {
        title: '为部门 "{{name}}" 添加成员'
      },
      roleEditModal: {
        title: '编辑成员角色 - {{name}}',
        description: '请选择成员在部门中的角色：'
      },
      columns: {
        action: '操作'
      },
      messages: {
        fetchDepartmentsFailed: '无法获取部门列表',
        fetchMembersFailed: '无法获取部门成员',
        deleteSuccess: '删除成功',
        deleteFailed: '删除失败',
        updateSuccess: '更新成功',
        createSuccess: '创建成功',
        addMembersSuccess: '成功添加 {{count}} 名成员',
        addMembersFailed: '添加成员失败',
        removeMemberSuccess: '移除成员成功',
        removeMemberFailed: '移除成员失败',
        updateRoleSuccess: '更新角色成功',
        updateRoleFailed: '更新角色失败'
      }
    },
    tenantMemberRoleManager: {
      title: '租户成员角色管理',
      subtitle: '为租户成员分配角色',
      columns: {
        member: '成员',
        username: '用户名',
        email: '邮箱',
        status: '状态',
        action: '操作'
      },
      action: {
        assignRole: '分配角色'
      },
      modal: {
        title: '为成员 "{{nickname}}" 分配角色',
        titles: {
          unassigned: '未分配角色',
          assigned: '已分配角色'
        }
      },
      messages: {
        fetchMembersFailed: '无法获取成员列表',
        fetchRolesFailed: '无法获取角色列表',
        fetchMemberRolesFailed: '无法获取成员角色',
        assignSuccess: '角色分配成功',
        assignFailed: '角色分配失败'
      }
    },
    myTenantRoleManager: {
      title: '我的角色管理',
      subtitle: '管理当前组织的角色',
      filter: {
        id: '角色ID',
        idPlaceholder: '输入角色 ID'
      },
      action: {
        addNew: '新增角色',
        assignPermission: '分配权限',
      },
      modal: {
        name: {
          label: '角色名称',
          required: '请输入角色名称',
          placeholder: '输入角色名称'
        },
        parentId: {
          label: '父角色'
        },
        description: {
          label: '描述',
          placeholder: '输入描述（可选）'
        }
      },
      permissionModal: {
        title: '为角色 "{{name}}" 分配权限',
        titles: {
          available: '可用权限',
          assigned: '已分配权限'
        }
      },
      messages: {
        fetchPermissionsFailed: '无法获取权限列表',
        fetchRolePermissionsFailed: '无法获取角色权限',
        assignSuccess: '权限分配成功',
        assignFailed: '权限分配失败'
      }
    },
    myTenantMemberManager: {
      title: '我的组织成员',
      subtitle: '管理当前组织成员信息',
      filter: {
        status: '状态',
        all: '全部',
        id: '成员 ID',
        idPlaceholder: '输入成员 ID',
        memberUserId: '用户 ID',
        memberUserIdPlaceholder: '输入用户 ID'
      },
      modal: {
        status: {
          label: '状态',
          required: '请选择状态',
          placeholder: '选择状态'
        }
      }
    },
    myTenantMemberRoleManager: {
      title: '我的成员角色管理',
      subtitle: '为当前组织成员分配角色',
      columns: {
        member: '成员',
        username: '用户名',
        email: '邮箱',
        status: '状态',
        action: '操作'
      },
      action: {
        assignRole: '分配角色'
      },
      modal: {
        title: '为成员 "{{nickname}}" 分配角色',
        titles: {
          unassigned: '未分配角色',
          assigned: '已分配角色'
        }
      },
      messages: {
        fetchMembersFailed: '无法获取成员列表',
        fetchRolesFailed: '无法获取角色列表',
        fetchMemberRolesFailed: '无法获取成员角色',
        assignSuccess: '角色分配成功',
        assignFailed: '角色分配失败'
      }
    },
    myTenantInvitationManager: {
      title: '我的组织邀请码',
      subtitle: '管理当前组织的邀请码',
      filter: {
        id: '邀请码ID',
        idPlaceholder: '输入邀请码 ID'
      },
      action: {
        addNew: '新增邀请码',
        copyLink: '复制邀请码链接',
        copyLinkTooltip: '复制邀请码链接'
      },
      modal: {
        departmentId: {
          label: '部门（可选）',
          placeholder: '选择部门（可选）'
        },
        requiresReviewing: {
          label: '需要审核'
        },
        invitationCount: {
          label: '可邀请次数',
          required: '请输入可邀请次数',
          placeholder: '输入可邀请次数'
        },
        expiresTime: {
          label: '过期时间（可选）',
          placeholder: '选择过期时间（可选）'
        }
      },
      messages: {
        copySuccess: '已将邀请链接复制到剪切板',
        copyFailed: '复制失败，请手动复制'
      }
    },
    myTenantMessageChannelManager: {
      title: '我的消息渠道',
      subtitle: '管理当前组织的消息渠道',
      addChannel: '新增渠道',
      filter: {
        id: '渠道ID',
        idPlaceholder: '输入渠道 ID',
        type: '渠道类型',
        all: '全部'
      },
      modal: {
        channelType: {
          label: '渠道类型',
          required: '请选择渠道类型',
          placeholder: '选择渠道类型',
          switchConfirmTitle: '切换渠道类型',
          switchConfirmContent: '切换类型会重置当前配置，确认继续？'
        },
        name: {
          label: '渠道名称',
          required: '请输入渠道名称',
          maxLength: '渠道名称不能超过 64 个字符',
          placeholder: '输入渠道名称'
        },
        enabled: {
          label: '是否启用'
        },
        config: {
          label: '渠道配置',
          required: '请填写渠道配置',
          placeholder: '填写渠道配置（JSON）',
          encryptedHint: '出于安全考虑，敏感字段（如密码、密钥）不会回显，编辑时请重新填写'
        }
      },
      messages: {
        statusUpdateSuccess: '状态更新成功',
        statusUpdateFailed: '状态更新失败'
      }
    },
    myTenantDepartmentManager: {
      title: '我的部门管理',
      subtitle: '管理当前组织的部门及成员',
      columns: {
        action: '操作'
      },
      action: {
        addNew: '新增部门',
        addMember: '添加成员',
        edit: '编辑',
        delete: '删除',
        editRole: '编辑角色',
        remove: '移除'
      },
      card: {
        departmentList: '部门列表',
        noDepartment: '暂无部门',
        addDepartment: '添加部门',
        description: '描述',
        parentDepartment: '父部门',
        createdTime: '创建时间',
        departmentMembers: '部门成员',
        selectDepartment: '请从左侧选择一个部门'
      },
      modal: {
        addTitle: '新增部门',
        editTitle: '编辑部门',
        deleteTitle: '删除部门',
        deleteContent: '确定要删除部门 "{{name}}" 吗？',
        removeMemberTitle: '移除成员',
        removeMemberContent: '确定要将成员 "{{name}}" 从部门中移除吗？',
        addMemberTitle: '为部门 "{{name}}" 添加成员',
        editRoleTitle: '编辑成员角色 - {{name}}',
        roleDescription: '请选择成员在部门中的角色：',
        name: {
          label: '部门名称',
          required: '请输入部门名称',
          placeholder: '输入部门名称'
        },
        parentId: {
          label: '父部门'
        },
        description: {
          label: '描述',
          placeholder: '输入描述（可选）'
        }
      },
      messages: {
        fetchDepartmentsFailed: '无法获取部门列表',
        fetchMembersFailed: '无法获取部门成员',
        deleteSuccess: '删除成功',
        deleteFailed: '删除失败',
        updateSuccess: '更新成功',
        createSuccess: '创建成功',
        addMembersSuccess: '成功添加 {{count}} 名成员',
        addMembersFailed: '添加成员失败',
        removeMemberSuccess: '移除成员成功',
        removeMemberFailed: '移除成员失败',
        updateRoleSuccess: '更新角色成功',
        updateRoleFailed: '更新角色失败'
      }
    },
    storageProviderManager: {
      title: '存储提供商管理',
      subtitle: '管理系统存储提供商',
      filter: {
        type: '类型',
        all: '全部',
        id: '存储ID',
        idPlaceholder: '输入存储 ID'
      },
      columns: {
        active: '启用状态'
      },
      modal: {
        name: {
          label: '名称',
          required: '请输入名称',
          maxLength: '名称长度不能超过64个字符',
          placeholder: '存储提供商名称'
        },
        type: {
          label: '类型',
          required: '请选择类型',
          placeholder: '选择存储类型',
          localFileSystem: '本地文件系统',
          aliyunOss: '阿里云 OSS',
          tencentCos: '腾讯 OSS',
          volcEngineTos: '火山引擎 TOS'
        },
        description: {
          label: '描述',
          maxLength: '描述长度不能超过512个字符',
          placeholder: '存储提供商描述'
        },
        baseUrl: {
          label: '基础URL',
          required: '请输入基础URL',
          maxLength: '基础URL长度不能超过256个字符',
          placeholder: '访问基础URL'
        },
        properties: {
          label: '配置属性(JSON)',
          required: '请输入配置属性',
          placeholder: '输入JSON格式的配置属性...'
        }
      },
      messages: {
        statusUpdateSuccess: '状态更新成功',
        statusUpdateFailed: '状态更新失败'
      }
    },
    storageProviderRoutingRuleManager: {
      title: '路由规则',
      subtitle: '从上往下优先匹配，第一条命中的规则生效',
      modal: {
        name: { label: '规则名称', required: '请输入规则名称', placeholder: '规则名称' },
        conditionTree: {
          label: '匹配条件',
          description: '不配置条件则匹配所有上传（兜底规则）',
          fields: {
            fileType: '文件类型',
            fileName: '文件名',
            fileExtension: '扩展名',
            fileContentType: '内容类型',
            fileSize: '文件大小（字节）',
            userId: '用户 ID',
            hourOfDay: '小时（0-23）',
            dayOfWeek: '星期（1-7）',
          },
        },
        targetProviders: { label: '目标存储', required: '请至少选择一个存储提供商' },
        distributionType: { label: '分发方式' },
        enabled: { label: '启用' },
      },
      columns: { enabled: '启用状态', conditionTree: '匹配条件' },
      reorderModal: {
        title: '拖拽排序',
        description: '规则从上往下优先匹配，拖动调整优先级。',
        matchAll: '（匹配所有 — 兜底）',
        save: '保存顺序',
        cancel: '取消',
      },
      messages: {
        reorderSuccess: '优先级已保存',
        reorderFailed: '优先级保存失败',
        enabledToggleSuccess: '状态已更新',
        enabledToggleFailed: '状态更新失败',
      },
      simulate: {
        button: '测试路由',
        modalTitle: '模拟文件路由',
        description: '填入一个假设的上传上下文，查看会命中哪条规则以及最终会选中哪个提供商。不会持久化任何数据。',
        form: {
          userId: '用户（可选）',
          fileType: '文件类型',
          fileTypeRequired: '请选择文件类型',
          fileName: '文件名（可选）',
          fileNamePlaceholder: '例如：avatar.png',
          fileNameExtra: '扩展名会从文件名自动派生。',
          fileContentType: 'Content Type（可选）',
          fileContentTypePlaceholder: '例如：image/png',
          fileSize: '文件大小 字节（可选）',
          fileSizePlaceholder: '例如：102400',
          uploadTimestamp: '上传时间（可选）',
        },
        result: {
          header: '模拟结果',
          selectedProvider: '选中的提供商',
          noRuleMatched: '未命中任何规则 — 不会选出提供商',
          matchedNoProvider: '规则命中但未选中任何提供商',
          matched: '命中',
          notMatched: '未命中',
          matchAll: '匹配所有（无条件）',
          actualLabel: '实际值',
          selectedProviderIds: '选中的提供商 ID',
          priority: '优先级',
        },
        actions: { run: '模拟', close: '关闭' },
        errors: { simulateFailed: '模拟失败' },
      },
      action: { reorder: '优先级排序' },
      filter: { actions: '操作' },
    },
    fileResourceManager: {
      title: '文件资源管理',
      subtitle: '管理系统文件资源',
      filter: {
        type: '类型',
        all: '全部',
        id: '文件ID',
        idPlaceholder: '输入文件 ID'
      },
      modal: {
        userId: {
          label: '所属用户',
          required: '请选择所属用户'
        },
        type: {
          label: '文件类型',
          required: '请选择文件类型',
          placeholder: '选择文件类型'
        },
        storageProviderId: {
          label: '存储提供商',
          required: '请选择存储提供商'
        },
        fileName: {
          label: '文件名',
          required: '请输入文件名',
          maxLength: '文件名长度不能超过256个字符',
          placeholder: '文件名'
        },
        fileExtension: {
          label: '扩展名',
          required: '请输入扩展名',
          maxLength: '扩展名长度不能超过64个字符',
          placeholder: '文件扩展名'
        },
        md5: {
          label: 'MD5',
          required: '请输入MD5',
          maxLength: 'MD5长度不能超过32个字符',
          placeholder: '文件MD5值'
        },
        fileSize: {
          label: '文件大小 (Bytes)',
          required: '请输入文件大小',
          placeholder: '文件大小(字节)'
        },
        objectKey: {
          label: '对象键',
          required: '请输入对象键',
          maxLength: '对象键长度不能超过256个字符',
          placeholder: '存储对象键'
        }
      },
      actions: {
        download: '下载',
        copyLink: '复制链接'
      },
      messages: {
        downloadFailed: '无法获取文件下载链接',
        copyLinkSuccess: '链接已复制到剪贴板',
        copyLinkFailed: '复制链接失败'
      }
    },
    userRoleManager: {
      title: '用户角色管理',
      subtitle: '管理系统用户角色',
      filter: {
        id: '角色ID',
        idPlaceholder: '输入角色 ID'
      },
      modal: {
        name: {
          label: '角色名称',
          required: '请输入角色名称',
          maxLength: '角色名称长度不能超过128个字符'
        },
        description: {
          label: '角色描述',
          maxLength: '角色描述长度不能超过512个字符',
          placeholder: '输入角色描述...'
        }
      },
      action: {
        assignPermission: '分配权限'
      },
      permissionModal: {
        title: '为角色 "{{name}}" 分配权限',
        titles: {
          available: '可用权限',
          assigned: '已分配权限'
        }
      },
      messages: {
        fetchPermissionsFailed: '无法获取权限列表',
        fetchRolePermissionsFailed: '无法获取角色权限',
        assignSuccess: '权限分配成功',
        assignFailed: '权限分配失败'
      }
    },
    userRoleRelationManager: {
      title: '用户角色关联管理',
      subtitle: '管理系统用户角色关联',
      columns: {
        user: '用户',
        username: '用户名',
        email: '邮箱',
        action: '操作'
      },
      action: {
        assignRole: '分配角色'
      },
      modal: {
        title: '为用户 "{{name}}" 分配角色',
        titles: {
          available: '可用角色',
          assigned: '已分配角色'
        }
      },
      messages: {
        fetchUsersFailed: '无法获取用户列表',
        fetchRolesFailed: '无法获取角色列表',
        fetchUserRolesFailed: '无法获取用户角色',
        assignSuccess: '角色分配成功',
        assignFailed: '角色分配失败'
      }
    },
    userPermissionManager: {
      title: '用户权限管理',
      subtitle: '管理系统用户权限',
      switch: {
        overview: '树形概览',
        management: '权限管理'
      },
      filter: {
        type: '类型',
        all: '全部',
        id: '权限ID',
        idPlaceholder: '输入权限 ID'
      },
      modal: {
        name: {
          label: '权限名称',
          required: '请输入权限名称',
          maxLength: '权限名称长度不能超过256个字符'
        },
        type: {
          label: '权限类型',
          required: '请选择权限类型',
          placeholder: '选择权限类型'
        },
        path: {
          label: '资源路径',
          maxLength: '资源路径长度不能超过256个字符'
        },
        description: {
          label: '权限描述',
          maxLength: '权限描述长度不能超过512个字符',
          placeholder: '输入权限描述...'
        }
      }
    },
    mailTemplateTypeManager: {
      title: '邮件模板类型管理',
      subtitle: '管理系统邮件模板类型',
      filter: {
        id: '类型ID',
        idPlaceholder: '输入类型 ID'
      },
      modal: {
        name: {
          label: '名称',
          required: '请输入名称',
          maxLength: '名称长度不能超过128个字符',
          placeholder: '类型名称'
        },
        categoryId: {
          label: '分类',
          required: '请选择分类',
          placeholder: '选择分类'
        },
        description: {
          label: '描述',
          maxLength: '描述长度不能超过512个字符',
          placeholder: '类型描述'
        },
        variables: {
          label: '变量(JSON格式)',
          required: '请输入变量',
          placeholder: '{"username": "用户名", "code": "验证码"}'
        },
        allowMultiple: {
          label: '允许多个'
        }
      }
    },
    mailTemplateManager: {
      title: '邮件模板管理',
      subtitle: '管理系统邮件模板',
      enabledStatus: '启用状态',
      filter: {
        templateType: '模板类型',
        placeholder: '选择模板类型',
        id: '模板ID',
        idPlaceholder: '输入模板 ID'
      },
      modal: {
        name: {
          label: '名称',
          required: '请输入名称',
          maxLength: '名称长度不能超过128个字符',
          placeholder: '模板名称'
        },
        typeId: {
          label: '类型',
          required: '请选择类型',
          placeholder: '选择类型'
        },
        title: {
          label: '标题',
          required: '请输入标题',
          maxLength: '标题长度不能超过512个字符',
          placeholder: '邮件标题'
        },
        description: {
          label: '描述',
          maxLength: '描述长度不能超过512个字符',
          placeholder: '模板描述'
        },
        variables: {
          label: '可用变量'
        },
        content: {
          label: '内容',
          required: '请输入内容',
          placeholder: '邮件模板内容，支持变量替换'
        },
        active: {
          label: '启用状态'
        }
      },
      messages: {
        copySuccess: '已复制 {{variable}} 到剪切板',
        statusUpdateSuccess: '状态更新成功',
        statusUpdateFailed: '状态更新失败'
      }
    },
    mailTemplateCategoryManager: {
      title: '邮件模板分类管理',
      subtitle: '管理系统邮件模板分类',
      filter: {
        id: '分类ID',
        idPlaceholder: '输入分类 ID'
      },
      modal: {
        name: {
          label: '名称',
          required: '请输入名称',
          maxLength: '名称长度不能超过128个字符',
          placeholder: '分类名称'
        },
        description: {
          label: '描述',
          maxLength: '描述长度不能超过512个字符',
          placeholder: '分类描述'
        }
      }
    },
    actuatorDashboard: {
      title: '仪表盘',
      subtitle: '在此处查看系统基本信息'
    },
    auditLogManager: {
      title: '审计日志',
      subtitle: '查看系统操作审计日志',
      filter: {
        action: '操作类型',
        all: '全部',
        userId: '用户ID',
        userIdPlaceholder: '输入用户ID',
        username: '用户名',
        usernamePlaceholder: '输入用户名',
        path: '操作路径',
        pathPlaceholder: '输入路径',
        remoteIp: 'IP地址',
        remoteIpPlaceholder: '输入IP',
        id: '日志ID',
        idPlaceholder: '输入日志 ID'
      },
      actionType: {
        unknown: '未知',
        create: '创建',
        read: '读取',
        update: '更新',
        delete: '删除'
      }
    },
    mailSendLogManager: {
      title: '邮件发送日志',
      subtitle: '查看系统邮件发送记录',
      filter: {
        keyword: '关键词',
        keywordPlaceholder: '搜索收件人或主题',
        toEmail: '收件人',
        toEmailPlaceholder: '输入收件邮箱',
        status: '状态',
        all: '全部',
        success: '成功',
        failed: '失败',
        userId: '用户ID',
        userIdPlaceholder: '输入用户ID',
        id: '日志ID',
        idPlaceholder: '输入日志 ID'
      }
    },
    userLoginLogManager: {
      title: '用户登录日志',
      subtitle: '查看用户登录记录',
      filter: {
        userId: '用户ID',
        userIdPlaceholder: '输入用户ID',
        username: '用户名',
        usernamePlaceholder: '输入用户名',
        loginMethod: '登录方式',
        status: '状态',
        all: '全部',
        success: '成功',
        failed: '失败',
        remoteIp: 'IP地址',
        remoteIpPlaceholder: '输入IP地址',
        id: '日志ID',
        idPlaceholder: '输入日志 ID'
      },
      loginMethod: {
        password: '密码登录',
        oauth2: 'OAuth2登录'
      }
    },
    systemSettingsManager: {
      title: '系统设置',
      subtitle: '管理系统配置信息',
      fetchFailed: '无法获取系统设置',
      saveSuccess: '系统设置已保存',
      saveFailed: '系统设置保存失败',
      importEmpty: '导入的配置为空',
      importFailed: '导入配置失败',
      saveSettings: '保存设置',
      operation: '操作',
      importConfig: '导入配置',
      exportConfig: '导出配置',
      maintenanceMode: '维护模式',
      switchMaintenanceModeFailed: '无法切换维护模式',
      maintenanceConfirmEnableTitle: '确认开启维护模式',
      maintenanceConfirmEnableContent: '开启维护模式后，普通用户将无法访问系统。确定要继续吗？',
      maintenanceConfirmDisableTitle: '确认关闭维护模式',
      maintenanceConfirmDisableContent: '关闭维护模式后，所有用户将恢复正常访问。确定要继续吗？',
      maintenanceConfirmOk: '确认',
      maintenanceConfirmCancel: '取消',
      testSendEmail: {
        button: '发送测试邮件',
        modalTitle: '发送测试邮件',
        emailLabel: '收件邮箱',
        emailPlaceholder: '请输入收件邮箱',
        emailRequired: '请输入收件邮箱',
        emailInvalid: '邮箱格式不正确',
        confirm: '发送',
        cancel: '取消',
        sendSuccess: '测试邮件发送成功',
        sendFailed: '测试邮件发送失败',
      },
      testSendLark: {
        button: '发送飞书测试消息',
        modalTitle: '发送飞书测试消息',
        idTypeLabel: 'ID 类型',
        idTypes: {
          openId: 'Open ID',
          userId: 'User ID',
          unionId: 'Union ID',
          email: '邮箱',
          chatId: 'Chat ID',
        },
        idValueLabel: '收件人 ID',
        idValuePlaceholder: '请输入对应类型的 ID 值',
        idValueRequired: '请输入收件人 ID',
        contentLabel: '消息内容（XML）',
        contentPlaceholder: '请输入 XML 格式的消息内容',
        contentHint: '支持 MessageChain XML 语法；为空时使用 application.yaml 中的默认值',
        confirm: '发送',
        cancel: '取消',
        sendSuccess: '飞书测试消息发送成功',
        sendFailed: '飞书测试消息发送失败',
      },
      keys: {
        'basic.baseUrl': 'API 基本地址',
        'basic.frontendBaseUrl': '前端基本地址',
        'basic.waterMark.enabled': '是否显示水印',
        'basic.waterMark.type': '水印类型',
        'basic.waterMark.customValue': '自定义水印内容',
        'basic.waterMark.fontColor': '水印字体颜色',
        'bootstrap.autoCheckRbacTableData': '自动校验 RBAC 表数据',
        'mail.smtp.username': '用户名',
        'mail.smtp.password': '密码',
        'mail.smtp.host': '主机',
        'mail.smtp.port': '端口',
        'mail.smtp.ssl': '是否启用 SSL',
        'mail.smtp.fromEmail': '发件地址',
        'messageChannel.lark.appId': '应用 App ID',
        'messageChannel.lark.appSecret': '应用 App Secret',
        'messageChannel.lark.baseUrl': '开放平台地址',
        'security.api.encrypt.enabled': '是否启用',
        'security.api.encrypt.scope': '作用域',
        'security.api.encrypt.securityLevel': '安全等级',
        'security.loginRateLimit.enabled': '是否启用',
        'security.loginRateLimit.windowSeconds': '滑动窗口时长（秒）',
        'security.loginRateLimit.maxAttemptsPerIp': '单 IP 窗口内最大尝试次数',
        'security.loginRateLimit.maxAttemptsPerAccount': '单账号窗口内最大尝试次数',
        'security.loginRateLimit.lockThreshold': '触发锁定的连续失败次数',
        'security.loginRateLimit.lockBaseSeconds': '锁定基础时长（秒）',
        'security.loginRateLimit.lockMaxSeconds': '锁定最大时长（秒）',
        'security.emailCodeRateLimit.enabled': '是否启用',
        'security.emailCodeRateLimit.windowSeconds': '滑动窗口时长（秒）',
        'security.emailCodeRateLimit.maxPerIp': '单 IP 窗口内最大发送次数',
        'security.emailCodeRateLimit.maxPerEmail': '单邮箱窗口内最大发送次数',
        'security.emailCodeRateLimit.maxGlobal': '全局窗口内最大发送次数',
        'security.outbound.allowedHosts': '允许的出站域名',
        'security.outbound.allowedSmtpHosts': '允许的 SMTP 出站主机',
        'oauth.github.enabled': '启用',
        'oauth.github.useDefault': '使用系统默认配置',
        'oauth.github.authorizationUri': '授权端点',
        'oauth.github.tokenUri': 'Token 端点',
        'oauth.github.userInfoUri': '用户信息端点',
        'oauth.github.userNameAttribute': '用户名属性',
        'oauth.github.clientId': 'Client ID',
        'oauth.github.clientSecret': 'Client Secret',
        'oauth.github.scope': '授权范围',
        'oauth.google.enabled': '启用',
        'oauth.google.useDefault': '使用系统默认配置',
        'oauth.google.authorizationUri': '授权端点',
        'oauth.google.tokenUri': 'Token 端点',
        'oauth.google.userInfoUri': '用户信息端点',
        'oauth.google.userNameAttribute': '用户名属性',
        'oauth.google.clientId': 'Client ID',
        'oauth.google.clientSecret': 'Client Secret',
        'oauth.google.scope': '授权范围',
        'oauth.oicq.enabled': '启用',
        'oauth.oicq.authorizationUri': '授权端点',
        'oauth.oicq.tokenUri': 'Token 端点',
        'oauth.oicq.userInfoUri': '用户信息端点',
        'oauth.oicq.userNameAttribute': '用户名属性',
        'oauth.oicq.clientId': 'Client ID',
        'oauth.oicq.clientSecret': 'Client Secret',
        'oauth.oicq.scope': '授权范围',
        'module.tenant.enabled': '启用租户模块',
        'module.approval.enabled': '启用审批模块',
        'module.message.systemPeerEnabled': '启用系统用户私聊',
        'module.message.tenantScopeEnabled': '启用租户内私聊',
        'module.message.tenantDeskEnabled': '启用租户服务台',
        'resource.signedUrl.ttlSeconds': '签名 URL 有效期（秒）',
        'resource.visibility.userAvatar': '用户头像可见性',
        'resource.visibility.tenantIcon': '租户图标可见性',
        'resource.visibility.tenantMemberAvatar': '租户成员头像可见性',
      },
      groups: {
        'basic': '基本设置',
        'basic.waterMark': '水印设置',
        'bootstrap': '启动设置项',
        'mail.smtp': 'SMTP 邮件服务',
        'messageChannel.lark': '飞书',
        'security.api.encrypt': 'Api 安全设置',
        'security.loginRateLimit': '登录限流与锁定',
        'security.emailCodeRateLimit': '邮件验证码限流',
        'security.outbound': '出站请求',
        'oauth.github': 'GitHub',
        'oauth.google': 'Google',
        'oauth.oicq': 'QQ',
        'module.tenant': '租户模块',
        'module.approval': '审批模块',
        'module.message': '消息模块',
        'resource.signedUrl': '签名 URL',
        'resource.visibility': '资源访问权限',
      },
      tabs: {
        'basic': '基本',
        'bootstrap': '启动',
        'mail': '邮件',
        'messageChannel': '消息渠道',
        'security': '安全',
        'oauth': 'OAuth',
        'module': '功能模块',
        'resource': '资源',
      },
      enums: {
        'security.api.encrypt.scope': {
          'ALL': '所有接口',
          'ALL_ANNOTATED': '所有被修饰的接口',
          'BY_ANNOTATED_LEVEL': '仅限安全等级匹配的接口',
        },
        'basic.waterMark.type': {
          'SYSTEM_NAME': '系统名称',
          'USER_NAME': '用户名称',
          'CUSTOM': '自定义',
        },
        'resource.visibility.userAvatar': {
          'PUBLIC': '公开（任何人可访问）',
          'AUTHENTICATED': '已登录用户',
          'SCOPE_MEMBER': '同租户成员',
          'OWNER_ONLY': '仅上传者本人',
          'SYSTEM_ADMIN': '仅系统管理员',
        },
        'resource.visibility.tenantIcon': {
          'PUBLIC': '公开（任何人可访问）',
          'AUTHENTICATED': '已登录用户',
          'SCOPE_MEMBER': '同租户成员',
          'OWNER_ONLY': '仅上传者本人',
          'SYSTEM_ADMIN': '仅系统管理员',
        },
        'resource.visibility.tenantMemberAvatar': {
          'PUBLIC': '公开（任何人可访问）',
          'AUTHENTICATED': '已登录用户',
          'SCOPE_MEMBER': '同租户成员',
          'OWNER_ONLY': '仅上传者本人',
          'SYSTEM_ADMIN': '仅系统管理员',
        },
      }
    },
    tenantPersonalProfile: {
      title: '个人资料',
      subtitle: '维护您在当前组织内的个人资料信息',
      tabs: {
        info: '基础资料',
        oauth: '第三方账号'
      },
      info: {
        name: '真实姓名',
        nameHint: '不可修改',
        phone: '联系电话',
        nickname: '昵称',
        nicknameHint: '留空则使用账户昵称',
        email: '邮箱',
        emailHint: '留空则使用账户邮箱',
        bio: '个人简介',
        gender: '性别',
        birthday: '生日',
        timezone: '时区',
        locale: '语言',
        save: '保存',
        saveSuccess: '已保存个人资料',
        saveFailed: '保存个人资料失败',
        loadFailed: '加载个人资料失败',
        phoneRequired: '请填写联系电话',
        invalidEmail: '邮箱格式不正确'
      },
      card: {
        unboundEmail: '尚未填写邮箱',
        unboundPhone: '尚未填写联系电话',
        joinedAt: '加入时间：{{time}}'
      },
      oauth: {
        availablePlatforms: '可绑定的平台',
        unbind: '解绑',
        unbindTitle: '解绑第三方账号',
        unbindConfirm: '确认解绑账号 {{nickname}} 吗？',
        unbindSuccess: '已解绑',
        unbindFailed: '解绑失败',
        bindSuccess: '已绑定',
        bindFailed: '绑定失败',
        alreadyBoundToUser: '该第三方账号已绑定至其他系统账户，无法在当前组织内重复绑定'
      },
      avatar: {
        cropTitle: '裁剪头像',
        confirmUpload: '确认上传',
        cancel: '取消',
        uploadSuccess: '头像上传成功',
        uploadFailed: '头像上传失败',
        invalidType: '请上传 JPG、PNG 或 WebP 格式的图片',
        maxSize: '图片大小不能超过 5MB'
      }
    },
    tenantSettingsManager: {
      fetchFailed: '无法获取组织设置',
      saveSuccess: '组织设置已保存',
      saveFailed: '组织设置保存失败',
      saveSettings: '保存设置',
      channelSelectPlaceholder: '请选择消息渠道',
      keys: {
        'notification.memberJoin.email': '邮件通知',
        'notification.memberJoin.channels': '消息渠道',
        'notification.memberJoin.content': '消息通知内容',
        'notification.memberJoinReview.email': '邮件通知',
        'notification.memberJoinReview.channels': '消息渠道',
        'notification.memberJoinReview.content': '消息通知内容',
      },
      groups: {
        'notification.memberJoin': '成员加入通知',
        'notification.memberJoinReview': '成员加入审核通知',
      },
      tabs: {
        'notification': '通知',
      },
      enums: {}
    },
    announcementManager: {
      title: '公告管理',
      subtitle: '管理系统公告信息',
      columns: {
        title: '标题',
        content: '内容',
        status: '状态',
        target: '展示端',
        priority: '优先级',
        createdTime: '创建时间',
      },
      filter: {
        id: '公告ID',
        idPlaceholder: '请输入公告ID',
        status: '状态',
        target: '展示端',
        priority: '优先级',
        all: '全部',
      },
      modal: {
        title: {
          label: '标题',
          required: '请输入标题',
          maxLength: '标题长度不能超过256个字符',
        },
        content: {
          label: '内容',
          required: '请输入内容',
        },
        status: {
          label: '状态',
        },
        target: {
          label: '展示端',
        },
        priority: {
          label: '优先级',
        },
      },
      messages: {
        statusUpdateSuccess: '状态更新成功',
        statusUpdateFailed: '状态更新失败',
      },
    },
    broadcastManager: {
      title: '广播管理',
      subtitle: '管理系统范围的广播（系统公告）',
      modal: {
        title: {
          label: '标题',
          required: '请输入标题',
          maxLength: '标题不能超过 256 个字符',
        },
        content: {
          label: '内容',
          required: '请输入内容',
        },
        category: {
          label: '分类',
        },
        audienceType: {
          label: '受众',
          required: '请选择受众',
        },
        publishTime: {
          label: '发布时间',
          placeholder: '留空则立即发布',
          help: '到达该时间后用户才可见，留空表示立即发布',
        },
        expireTime: {
          label: '过期时间',
          placeholder: '留空则永不过期',
          help: '到达该时间后自动下架，留空表示永不过期',
        },
      },
    },
    managerContainer: {
      notOrganizationIdentity: '非组织身份',
      current: '当前',
      switching: '切换中...',
      userProfile: '个人中心',
      customTheme: '主题设置',
      logout: '退出登录',
      menu: '菜单',
      switchSuccess: '已切换到 {{tenantName}}',
      switchFailed: '切换到 {{tenantName}} 失败',
      tabClose: '关闭',
      tabCloseOthers: '关闭其他',
      tabCloseLeft: '关闭左侧',
      tabCloseRight: '关闭右侧',
    },
    tenantInvitation: {
      title: '加入组织',
      inputStepHint: '请输入管理员提供的邀请码以继续',
      infoStepHint: '请确认组织信息',
      formStepHint: '请填写个人信息完成加入',
      inviteCodeLabel: '组织邀请码',
      inviteCodePlaceholder: '例如：1brxVqQH2R6c568N',
      noDescription: '暂无描述',
      reachedUsageLimit: '邀请码已达使用上限',
      expired: '邀请码已过期',
      validUntil: '有效期至',
      permanentValid: '永久有效',
      nextStep: '下一步',
      modifyInviteCode: '修改邀请码',
      realName: '真实姓名',
      realNamePlaceholder: '在此输入您的真实姓名',
      realNameRequired: '请输入您的姓名',
      phoneNumber: '手机号',
      phoneNumberPlaceholder: '请输入手机号',
      phoneNumberRequired: '请输入有效的手机号码',
      acceptInvitation: '接受邀请',
      previousStep: '上一步',
      submittedTitle: '申请已送出！',
      submittedDescription: '我们已通知 {{tenantName}} 的管理员。审核通过后您将收到电子邮件通知。',
      backToHome: '返回首页',
      enterInviteCode: '请输入邀请码',
      invalidInviteCodeLength: '邀请码长度无效（至少需要 8 位）',
      fetchTenantFailed: '获取组织信息失败',
      invalidInviteCode: '邀请码无效',
      inviteCodeExpired: '邀请码无效或已过期',
      submitSuccess: '申请已提交！请等待管理员审核。',
      submitFailed: '提交失败，请重试'
    },
    notFound: {
      title: '哎呀！路径丢失了',
      description: '您访问的页面可能已被移动、删除或不存在。',
      backToHome: '返回首页'
    },
    maintenance: {
      documentTitle: '维护中',
      title: '系统维护中',
      description: '我们正在进行系统升级与维护，请稍后再试。',
      hint: '维护期间所有功能暂不可用，感谢您的耐心等待。'
    },
    serviceUnavailable: {
      title: '服务不可用',
      description: '无法连接到服务器，请检查网络后刷新页面。'
    },
    sessionMonitor: {
      title: '在线会话监控',
      subtitle: '查看当前系统中的在线会话列表',
      entityName: '会话',
      filter: {
        type: '会话类型',
        typePlaceholder: '按类型筛选'
      }
    },
    systemMonitor: {
      title: '系统监控',
      subtitle: '实时查看服务器 CPU、内存、磁盘和 JVM 等系统指标',
      chartTitle: '指标趋势',
      timeRange: '时间范围',
      syncCrosshair: '同步十字',
      columns: {auto: '自动', col1: '1 列', col2: '2 列', col3: '3 列'},
      durations: {
        m1: '1 分钟',
        m5: '5 分钟',
        m15: '15 分钟',
        m30: '30 分钟',
        h1: '1 小时',
        h3: '3 小时',
        h5: '5 小时',
        h12: '12 小时',
        d1: '1 天',
        d3: '3 天',
        d5: '5 天',
        d7: '7 天',
        d14: '14 天',
      },
      metrics: {
        cpuUsage: 'CPU 使用率 (%)',
        cpuLoadAverage: 'CPU 负载',
        memoryUsed: '内存使用 (GB)',
        jvmHeapUsed: 'JVM 堆使用 (GB)',
        jvmNonHeapCommitted: 'JVM 非堆已分配 (GB)',
        jvmNonHeapUsed: 'JVM 非堆使用 (GB)',
        diskUsed: '磁盘使用 (GB)',
        dbConnectionsActive: '数据库活跃连接',
        gcCount: 'GC 次数',
        gcTime: 'GC 时间 (ms)',
      },
    },
    tenantDictTypeManager: {
      title: '租户字典类型管理',
      subtitle: '管理租户的字典类型',
      action: {
        addNew: '新增字典类型',
        manageItems: '管理字典项',
      },
      modal: {
        code: { label: '编码', placeholder: '请输入字典类型编码', required: '编码不能为空' },
        name: { label: '名称', placeholder: '请输入字典类型名称', required: '名称不能为空' },
        remark: { label: '备注', placeholder: '请输入备注' },
        status: { label: '状态' },
      },
    },
    systemDictTypeManager: {
      title: '系统字典类型管理',
      subtitle: '管理系统级字典类型',
      action: {
        addNew: '新增字典类型',
        manageItems: '管理字典项',
      },
      modal: {
        code: { label: '编码', placeholder: '请输入字典类型编码', required: '编码不能为空' },
        name: { label: '名称', placeholder: '请输入字典类型名称', required: '名称不能为空' },
        remark: { label: '备注', placeholder: '请输入备注' },
        status: { label: '状态' },
      },
    },
    systemDictItemManager: {
      title: '系统字典项管理',
      subtitle: '管理系统级字典项',
      action: {
        addNew: '新增字典项',
        back: '返回',
      },
    },
    tenantDictItemManager: {
      title: '字典项管理',
      subtitle: '管理当前字典类型下的字典项',
      noTypeSelected: '请从字典类型列表进入',
      action: {
        addNew: '新增字典项',
        back: '返回',
      },
      modal: {
        itemCode: { label: '编码', placeholder: '请输入字典项编码', required: '编码不能为空' },
        itemValue: { label: '显示值', placeholder: '请输入字典项显示值', required: '显示值不能为空' },
        sortOrder: { label: '排序' },
        status: { label: '状态' },
        isDefault: { label: '默认选中' },
      },
    },
    myTenantDictTypeManager: {
      title: '字典管理',
      subtitle: '管理组织字典',
      action: {
        addNew: '新增字典类型',
        manageItems: '管理字典项',
      },
      modal: {
        code: { label: '编码', placeholder: '请输入字典类型编码', required: '编码不能为空' },
        name: { label: '名称', placeholder: '请输入字典类型名称', required: '名称不能为空' },
        remark: { label: '备注', placeholder: '请输入备注' },
        status: { label: '状态' },
      },
    },
    approvalFlowDefinitionManager: {
      title: '系统审批流程管理',
      subtitle: '管理系统级审批流程定义',
      action: {
        addNew: '新增流程定义',
      },
      modal: {
        name: { label: '名称', placeholder: '请输入流程定义名称', required: '名称不能为空' },
        description: { label: '描述', placeholder: '请输入描述' },
        status: { label: '状态' },
      },
    },
    tenantApprovalFlowDefinitionManager: {
      title: '租户审批流程管理',
      subtitle: '管理租户级审批流程定义',
      action: {
        addNew: '新增流程定义',
      },
      modal: {
        name: { label: '名称', placeholder: '请输入流程定义名称', required: '名称不能为空' },
        description: { label: '描述', placeholder: '请输入描述' },
        status: { label: '状态' },
      },
    },
    myApprovalFlowDefinitionManager: {
      title: '审批流程管理',
      subtitle: '管理组织审批流程定义',
      action: {
        addNew: '新增流程定义',
      },
      modal: {
        name: { label: '名称', placeholder: '请输入流程定义名称', required: '名称不能为空' },
        description: { label: '描述', placeholder: '请输入描述' },
        status: { label: '状态' },
      },
    },
    initiableApprovalFlows: {
      title: '发起审批',
      subtitle: '查看当前可发起的审批流程',
      tab: {
        system: '系统审批',
        tenant: '租户审批'
      },
      noTenantTip: '当前账号尚未加入任何组织，仅可发起系统级审批',
      noPermissionTip: '当前账号没有任何 scope 的查看权限',
      action: {
        initiate: '发起'
      },
      modal: {
        title: '发起审批 - {{name}}',
        formPlaceholder: '当前流程暂无表单，点击确定即可发起',
        confirm: '确定',
        cancel: '取消',
        success: '发起成功',
        failed: '发起失败'
      }
    },
    myApprovalFlows: {
      title: '我的审批',
      subtitle: '查看我发起的审批',
      tab: {
        system: '系统审批',
        tenant: '租户审批'
      },
      filter: {
        status: '状态',
        all: '全部'
      },
      noTenantTip: '当前账号尚未加入任何组织，租户范围暂无数据'
    },
    approvalTaskHandle: {
      title: '审批处理',
      subtitle: '查看并处理分配给我的审批任务',
      tab: {
        system: '系统审批',
        tenant: '租户审批'
      },
      filter: {
        status: '状态',
        all: '全部'
      },
      noTenantTip: '当前账号尚未加入任何组织，租户范围暂无数据',
      action: {
        handle: '处理'
      },
      modal: {
        title: '处理审批任务',
        comment: '审批意见',
        commentPlaceholder: '请输入审批意见（可选）',
        approve: '同意',
        reject: '拒绝',
        cancel: '取消',
        success: '处理成功',
        failed: '处理失败',
        formPlaceholder: '当前节点未配置表单',
        formLoadFailed: '加载表单失败'
      }
    },
    myTenantApprovalFlowInstanceManager: {
      title: '审批管理',
      subtitle: '查看本组织成员发起的所有审批',
      filter: {
        status: '状态',
        all: '全部'
      },
      noTenantTip: '当前账号尚未加入任何组织'
    },
    tenantApprovalFlowInstanceManager: {
      title: '审批管理',
      subtitle: '管理任一租户的审批申请',
      filter: {
        status: '状态',
        all: '全部'
      }
    },
    approvalFlowInstanceManager: {
      title: '用户审批管理',
      subtitle: '查看系统级所有用户发起的审批',
      filter: {
        status: '状态',
        all: '全部'
      }
    },
  },

  components: {
    approvalEditor: {
      action: {
        editFlow: '编辑流程',
        close: '关闭'
      },
      header: {
        loading: '加载中...',
        save: '保存',
        saveSuccess: '流程保存成功',
        validationFailed: '流程图校验失败'
      },
      toolbar: {
        undo: '撤销 (CTRL+Z)',
        redo: '重做 (CTRL+Y)',
        fitViewport: '适应视口',
        autoArrange: '自动排列',
        autoArrangeTooltip: '自动排列节点'
      },
      contextMenu: {
        createNode: '创建节点'
      },
      statusBar: {
        position: '坐标',
        scale: '缩放'
      },
      inspector: {
        title: '节点属性',
        emptyHint: '选择一个节点以查看属性',
        id: 'ID',
        nodeKey: '节点标识',
        name: '名称',
        type: '类型',
        config: '配置',
        formSchema: '表单结构',
        position: '位置',
        newNode: '(新节点)',
        emptyValue: '(空)',
        unnamed: '(未命名)',
        none: '(无)',
        approveMode: '审批方式',
        approvers: '审批人',
        ccAssignees: '抄送人',
        ccRoles: '抄送角色',
        ccChannels: '消息渠道'
      },
      validation: {
        duplicateNodeKey: '节点标识重复: "{{nodeKey}}"',
        emptyNodeKey: '节点标识不能为空 (节点: "{{name}}")',
        duplicateStart: '只允许存在一个开始节点'
      }
    },
    approvalFlowViewer: {
      action: {
        viewFlow: '查看流程',
        close: '关闭'
      },
      header: {
        loading: '加载中...',
        instanceStatus: '实例状态',
        version: '版本'
      },
      statusBar: {
        position: '坐标',
        scale: '缩放'
      },
      records: {
        title: '审批记录',
        empty: '暂无审批记录',
        emptyForNode: '该节点暂无审批记录',
        selectNodeHint: '选中节点查看其审批记录',
        operator: '操作人',
        action: '操作',
        comment: '备注',
        time: '时间',
        node: '节点'
      },
      tabs: {
        records: '审批记录',
        form: '表单数据'
      },
      form: {
        snapshotTitle: '当前表单快照',
        snapshotSubtitle: '该审批实例的当前表单数据（只读）',
        timelineTitle: '变更时间线',
        timelineSubtitle: '每行展示对应操作人在该步骤填写（发起）或修改（审批）的内容',
        emptyTimeline: '暂无变更记录',
        noSchema: '该流程未配置表单',
        initiateFields: '初始填写',
        changedFields: '本次修改'
      },
      error: {
        loadFailed: '加载审批实例失败'
      }
    },
    approvalFormRenderer: {
      empty: '该流程未配置表单字段',
      error: {
        required: '该字段为必填项',
        maxLength: '内容超过长度限制',
        pattern: '格式不正确',
        min: '数值低于最小值',
        max: '数值高于最大值',
        minCount: '请至少选择规定数量的选项',
        maxCount: '选择项数量超过上限'
      }
    },
    conditionNodeInspector: {
      title: '条件路由',
      subtitle: '根据表单字段值将流程路由到不同的下游节点',
      noSchemaHint: '当前流程未配置表单，请先前往「表单设计器」定义字段后再添加条件',
      unsavedNodesWarning: '存在未保存的节点，请先保存流程图再将其设为目标节点',
      emptyRoutes: '尚未配置路由，点击「新增路由」创建',
      emptyLeaves: '该路由暂无条件',
      routeTitle: '路由 #{{n}}',
      addRoute: '新增路由',
      deleteRoute: '删除',
      addLeaf: '新增条件',
      deleteLeaf: '删除条件',
      target: '目标节点',
      selectTarget: '选择下游节点',
      logic: '组合方式'
    },
    nodeFormOverlay: {
      title: '字段可见性覆盖',
      subtitle: '为该节点单独覆盖每个字段的展示方式，未调整的开关沿用定义级设置',
      empty: '尚未定义任何字段，请先前往「表单设计器」新增字段',
      ccInfo: '抄送节点不会修改表单数据，「只读」已强制打开',
      ccLockedTooltip: '抄送节点始终为只读，不可修改',
      column: {
        field: '字段',
        visible: '可见',
        readonly: '只读',
        required: '必填'
      }
    },
    approvalFormDesigner: {
      title: '表单设计器',
      subtitle: '设计发起时使用的表单。审批节点可在此基础上做可见性覆盖。',
      tabs: {
        node: '节点属性',
        designer: '表单设计器',
        preview: '预览'
      },
      fieldList: {
        title: '字段列表',
        addField: '新增字段',
        deleteField: '删除字段',
        empty: '暂无字段，点击"新增字段"添加'
      },
      property: {
        title: '字段属性',
        empty: '请从上方选择一个字段进行编辑',
        key: '字段 Key',
        keyHint: 'camelCase 命名，只允许字母和数字，且在当前 schema 中唯一',
        label: '标题',
        type: '类型',
        description: '描述',
        placeholder: '占位提示',
        required: '必填',
        visible: '可见',
        readonly: '只读',
        groupKey: '所属分组',
        noGroup: '（不分组）',
        validationTitle: '校验规则',
        multiple: '多选',
        maxLength: '最大长度',
        pattern: '正则模式',
        min: '最小值',
        max: '最大值',
        precision: '小数精度',
        minCount: '最少选择',
        maxCount: '最多选择',
        minDate: '最早日期',
        maxDate: '最晚日期',
        options: '选项',
        addOption: '新增选项',
        deleteOption: '删除选项',
        patternHint: 'JavaScript 正则表达式源字符串',
        dictScope: '字典范围',
        dictScopeHint: '默认继承流程范围。系统流程只能引用系统字典；租户流程可引用系统或本租户字典。',
        dictScopeInherit: '继承流程范围',
        dictScopeSystem: '系统字典',
        dictScopeTenant: '租户字典',
        dictCode: '字典类型',
        dictCodeHint: '选择字典类型，其可选项将作为该字段的候选值。',
        error: {
          keyEmpty: 'Key 不能为空',
          keyPattern: 'Key 必须为 camelCase（仅允许字母数字，首字母小写）',
          keyDuplicated: 'Key 已存在，请修改'
        }
      },
      preview: {
        title: '预览',
        subtitle: '这是发起人看到的表单效果。已被标记为不可见的字段不会显示',
        empty: '暂无字段可预览'
      },
      save: {
        button: '保存表单结构',
        success: '表单结构保存成功',
        failed: '保存失败',
        hasInvalidFields: '存在无效字段 Key，无法保存',
        confirmTitle: '确认保存表单结构？',
        confirmContent: '保存后的表单仅对新发起的审批生效。已发起的审批保留发起时的表单结构，不受影响。',
        confirmOk: '保存',
        confirmCancel: '取消'
      }
    },
    addressPicker: {
      triggerTooltip: '在地图上选择地址',
      akMissing: '未配置百度地图 AK，无法使用地图选址',
      emptyNearby: '附近暂无可选地址',
      loading: '正在搜索附近地址...'
    },
    notification: {
      // NotificationBell / NotificationCenter
      title: '消息中心',
      emptyHistory: '暂无公告',
      expired: '已过期',
      noMore: '没有更多了',
      conversations: {
        title: '会话',
        systemBroadcast: '系统公告',
        personal: '个人',
        organizationTag: '组织',
        deskTag: '客服台',
        external: '外部',
        systemTab: '系统用户',
        deskContactTag: '联系的客服台',
        membersTag: '成员',
        externalTag: '外部'
      },
      startConversation: {
        start: '发起会话',
        title: '发起会话',
        userTab: '用户',
        tenantTab: '租户',
        tenantMemberTab: '组织成员',
        tenantScopeDisabled: '租户会话功能已被管理员禁用',
        allFeaturesDisabled: '会话功能已被管理员禁用'
      },
      contactUser: {
        searchPlaceholder: '输入完整用户名或邮箱',
        hint: '输入完整的用户名或邮箱以发起会话',
        empty: '未找到匹配的用户'
      },
      tenantChat: {
        title: '选择组织成员',
        tenantPlaceholder: '选择组织',
        empty: '该组织暂无成员'
      },
      contact: {
        start: '发起会话',
        title: '选择租户',
        searchPlaceholder: '搜索租户名称',
        empty: '暂无可联系的租户',
        draftTag: '草稿',
        startHint: '发送第一条消息以开始会话',
        composerPlaceholder: '输入消息，Enter 发送，Shift+Enter 换行',
        send: '发送'
      }
    },
    dashboard: {
      // DashboardPage
      greeting: {
        earlyMorning: '早点休息',
        morning: '早上好',
        lateMorning: '上午好',
        afternoon: '下午好',
        evening: '晚上好',
        user: '用户'
      },
      timeRange: {
        '1d': '1天',
        '3d': '3天',
        '5d': '5天',
        '1w': '1周',
        '2w': '2周',
        '1m': '1月',
        '3m': '3月',
        '6m': '半年',
        '1y': '1年'
      },

      // BusinessStatistics
      businessStats: {
        title: '业务统计',
        totalUsers: '总用户数',
        totalTenants: '总租户数',
        totalTenantMembers: '租户成员数',
        totalFileResources: '文件资源数',
        totalMailSent: '邮件发送量',
        totalInvitations: '邀请码数',
        totalInvitationRecords: '邀请记录数',
        totalOAuthAccounts: 'OAuth 绑定数'
      },

      // SystemMetrics
      systemMetrics: {
        title: '系统资源监控',
        lastUpdated: '最后更新于',
        autoRefresh: '自动刷新',
        refreshOptions: {
          '1s': '1秒',
          '3s': '3秒',
          '5s': '5秒',
          '1m': '1分钟',
          '3m': '3分钟',
          '5m': '5分钟',
          '10m': '10分钟',
          '15m': '15分钟',
          '30m': '30分钟'
        },
        metrics: {
          cpuUsage: 'CPU 使用率',
          memoryUsage: '内存占用',
          jvmHeapMemory: 'JVM 堆内存',
          jvmNonHeapMemory: 'JVM 非堆内存',
          systemLoad: '系统负载',
          diskUsage: '磁盘使用率',
          dbConnections: '数据库连接池',
          gcPauseTime: 'GC 暂停时间',
        },
        units: {
          usage: '使用率',
          cores: '核心',
          unit: '单位'
        },
        loadFailed: '无法获取系统监控报告'
      },

      // MyJoinedTenants
      myJoinedTenants: {
        title: '我加入的组织',
        count: '数量',
        current: '当前',
        noDescription: '暂无描述~',
        noTenants: '暂无加入的组织',
        joinByCode: '通过邀请码加入',
        joinByCodeDesc: '输入邀请码加入新组织',
        loadFailed: '加载组织列表失败'
      },

      // SystemAnnouncements
      systemAnnouncements: {
        title: '系统公告',
        noAnnouncements: '暂无公告',
        loadFailed: '加载公告失败'
      }
    },

    tenantSelectorWithDetail: {
      label: '选择租户',
      reselect: '重新选择',
      tenantId: '租户ID',
      status: '状态',
      contactName: '联系人',
      contactPhone: '联系电话',
      contactEmail: '联系邮箱',
      address: '地址',
      subscribedTime: '订阅时间',
      expiresTime: '过期时间',
      description: '描述'
    },

    selector: {
      entitySelector: {
        title: '选择{{entityName}}',
        cancelText: '取消',
        okText: '确定'
      },
      entityIdSelector: {
        placeholder: '选择',
        clear: '清除'
      },
      dictTypeCodeSelector: {
        placeholder: '选择字典类型'
      }
    },

    managerPageContainer: {
      addNew: '新增{{entityName}}',
      edit: '编辑',
      create: '新建',
      deleteSuccess: '{{entityName}}已删除',
      deleteFailed: '{{entityName}}删除失败',
      updateSuccess: '{{entityName}}更新成功',
      updateFailed: '{{entityName}}更新失败',
      createSuccess: '新增{{entityName}}成功',
      createFailed: '新增{{entityName}}失败',
      deleteConfirm: '确定要删除此{{entityName}}？',
      confirm: '确认',
      cancel: '取消',
      batchOperation: '批量操作',
      batchDelete: '全部删除',
      batchDeleteTitle: '删除所有选中的项目',
      batchDeleteConfirm: '此操作不可恢复，请确认是否要继续？',
      batchDeleteSuccess: '批量删除成功',
      batchDeleteFailed: '批量删除失败',
      execute: '执行',
      action: '操作',
      refresh: '刷新',
      timeRange: '时间范围',
      startTime: '开始时间',
      tillNow: '直到现在',
      last5Minutes: '最近 5 分钟',
      last10Minutes: '最近 10 分钟',
      last15Minutes: '最近 15 分钟',
      last30Minutes: '最近 30 分钟',
      last1Hour: '最近 1 小时',
      last2Hours: '最近 2 小时',
      last3Hours: '最近 3 小时',
      last4Hours: '最近 4 小时',
      last8Hours: '最近 8 小时',
      last12Hours: '最近 12 小时',
      last1Day: '最近 1 天',
      last3Days: '最近 3 天',
      last5Days: '最近 5 天',
      last7Days: '最近 7 天',
      last14Days: '最近 14 天',
      last30Days: '最近 30 天',
      todayToNow: '今天开始到现在'
    },

    protectedController: {
      readonlyError: '只读模式，无法创建',
      warningTitle: '高危操作警告',
      warningContent: '此页面推荐以只读模式进入，任何错误操作将会引发不可预料的后果，请选择操作模式：',
      editMode: '以编辑模式继续',
      readonlyMode: '以只读模式继续',
      readonlyBadge: '只读模式',
      editModeBadge: '正在以编辑模式访问'
    },

    maintenanceBanner: {
      message: '系统当前处于维护模式，普通用户暂时无法访问部分功能。',
    },

    themeSettings: {
      title: '主题设置',
      themeColor: {
        title: '自定义主题色',
        description: '选择你喜欢的主题色，将会应用到整个系统界面'
      },
      tabs: {
        title: '标签页',
        enableTabs: '启用标签页',
        enableTabsDesc: '将会在页面上方展示历史打开的页面',
        tabSize: '标签页大小',
        tabSizeDesc: '标签页选项卡尺寸',
        sizeSmall: '小',
        sizeMiddle: '中',
        sizeLarge: '大',
      },
      pageAnimation: {
        title: '页面动画',
        description: '选择页面切换时的过渡动画效果',
        none: '无',
        fade: '淡入淡出',
        slideLeft: '左滑入',
        slideRight: '右滑入',
        slideUp: '上滑入',
        scale: '缩放',
      }
    },

    storageProviderConfig: {
      localFileSystem: '本地文件系统',
      tencentCos: '腾讯云 COS',
      aliyunOss: '阿里云 OSS',
      volcEngineTos: '火山引擎 TOS',
      selectTemplate: '选择配置模板',
      applyTemplate: '应用模板',
      applyTemplateTooltip: '应用模板'
    },

    messageChannelConfig: {
      email: '邮件渠道',
      lark: '飞书渠道',
      selectTemplate: '选择配置模板',
      applyTemplate: '应用模板',
      applyTemplateTooltip: '应用所选渠道类型的配置模板',
      disabledHint: '请先选择渠道类型',
      applyOverwriteTitle: '应用模板',
      applyOverwriteContent: '应用模板会覆盖当前已编辑的配置，确认继续？'
    },

    settings: {
      secretPlaceholderConfigured: '已配置，留空保持不变',
      secretPlaceholderEmpty: '尚未配置',
      viewChanges: '查看变更',
      noChanges: '没有需要保存的变更',
      changesModalTitle: '待保存的变更（{{count}} 项）',
      changesColumnKey: '设置项',
      changesColumnBefore: '当前值',
      changesColumnAfter: '新值',
      valueEmpty: '(空)',
      secretUnchangedTag: '已配置',
      secretModifiedTag: '将更新为新密钥'
    },

    columns: {
      fileResource: {
        preview: '预览',
        fileInfo: '文件信息',
        fileSize: '大小',
        md5: 'MD5',
        storageProvider: '存储提供商',
        uploader: '上传者',
        fileType: '文件类型',
        status: '状态',
        createdTime: '创建时间',
        userId: '用户ID',
        providerId: '提供商ID'
      },
      storageProvider: {
        name: '名称',
        type: '类型',
        description: '描述',
        baseUrl: '基础URL',
        config: '配置'
      },
      storageProviderRoutingRule: {
        name: '规则名称',
        priority: '优先级',
        targetProviders: '目标存储',
        distributionType: '分发方式',
        enabled: '启用状态',
        unknownProvider: '未知存储'
      },
      userPermission: {
        permission: '权限',
        type: '类型',
        description: '描述',
        path: '资源路径'
      },
      permissionTree: {
        actions: '操作'
      },
      userRole: {
        role: '角色',
        description: '描述'
      },
      user: {
        userInfo: '用户信息',
        nickname: '昵称',
        email: '邮箱',
        status: '状态',
        enabled: '已启用',
        disabled: '已禁用',
        banned: '已封禁'
      },
      userBanRecord: {
        user: '用户',
        reason: '原因',
        banUntil: '封禁至',
        permanent: '永久封禁',
        operator: '操作人',
        unknownUser: '未知用户',
        status: {
          label: '状态',
          active: '生效中',
          lifted: '已解除',
          expired: '已过期'
        }
      },
      tenant: {
        tenantName: '租户名称',
        description: '描述',
        status: '状态',
        tireType: '套餐类型',
        owner: '所有者',
        contact: '联系人',
        address: '地址',
        subscriptionInfo: '订阅信息',
        subscribedTime: '订阅时间',
        expiresTime: '过期时间',
        expired: '已过期'
      },
      tenantRole: {
        roleInfo: '角色信息',
        description: '描述',
        parentRole: '父角色'
      },
      tenantPermission: {
        permissionInfo: '权限信息',
        permissionName: '权限名称',
        description: '描述',
        type: '类型',
        path: '路径',
        createdTime: '创建时间'
      },
      tenantMember: {
        recordInfo: '记录信息',
        tenantId: '租户 ID',
        userInfo: '用户信息',
        userId: '用户ID',
        email: '邮箱',
        status: '状态'
      },
      myTenantMember: {
        recordInfo: '记录信息',
        tenantId: '租户 ID',
        member: '成员',
        email: '邮箱',
        status: '状态'
      },
      tenantDepartment: {
        departmentName: '部门名称',
        description: '描述',
        parentDepartment: '父部门',
        parentDepartmentId: '父部门ID'
      },
      tenantDepartmentMember: {
        recordInfo: '记录信息',
        memberId: '成员 ID',
        tenantId: '租户 ID',
        userInfo: '用户信息',
        userId: '用户ID',
        email: '邮箱',
        memberStatus: '成员状态',
        departmentRole: '部门角色'
      },
      tenantInvitation: {
        recordInfo: '记录信息',
        invitationCode: '邀请码',
        invitationCount: '可邀请次数',
        times: '次',
        creator: '创建者',
        department: '部门',
        requiresReviewing: '需要审核',
        requiresReviewingTooltip: '需要审核',
        noReviewingTooltip: '无需审核',
        expiresTime: '过期时间',
        tenantId: '租户 ID',
        memberId: '成员ID',
        userId: '用户ID',
        departmentId: '部门ID',
        notSpecified: '未指定',
        yes: '是',
        no: '否',
        neverExpires: '永不过期',
        expired: '已过期',
        status: '状态',
        usedCount: '已使用次数',
        createdTime: '创建时间'
      },
      tenantMessageChannel: {
        recordInfo: '记录信息',
        tenantId: '租户 ID',
        name: '渠道名称',
        channelType: '渠道类型',
        enabled: '启用状态',
        statusEnabled: '已启用',
        statusDisabled: '已禁用',
        config: '配置'
      },
      messageChannel: {
        recordInfo: '记录信息',
        scopeId: '归属 ID',
        name: '渠道名称',
        channelType: '渠道类型',
        enabled: '启用状态',
        statusEnabled: '已启用',
        statusDisabled: '已禁用',
        config: '配置'
      },
      tenantTireType: {
        name: '名称',
        description: '描述'
      },
      tenantTireBenefitFeature: {
        featureKey: '权益标识',
        name: '名称',
        description: '描述',
        featureType: '权益类型',
        defaultValue: '默认值',
      },
      tenantTireBenefitValue: {
        recordInfo: '记录信息',
        tireType: '套餐类型',
        feature: '权益项',
        featureType: '类型',
        featureValue: '权益值'
      },
      oAuthAccount: {
        identifier: '标识',
        platform: '平台',
        systemUser: '系统用户',
        unbound: '未绑定用户',
        userInfo: '用户信息',
        noAvatar: '无头像'
      },
      broadcast: {
        title: '标题',
        content: '内容',
        category: '分类',
        scopeType: '范围',
        audience: '受众',
        sender: '发送方',
        publishTime: '发布时间',
        expireTime: '过期时间',
        systemSender: '系统',
        audienceRef: '引用',
        unknown: '未知'
      },
      mailTemplate: {
        name: '名称',
        type: '类型',
        title: '标题',
        description: '描述',
        content: '内容'
      },
      mailTemplateCategory: {
        name: '名称',
        description: '描述'
      },
      mailTemplateType: {
        name: '名称',
        description: '描述',
        variables: '变量',
        category: '分类',
        allowMultiple: '允许多模板',
        yes: '是',
        no: '否'
      },
      auditLog: {
        userInfo: '操作用户',
        action: '操作类型',
        resourceType: '资源类型',
        request: '请求信息',
        status: '状态',
        success: '成功',
        failed: '失败'
      },
      mailSendLog: {
        fromEmail: '发件人',
        toEmail: '收件人',
        subject: '主题',
        user: '发送用户',
        status: '状态',
        success: '成功',
        failed: '失败'
      },
      userLoginLog: {
        user: '用户',
        loginMethod: '登录方式',
        oauth2Username: 'OAuth2用户名',
        remoteIp: 'IP地址',
        userAgent: 'User Agent',
        status: '状态',
        success: '成功',
        failed: '失败',
        loginMethodTypes: {
          password: '密码登录',
          oauth2: 'OAuth2登录',
          unknown: '未知'
        }
      },
      sessionMonitor: {
        sessionId: '会话ID',
        type: '类型',
        user: '用户',
        tenant: '租户',
        remoteIp: 'IP地址',
        userAgent: 'User Agent'
      },
      tenantDictType: {
        code: '编码',
        name: '名称',
        remark: '备注',
        status: '状态'
      },
      tenantDictItem: {
        itemCode: '编码',
        itemValue: '显示值',
        sortOrder: '排序',
        isDefault: '默认',
        status: '状态',
        yes: '是',
        no: '否'
      },
      approvalFlowDefinition: {
        name: '名称',
        definitionId: '流程ID',
        description: '描述',
        currentVersion: '版本',
        status: '状态'
      },
      approvalFlowInstance: {
        scope: '范围',
        definition: '流程',
        definitionId: '流程ID',
        definitionVersion: '版本',
        initiator: '发起人',
        status: '状态',
        unknownDefinition: '流程已删除'
      },
      approvalFlowTask: {
        scope: '范围',
        flow: '流程',
        instanceId: '实例ID',
        initiator: '发起人',
        node: '节点',
        status: '状态',
        comment: '审批意见',
        unknownFlow: '流程已删除',
        unknownNode: '节点已删除',
        unknownInitiator: '发起人已删除'
      }
    },
    entityTable: {
      recordTime: '记录时间',
      createdTime: '创建时间',
      modifiedTime: '修改时间',
      action: '操作',
      search: '搜索',
      searchPlaceholder: '搜索{{entityName}}...',
      fetchError: '无法获取{{entityName}}列表',
      combineLogic: '组合逻辑',
      combineAnd: '且',
      combineOr: '或',
      pagination: {
        total: '共 {{total}} 条记录',
      },
      columnFilter: {
        label: '列设置',
        button: '列筛选',
        title: '显示列',
        selectAll: '全选',
        reset: '重置',
        dragHint: '拖拽调整列顺序',
        dragHandle: '拖拽排序',
      }
    },
    filterBuilder: {
      filters: '筛选',
      addCondition: '添加条件',
      fillRequired: '请填写所有条件值',
      addGroup: '添加分组',
      and: '且',
      or: '或',
      apply: '应用',
      cancel: '取消',
      reset: '重置',
      rootLogic: '组内逻辑',
      group: '分组',
      noConditions: '暂无筛选条件，请添加条件或分组',
      selectField: '选择字段',
      selectOperator: '选择操作符',
      selectValue: '选择值',
      valuePlaceholder: '输入值',
      addValue: '添加值',
      operators: {
        eq: '等于',
        ne: '不等于',
        contains: '包含',
        like: '模糊匹配',
        gt: '大于',
        gte: '大于等于',
        lt: '小于',
        lte: '小于等于',
        in: '包含于',
      }
    },
    jsonEditor: {
      root: '根节点',
      index: '索引',
      type: {
        string: '字符串',
        number: '数字',
        boolean: '布尔',
        object: '对象',
        array: '数组',
        null: '空值'
      },
      emptyObject: '空对象',
      emptyArray: '空数组',
      visual: '可视化',
      source: '源码',
      invalidJson: '无效的 JSON 格式',
      valid: '格式有效',
      invalid: '格式错误'
    },
    htmlEditor: {
      code: '代码',
      preview: '预览',
      placeholder: '输入 HTML 代码...'
    },
    messageChainEditor: {
      visualMode: '可视化',
      sourceMode: '源码',
      placeholder: '输入消息内容...',
      modalTitle: '编辑消息内容',
      modalOk: '确定',
      insert: {
        at: '提及',
        link: '链接',
        image: '图片',
        br: '换行'
      },
      fields: {
        userId: '用户 ID',
        tenantId: '租户 ID',
        displayName: '显示名称',
        href: '链接地址',
        title: '标题',
        src: '图片地址'
      },
      confirm: '插入',
      cancel: '取消'
    },
    imageCropper: {
      loading: '加载中...',
      rotateLeft: '向左旋转',
      rotateRight: '向右旋转',
      reset: '重置'
    },
    actuatorMetric: {
      tags: '标签',
      optionalValues: '可选值',
      measurements: '测量值'
    },
    chip: {
      tenantMember: {
        unknown: '未知成员'
      }
    },
    popCard: {
      user: {
        notFound: '未找到用户信息',
        email: '邮箱'
      },
      tenantMember: {
        notFound: '未找到成员信息',
        memberId: '成员 ID',
        email: '邮箱'
      },
      tenantDepartment: {
        notFound: '未找到部门信息',
        description: '描述',
        parentDepartment: '父部门',
        tenantId: '租户ID'
      },
      storageProvider: {
        notFound: '未找到存储提供商信息',
        type: '类型',
        unknownType: '未知类型',
        description: '描述',
        status: '状态',
        enabled: '启用',
        disabled: '禁用',
        localFileSystem: '本地文件系统',
        aliyunOSS: '阿里云OSS',
        tencentCOS: '腾讯云COS'
      },
      mailTemplateType: {
        notFound: '未找到类型信息',
        description: '描述',
        allowMultiple: '允许多模板',
        variables: '变量'
      },
      templateVariablesTag: {
        copySuccess: '已复制 {{variable}} 到剪切板'
      }
    },
    scopedUserDisplay: {
      unknown: '用户不存在'
    },
    permissionTree: {
      scopes: {
        system: '系统',
        x: '跨域',
        tenant: '跨租户',
        iTenant: '本租户'
      },
      modules: {
        permission: '权限',
        role: '角色',
        user: '用户',
        settings: '设置',
        oauth: 'OAuth',
        file: '文件',
        storage: '存储',
        mail: '邮件',
        tenant: '租户',
        message: '消息',
        audit: '审计',
        monitor: '监控',
        announcement: '公告',
        approval: '审批流',
        dict: '字典',
        dashboard: '仪表盘',
        maintenance: '维护',
        department: '部门',
        member: '成员',
        invitation: '邀请',
        profile: '资料',
        personal: '个人'
      }
    }
  },

  api: {
    sessionExpired: '验证信息已过期',
    forbidden: '你无权访问当前资源',
    tooManyRequests: '操作过于频繁，请稍后再试',
    unknownError: '未知错误',
    forbiddenModal: {
      title: '拒绝访问',
      reasonLabel: '原因',
      scopeLabel: '资源域',
      requiredPermissionsLabel: '需要权限',
      noPermissionsRequired: '未声明具体权限',
      messageLabel: '服务端提示'
    },
    banModal: {
      title: '账号已被封禁',
      reasonLabel: '封禁原因',
      noReason: '未提供原因',
      bannedAtLabel: '封禁时间',
      banUntilLabel: '封禁至',
      permanent: '永久封禁'
    },
    disabledModal: {
      title: '账号已被禁用',
      description: '你的账号已被管理员禁用，无法登录。如有疑问请联系管理员。'
    },
    rateLimitModal: {
      title: '操作过于频繁',
      retryAfter: '操作过于频繁，请在 {{seconds}} 秒后重试。'
    }
  },

  enums: {
    unknown: '未知',
    tenantMemberStatus: {
      0: '未激活',
      1: '已离职',
      2: '已辞职',
      3: '审核中',
      4: '正常'
    },
    sessionType: {
      0: '用户',
      1: 'Prometheus'
    },
    tenantStatus: {
      0: '审核中',
      1: '活跃',
      2: '已关闭'
    },
    resourceFileType: {
      0: '用户头像',
      1: '租户图标',
      2: '租户成员头像'
    },
    fileResourceStatus: {
      0: '上传中',
      1: '已提交',
      2: '待清理'
    },
    departmentMemberRoleType: {
      0: '普通成员',
      1: '管理员',
      2: '超级管理员'
    },
    actuatorMetrics: {
      'application.started.time': '应用启动时间',
      'application.ready.time': '应用就绪时间',
      'process.start.time': '进程启动时间',
      'process.uptime': '进程运行时长',
      'system.cpu.usage': '系统 CPU 使用率',
      'system.cpu.count': 'CPU 核心数',
      'system.load.average.1m': '1分钟平均负载',
      'process.cpu.usage': '进程 CPU 使用率',
      'process.cpu.time': '进程 CPU 总时间',
      'disk.total': '磁盘总容量',
      'disk.free': '磁盘可用容量',
      'jvm.info': 'JVM 版本信息',
      'jvm.memory.used': 'JVM 已用内存',
      'jvm.memory.max': 'JVM 最大内存',
      'jvm.memory.committed': 'JVM 已分配内存',
      'jvm.memory.usage.after.gc': 'GC 后内存使用',
      'jvm.buffer.count': '缓冲区数量',
      'jvm.buffer.memory.used': '缓冲区已用内存',
      'jvm.buffer.total.capacity': '缓冲区总容量',
      'jvm.gc.pause': 'GC 暂停时间',
      'jvm.gc.overhead': 'GC 开销',
      'jvm.gc.memory.allocated': 'GC 分配内存',
      'jvm.gc.memory.promoted': 'GC 晋升内存',
      'jvm.gc.live.data.size': 'GC 存活数据大小',
      'jvm.gc.max.data.size': 'GC 最大数据大小',
      'jvm.gc.concurrent.phase.time': 'GC 并发阶段时间',
      'jvm.classes.loaded': '已加载类数量',
      'jvm.classes.loaded.count': '已加载类总数',
      'jvm.classes.unloaded': '已卸载类数量',
      'jvm.compilation.time': 'JIT 编译时间',
      'jvm.threads.live': '活跃线程数',
      'jvm.threads.peak': '峰值线程数',
      'jvm.threads.daemon': '守护线程数',
      'jvm.threads.started': '已启动线程总数',
      'jvm.threads.states': '各状态线程数',
      'executor.active': '活跃线程数',
      'executor.completed': '已完成任务数',
      'executor.pool.core': '核心线程数',
      'executor.pool.max': '最大线程数',
      'executor.pool.size': '当前线程池大小',
      'executor.queue.remaining': '队列剩余容量',
      'executor.queued': '队列中任务数',
      'http.server.requests': 'HTTP 请求统计',
      'http.server.requests.active': '活跃请求数',
      'r2dbc.pool.acquired': '已获取连接数',
      'r2dbc.pool.allocated': '已分配连接数',
      'r2dbc.pool.idle': '空闲连接数',
      'r2dbc.pool.max.allocated': '最大分配连接数',
      'r2dbc.pool.pending': '等待连接数',
      'r2dbc.pool.max.pending': '最大等待连接数',
      'lettuce': 'Redis 连接数',
      'lettuce.active': 'Redis 活跃连接',
      'spring.data.repository.invocations': 'Repository 调用次数',
      'spring.integration.channels': '集成通道数',
      'spring.integration.handlers': '集成处理器数',
      'spring.integration.sources': '集成源数量',
      'spring.security.authorizations': '授权次数',
      'spring.security.authorizations.active': '活跃授权数',
      'spring.security.filterchains': '过滤器链',
      'spring.security.filterchains.active': '活跃过滤器链',
      'spring.security.http.secured.requests': '安全请求统计',
      'spring.security.http.secured.requests.active': '活跃安全请求',
      'logback.events': '日志事件数',
      'process.files.max': '最大文件句柄数',
      'process.files.open': '已打开文件句柄数'
    },

    tenantPermissionType: {
      0: '操作权限',
      1: '菜单权限'
    },

    permissionType: {
      0: '操作权限',
      1: '菜单权限',
      2: '组件权限'
    },

    systemSettingsItemValueType: {
      STRING: '字符串',
      NUMBER: '整数',
      DECIMAL: '小数',
      BOOLEAN: '布尔值',
      STRING_ARRAY: '字符串数组',
      NUMBER_ARRAY: '整数数组',
      DECIMAL_ARRAY: '小数数组',
      BOOLEAN_ARRAY: '布尔值数组'
    },

    storageProviderType: {
      0: '本地文件系统',
      1: '阿里云 OSS',
      2: '腾讯云 COS',
      3: '火山引擎 TOS'
    },

    ruleDistributionType: {
      0: '优先第一个',
      1: '随机'
    },

    oAuthPlatform: {
      0: 'GitHub',
      1: 'Google',
      2: 'QQ'
    },

    oAuthBindingScope: {
      0: '系统',
      1: '租户'
    },

    channelType: {
      1: '邮件',
      2: '飞书'
    },

    messageChannelPreset: {
      '1_empty': '空模板',
      '1_mail163': '163 邮箱',
      '2_empty': '空模板'
    },

    tenantBenefitType: {
      0: '开关',
      1: '数量限制',
      2: '枚举'
    },

    announcementStatus: {
      0: '草稿',
      1: '已发布',
      2: '已下线'
    },

    announcementTarget: {
      0: '仅用户端',
      1: '仅管理端',
      2: '两端都显示'
    },

    gender: {
      0: '未指定',
      1: '男',
      2: '女',
      3: '其他'
    },
    dictTypeStatus: {
      0: '禁用',
      1: '启用'
    },
    dictItemStatus: {
      0: '禁用',
      1: '启用'
    },
    approvalFlowDefinitionStatus: {
      0: '草稿',
      1: '已发布',
      2: '已禁用'
    },
    approvalFlowNodeType: {
      0: '开始',
      1: '结束',
      2: '审批',
      3: '条件',
      4: '抄送',
      5: '分支',
      6: '汇合'
    },
    approvalFlowApproveMode: {
      0: '会签（所有人通过）',
      1: '或签（任一人通过）'
    },
    approvalFlowInstanceStatus: {
      0: '审批中',
      1: '已通过',
      2: '已驳回',
      3: '已取消'
    },
    approvalFlowScope: {
      0: '系统',
      1: '租户'
    },
    approvalFlowTaskStatus: {
      0: '待处理',
      1: '已通过',
      2: '已拒绝',
      3: '已跳过'
    },
    approvalFlowRecordAction: {
      0: '发起',
      1: '通过',
      2: '拒绝',
      3: '系统转办'
    },
    approvalFieldType: {
      text: '单行文本',
      textarea: '多行文本',
      number: '数字',
      boolean: '布尔',
      select: '下拉选择',
      radio: '单选',
      checkbox: '多选',
      date: '日期',
      datetime: '日期时间',
      dict: '字典'
    },
    forbiddenReason: {
      MISSING_PERMISSION: '缺少所需权限',
      SCOPE_MISMATCH: '资源不属于当前范围',
      PROTECTED_RESOURCE: '该资源受保护',
      NOT_TENANT_MEMBER: '你不是该租户的成员',
      ROLE_PROTECTED: '该角色受保护，禁止执行此操作',
      PERMISSION_ESCALATION: '该操作会导致权限提升',
      INVALID_INITIALIZATION_TOKEN: '系统初始化令牌无效'
    },
    forbiddenScope: {
      SYSTEM: '系统',
      TENANT: '租户'
    },
    scopeType: {
      0: '系统',
      1: '租户'
    },
    partyType: {
      0: '用户',
      1: '系统',
      2: '租户'
    },
    audienceType: {
      0: '全体用户',
      1: '租户成员',
      2: '人群分组'
    },
    broadcastCategory: {
      0: '公告'
    }
  },

  entityNames: {
    user: '用户',
    userBanRecord: '用户封禁记录',
    oauthAccount: 'OAuth账号',
    userRole: '用户角色',
    userPermission: '用户权限',
    tenant: '租户',
    tenantMember: '租户成员',
    tenantRole: '租户角色',
    tenantPermission: '租户权限',
    tenantDepartment: '租户部门',
    tenantInvitation: '邀请码',
    tenantMessageChannel: '消息渠道',
    messageChannel: '消息渠道',
    tenantTireType: '套餐类型',
    tenantTireBenefitFeature: '权益项',
    tenantTireBenefitValue: '套餐权益取值',
    fileResource: '文件资源',
    storageProvider: '存储提供商',
    storageProviderRoutingRule: '路由规则',
    mailTemplate: '邮件模板',
    mailTemplateType: '邮件模板类型',
    mailTemplateCategory: '邮件模板分类',
    myTenantMember: '组织成员',
    myTenantRole: '角色',
    auditLog: '审计日志',
    mailSendLog: '邮件发送日志',
    userLoginLog: '用户登录日志',
    sessionMonitor: '会话',
    announcement: '公告',
    broadcast: '广播',
    tenantDictType: '字典类型',
    tenantDictItem: '字典项',
    approvalFlowDefinition: '审批流程定义',
    approvalFlowInstance: '审批申请',
    approvalFlowTask: '审批任务'
  },

  menu: {
    // 公共菜单
    pub: {
      dashboard: '仪表盘',
      profile: '个人中心',
      messageCenter: '消息中心',
      initiableApprovalFlows: '发起审批',
      myApprovalFlows: '我的审批',
      approvalTaskHandle: '审批处理'
    },

    // 租户菜单 (i_tenant)
    myTenant: {
      dashboard: '我的组织',
      members: '成员管理',
      invitations: '邀请码管理',
      roles: '角色管理',
      memberRoles: '成员角色管理',
      departments: '部门管理',
      profile: '组织设置',
      messageChannels: '消息渠道管理',
      personalProfile: '个人资料',
      dictTypes: '字典管理',
      approvalFlowDefinitions: '审批流程管理',
      approvalFlowInstances: '审批管理'
    },

    // 管理员菜单
    admin: {
      users: '用户管理',
      userBanRecords: '用户封禁记录',
      oauthAccounts: 'OAuth账号管理',
      userRoles: '用户角色管理',
      userPermissions: '用户权限管理',
      userRolesRelation: '用户角色分配',
      tenants: '租户管理',
      tenantMembers: '成员管理',
      tenantRoles: '角色管理',
      tenantPermissions: '权限管理',
      tenantMemberRoles: '成员角色管理',
      tenantDepartments: '部门管理',
      tenantInvitations: '邀请码管理',
      tenantMessageChannels: '消息渠道管理',
      systemMessageChannels: '系统消息渠道',
      tenantTireTypes: '套餐类型管理',
      tenantTireBenefitFeatures: '套餐权益项管理',
      tenantTireBenefitValues: '套餐权益管理',
      fileResources: '文件资源管理',
      storageProviders: '存储提供商管理',
      storageProviderRoutingRules: '存储路由规则',
      mailTemplates: '邮件模板管理',
      mailTemplateTypes: '邮件模板类型',
      mailTemplateCategories: '邮件模板分类',
      mailSendLogs: '邮件发送日志',
      auditLogs: '审计日志',
      userLoginLogs: '用户登录日志',
      settings: '系统设置',
      sessions: '在线会话',
      systemMonitor: '系统监控',
      announcements: '公告管理',
      broadcasts: '广播管理',
      tenantDictTypes: '字典类型管理',
      tenantDictItems: '字典项管理',
      approvalFlowDefinitions: '流程定义管理',
      approvalFlowInstances: '用户审批管理',
      tenantApprovalFlowDefinitions: '租户流程定义管理',
      tenantApprovalFlowInstances: '租户审批管理',
      systemDictTypes: '系统字典类型',
      systemDictItems: '系统字典项'
    },

    // 菜单分组
    groups: {
      system_user: '系统用户管理',
      rbac: '用户权限',
      system_storage: '系统储存',
      mail_template: '邮件模板',
      tenant: '租户管理',
      i_tenant: '组织管理',
      approval: '审批管理',
      logs: '日志管理',
      monitor: '系统监控'
    }
  },
};
