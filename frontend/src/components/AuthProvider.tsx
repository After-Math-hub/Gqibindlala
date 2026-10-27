import { createContext, PropsWithChildren, useContext, useEffect, useState } from "react";
import { fetchProfile, loginAccount, registerAccount } from "../api/client";
import { AuthResponse, UserProfile } from "../types/equipment";

interface AuthContextValue {
  user: UserProfile | null;
  loading: boolean;
  login: (loginIdentifier: string, password: string) => Promise<void>;
  register: (details: { fullName: string; email: string; phone: string; password: string }) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function saveSession(session: AuthResponse) {
  window.localStorage.setItem("gqibindlala-token", session.token);
  return session.user;
}

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = window.localStorage.getItem("gqibindlala-token");
    if (!token) {
      setLoading(false);
      return;
    }
    fetchProfile()
      .then(setUser)
      .catch(() => {
        window.localStorage.removeItem("gqibindlala-token");
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, []);

  async function login(loginIdentifier: string, password: string) {
    setUser(saveSession(await loginAccount(loginIdentifier, password)));
  }

  async function register(details: { fullName: string; email: string; phone: string; password: string }) {
    await registerAccount(details);
  }

  function logout() {
    window.localStorage.removeItem("gqibindlala-token");
    setUser(null);
  }

  return <AuthContext.Provider value={{ user, loading, login, register, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
}