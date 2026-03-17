export const zhCN = {
  pages: {
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
        bind: '绑定',
        availablePlatforms: '可绑定的平台'
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
        }
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
        all: '全部'
      }
    },
    tenantManager: {
      title: '租户管理',
      subtitle: '管理系统租户信息',
      filter: {
        status: '状态',
        all: '全部'
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
    tenantRoleManager: {
      title: '租户角色管理',
      subtitle: '管理系统租户角色',
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
      action: {
        addNew: '新增权限'
      },
      filter: {
        type: '权限类型',
        all: '全部'
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
    tenantMemberManager: {
      title: '租户成员管理',
      subtitle: '管理系统租户成员',
      action: {
        addNew: '新增租户成员'
      },
      filter: {
        status: '状态',
        all: '全部'
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
    tenantRolePermissionManager: {
      title: '租户角色权限管理',
      subtitle: '为租户角色分配权限',
      columns: {
        role: '角色',
        description: '描述',
        action: '操作'
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
        fetchRolesFailed: '无法获取角色列表',
        fetchPermissionsFailed: '无法获取权限列表',
        fetchRolePermissionsFailed: '无法获取角色权限',
        assignSuccess: '权限分配成功',
        assignFailed: '权限分配失败'
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
      action: {
        addNew: '新增角色'
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
      }
    },
    myTenantMemberManager: {
      title: '我的组织成员',
      subtitle: '管理当前组织成员信息',
      filter: {
        status: '状态',
        all: '全部'
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
        all: '全部'
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
          tencentCos: '腾讯 OSS'
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
    fileResourceManager: {
      title: '文件资源管理',
      subtitle: '管理系统文件资源',
      filter: {
        type: '类型',
        all: '全部'
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
      messages: {
        downloadFailed: '无法获取文件下载链接'
      }
    },
    userRoleManager: {
      title: '用户角色管理',
      subtitle: '管理系统用户角色',
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
      filter: {
        type: '类型',
        all: '全部'
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
        placeholder: '选择模板类型'
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
      keys: {
        'basic.baseUrl': 'API 基本地址',
        'bootstrap.autoCheckRbacTableData': '自动校验 RBAC 表数据',
        'mail.smtp.username': '用户名',
        'mail.smtp.password': '密码',
        'mail.smtp.host': '主机',
        'mail.smtp.port': '端口',
        'mail.smtp.ssl': '是否启用 SSL',
        'mail.smtp.fromEmail': '发件地址'
      },
      groups: {
        'basic': '基本设置',
        'bootstrap': '启动设置项',
        'mail.smtp': 'SMTP 邮件服务'
      }
    },
    managerContainer: {
      notOrganizationIdentity: '非组织身份',
      current: '当前',
      switching: '切换中...',
      userProfile: '个人中心',
      customTheme: '自定义主题',
      logout: '退出登录',
      menu: '菜单',
      switchSuccess: '已切换到 {{tenantName}}',
      switchFailed: '切换到 {{tenantName}} 失败',
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
  },

  components: {
    dashboard: {
      // DashboardPage
      greeting: {
        earlyMorning: '凌晨好',
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
      refresh: '刷新'
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

    themeColorPicker: {
      title: '自定义主题色',
      description: '选择你喜欢的主题色，将会应用到整个系统界面'
    },

    storageProviderConfig: {
      localFileSystem: '本地文件系统',
      tencentCos: '腾讯云 COS',
      aliyunOss: '阿里云 OSS',
      selectTemplate: '选择配置模板',
      applyTemplate: '应用模板',
      applyTemplateTooltip: '应用模板'
    },

    columns: {
      fileResource: {
        fileInfo: '文件信息',
        fileSize: '大小',
        md5: 'MD5',
        storageProvider: '存储提供商',
        uploader: '上传者',
        fileType: '文件类型',
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
      userPermission: {
        permission: '权限',
        type: '类型',
        description: '描述',
        path: '资源路径'
      },
      userRole: {
        role: '角色',
        description: '描述'
      },
      user: {
        userInfo: '用户信息',
        nickname: '昵称',
        email: '邮箱'
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
      tenantTireType: {
        name: '名称',
        description: '描述'
      },
      oAuthAccount: {
        identifier: '标识',
        platform: '平台',
        systemUser: '系统用户',
        unbound: '未绑定用户',
        userInfo: '用户信息',
        noAvatar: '无头像'
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
      pagination: {
        total: '共 {{total}} 条记录',
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
    popCard: {
      user: {
        notFound: '未找到用户信息',
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
    }
  },

  enums: {
    tenantMemberStatus: {
      0: '未激活',
      1: '已离职',
      2: '已辞职',
      3: '审核中',
      4: '正常'
    },
    tenantStatus: {
      0: '审核中',
      1: '活跃',
      2: '已关闭'
    },
    resourceFileType: {
      0: '用户头像',
      1: '租户图标'
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
      BOOLEAN: '布尔值'
    },

    storageProviderType: {
      0: '本地文件系统',
      1: '阿里云 OSS',
      2: '腾讯云 COS'
    },

    oAuthPlatform: {
      0: 'GitHub',
      1: 'Google',
      2: 'QQ'
    }
  },

  entityNames: {
    user: '用户',
    oauthAccount: 'OAuth账号',
    userRole: '用户角色',
    userPermission: '用户权限',
    tenant: '租户',
    tenantMember: '租户成员',
    tenantRole: '租户角色',
    tenantPermission: '租户权限',
    tenantDepartment: '租户部门',
    tenantInvitation: '邀请码',
    tenantTireType: '套餐类型',
    fileResource: '文件资源',
    storageProvider: '存储提供商',
    mailTemplate: '邮件模板',
    mailTemplateType: '邮件模板类型',
    mailTemplateCategory: '邮件模板分类',
    myTenantMember: '组织成员',
    myTenantRole: '角色'
  },

  menu: {
    // 公共菜单
    pub: {
      dashboard: '仪表盘',
      profile: '个人中心'
    },

    // 租户菜单 (i_tenant)
    myTenant: {
      dashboard: '我的组织',
      members: '成员管理',
      invitations: '邀请码管理',
      roles: '角色管理',
      memberRoles: '成员角色管理',
      departments: '部门管理',
      profile: '组织设置'
    },

    // 管理员菜单
    admin: {
      users: '用户管理',
      oauthAccounts: 'OAuth账号管理',
      userRoles: '用户角色管理',
      userPermissions: '用户权限管理',
      userRolesRelation: '用户角色分配',
      tenants: '租户管理',
      tenantMembers: '成员管理',
      tenantRoles: '角色管理',
      tenantPermissions: '权限管理',
      tenantRolePermissions: '角色权限管理',
      tenantMemberRoles: '成员角色管理',
      tenantDepartments: '部门管理',
      tenantInvitations: '邀请码管理',
      tenantTireTypes: '套餐类型管理',
      fileResources: '文件资源管理',
      storageProviders: '存储提供商管理',
      mailTemplates: '邮件模板管理',
      mailTemplateTypes: '邮件模板类型',
      mailTemplateCategories: '邮件模板分类',
      settings: '系统设置'
    },

    // 菜单分组
    groups: {
      rbac: '用户权限',
      system_storage: '系统储存',
      mail_template: '邮件模板',
      tenant: '租户管理',
      i_tenant: '组织管理'
    }
  },
};
