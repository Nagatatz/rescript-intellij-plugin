// Client Component: "use client" opts into the browser bundle so we can
// use React.useState and event handlers.
"use client";

import GreetFormRescript from "../../GreetForm.gen";

export default function GreetForm() {
  return <GreetFormRescript />;
}