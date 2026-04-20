// Server Component: renders the App shell + the Client Component GreetForm.
// This is the default rendering mode for Next.js app router — no "use client" directive.
import App from "../App.gen";
import GreetForm from "./client/GreetForm";

export default function Page() {
  return (
    <main style={{ padding: "2rem", fontFamily: "sans-serif" }}>
      <App />
      <GreetForm />
    </main>
  );
}