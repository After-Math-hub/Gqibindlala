import { FormEvent, useEffect, useState } from "react";
import { fetchOrderMessages, sendOrderMessage } from "../api/client";
import { OrderMessage } from "../types/equipment";

export default function OrderConversation({ orderId }: { orderId: number }) {
  const [messages, setMessages] = useState<OrderMessage[]>([]);
  const [content, setContent] = useState("");
  const [expanded, setExpanded] = useState(false);
  const [loading, setLoading] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");

  async function loadMessages() {
    setLoading(true);
    try {
      setMessages(await fetchOrderMessages(orderId));
      setError("");
    } catch {
      setError("Messages could not be loaded.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (expanded) void loadMessages();
  }, [expanded, orderId]);

  async function send(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmed = content.trim();
    if (!trimmed) return;
    setSending(true);
    setError("");
    try {
      const message = await sendOrderMessage(orderId, trimmed);
      setMessages((previous) => [...previous, message]);
      setContent("");
    } catch {
      setError("Your message could not be sent. Please try again.");
    } finally {
      setSending(false);
    }
  }

  return (
    <section className="order-conversation">
      <button className="conversation-toggle" type="button" aria-expanded={expanded} onClick={() => setExpanded((value) => !value)}>
        <span>Order messages</span><span>{expanded ? "−" : "+"}</span>
      </button>
      {expanded && (
        <div className="conversation-body">
          <div className="conversation-messages" aria-live="polite">
            {loading && <p className="conversation-empty">Loading messages…</p>}
            {!loading && messages.length === 0 && <p className="conversation-empty">No messages yet. Keep order questions and updates here.</p>}
            {messages.map((message) => (
              <article className="conversation-message" key={message.id}>
                <header><strong>{message.senderName}</strong><span>{message.departmentName || message.senderRole}</span><time>{new Date(message.createdAt).toLocaleString()}</time></header>
                <p>{message.content}</p>
              </article>
            ))}
          </div>
          <form className="conversation-form" onSubmit={(event) => void send(event)}>
            <label className="field-label">Message<textarea required maxLength={4000} rows={2} value={content} onChange={(event) => setContent(event.target.value)} placeholder="Write an update or question…" /></label>
            <button type="submit" className="button button-gold" disabled={sending || !content.trim()}>{sending ? "Sending…" : "Send message"}</button>
          </form>
          {error && <p className="form-alert" role="alert">{error}</p>}
        </div>
      )}
    </section>
  );
}
