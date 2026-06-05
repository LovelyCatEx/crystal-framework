UPDATE public.tenant_settings
SET config_key = 'notification.memberJoin.email'
WHERE config_key = 'notification.memberJoinNotifyEmail';

UPDATE public.tenant_settings
SET config_key = 'notification.memberJoinReview.email'
WHERE config_key = 'notification.memberJoinReviewNotifyEmail';
