import { type RouteConfig, index } from "@react-router/dev/routes";

// `index("routes/home.tsx")` mounts the file at `/`. Add more entries with
// `route("about", "routes/about.tsx")` or nested layouts with `layout(...)`.
export default [index("routes/home.tsx")] satisfies RouteConfig;
