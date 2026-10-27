import { BrowserRouter, Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import BookingPage from "./pages/BookingPage";
import AdminDashboardPage from "./pages/AdminDashboardPage";
import SiteShell from "./components/SiteShell";
import AccountPage from "./pages/AccountPage";
import EmailVerificationPage from "./pages/EmailVerificationPage";
import { AuthProvider } from "./components/AuthProvider";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<SiteShell><HomePage /></SiteShell>} />
          <Route path="/book" element={<SiteShell><BookingPage /></SiteShell>} />
          <Route path="/account" element={<SiteShell><AccountPage /></SiteShell>} />
          <Route path="/verify-email" element={<SiteShell><EmailVerificationPage /></SiteShell>} />

          <Route path="/admin" element={<SiteShell><AdminDashboardPage /></SiteShell>} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
