import { FormEvent, useEffect, useState } from "react";
import { fetchInventoryChangeMessages, sendInventoryChangeMessage } from "../api/client";
import { InventoryChangeMessage } from "../types/equipment";

export default function InventoryChangeDiscussion({ requestId }: { requestId: number }) {
  const [messages, setMessages] = useState<InventoryChangeMessage[]>([]);
  const [content, setContent] = useState("");
  const [expanded, setExpanded] = useState(false);
  const [error, setError] = useState("");
  const [sending, setSending] = useState(false);

  async function load() {
    try {
      setMessages(await fetchInventoryChangeMessages(requestId));
      setError("");
    } catch {
      setError("The internal discussion could not be loaded.");
    }
  }

  useEffect(() => {
    if (expanded) void load();
  }, [expanded, requestId]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const text = content.trim();
    if (!text) return;
    setSending(true);
    setError("");
    try {
      const message = await sendInventoryChangeMessage(requestId, text);
      setMessages((previous) => [...previous, message]);
      setContent("");
    } catch {
      setError("Message could not be sent.");
    } finally {
      setSending(false);
    }
  }

  return (
    <div className="internal-discussion">
      <button className="conversation-toggle" type="button" aria-expanded={expanded} onClick={() => setExpanded((value) => !value)}>
        <span>Staff discussion</span><span>{expanded ? "−" : "+"}</span>
      </button>
      {expanded && <div className="conversation-body">
        <div className="conversation-messages" aria-live="polite">
          {messages.length === 0 && !error && <p className="conversation-empty">No discussion yet. Admin and staff replies stay inside the site.</p>}
          {messages.map((message) => <article className="conversation-message" key={message.id}>
            <header><strong>{message.senderName}</strong><span>{message.senderRole}</span><time>{new Date(message.createdAt).toLocaleString()}</time></header>
            <p>{message.content}</p>
          </article>)}
        </div>
        <form className="conversation-form" onSubmit={(event) => void submit(event)}>
          <label className="field-label">Reply<textarea required maxLength={4000} rows={2} value={content} onChange={(event) => setContent(event.target.value)} placeholder="Discuss the request with the team…" /></label>
          <button type="submit" className="button button-gold" disabled={sending || !content.trim()}>{sending ? "Sending…" : "Send reply"}</button>
        </form>
        {error && <p className="form-alert" role="alert">{error}</p>}
      </div>}
    </div>
  );
}
