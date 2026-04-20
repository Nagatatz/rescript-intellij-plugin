/** Splits [xs] into arrays of [size] elements. Last chunk may be shorter. */
@genType
let chunk = (xs: array<'a>, ~size: int): array<array<'a>> => {
  if size <= 0 {
    []
  } else {
    let buckets = ref([])
    let current = ref([])
    xs->Array.forEach(x => {
      current.contents->Array.push(x)
      if current.contents->Array.length >= size {
        buckets.contents->Array.push(current.contents)
        current := []
      }
    })
    if current.contents->Array.length > 0 {
      buckets.contents->Array.push(current.contents)
    }
    buckets.contents
  }
}

/**
 * Partitions [xs] by running [f] on each element. Results that are `Ok`
 * land in the first array; `Error` values in the second.
 */
@genType
let partitionMap = (xs: array<'a>, f: 'a => result<'b, 'c>): (array<'b>, array<'c>) => {
  let oks = []
  let errs = []
  xs->Array.forEach(x =>
    switch f(x) {
    | Ok(v) => oks->Array.push(v)
    | Error(e) => errs->Array.push(e)
    }
  )
  (oks, errs)
}