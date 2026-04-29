// React Router v7 loaders return any serialisable value. We expose a tiny
// helper from ReScript so the route can keep its TSX glue thin and unit-test
// the loader on its own.

type input = {project: string}

type loaderData = {name: string}

let homeLoader = (input: input): loaderData => {
  name: input.project,
}
