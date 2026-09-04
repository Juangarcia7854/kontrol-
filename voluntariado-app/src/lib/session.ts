import { SessionOptions } from 'iron-session'

export interface SessionData {
  userId?: string;
  email?: string;
  isLoggedIn: boolean;
}

export const defaultSession: SessionData = {
  isLoggedIn: false,
}

export const sessionOptions: SessionOptions = {
  password: process.env.IRON_PASSWORD as string,
  cookieName: 'voluntariado_admin_session',
  cookieOptions: {
    secure: process.env.NODE_ENV === 'production',
  },
}
