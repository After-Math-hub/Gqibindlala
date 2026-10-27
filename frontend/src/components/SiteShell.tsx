import { PropsWithChildren, useEffect, useState } from "react";
import { Link, NavLink } from "react-router-dom";
import { useAuth } from "./AuthProvider";

type Theme = "light" | "dark";

export default function SiteShell({ children }: PropsWithChildren) {
  const { user, logout } = useAuth();
  const [theme, setTheme] = useState<Theme>(() => {
    const savedTheme = window.localStorage.getItem("gqibindlala-theme");
    return savedTheme === "dark" ? "dark" : "light";
  });

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    window.localStorage.setItem("gqibindlala-theme", theme);
  }, [theme]);

  return (
    <div className="site-shell">
      <header className="site-header">
        <Link className="brand" to="/" aria-label="Gqibindlala home">
          <span className="brand-mark" aria-hidden="true">G</span>
          <span className="brand-copy">
            <span className="brand-name">Gqibindlala</span>
            <span className="brand-caption">CATERING &amp; TENT HIRING</span>
          </span>
        </Link>

        <nav className="main-nav" aria-label="Main navigation">
          <NavLink to="/" end className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            Collection
          </NavLink>
          <NavLink to="/book" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            Make an enquiry
          </NavLink>
        </nav>

        <div className="header-actions">
          <button
            className="theme-toggle"
            type="button"
            onClick={() => setTheme((current) => current === "light" ? "dark" : "light")}
            aria-label={`Switch to ${theme === "light" ? "dark" : "light"} mode`}
            title={`Switch to ${theme === "light" ? "dark" : "light"} mode`}
          >
            <span aria-hidden="true">{theme === "light" ? "◐" : "☼"}</span>
            <span>{theme === "light" ? "Dark" : "Light"}</span>
          </button>
          {user ? (
            <div className="account-header-actions">
              <Link className="header-cta" to="/account">{user.role === "CUSTOMER" ? `${user.fullName.split(" ")[0]}'s account` : "Staff workspace"}</Link>
              <button type="button" className="logout-button" onClick={logout}>Sign out</button>
            </div>
          ) : (
            <Link className="header-cta" to="/account">Sign in <span aria-hidden="true">↗</span></Link>
          )}
        </div>
      </header>

      <main>{children}</main>

      <footer className="site-footer">
        <Link className="footer-brand" to="/">Gqibindlala <span>·</span> Catering &amp; Tent Hiring</Link>
        <span>Thoughtful essentials for gatherings worth remembering.</span>
        <Link to="/admin" className="footer-admin">Business portal</Link>
      </footer>
    </div>
  );
}