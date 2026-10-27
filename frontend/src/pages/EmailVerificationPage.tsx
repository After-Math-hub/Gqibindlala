import { useEffect, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { verifyEmail } from "../api/client";

export default function EmailVerificationPage() {
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState<"loading" | "success" | "error">("loading");
  const [message, setMessage] = useState("Verifying your email address…");
  const token = searchParams.get("token");
  const started = useRef(false);

  useEffect(() => {
    if (started.current) return;
    started.current = true;
    if (!token) {
      setStatus("error");
      setMessage("This verification link is missing its token.");
      return;
    }
    verifyEmail(token)
      .then((response) => {
        setStatus("success");
        setMessage(response.message);
      })
      .catch(() => {
        setStatus("error");
        setMessage("This verification link is invalid or has expired. Please register again or contact the team.");
      });
  }, [token]);

  return (
    <section className="booking-success email-verification-page">
      <span className={`success-mark ${status === "error" ? "verification-error" : ""}`} aria-hidden="true">{status === "loading" ? "…" : status === "success" ? "✓" : "!"}</span>
      <p className="eyebrow"><span /> ACCOUNT SECURITY</p>
      <h1>{status === "success" ? "You're verified." : status === "error" ? "Link unavailable." : "Checking your email."}</h1>
      <p role="status">{message}</p>
      {status !== "loading" && <Link to="/account" className="button button-gold">Continue to sign in <span aria-hidden="true">↗</span></Link>}
    </section>
  );
}
