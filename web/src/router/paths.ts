/**
 * Route path constants.
 * Kept in a separate file so that api/system-request.ts can import menuPathLogin
 * without pulling in the full router module (which imports components and api controllers,
 * creating a circular chunk dependency).
 */
export const menuPathDashboard = "/manager/dashboard";
export const menuPathProfile = "/manager/profile";
export const menuPathLogin = "/auth/login";
export const menuPathRegister = "/auth/register";
export const menuPathResetPassword = "/auth/reset-password";
export const menuPathOAuthCode = "/auth/oauth2-code";
export const menuPathOAuthBind = "/auth/oauth2-bind";
