import type {I18nRules} from "@/i18n/i18n-rules.ts";

export const enUS: I18nRules = {
  pages: {
    systemInitializePage: {
      title: 'System Initialization',
      loading: 'Loading...',
      steps: {
        adminAccount: 'Set Up Admin',
        adminAccountDesc: 'Create admin account',
        mailServer: 'Configure Email',
        mailServerDesc: 'Set up mail server',
        complete: 'Complete',
        completeDesc: 'Start using the system'
      },
      adminAccount: {
        title: 'Set Up Admin Account',
        subtitle: 'Please set up the system administrator account and password',
        form: {
          username: {
            placeholder: 'Admin Username',
            required: 'Please enter admin username',
            pattern: 'Username can only contain letters, numbers, and underscores',
            max: 'Username cannot exceed 64 characters'
          },
          email: {
            placeholder: 'Admin Email',
            required: 'Please enter admin email',
            type: 'Invalid email format',
            max: 'Email cannot exceed 256 characters'
          },
          password: {
            placeholder: 'Password',
            required: 'Please enter password',
            pattern: 'Password must be at least 8 characters with letters and numbers',
            max: 'Password cannot exceed 128 characters'
          },
          confirmPassword: {
            placeholder: 'Confirm Password',
            required: 'Please confirm your password',
            mismatch: 'The two passwords do not match'
          }
        },
        messages: {
          success: 'Admin account created successfully',
          failed: 'Setup failed, please try again'
        }
      },
      mailServer: {
        title: 'Configure Mail Server',
        subtitle: 'Set up system email sending service (optional)',
        form: {
          host: {
            label: 'Server Address',
            placeholder: 'smtp.example.com',
            required: 'Please enter mail server address'
          },
          port: {
            label: 'Port',
            placeholder: '587'
          },
          username: {
            label: 'Username',
            placeholder: 'your-email@example.com',
            required: 'Please enter mail username'
          },
          password: {
            label: 'Password/Auth Code',
            placeholder: 'Mail password or auth code',
            required: 'Please enter mail password or auth code'
          },
          fromEmail: {
            label: 'Sender Email',
            placeholder: 'noreply@example.com',
            required: 'Please enter sender email',
            type: 'Invalid email format'
          },
          fromName: {
            label: 'Sender Name',
            required: 'Please enter sender name'
          }
        },
        messages: {
          success: 'Mail server configured successfully',
          failed: 'Configuration failed, please try again'
        }
      },
      complete: {
        title: 'Initialization Complete',
        subtitle: 'System has been successfully initialized, you can start using it',
        nextSteps: 'Next Steps',
        nextStepsList: {
          login: 'Login with admin account',
          settings: 'Configure system settings and mail templates',
          tenant: 'Create tenant and invite members'
        },
        button: 'Go to Login Page',
        message: 'System initialization complete',
        messages: {
          failed: 'System initialization failed, please try again'
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
          linkText: 'Back to Login'
        }
      },

      // LoginPage
      login: {
        title: 'Welcome Back',
        subtitle: 'Enter your credentials to access the console',
        footerText: "Don't have an account?",
        footerLink: 'Sign up now',
        form: {
          username: {
            placeholder: 'Username or Email',
            required: 'Please enter your username or email',
            pattern: 'Can only contain letters, numbers, underscores, hyphens, or email address'
          },
          password: {
            placeholder: 'Password',
            required: 'Please enter your password'
          },
          remember: 'Remember me',
          forgotPassword: 'Forgot password?',
          agreement: {
            text: 'I have read and agree to',
            privacyPolicy: 'Privacy Policy',
            and: 'and',
            termsOfService: 'Terms of Service',
            required: 'Please read and agree to the Terms of Service and Privacy Policy'
          },
          submit: 'Sign In'
        },
        messages: {
          success: 'Login successful',
          failed: 'Login failed',
          unknownError: 'Login failed: Unknown error'
        },
        divider: 'Or continue with',
        joinedTenant: {
          title: 'Select Organization',
          noTenant: 'You do not belong to any organization',
          skip: 'Skip',
          confirm: 'Confirm Login',
          loginAsNonTenant: 'Login without organization'
        }
      },

      // RegisterPage
      register: {
        title: 'Create Account',
        subtitle: 'Start your new journey',
        footerText: 'Already have an account?',
        footerLink: 'Back to Login',
        form: {
          username: {
            placeholder: 'Username',
            required: 'Please enter a username',
            pattern: 'Username can only contain numbers, letters, and underscores',
            max: 'Username cannot exceed 64 characters'
          },
          email: {
            placeholder: 'Email Address',
            required: 'Please enter your email',
            type: 'Invalid email format',
            max: 'Email cannot exceed 256 characters'
          },
          emailCode: {
            placeholder: 'Verification Code',
            required: 'Please enter the verification code',
            send: 'Send Code',
            retry: 'Retry in {{count}}s'
          },
          password: {
            placeholder: 'Password',
            required: 'Please enter a password',
            pattern: 'Password must be at least 8 characters with letters and numbers',
            max: 'Password cannot exceed 128 characters'
          },
          confirmPassword: {
            placeholder: 'Confirm Password',
            required: 'Please confirm your password',
            mismatch: 'The two passwords do not match'
          },
          agreement: {
            text: 'I have read and agree to',
            privacyPolicy: 'Privacy Policy',
            and: 'and',
            termsOfService: 'Terms of Service',
            required: 'Please read and agree to the Terms of Service and Privacy Policy'
          },
          submit: 'Create Account'
        },
        messages: {
          emailRequired: 'Please enter your email first',
          codeSendSuccess: 'Verification code sent successfully, please check your inbox',
          codeSendFailed: 'Failed to send verification code, please try again later',
          registerSuccess: 'Registration successful!',
          registerFailed: 'Registration failed, please try again later'
        }
      },

      // ForgotPasswordPage
      forgotPassword: {
        title: 'Forgot Password',
        subtitle: 'Reset your password via email',
        footerText: 'Remember your password?',
        footerLink: 'Back to Login',
        form: {
          email: {
            placeholder: 'Email Address',
            required: 'Please enter your email',
            type: 'Invalid email format',
            max: 'Email cannot exceed 256 characters'
          },
          emailCode: {
            placeholder: 'Verification Code',
            required: 'Please enter the verification code',
            send: 'Send Code',
            retry: 'Retry in {{count}}s'
          },
          newPassword: {
            placeholder: 'New Password',
            required: 'Please enter a new password',
            pattern: 'Password must be at least 8 characters with letters and numbers',
            max: 'Password cannot exceed 128 characters'
          },
          confirmPassword: {
            placeholder: 'Confirm New Password',
            required: 'Please confirm the new password',
            mismatch: 'The two passwords do not match'
          },
          submit: 'Reset Password'
        },
        messages: {
          emailRequired: 'Please enter your email first',
          codeSendSuccess: 'Verification code sent successfully, please check your inbox',
          codeSendFailed: 'Failed to send verification code, please try again later',
          resetSuccess: 'Password reset successful!',
          resetFailed: 'Password reset failed, please try again later'
        }
      },

      // OAuth2CodePage
      oauth2: {
        title: 'Third-Party Login Verification',
        subtitle: 'Please click the button below to complete login verification',
        button: {
          processing: 'Verifying...',
          confirm: 'Confirm Login'
        },
        messages: {
          invalidLoginInfo: 'Invalid login information',
          success: 'Login successful',
          failed: 'Login failed',
          unknownError: 'Login failed: Unknown error'
        },
        bind: {
          title: 'Bind Account',
          subtitle: 'This third-party account is not bound yet, please select an action',
          tabs: {
            current: 'Bind to Current Account',
            register: 'Register New Account',
            bind: 'Bind Existing Account'
          },
          currentUser: {
            label: 'Currently Logged In User',
            button: 'Bind to Current Account'
          },
          register: {
            username: {
              placeholder: 'Username',
              required: 'Please enter a username',
              pattern: 'Can only contain letters, numbers, underscores, and hyphens'
            },
            password: {
              placeholder: 'Password',
              required: 'Please enter a password'
            },
            confirmPassword: {
              placeholder: 'Confirm Password',
              required: 'Please confirm your password',
              mismatch: 'The two passwords do not match'
            },
            nickname: {
              placeholder: 'Nickname',
              required: 'Please enter a nickname'
            },
            submit: 'Register and Bind'
          },
          bindExisting: {
            username: {
              placeholder: 'Username',
              required: 'Please enter your username or email',
              pattern: 'Can only contain letters, numbers, underscores, hyphens, or email address'
            },
            password: {
              placeholder: 'Password',
              required: 'Please enter your password'
            },
            submit: 'Bind Account'
          },
          messages: {
            bindSuccess: 'Binding successful',
            bindFailed: 'Binding failed',
            bindRetry: 'Binding failed, please try again',
            registerBindSuccess: 'Registration and binding successful',
            registerFailed: 'Registration failed'
          }
        }
      },
    },

    // User Profile
    userProfile: {
      title: 'Profile',
      subtitle: 'View/Edit your personal profile',
      tabs: {
        basicInfo: 'Basic Info',
        security: 'Security',
        oauth: 'Third-party Accounts'
      },
      basicInfo: {
        username: 'Username',
        nickname: 'Nickname',
        email: 'Email',
        emailHint: 'To change email, please go to Security settings',
        save: 'Save',
        updateSuccess: 'Profile updated successfully',
        updateFailed: 'Failed to update profile'
      },
      security: {
        accountPassword: {
          title: 'Account Password',
          desc: 'Set a strong password to protect your account',
          status: 'Security: High',
          action: 'Change'
        },
        email: {
          title: 'Email',
          statusBound: 'Bound',
          statusUnbound: 'Not bound',
          action: 'Change'
        },
        passwordModal: {
          title: 'Change Password',
          email: 'Email',
          verificationCode: 'Verification Code',
          sendCode: 'Send Code',
          resendCode: 'Retry in {{seconds}}s',
          newPassword: 'New Password',
          confirmPassword: 'Confirm Password',
          passwordHint: 'At least 8 characters with letters and numbers',
          confirm: 'Confirm',
          updateSuccess: 'Password changed successfully!',
          emailRequired: 'Please enter email first',
          codeSendSuccess: 'Verification code sent, please check your inbox',
          codeSendFailed: 'Failed to send verification code'
        },
        emailModal: {
          title: 'Change Email',
          currentEmail: 'Current Email',
          newEmail: 'New Email',
          verificationCode: 'Verification Code',
          sendCode: 'Send Code',
          resendCode: 'Retry in {{seconds}}s',
          confirm: 'Confirm',
          updateSuccess: 'Email changed successfully!',
          newEmailRequired: 'Please enter new email',
          codeSendSuccess: 'Verification code sent, please check your inbox',
          codeSendFailed: 'Failed to send verification code'
        }
      },
      oauth: {
        unbindTitle: 'Unbind Third-party Account',
        unbindConfirm: 'Are you sure you want to unbind {{nickname}}',
        unbindSuccess: 'Account unbound successfully',
        unbindFailed: 'Failed to unbind account',
        bind: 'Bind',
        availablePlatforms: 'Available Platforms'
      },
      card: {
        unbound: 'Not bound',
        registeredAt: 'Registered at'
      },
      avatar: {
        cropTitle: 'Crop Avatar',
        confirmUpload: 'Confirm Upload',
        cancel: 'Cancel',
        uploadSuccess: 'Avatar uploaded successfully',
        uploadFailed: 'Failed to upload avatar',
        invalidType: 'Please upload JPG, PNG or WebP format',
        maxSize: 'Image size cannot exceed 5MB'
      }
    },

    // My Tenant Dashboard
    myTenantDashboard: {
      title: 'My Organization',
      subtitle: 'View your current organization information',
      noTenants: 'You have not joined any organizations yet',
      tenantId: 'Tenant ID',
      basicInfo: 'Basic Information',
      basicInfoDesc: 'View organization detailed information',
      contact: {
        name: 'Contact Name',
        email: 'Contact Email',
        phone: 'Contact Phone',
        address: 'Contact Address',
        notSet: 'Not set',
        title: 'Contact Information'
      },
      subscription: {
        subscribedTime: 'Subscribed Time',
        expiresTime: 'Expires Time',
        daysLeft: '{{days}} days left',
        expired: 'Expired',
        nearExpire: 'Expiring soon'
      },
      member: {
        title: 'Member Info',
        status: 'Member Status',
        joinedAt: 'Joined at'
      },
      owner: {
        title: 'Owner Info',
        username: 'Username',
        email: 'Email',
        nickname: 'Nickname'
      },
      stats: {
        totalMembers: 'Total Members',
        totalRoles: 'Roles',
        totalDepartments: 'Departments',
        totalInvitations: 'Invitations'
      },
      timeInfo: 'Time Information',
      time: {
        createdTime: 'Created Time',
        updatedTime: 'Updated Time'
      },
      joinedTenants: {
        title: 'Joined Organizations',
        subtitle: 'View all organizations you have joined',
        current: 'Current',
        unknown: 'Unknown'
      }
    },

    // My Tenant Settings
    myTenantSettings: {
      title: 'Organization Settings',
      subtitle: 'View and manage tenant organization profile',
      loading: 'Loading...',
      loadFailed: 'Failed to load tenant profile',
      updateSuccess: 'Tenant profile updated successfully',
      updateFailed: 'Failed to update tenant profile',
      basicInfo: 'Basic Information',
      basicInfoDesc: 'Edit your tenant organization profile information',
      form: {
        name: 'Tenant Name',
        contactName: 'Contact Name',
        contactEmail: 'Contact Email',
        contactPhone: 'Contact Phone',
        address: 'Contact Address',
        description: 'Description'
      },
      placeholders: {
        name: 'Enter tenant name',
        contactName: 'Enter contact name',
        contactEmail: 'Enter contact email',
        contactPhone: 'Enter contact phone',
        address: 'Enter contact address',
        description: 'Enter tenant description'
      },
      validation: {
        nameRequired: 'Please enter tenant name',
        nameMax: 'Tenant name cannot exceed 64 characters',
        contactNameRequired: 'Please enter contact name',
        contactNameMax: 'Contact name cannot exceed 64 characters',
        emailRequired: 'Please enter contact email',
        emailInvalid: 'Invalid email format',
        emailMax: 'Email cannot exceed 256 characters',
        phoneRequired: 'Please enter contact phone',
        phoneMax: 'Phone cannot exceed 32 characters',
        addressRequired: 'Please enter contact address',
        addressMax: 'Address cannot exceed 256 characters',
        descriptionMax: 'Description cannot exceed 512 characters'
      },
      buttons: {
        save: 'Save Changes',
        reset: 'Reset'
      },
      avatar: {
        cropTitle: 'Crop Avatar',
        confirmUpload: 'Confirm Upload',
        cancel: 'Cancel',
        uploadSuccess: 'Avatar uploaded successfully',
        uploadFailed: 'Failed to upload avatar',
        invalidType: 'Please upload JPG, PNG or WebP format',
        maxSize: 'Image size cannot exceed 5MB'
      }
    },

    // Manager Pages
    userManager: {
      title: 'User Management',
      subtitle: 'Manage system users',
      modal: {
        username: {
          label: 'Username',
          required: 'Please enter username',
          maxLength: 'Username cannot exceed 64 characters'
        },
        nickname: {
          label: 'Nickname',
          required: 'Please enter nickname',
          maxLength: 'Nickname cannot exceed 32 characters'
        },
        email: {
          label: 'Email',
          maxLength: 'Email cannot exceed 256 characters'
        },
        password: {
          label: 'Password',
          required: 'Please enter password'
        }
      },
      filter: {
        username: 'Username',
        usernamePlaceholder: 'Filter by username',
        email: 'Email',
        emailPlaceholder: 'Filter by email',
        nickname: 'Nickname',
        nicknamePlaceholder: 'Filter by nickname',
        id: 'User ID',
        idPlaceholder: 'Enter user ID',
      }
    },
    oauthAccountManager: {
      title: 'OAuth Account Management',
      subtitle: 'Manage system OAuth account bindings',
      modal: {
        userId: {
          label: 'System User'
        },
        platform: {
          label: 'Platform',
          required: 'Please select platform',
          placeholder: 'Select platform'
        },
        identifier: {
          label: 'Platform Identifier',
          required: 'Please enter platform identifier',
          maxLength: 'Platform identifier cannot exceed 256 characters',
          placeholder: 'Please enter platform identifier',
        },
        nickname: {
          label: 'Nickname',
          placeholder: 'User nickname',
          maxLength: 'Nickname cannot exceed 128 characters'
        },
        avatar: {
          label: 'Avatar URL',
          placeholder: 'Avatar link',
          maxLength: 'Avatar URL cannot exceed 256 characters'
        }
      },
      filter: {
        platform: 'Platform',
        all: 'All',
        id: 'Account ID',
        idPlaceholder: 'Enter account ID',
      }
    },
    tenantManager: {
      title: 'Tenant Management',
      subtitle: 'Manage system tenant information',
      filter: {
        status: 'Status',
        all: 'All',
        id: 'Tenant ID',
        idPlaceholder: 'Enter tenant ID',
      },
      modal: {
        name: {
          label: 'Tenant Name',
          required: 'Please enter tenant name',
          maxLength: 'Tenant name cannot exceed 64 characters',
          placeholder: 'Tenant name'
        },
        ownerUserId: {
          label: 'Owner User',
          required: 'Please select owner user'
        },
        tireTypeId: {
          label: 'Tier Type',
          required: 'Please select tier type',
          placeholder: 'Select tier type'
        },
        status: {
          label: 'Status',
          placeholder: 'Select status'
        },
        subscribedTime: {
          label: 'Subscribed Time',
          required: 'Please select subscribed time',
          placeholder: 'Select subscribed time'
        },
        expiresTime: {
          label: 'Expires Time',
          required: 'Please select expires time',
          placeholder: 'Select expires time'
        },
        contactName: {
          label: 'Contact Name',
          required: 'Please enter contact name',
          maxLength: 'Contact name cannot exceed 64 characters',
          placeholder: 'Contact name'
        },
        contactEmail: {
          label: 'Contact Email',
          required: 'Please enter contact email',
          maxLength: 'Email cannot exceed 256 characters',
          placeholder: 'Contact email'
        },
        contactPhone: {
          label: 'Contact Phone',
          required: 'Please enter contact phone',
          maxLength: 'Phone cannot exceed 32 characters',
          placeholder: 'Contact phone'
        },
        address: {
          label: 'Address',
          required: 'Please enter address',
          placeholder: 'Address'
        },
        config: {
          label: 'Config (JSON)',
          placeholder: 'Enter JSON format config'
        },
        description: {
          label: 'Description',
          maxLength: 'Description cannot exceed 512 characters',
          placeholder: 'Tenant description'
        }
      }
    },
    tenantTireTypeManager: {
      title: 'Tier Type Management',
      subtitle: 'Manage system tier types',
      filter: {
        id: 'Tier ID',
        idPlaceholder: 'Enter tier ID',
        name: 'Name',
        description: 'Description'
      },
      modal: {
        name: {
          label: 'Name',
          required: 'Please enter name',
          maxLength: 'Name cannot exceed 32 characters',
          placeholder: 'Tier type name'
        },
        description: {
          label: 'Description',
          maxLength: 'Description cannot exceed 512 characters',
          placeholder: 'Tier type description'
        }
      }
    },
    tenantTireBenefitFeatureManager: {
      title: 'Tier Benefit Features',
      subtitle: 'Manage tier benefit feature definitions',
      filter: {
        featureKey: 'Feature Key',
        name: 'Name',
        description: 'Description',
        featureType: 'Feature Type',
        featureTypePlaceholder: 'Select feature type'
      },
      modal: {
        featureKey: {
          label: 'Feature Key',
          required: 'Please enter feature key',
          maxLength: 'Feature key cannot exceed 64 characters',
          placeholder: 'e.g. invitation.max_count'
        },
        name: {
          label: 'Name',
          required: 'Please enter name',
          maxLength: 'Name cannot exceed 128 characters',
          placeholder: 'Feature name'
        },
        featureType: {
          label: 'Feature Type',
          required: 'Please select feature type',
          placeholder: 'Select feature type'
        },
        defaultValue: {
          label: 'Default Value',
          placeholder: 'Default value',
          placeholderBoolean: 'Select default',
          placeholderLimit: 'Enter default number',
          placeholderEnum: 'Type option and press Enter',
          requiredEnum: 'Please enter at least one option'
        },
        description: {
          label: 'Description',
          maxLength: 'Description cannot exceed 512 characters',
          placeholder: 'Feature description'
        }
      }
    },
    tenantTireBenefitValueManager: {
      title: 'Tier Benefit Values',
      subtitle: 'Configure benefit values for each tier',
      switch: {
        planBenefits: 'Plan Benefits',
        crossOverview: 'Benefit Overview',
        management: 'Detailed Management',
      },
      overview: {
        title: 'Tier Benefit Overview',
        subtitle: 'View all benefit values configured for each tier type',
        selectTireType: 'Select Tier Type',
        selectTireTypePlaceholder: 'Please select a tier type',
        name: 'Name',
        featureKey: 'Feature Key',
        featureType: 'Feature Type',
        defaultValue: 'Default Value',
        currentValue: 'Current Value',
        default: 'Default',
        save: 'Save',
        cancel: 'Cancel',
      },
      crossOverview: {
        title: 'Benefit Overview',
        subtitle: 'Compare benefit values across all plan types',
        name: 'Benefit Name',
        description: 'Description',
        featureKey: 'Key',
        featureType: 'Type',
        readOnly: 'Read Only',
        edit: 'Edit',
        showDefaultValue: 'Show Default',
      },
      filter: {
        tireTypeId: 'Tier Type',
        tireTypeIdPlaceholder: 'Select tier type',
        featureId: 'Feature',
        featureIdPlaceholder: 'Select feature'
      },
      modal: {
        tireTypeId: {
          label: 'Tier Type',
          required: 'Please select tier type',
          placeholder: 'Select tier type'
        },
        featureId: {
          label: 'Feature',
          required: 'Please select feature',
          placeholder: 'Select feature'
        },
        featureValue: {
          label: 'Feature Value',
          required: 'Please enter feature value',
          placeholder: 'Value for this feature',
          placeholderEnum: 'Select enum value',
          booleanTrue: 'Enabled',
          booleanFalse: 'Disabled'
        }
      }
    },
    tenantRoleManager: {
      title: 'Tenant Role Management',
      subtitle: 'Manage system tenant roles',
      filter: {
        id: 'Role ID',
        idPlaceholder: 'Enter role ID',
      },
      action: {
        addNew: 'Add New Tenant Role',
        assignPermission: 'Assign Permission',
      },
      modal: {
        name: {
          label: 'Role Name',
          required: 'Please enter role name',
          placeholder: 'Enter role name'
        },
        parentId: {
          label: 'Parent Role'
        },
        description: {
          label: 'Description',
          placeholder: 'Enter description (optional)'
        }
      },
      permissionModal: {
        title: 'Assign permissions to role "{{name}}"',
        titles: {
          available: 'Available Permissions',
          assigned: 'Assigned Permissions'
        }
      },
      messages: {
        fetchPermissionsFailed: 'Failed to fetch permission list',
        fetchRolePermissionsFailed: 'Failed to fetch role permissions',
        assignSuccess: 'Permissions assigned successfully',
        assignFailed: 'Failed to assign permissions'
      }
    },
    tenantPermissionManager: {
      title: 'Tenant Permission Management',
      subtitle: 'Manage system tenant permissions',
      action: {
        addNew: 'Add New Permission'
      },
      filter: {
        type: 'Permission Type',
        all: 'All',
        id: 'Permission ID',
        idPlaceholder: 'Enter permission ID',
      },
      modal: {
        name: {
          label: 'Permission Name',
          required: 'Please enter permission name',
          placeholder: 'Enter permission name'
        },
        type: {
          label: 'Permission Type',
          required: 'Please select permission type',
          placeholder: 'Select permission type'
        },
        path: {
          label: 'Path',
          placeholder: 'Enter path (optional)'
        },
        description: {
          label: 'Description',
          placeholder: 'Enter description (optional)'
        }
      }
    },
    tenantMemberManager: {
      title: 'Tenant Member Management',
      subtitle: 'Manage system tenant members',
      action: {
        addNew: 'Add New Tenant Member'
      },
      filter: {
        status: 'Status',
        all: 'All',
        id: 'Member ID',
        idPlaceholder: 'Enter member ID',
        memberUserId: 'User ID',
        memberUserIdPlaceholder: 'Enter user ID'
      },
      modal: {
        memberUserId: {
          label: 'Member User (only valid when creating)',
          required: 'Please select member user'
        },
        status: {
          label: 'Status',
          required: 'Please select status',
          placeholder: 'Select status'
        }
      }
    },
    tenantInvitationManager: {
      title: 'Invitation Code Management',
      subtitle: 'Manage system tenant invitation codes',
      filter: {
        id: 'Invitation ID',
        idPlaceholder: 'Enter invitation ID',
      },
      addInvitationCode: 'Add Invitation Code',
      copyInvitationLink: 'Copy Invitation Link',
      copySuccess: 'Invitation link copied to clipboard',
      copyFailed: 'Copy failed, please copy manually',
      action: {
        addNew: 'Add New Invitation Code'
      },
      modal: {
        creatorMemberId: {
          label: 'Creator Member',
          required: 'Please select creator member',
          placeholder: 'Select creator member'
        },
        departmentId: {
          label: 'Department (optional)',
          placeholder: 'Select department (optional)'
        },
        invitationCount: {
          label: 'Invitation Count',
          required: 'Please enter invitation count',
          placeholder: 'Enter invitation count'
        },
        expiresTime: {
          label: 'Expires Time (optional)',
          placeholder: 'Select expires time (optional)'
        },
        requiresReviewing: {
          label: 'Requires Reviewing'
        }
      },
      messages: {
        copySuccess: 'Invitation link copied to clipboard',
        copyFailed: 'Copy failed, please copy manually'
      }
    },
    tenantDepartmentManager: {
      title: 'Department Management',
      subtitle: 'Manage system tenant departments',
      action: {
        addNew: 'Add New Department',
        addDepartment: 'Add Department',
        edit: 'Edit',
        delete: 'Delete',
        addMember: 'Add Member',
        editRole: 'Edit Role',
        remove: 'Remove'
      },
      card: {
        departmentList: 'Department List',
        members: 'Department Members'
      },
      info: {
        description: 'Description',
        parentDepartment: 'Parent Department',
        createdTime: 'Created Time'
      },
      empty: {
        noDepartments: 'No departments yet',
        selectDepartment: 'Please select a department from the left'
      },
      modal: {
        add: {
          title: 'Add Department'
        },
        edit: {
          title: 'Edit Department'
        },
        delete: {
          title: 'Delete Department',
          content: 'Are you sure you want to delete department "{{name}}"?'
        },
        removeMember: {
          title: 'Remove Member',
          content: 'Are you sure you want to remove member "{{name}}" from the department?'
        },
        name: {
          label: 'Department Name',
          required: 'Please enter department name',
          placeholder: 'Enter department name'
        },
        parentId: {
          label: 'Parent Department'
        },
        description: {
          label: 'Description',
          placeholder: 'Enter description (optional)'
        }
      },
      memberSelectorModal: {
        title: 'Add members to department "{{name}}"'
      },
      roleEditModal: {
        title: 'Edit Member Role - {{name}}',
        description: 'Please select the role for this member in the department:'
      },
      columns: {
        action: 'Action'
      },
      messages: {
        fetchDepartmentsFailed: 'Failed to fetch department list',
        fetchMembersFailed: 'Failed to fetch department members',
        deleteSuccess: 'Delete successful',
        deleteFailed: 'Delete failed',
        updateSuccess: 'Update successful',
        createSuccess: 'Create successful',
        addMembersSuccess: 'Successfully added {{count}} members',
        addMembersFailed: 'Failed to add members',
        removeMemberSuccess: 'Member removed successfully',
        removeMemberFailed: 'Failed to remove member',
        updateRoleSuccess: 'Role updated successfully',
        updateRoleFailed: 'Failed to update role'
      }
    },
    tenantRolePermissionManager: {
      title: 'Tenant Role Permission Management',
      subtitle: 'Assign permissions to tenant roles',
      columns: {
        role: 'Role',
        description: 'Description',
        action: 'Action'
      },
      action: {
        assignPermission: 'Assign Permissions'
      },
      permissionModal: {
        title: 'Assign permissions to role "{{name}}"',
        titles: {
          available: 'Available Permissions',
          assigned: 'Assigned Permissions'
        }
      },
      messages: {
        fetchRolesFailed: 'Failed to fetch role list',
        fetchPermissionsFailed: 'Failed to fetch permission list',
        fetchRolePermissionsFailed: 'Failed to fetch role permissions',
        assignSuccess: 'Permissions assigned successfully',
        assignFailed: 'Failed to assign permissions'
      }
    },
    tenantMemberRoleManager: {
      title: 'Tenant Member Role Management',
      subtitle: 'Assign roles to tenant members',
      columns: {
        member: 'Member',
        username: 'Username',
        email: 'Email',
        status: 'Status',
        action: 'Action'
      },
      action: {
        assignRole: 'Assign Roles'
      },
      modal: {
        title: 'Assign roles to member "{{nickname}}"',
        titles: {
          unassigned: 'Unassigned Roles',
          assigned: 'Assigned Roles'
        }
      },
      messages: {
        fetchMembersFailed: 'Failed to fetch member list',
        fetchRolesFailed: 'Failed to fetch role list',
        fetchMemberRolesFailed: 'Failed to fetch member roles',
        assignSuccess: 'Roles assigned successfully',
        assignFailed: 'Failed to assign roles'
      }
    },
    myTenantRoleManager: {
      title: 'My Role Management',
      subtitle: 'Manage roles for current organization',
      filter: {
        id: 'Role ID',
        idPlaceholder: 'Enter role ID',
      },
      action: {
        addNew: 'Add New Role'
      },
      modal: {
        name: {
          label: 'Role Name',
          required: 'Please enter role name',
          placeholder: 'Enter role name'
        },
        parentId: {
          label: 'Parent Role'
        },
        description: {
          label: 'Description',
          placeholder: 'Enter description (optional)'
        }
      }
    },
    myTenantMemberManager: {
      title: 'My Organization Members',
      subtitle: 'Manage members in current organization',
      filter: {
        status: 'Status',
        all: 'All',
        id: 'Member ID',
        idPlaceholder: 'Enter member ID',
        memberUserId: 'User ID',
        memberUserIdPlaceholder: 'Enter user ID'
      },
      modal: {
        status: {
          label: 'Status',
          required: 'Please select status',
          placeholder: 'Select status'
        }
      }
    },
    myTenantMemberRoleManager: {
      title: 'My Member Role Management',
      subtitle: 'Assign roles to organization members',
      columns: {
        member: 'Member',
        username: 'Username',
        email: 'Email',
        status: 'Status',
        action: 'Action'
      },
      action: {
        assignRole: 'Assign Role'
      },
      modal: {
        title: 'Assign roles to member "{{nickname}}"',
        titles: {
          unassigned: 'Unassigned Roles',
          assigned: 'Assigned Roles'
        }
      },
      messages: {
        fetchMembersFailed: 'Failed to fetch member list',
        fetchRolesFailed: 'Failed to fetch role list',
        fetchMemberRolesFailed: 'Failed to fetch member roles',
        assignSuccess: 'Roles assigned successfully',
        assignFailed: 'Failed to assign roles'
      }
    },
    myTenantInvitationManager: {
      title: 'My Organization Invitations',
      subtitle: 'Manage invitation codes for current organization',
      filter: {
        id: 'Invitation ID',
        idPlaceholder: 'Enter invitation ID',
      },
      action: {
        addNew: 'Add New Invitation',
        copyLink: 'Copy Invitation Link',
        copyLinkTooltip: 'Copy invitation link'
      },
      modal: {
        departmentId: {
          label: 'Department (Optional)',
          placeholder: 'Select department (optional)'
        },
        requiresReviewing: {
          label: 'Requires Review'
        },
        invitationCount: {
          label: 'Invitation Count',
          required: 'Please enter invitation count',
          placeholder: 'Enter invitation count'
        },
        expiresTime: {
          label: 'Expiration Time (Optional)',
          placeholder: 'Select expiration time (optional)'
        }
      },
      messages: {
        copySuccess: 'Invitation link copied to clipboard',
        copyFailed: 'Copy failed, please copy manually'
      }
    },
    myTenantDepartmentManager: {
      title: 'My Department Management',
      subtitle: 'Manage departments and members in current organization',
      columns: {
        action: 'Action'
      },
      action: {
        addNew: 'Add New Department',
        addMember: 'Add Member',
        edit: 'Edit',
        delete: 'Delete',
        editRole: 'Edit Role',
        remove: 'Remove'
      },
      card: {
        departmentList: 'Department List',
        noDepartment: 'No departments',
        addDepartment: 'Add Department',
        description: 'Description',
        parentDepartment: 'Parent Department',
        createdTime: 'Created Time',
        departmentMembers: 'Department Members',
        selectDepartment: 'Please select a department from the left'
      },
      modal: {
        addTitle: 'Add New Department',
        editTitle: 'Edit Department',
        deleteTitle: 'Delete Department',
        deleteContent: 'Are you sure you want to delete department "{{name}}"?',
        removeMemberTitle: 'Remove Member',
        removeMemberContent: 'Are you sure you want to remove member "{{name}}" from the department?',
        addMemberTitle: 'Add members to department "{{name}}"',
        editRoleTitle: 'Edit Member Role - {{name}}',
        roleDescription: 'Please select the member\'s role in the department:',
        name: {
          label: 'Department Name',
          required: 'Please enter department name',
          placeholder: 'Enter department name'
        },
        parentId: {
          label: 'Parent Department'
        },
        description: {
          label: 'Description',
          placeholder: 'Enter description (optional)'
        }
      },
      messages: {
        fetchDepartmentsFailed: 'Failed to fetch department list',
        fetchMembersFailed: 'Failed to fetch department members',
        deleteSuccess: 'Deleted successfully',
        deleteFailed: 'Failed to delete',
        updateSuccess: 'Updated successfully',
        createSuccess: 'Created successfully',
        addMembersSuccess: 'Successfully added {{count}} members',
        addMembersFailed: 'Failed to add members',
        removeMemberSuccess: 'Member removed successfully',
        removeMemberFailed: 'Failed to remove member',
        updateRoleSuccess: 'Role updated successfully',
        updateRoleFailed: 'Failed to update role'
      }
    },
    storageProviderManager: {
      title: 'Storage Provider Management',
      subtitle: 'Manage system storage providers',
      filter: {
        type: 'Type',
        all: 'All',
        id: 'Storage ID',
        idPlaceholder: 'Enter storage ID',
      },
      columns: {
        active: 'Active Status'
      },
      modal: {
        name: {
          label: 'Name',
          required: 'Please enter name',
          maxLength: 'Name cannot exceed 64 characters',
          placeholder: 'Storage provider name'
        },
        type: {
          label: 'Type',
          required: 'Please select type',
          placeholder: 'Select storage type',
          localFileSystem: 'Local File System',
          aliyunOss: 'Aliyun OSS',
          tencentCos: 'Tencent COS'
        },
        description: {
          label: 'Description',
          maxLength: 'Description cannot exceed 512 characters',
          placeholder: 'Storage provider description'
        },
        baseUrl: {
          label: 'Base URL',
          required: 'Please enter base URL',
          maxLength: 'Base URL cannot exceed 256 characters',
          placeholder: 'Access base URL'
        },
        properties: {
          label: 'Properties (JSON)',
          required: 'Please enter properties',
          placeholder: 'Enter JSON format properties...'
        }
      },
      messages: {
        statusUpdateSuccess: 'Status updated successfully',
        statusUpdateFailed: 'Failed to update status'
      }
    },
    fileResourceManager: {
      title: 'File Resource Management',
      subtitle: 'Manage system file resources',
      filter: {
        type: 'Type',
        all: 'All',
        id: 'File ID',
        idPlaceholder: 'Enter file ID',
      },
      modal: {
        userId: {
          label: 'User',
          required: 'Please select user'
        },
        type: {
          label: 'File Type',
          required: 'Please select file type',
          placeholder: 'Select file type'
        },
        storageProviderId: {
          label: 'Storage Provider',
          required: 'Please select storage provider'
        },
        fileName: {
          label: 'File Name',
          required: 'Please enter file name',
          maxLength: 'File name cannot exceed 256 characters',
          placeholder: 'File name'
        },
        fileExtension: {
          label: 'Extension',
          required: 'Please enter extension',
          maxLength: 'Extension cannot exceed 64 characters',
          placeholder: 'File extension'
        },
        md5: {
          label: 'MD5',
          required: 'Please enter MD5',
          maxLength: 'MD5 cannot exceed 32 characters',
          placeholder: 'File MD5 value'
        },
        fileSize: {
          label: 'File Size (Bytes)',
          required: 'Please enter file size',
          placeholder: 'File size (bytes)'
        },
        objectKey: {
          label: 'Object Key',
          required: 'Please enter object key',
          maxLength: 'Object key cannot exceed 256 characters',
          placeholder: 'Storage object key'
        }
      },
      messages: {
        downloadFailed: 'Failed to get file download link'
      }
    },
    userRoleManager: {
      title: 'User Role Management',
      subtitle: 'Manage system user roles',
      filter: {
        id: 'Role ID',
        idPlaceholder: 'Enter role ID',
      },
      modal: {
        name: {
          label: 'Role Name',
          required: 'Please enter role name',
          maxLength: 'Role name cannot exceed 128 characters'
        },
        description: {
          label: 'Role Description',
          maxLength: 'Role description cannot exceed 512 characters',
          placeholder: 'Enter role description...'
        }
      },
      action: {
        assignPermission: 'Assign Permissions'
      },
      permissionModal: {
        title: 'Assign permissions to role "{{name}}"',
        titles: {
          available: 'Available Permissions',
          assigned: 'Assigned Permissions'
        }
      },
      messages: {
        fetchPermissionsFailed: 'Failed to fetch permission list',
        fetchRolePermissionsFailed: 'Failed to fetch role permissions',
        assignSuccess: 'Permissions assigned successfully',
        assignFailed: 'Failed to assign permissions'
      }
    },
    userRoleRelationManager: {
      title: 'User Role Relation Management',
      subtitle: 'Manage system user role relations',
      columns: {
        user: 'User',
        username: 'Username',
        email: 'Email',
        action: 'Action'
      },
      action: {
        assignRole: 'Assign Roles'
      },
      modal: {
        title: 'Assign roles to user "{{name}}"',
        titles: {
          available: 'Available Roles',
          assigned: 'Assigned Roles'
        }
      },
      messages: {
        fetchUsersFailed: 'Failed to fetch user list',
        fetchRolesFailed: 'Failed to fetch role list',
        fetchUserRolesFailed: 'Failed to fetch user roles',
        assignSuccess: 'Roles assigned successfully',
        assignFailed: 'Failed to assign roles'
      }
    },
    userPermissionManager: {
      title: 'User Permission Management',
      subtitle: 'Manage system user permissions',
      filter: {
        type: 'Type',
        all: 'All',
        id: 'Permission ID',
        idPlaceholder: 'Enter permission ID',
      },
      modal: {
        name: {
          label: 'Permission Name',
          required: 'Please enter permission name',
          maxLength: 'Permission name cannot exceed 256 characters'
        },
        type: {
          label: 'Permission Type',
          required: 'Please select permission type',
          placeholder: 'Select permission type'
        },
        path: {
          label: 'Resource Path',
          maxLength: 'Resource path cannot exceed 256 characters'
        },
        description: {
          label: 'Permission Description',
          maxLength: 'Permission description cannot exceed 512 characters',
          placeholder: 'Enter permission description...'
        }
      }
    },
    mailTemplateTypeManager: {
      title: 'Mail Template Type Management',
      subtitle: 'Manage system mail template types',
      filter: {
        id: 'Type ID',
        idPlaceholder: 'Enter type ID',
      },
      modal: {
        name: {
          label: 'Name',
          required: 'Please enter name',
          maxLength: 'Name cannot exceed 128 characters',
          placeholder: 'Type name'
        },
        categoryId: {
          label: 'Category',
          required: 'Please select category',
          placeholder: 'Select category'
        },
        description: {
          label: 'Description',
          maxLength: 'Description cannot exceed 512 characters',
          placeholder: 'Type description'
        },
        variables: {
          label: 'Variables (JSON format)',
          required: 'Please enter variables',
          placeholder: '{"username": "Username", "code": "Verification code"}'
        },
        allowMultiple: {
          label: 'Allow Multiple'
        }
      }
    },
    mailTemplateManager: {
      title: 'Mail Template Management',
      subtitle: 'Manage system mail templates',
      enabledStatus: 'Enabled Status',
      filter: {
        templateType: 'Template Type',
        placeholder: 'Select template type',
        id: 'Template ID',
        idPlaceholder: 'Enter template ID',
      },
      modal: {
        name: {
          label: 'Name',
          required: 'Please enter name',
          maxLength: 'Name cannot exceed 128 characters',
          placeholder: 'Template name'
        },
        typeId: {
          label: 'Type',
          required: 'Please select type',
          placeholder: 'Select type'
        },
        title: {
          label: 'Title',
          required: 'Please enter title',
          maxLength: 'Title cannot exceed 512 characters',
          placeholder: 'Email title'
        },
        description: {
          label: 'Description',
          maxLength: 'Description cannot exceed 512 characters',
          placeholder: 'Template description'
        },
        variables: {
          label: 'Available Variables'
        },
        content: {
          label: 'Content',
          required: 'Please enter content',
          placeholder: 'Email template content, supports variable replacement'
        },
        active: {
          label: 'Active Status'
        }
      },
      messages: {
        copySuccess: 'Copied {{variable}} to clipboard',
        statusUpdateSuccess: 'Status updated successfully',
        statusUpdateFailed: 'Failed to update status'
      }
    },
    mailTemplateCategoryManager: {
      title: 'Mail Template Category Management',
      subtitle: 'Manage system mail template categories',
      filter: {
        id: 'Category ID',
        idPlaceholder: 'Enter category ID',
      },
      modal: {
        name: {
          label: 'Name',
          required: 'Please enter name',
          maxLength: 'Name cannot exceed 128 characters',
          placeholder: 'Category name'
        },
        description: {
          label: 'Description',
          maxLength: 'Description cannot exceed 512 characters',
          placeholder: 'Category description'
        }
      }
    },
    actuatorDashboard: {
      title: 'Dashboard',
      subtitle: 'View system basic information here'
    },
    auditLogManager: {
      title: 'Audit Logs',
      subtitle: 'View system operation audit logs',
      filter: {
        action: 'Action Type',
        all: 'All',
        userId: 'User ID',
        userIdPlaceholder: 'Enter user ID',
        username: 'Username',
        usernamePlaceholder: 'Enter username',
        path: 'Path',
        pathPlaceholder: 'Enter path',
        remoteIp: 'IP Address',
        remoteIpPlaceholder: 'Enter IP',
        id: 'Log ID',
        idPlaceholder: 'Enter log ID',
      },
      actionType: {
        unknown: 'Unknown',
        create: 'Create',
        read: 'Read',
        update: 'Update',
        delete: 'Delete'
      }
    },
    mailSendLogManager: {
      title: 'Mail Send Logs',
      subtitle: 'View system mail send records',
      filter: {
        keyword: 'Keyword',
        keywordPlaceholder: 'Search recipient or subject',
        toEmail: 'Recipient',
        toEmailPlaceholder: 'Enter recipient email',
        status: 'Status',
        all: 'All',
        success: 'Success',
        failed: 'Failed',
        userId: 'User ID',
        userIdPlaceholder: 'Enter user ID',
        id: 'Log ID',
        idPlaceholder: 'Enter log ID',
      }
    },
    userLoginLogManager: {
      title: 'User Login Logs',
      subtitle: 'View user login records',
      filter: {
        userId: 'User ID',
        userIdPlaceholder: 'Enter user ID',
        username: 'Username',
        usernamePlaceholder: 'Enter username',
        loginMethod: 'Login Method',
        status: 'Status',
        all: 'All',
        success: 'Success',
        failed: 'Failed',
        remoteIp: 'IP Address',
        remoteIpPlaceholder: 'Enter IP address',
        id: 'Log ID',
        idPlaceholder: 'Enter log ID',
      },
      loginMethod: {
        password: 'Password Login',
        oauth2: 'OAuth2 Login'
      }
    },
    systemSettingsManager: {
      title: 'System Settings',
      subtitle: 'Manage system configuration',
      fetchFailed: 'Failed to fetch system settings',
      saveSuccess: 'System settings saved',
      saveFailed: 'Failed to save system settings',
      importEmpty: 'Imported configuration is empty',
      importFailed: 'Failed to import configuration',
      saveSettings: 'Save Settings',
      operation: 'Operation',
      importConfig: 'Import Config',
      exportConfig: 'Export Config',
      maintenanceMode: 'Maintenance Mode',
      switchMaintenanceModeFailed: 'Failed to switch maintenance mode',
      maintenanceConfirmEnableTitle: 'Enable Maintenance Mode',
      maintenanceConfirmEnableContent: 'Enabling maintenance mode will prevent regular users from accessing the system. Are you sure you want to continue?',
      maintenanceConfirmDisableTitle: 'Disable Maintenance Mode',
      maintenanceConfirmDisableContent: 'Disabling maintenance mode will restore access for all users. Are you sure you want to continue?',
      maintenanceConfirmOk: 'Confirm',
      maintenanceConfirmCancel: 'Cancel',
      testSendEmail: {
        button: 'Send Test Email',
        modalTitle: 'Send Test Email',
        emailLabel: 'Recipient Email',
        emailPlaceholder: 'Enter recipient email',
        emailRequired: 'Recipient email is required',
        emailInvalid: 'Invalid email format',
        confirm: 'Send',
        cancel: 'Cancel',
        sendSuccess: 'Test email sent successfully',
        sendFailed: 'Failed to send test email',
      },
      keys: {
        'basic.baseUrl': 'API Base URL',
        'basic.waterMark.enabled': 'Show Watermark',
        'basic.waterMark.type': 'Watermark Type',
        'basic.waterMark.customValue': 'Custom Watermark Content',
        'basic.waterMark.fontColor': 'Watermark Font Color',
        'bootstrap.autoCheckRbacTableData': 'Auto Check RBAC Table Data',
        'mail.smtp.username': 'Username',
        'mail.smtp.password': 'Password',
        'mail.smtp.host': 'Host',
        'mail.smtp.port': 'Port',
        'mail.smtp.ssl': 'Enable SSL',
        'mail.smtp.fromEmail': 'From Email',
        'messageChannel.lark.appId': 'App ID',
        'messageChannel.lark.appSecret': 'App Secret',
        'messageChannel.lark.baseUrl': 'Open Platform URL',
        'security.api.encrypt.enabled': 'Enabled',
        'security.api.encrypt.scope': 'Scope',
        'security.api.encrypt.securityLevel': 'Security Level',
      },
      groups: {
        'basic': 'Basic Settings',
        'basic.waterMark': 'Watermark Settings',
        'bootstrap': 'Bootstrap Settings',
        'mail.smtp': 'SMTP Mail Service',
        'messageChannel.lark': 'Lark',
        'security.api.encrypt': 'Api Security',
      },
      tabs: {
        'basic': 'Basic',
        'bootstrap': 'Bootstrap',
        'mail': 'Mail',
        'messageChannel': 'Message Channel',
        'security': 'Security',
      },
      enums: {
        'security.api.encrypt.scope': {
          'ALL': 'All api',
          'ALL_ANNOTATED': 'All annotated api',
          'BY_ANNOTATED_LEVEL': 'Only api matches the security level',
        },
        'basic.waterMark.type': {
          'SYSTEM_NAME': 'System Name',
          'USER_NAME': 'User Name',
          'CUSTOM': 'Custom',
        }
      }
    },
    announcementManager: {
      title: 'Announcement Management',
      subtitle: 'Manage system announcements',
      columns: {
        title: 'Title',
        content: 'Content',
        status: 'Status',
        target: 'Display Target',
        priority: 'Priority',
        createdTime: 'Created Time',
      },
      filter: {
        id: 'Announcement ID',
        idPlaceholder: 'Enter announcement ID',
        status: 'Status',
        target: 'Display Target',
        priority: 'Priority',
        all: 'All',
      },
      modal: {
        title: {
          label: 'Title',
          required: 'Please enter a title',
          maxLength: 'Title cannot exceed 256 characters',
        },
        content: {
          label: 'Content',
          required: 'Please enter content',
        },
        status: {
          label: 'Status',
        },
        target: {
          label: 'Display Target',
        },
        priority: {
          label: 'Priority',
        },
      },
      messages: {
        statusUpdateSuccess: 'Status updated successfully',
        statusUpdateFailed: 'Status update failed',
      },
    },
    managerContainer: {
      notOrganizationIdentity: 'Not Organization Identity',
      current: 'Current',
      switching: 'Switching...',
      userProfile: 'User Profile',
      customTheme: 'Theme Settings',
      logout: 'Logout',
      menu: 'Menu',
      switchSuccess: 'Switched to {{tenantName}}',
      switchFailed: 'Failed to switch to {{tenantName}}',
      tabClose: 'Close',
      tabCloseOthers: 'Close Others',
      tabCloseLeft: 'Close Left',
      tabCloseRight: 'Close Right',
    },
    tenantInvitation: {
      title: 'Join Organization',
      inputStepHint: 'Please enter the invitation code provided by the administrator to continue',
      infoStepHint: 'Please confirm the organization information',
      formStepHint: 'Please fill in your personal information to complete joining',
      inviteCodeLabel: 'Organization Invitation Code',
      inviteCodePlaceholder: 'e.g.: 1brxVqQH2R6c568N',
      noDescription: 'No description',
      reachedUsageLimit: 'Invitation code has reached usage limit',
      expired: 'Invitation code has expired',
      validUntil: 'Valid until',
      permanentValid: 'Permanent valid',
      nextStep: 'Next Step',
      modifyInviteCode: 'Modify Invitation Code',
      realName: 'Real Name',
      realNamePlaceholder: 'Enter your real name here',
      realNameRequired: 'Please enter your name',
      phoneNumber: 'Phone Number',
      phoneNumberPlaceholder: 'Please enter phone number',
      phoneNumberRequired: 'Please enter a valid phone number',
      acceptInvitation: 'Accept Invitation',
      previousStep: 'Previous Step',
      submittedTitle: 'Application Submitted!',
      submittedDescription: 'We have notified the administrator of {{tenantName}}. You will receive an email notification after approval.',
      backToHome: 'Back to Home',
      enterInviteCode: 'Please enter invitation code',
      invalidInviteCodeLength: 'Invalid invitation code length (at least 8 characters required)',
      fetchTenantFailed: 'Failed to get organization information',
      invalidInviteCode: 'Invalid invitation code',
      inviteCodeExpired: 'Invalid or expired invitation code',
      submitSuccess: 'Application submitted! Please wait for administrator review.',
      submitFailed: 'Submission failed, please try again'
    },
    notFound: {
      title: 'Oops! Page Not Found',
      description: 'The page you are looking for might have been moved, deleted, or does not exist.',
      backToHome: 'Back to Home'
    },
    maintenance: {
      documentTitle: 'Maintenance',
      title: 'Under Maintenance',
      description: 'We are performing system upgrades and maintenance. Please try again later.',
      hint: 'All features are temporarily unavailable during maintenance. Thank you for your patience.'
    },
    serviceUnavailable: {
      title: 'Service Unavailable',
      description: 'Unable to connect to the server. Please check your network and refresh the page.'
    },
    sessionMonitor: {
      title: 'Online Sessions Monitor',
      subtitle: 'View the list of online sessions in the system',
      entityName: 'Session'
    },
    systemMonitor: {
      title: 'System Monitor',
      subtitle: 'Real-time view of CPU, memory, disk, JVM and other system metrics',
      chartTitle: 'Metrics Trend',
      timeRange: 'Time Range',
      syncCrosshair: 'Sync Crosshair',
      columns: {auto: 'Auto', col1: '1 Col', col2: '2 Cols', col3: '3 Cols'},
      durations: {
        m1: '1 Minute',
        m5: '5 Minutes',
        m15: '15 Minutes',
        m30: '30 Minutes',
        h1: '1 Hour',
        h3: '3 Hours',
        h5: '5 Hours',
        h12: '12 Hours',
        d1: '1 Day',
        d3: '3 Days',
        d5: '5 Days',
        d7: '7 Days',
        d14: '14 Days',
      },
      metrics: {
        cpuUsage: 'CPU Usage',
        cpuLoadAverage: 'CPU Load Average',
        memoryUsed: 'Memory Used',
        jvmHeapUsed: 'JVM Heap Used',
        jvmNonHeapCommitted: 'JVM NonHeap Committed',
        jvmNonHeapUsed: 'JVM NonHeap Used',
        diskUsed: 'Disk Used',
        dbConnectionsActive: 'DB Connections Active',
        gcCount: 'GC Count',
        gcTime: 'GC Time',
      },
    },
  },

  components: {
    dashboard: {
      // DashboardPage
      greeting: {
        earlyMorning: 'Get some rest early',
        morning: 'Good morning',
        lateMorning: 'Good late morning',
        afternoon: 'Good afternoon',
        evening: 'Good evening',
        user: 'User'
      },
      timeRange: {
        '1d': '1 Day',
        '3d': '3 Days',
        '5d': '5 Days',
        '1w': '1 Week',
        '2w': '2 Weeks',
        '1m': '1 Month',
        '3m': '3 Months',
        '6m': '6 Months',
        '1y': '1 Year'
      },

      // BusinessStatistics
      businessStats: {
        title: 'Business Statistics',
        totalUsers: 'Total Users',
        totalTenants: 'Total Tenants',
        totalTenantMembers: 'Tenant Members',
        totalFileResources: 'File Resources',
        totalMailSent: 'Mail Sent',
        totalInvitations: 'Invitations',
        totalInvitationRecords: 'Invitation Records',
        totalOAuthAccounts: 'OAuth Accounts'
      },

      // SystemMetrics
      systemMetrics: {
        title: 'System Metrics',
        lastUpdated: 'Last updated at',
        autoRefresh: 'Auto Refresh',
        refreshOptions: {
          '1s': '1s',
          '3s': '3s',
          '5s': '5s',
          '1m': '1m',
          '3m': '3m',
          '5m': '5m',
          '10m': '10m',
          '15m': '15m',
          '30m': '30m'
        },
        metrics: {
          cpuUsage: 'CPU Usage',
          memoryUsage: 'Memory Usage',
          jvmHeapMemory: 'JVM Heap Memory',
          jvmNonHeapMemory: 'JVM Non-Heap Memory',
          systemLoad: 'System Load',
          diskUsage: 'Disk Usage',
          dbConnections: 'DB Connections',
          gcPauseTime: 'GC Pause Time',
        },
        units: {
          usage: 'Usage',
          cores: 'Cores',
          unit: 'Unit'
        },
        loadFailed: 'Failed to load system metrics'
      },

      // MyJoinedTenants
      myJoinedTenants: {
        title: 'My Joined Organizations',
        count: 'Count',
        current: 'Current',
        noDescription: 'No description~',
        noTenants: 'No joined organizations',
        joinByCode: 'Join by Invitation Code',
        joinByCodeDesc: 'Enter invitation code to join new organization',
        loadFailed: 'Failed to load organization list'
      },

      // SystemAnnouncements
      systemAnnouncements: {
        title: 'Announcements',
        noAnnouncements: 'No announcements',
        loadFailed: 'Failed to load announcements'
      }
    },

    tenantSelectorWithDetail: {
      label: 'Select Tenant',
      reselect: 'Reselect',
      tenantId: 'Tenant ID',
      status: 'Status',
      contactName: 'Contact Name',
      contactPhone: 'Contact Phone',
      contactEmail: 'Contact Email',
      address: 'Address',
      subscribedTime: 'Subscribed Time',
      expiresTime: 'Expires Time',
      description: 'Description'
    },

    selector: {
      entitySelector: {
        title: 'Select {{entityName}}',
        cancelText: 'Cancel',
        okText: 'OK'
      },
      entityIdSelector: {
        placeholder: 'Select',
        clear: 'Clear'
      }
    },

    managerPageContainer: {
      addNew: 'Add {{entityName}}',
      edit: 'Edit ',
      create: 'Create ',
      deleteSuccess: '{{entityName}} deleted',
      deleteFailed: 'Failed to delete {{entityName}}',
      updateSuccess: '{{entityName}} updated',
      updateFailed: 'Failed to update {{entityName}}',
      createSuccess: '{{entityName}} created',
      createFailed: 'Failed to create {{entityName}}',
      deleteConfirm: 'Are you sure you want to delete this {{entityName}}?',
      confirm: 'Confirm',
      cancel: 'Cancel',
      batchOperation: 'Batch Operation',
      batchDelete: 'Delete All',
      batchDeleteTitle: 'Delete all selected items',
      batchDeleteConfirm: 'This operation cannot be undone, please confirm to continue?',
      batchDeleteSuccess: 'Batch delete successful',
      batchDeleteFailed: 'Batch delete failed',
      execute: 'Execute',
      action: 'Action',
      refresh: 'Refresh',
      timeRange: 'Time Range',
      startTime: 'Start Time',
      tillNow: 'Till Now',
      last5Minutes: 'Last 5 Minutes',
      last10Minutes: 'Last 10 Minutes',
      last15Minutes: 'Last 15 Minutes',
      last30Minutes: 'Last 30 Minutes',
      last1Hour: 'Last 1 Hour',
      last2Hours: 'Last 2 Hours',
      last3Hours: 'Last 3 Hours',
      last4Hours: 'Last 4 Hours',
      last8Hours: 'Last 8 Hours',
      last12Hours: 'Last 12 Hours',
      last1Day: 'Last 1 Day',
      last3Days: 'Last 3 Days',
      last5Days: 'Last 5 Days',
      last7Days: 'Last 7 Days',
      last14Days: 'Last 14 Days',
      last30Days: 'Last 30 Days',
      todayToNow: 'Today to Now'
    },

    protectedController: {
      readonlyError: 'Read-only mode, cannot create',
      warningTitle: 'High Risk Operation Warning',
      warningContent: 'This page is recommended to be accessed in read-only mode. Any incorrect operation may cause unpredictable consequences. Please select the operation mode:',
      editMode: 'Continue in Edit Mode',
      readonlyMode: 'Continue in Read-only Mode',
      readonlyBadge: 'Read-only Mode',
      editModeBadge: 'Accessing in Edit Mode'
    },

    maintenanceBanner: {
      message: 'System is currently in maintenance mode. Some features may be unavailable for regular users.',
    },

    themeSettings: {
      title: 'Theme Settings',
      themeColor: {
        title: 'Custom Theme Color',
        description: 'Choose your preferred theme color, which will be applied to the entire system interface'
      },
      tabs: {
        title: 'Tabs',
        enableTabs: 'Enable Tabs',
        enableTabsDesc: 'Show historically opened pages at the top of the page',
        tabSize: 'Tab Size',
        tabSizeDesc: 'Tab size option',
        sizeSmall: 'Small',
        sizeMiddle: 'Medium',
        sizeLarge: 'Large',
      },
      pageAnimation: {
        title: 'Page Animation',
        description: 'Select transition animation for page switching',
        none: 'None',
        fade: 'Fade',
        slideLeft: 'Slide Left',
        slideRight: 'Slide Right',
        slideUp: 'Slide Up',
        scale: 'Scale',
      }
    },

    storageProviderConfig: {
      localFileSystem: 'Local File System',
      tencentCos: 'Tencent Cloud COS',
      aliyunOss: 'Aliyun OSS',
      selectTemplate: 'Select Config Template',
      applyTemplate: 'Apply Template',
      applyTemplateTooltip: 'Apply Template'
    },

    columns: {
      fileResource: {
        preview: 'Preview',
        fileInfo: 'File Info',
        fileSize: 'Size',
        md5: 'MD5',
        storageProvider: 'Storage Provider',
        uploader: 'Uploader',
        fileType: 'File Type',
        createdTime: 'Created Time',
        userId: 'User ID',
        providerId: 'Provider ID'
      },
      storageProvider: {
        name: 'Name',
        type: 'Type',
        description: 'Description',
        baseUrl: 'Base URL',
        config: 'Config'
      },
      userPermission: {
        permission: 'Permission',
        type: 'Type',
        description: 'Description',
        path: 'Resource Path'
      },
      userRole: {
        role: 'Role',
        description: 'Description'
      },
      user: {
        userInfo: 'User Info',
        nickname: 'Nickname',
        email: 'Email'
      },
      tenant: {
        tenantName: 'Tenant Name',
        description: 'Description',
        status: 'Status',
        tireType: 'Tier Type',
        owner: 'Owner',
        contact: 'Contact',
        address: 'Address',
        subscriptionInfo: 'Subscription Info',
        subscribedTime: 'Subscribed Time',
        expiresTime: 'Expires Time',
        expired: 'Expired'
      },
      tenantRole: {
        roleInfo: 'Role Info',
        description: 'Description',
        parentRole: 'Parent Role'
      },
      tenantPermission: {
        permissionInfo: 'Permission Info',
        permissionName: 'Permission Name',
        description: 'Description',
        type: 'Type',
        path: 'Path',
        createdTime: 'Created Time'
      },
      tenantMember: {
        recordInfo: 'Record Info',
        tenantId: 'Tenant ID',
        userInfo: 'User Info',
        userId: 'User ID',
        email: 'Email',
        status: 'Status'
      },
      myTenantMember: {
        recordInfo: 'Record Info',
        member: 'Member',
        email: 'Email',
        status: 'Status'
      },
      tenantDepartment: {
        departmentName: 'Department Name',
        description: 'Description',
        parentDepartment: 'Parent Department',
        parentDepartmentId: 'Parent Department ID'
      },
      tenantDepartmentMember: {
        recordInfo: 'Record Info',
        memberId: 'Member ID',
        tenantId: 'Tenant ID',
        userInfo: 'User Info',
        userId: 'User ID',
        email: 'Email',
        memberStatus: 'Member Status',
        departmentRole: 'Department Role'
      },
      tenantInvitation: {
        recordInfo: 'Record Info',
        invitationCode: 'Invitation Code',
        invitationCount: 'Invitation Count',
        times: 'times',
        creator: 'Creator',
        department: 'Department',
        requiresReviewing: 'Requires Reviewing',
        requiresReviewingTooltip: 'Requires Reviewing',
        noReviewingTooltip: 'No Reviewing Required',
        expiresTime: 'Expires Time',
        tenantId: 'Tenant ID',
        memberId: 'Member ID',
        userId: 'User ID',
        departmentId: 'Department ID',
        notSpecified: 'Not Specified',
        yes: 'Yes',
        no: 'No',
        neverExpires: 'Never Expires',
        expired: 'Expired',
        status: 'Status',
        usedCount: 'Used Count',
        createdTime: 'Created Time'
      },
      tenantTireType: {
        name: 'Name',
        description: 'Description'
      },
      tenantTireBenefitFeature: {
        featureKey: 'Feature Key',
        name: 'Name',
        description: 'Description',
        featureType: 'Feature Type',
        defaultValue: 'Default Value',
      },
      tenantTireBenefitValue: {
        recordInfo: 'Record Info',
        tireType: 'Tier Type',
        feature: 'Feature',
        featureValue: 'Feature Value'
      },
      oAuthAccount: {
        identifier: 'Identifier',
        platform: 'Platform',
        systemUser: 'System User',
        unbound: 'Unbound',
        userInfo: 'User Info',
        userId: 'User ID',
        noAvatar: 'No Avatar'
      },
      mailTemplate: {
        name: 'Name',
        type: 'Type',
        title: 'Title',
        description: 'Description',
        content: 'Content'
      },
      mailTemplateCategory: {
        name: 'Name',
        description: 'Description'
      },
      mailTemplateType: {
        name: 'Name',
        description: 'Description',
        variables: 'Variables',
        category: 'Category',
        allowMultiple: 'Allow Multiple',
        yes: 'Yes',
        no: 'No'
      },
      auditLog: {
        userInfo: 'User',
        action: 'Action',
        resourceType: 'Resource Type',
        request: 'Request',
        status: 'Status',
        success: 'Success',
        failed: 'Failed'
      },
      mailSendLog: {
        fromEmail: 'From',
        toEmail: 'To',
        subject: 'Subject',
        user: 'Sender',
        status: 'Status',
        success: 'Success',
        failed: 'Failed'
      },
      userLoginLog: {
        user: 'User',
        loginMethod: 'Login Method',
        oauth2Username: 'OAuth2 Username',
        remoteIp: 'IP Address',
        userAgent: 'User Agent',
        status: 'Status',
        success: 'Success',
        failed: 'Failed',
        loginMethodTypes: {
          password: 'Password Login',
          oauth2: 'OAuth2 Login',
          unknown: 'Unknown'
        }
      },
      sessionMonitor: {
        sessionId: 'Session ID',
        user: 'User',
        tenant: 'Tenant',
        remoteIp: 'IP Address',
        userAgent: 'User Agent'
      }
    },
    entityTable: {
      recordTime: 'Record Time',
      createdTime: 'Created Time',
      modifiedTime: 'Modified Time',
      action: 'Action',
      search: 'Search',
      searchPlaceholder: 'Search {{entityName}}...',
      fetchError: 'Failed to fetch {{entityName}} list',
      combineLogic: 'Combine Logic',
      combineAnd: 'AND',
      combineOr: 'OR',
      pagination: {
        total: 'Total {{total}} Item(s)',
      },
      columnFilter: {
        label: 'Column Settings',
        button: 'Filter Columns',
        title: 'Display Columns',
        selectAll: 'Select All',
      }
    },
    filterBuilder: {
      filters: 'Filters',
      addCondition: 'Add Condition',
      fillRequired: 'Fill in all condition values',
      addGroup: 'Add Group',
      and: 'AND',
      or: 'OR',
      apply: 'Apply',
      cancel: 'Cancel',
      reset: 'Reset',
      rootLogic: 'Group Logic',
      group: 'Group',
      noConditions: 'No conditions yet — add a condition or group',
      selectField: 'Select Field',
      selectOperator: 'Select Operator',
      selectValue: 'Select Value',
      valuePlaceholder: 'Enter value',
      addValue: 'Add Value',
      operators: {
        eq: 'Equals',
        ne: 'Not Equals',
        contains: 'Contains',
        like: 'Like',
        gt: 'Greater Than',
        gte: 'Greater Than or Equal',
        lt: 'Less Than',
        lte: 'Less Than or Equal',
        in: 'In',
      }
    },
    jsonEditor: {
      root: 'Root',
      index: 'Index',
      type: {
        string: 'String',
        number: 'Number',
        boolean: 'Boolean',
        object: 'Object',
        array: 'Array',
        null: 'Null'
      },
      emptyObject: 'Empty Object',
      emptyArray: 'Empty Array',
      visual: 'Visual',
      source: 'Source',
      invalidJson: 'Invalid JSON Format',
      valid: 'Valid',
      invalid: 'Invalid'
    },
    htmlEditor: {
      code: 'Code',
      preview: 'Preview',
      placeholder: 'Enter HTML code...'
    },
    imageCropper: {
      loading: 'Loading...',
      rotateLeft: 'Rotate Left',
      rotateRight: 'Rotate Right',
      reset: 'Reset'
    },
    actuatorMetric: {
      tags: 'Tags',
      optionalValues: 'Optional Values',
      measurements: 'Measurements'
    },
    popCard: {
      user: {
        notFound: 'User information not found',
        email: 'Email'
      },
      tenantDepartment: {
        notFound: 'Department information not found',
        description: 'Description',
        parentDepartment: 'Parent Department',
        tenantId: 'Tenant ID'
      },
      storageProvider: {
        notFound: 'Storage provider information not found',
        type: 'Type',
        unknownType: 'Unknown Type',
        description: 'Description',
        status: 'Status',
        enabled: 'Enabled',
        disabled: 'Disabled',
        localFileSystem: 'Local File System',
        aliyunOSS: 'Aliyun OSS',
        tencentCOS: 'Tencent COS'
      },
      mailTemplateType: {
        notFound: 'Template type information not found',
        description: 'Description',
        allowMultiple: 'Allow Multiple Templates',
        variables: 'Variables'
      },
      templateVariablesTag: {
        copySuccess: 'Copied {{variable}} to clipboard'
      }
    }
  },

  api: {
    sessionExpired: 'Your session has expired',
    forbidden: 'You do not have permission to access this resource',
    unknownError: 'Unknown error'
  },

  enums: {
    unknown: 'Unknown',
    tenantMemberStatus: {
      0: 'Inactive',
      1: 'Resigned',
      2: 'Quit',
      3: 'Pending',
      4: 'Active'
    },
    tenantStatus: {
      0: 'Pending',
      1: 'Active',
      2: 'Closed'
    },
    resourceFileType: {
      0: 'User Avatar',
      1: 'Tenant Icon'
    },
    departmentMemberRoleType: {
      0: 'Member',
      1: 'Admin',
      2: 'Super Admin'
    },
    actuatorMetrics: {
      'application.started.time': 'Application Started Time',
      'application.ready.time': 'Application Ready Time',
      'process.start.time': 'Process Start Time',
      'process.uptime': 'Process Uptime',
      'system.cpu.usage': 'System CPU Usage',
      'system.cpu.count': 'CPU Count',
      'system.load.average.1m': 'System Load Average 1m',
      'process.cpu.usage': 'Process CPU Usage',
      'process.cpu.time': 'Process CPU Time',
      'disk.total': 'Disk Total',
      'disk.free': 'Disk Free',
      'jvm.info': 'JVM Info',
      'jvm.memory.used': 'JVM Memory Used',
      'jvm.memory.max': 'JVM Memory Max',
      'jvm.memory.committed': 'JVM Memory Committed',
      'jvm.memory.usage.after.gc': 'JVM Memory Usage After GC',
      'jvm.buffer.count': 'JVM Buffer Count',
      'jvm.buffer.memory.used': 'JVM Buffer Memory Used',
      'jvm.buffer.total.capacity': 'JVM Buffer Total Capacity',
      'jvm.gc.pause': 'JVM GC Pause',
      'jvm.gc.overhead': 'JVM GC Overhead',
      'jvm.gc.memory.allocated': 'JVM GC Memory Allocated',
      'jvm.gc.memory.promoted': 'JVM GC Memory Promoted',
      'jvm.gc.live.data.size': 'JVM GC Live Data Size',
      'jvm.gc.max.data.size': 'JVM GC Max Data Size',
      'jvm.gc.concurrent.phase.time': 'JVM GC Concurrent Phase Time',
      'jvm.classes.loaded': 'JVM Classes Loaded',
      'jvm.classes.loaded.count': 'JVM Classes Loaded Count',
      'jvm.classes.unloaded': 'JVM Classes Unloaded',
      'jvm.compilation.time': 'JVM Compilation Time',
      'jvm.threads.live': 'JVM Threads Live',
      'jvm.threads.peak': 'JVM Threads Peak',
      'jvm.threads.daemon': 'JVM Threads Daemon',
      'jvm.threads.started': 'JVM Threads Started',
      'jvm.threads.states': 'JVM Threads States',
      'executor.active': 'Executor Active',
      'executor.completed': 'Executor Completed',
      'executor.pool.core': 'Executor Pool Core',
      'executor.pool.max': 'Executor Pool Max',
      'executor.pool.size': 'Executor Pool Size',
      'executor.queue.remaining': 'Executor Queue Remaining',
      'executor.queued': 'Executor Queued',
      'http.server.requests': 'HTTP Server Requests',
      'http.server.requests.active': 'HTTP Server Requests Active',
      'r2dbc.pool.acquired': 'R2DBC Pool Acquired',
      'r2dbc.pool.allocated': 'R2DBC Pool Allocated',
      'r2dbc.pool.idle': 'R2DBC Pool Idle',
      'r2dbc.pool.max.allocated': 'R2DBC Pool Max Allocated',
      'r2dbc.pool.pending': 'R2DBC Pool Pending',
      'r2dbc.pool.max.pending': 'R2DBC Pool Max Pending',
      'lettuce': 'Redis Connections',
      'lettuce.active': 'Redis Active Connections',
      'spring.data.repository.invocations': 'Spring Data Repository Invocations',
      'spring.integration.channels': 'Spring Integration Channels',
      'spring.integration.handlers': 'Spring Integration Handlers',
      'spring.integration.sources': 'Spring Integration Sources',
      'spring.security.authorizations': 'Spring Security Authorizations',
      'spring.security.authorizations.active': 'Spring Security Authorizations Active',
      'spring.security.filterchains': 'Spring Security Filter Chains',
      'spring.security.filterchains.active': 'Spring Security Filter Chains Active',
      'spring.security.http.secured.requests': 'Spring Security HTTP Secured Requests',
      'spring.security.http.secured.requests.active': 'Spring Security HTTP Secured Requests Active',
      'logback.events': 'Logback Events',
      'process.files.max': 'Process Files Max',
      'process.files.open': 'Process Files Open'
    },

    tenantPermissionType: {
      0: 'Action',
      1: 'Menu'
    },

    permissionType: {
      0: 'Action',
      1: 'Menu',
      2: 'Component',
    },

    systemSettingsItemValueType: {
      STRING: 'String',
      NUMBER: 'Number',
      DECIMAL: 'Decimal',
      BOOLEAN: 'Boolean'
    },

    storageProviderType: {
      0: 'Local File System',
      1: 'Aliyun OSS',
      2: 'Tencent COS'
    },

    oAuthPlatform: {
      0: 'GitHub',
      1: 'Google',
      2: 'QQ'
    },

    tenantBenefitType: {
      0: 'Boolean',
      1: 'Limit',
      2: 'Enum'
    },

    announcementStatus: {
      0: 'Draft',
      1: 'Published',
      2: 'Offline'
    },

    announcementTarget: {
      0: 'User Side Only',
      1: 'Manager Side Only',
      2: 'Both Sides'
    }
  },

  entityNames: {
    user: 'User',
    oauthAccount: 'OAuth Account',
    userRole: 'User Role',
    userPermission: 'User Permission',
    tenant: 'Tenant',
    tenantMember: 'Tenant Member',
    tenantRole: 'Tenant Role',
    tenantPermission: 'Tenant Permission',
    tenantDepartment: 'Tenant Department',
    tenantInvitation: 'Invitation Code',
    tenantTireType: 'Tire Type',
    tenantTireBenefitFeature: 'Benefit Feature',
    tenantTireBenefitValue: 'Tier Benefit Value',
    fileResource: 'File Resource',
    storageProvider: 'Storage Provider',
    mailTemplate: 'Mail Template',
    mailTemplateType: 'Mail Template Type',
    mailTemplateCategory: 'Mail Template Category',
    myTenantMember: 'Organization Member',
    myTenantRole: 'Role',
    auditLog: 'Audit Log',
    mailSendLog: 'Mail Send Log',
    userLoginLog: 'User Login Log',
    sessionMonitor: 'Session',
    announcement: 'Announcement'
  },

  menu: {
    // Public menus
    pub: {
      dashboard: 'Dashboard',
      profile: 'Profile'
    },

    // Tenant menus (i_tenant)
    myTenant: {
      dashboard: 'My Organization',
      members: 'Member Management',
      invitations: 'Invitation Code Management',
      roles: 'Role Management',
      memberRoles: 'Member Role Management',
      departments: 'Department Management',
      profile: 'Organization Settings'
    },

    // Admin menus
    admin: {
      users: 'User Management',
      oauthAccounts: 'OAuth Account Management',
      userRoles: 'User Role Management',
      userPermissions: 'User Permission Management',
      userRolesRelation: 'User Role Assignment',
      tenants: 'Tenant Management',
      tenantMembers: 'Member Management',
      tenantRoles: 'Role Management',
      tenantPermissions: 'Permission Management',
      tenantRolePermissions: 'Role Permission Management',
      tenantMemberRoles: 'Member Role Management',
      tenantDepartments: 'Department Management',
      tenantInvitations: 'Invitation Code Management',
      tenantTireTypes: 'Tire Type Management',
      tenantTireBenefitFeatures: 'Benefit Features',
      tenantTireBenefitValues: 'Tier Benefit Management',
      fileResources: 'File Resource Management',
      storageProviders: 'Storage Provider Management',
      mailTemplates: 'Mail Template Management',
      mailTemplateTypes: 'Mail Template Types',
      mailTemplateCategories: 'Mail Template Categories',
      mailSendLogs: 'Mail Send Logs',
      auditLogs: 'Audit Logs',
      userLoginLogs: 'User Login Logs',
      settings: 'System Settings',
      sessions: 'Online Sessions',
      systemMonitor: 'System Monitor',
      announcements: 'Announcement Management'
    },

    // Menu groups
    groups: {
      rbac: 'User Permissions',
      system_storage: 'System Storage',
      mail_template: 'Mail Templates',
      tenant: 'Tenant Management',
      i_tenant: 'Organization Management',
      logs: 'Log Management',
      monitor: 'System Monitor'
    }
  }
};
