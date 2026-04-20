import { describe, expect, it } from "vitest";
import { chunk, partitionMap } from "../ListUtils.res.mjs";

describe("chunk", () => {
  it("splits an array into fixed-size buckets", () => {
    expect(chunk([1, 2, 3, 4, 5], 2)).toEqual([[1, 2], [3, 4], [5]]);
  });
  it("returns an empty array when size is zero or negative", () => {
    expect(chunk([1, 2, 3], 0)).toEqual([]);
  });
});

describe("partitionMap", () => {
  it("splits into oks and errs", () => {
    const [oks, errs] = partitionMap([1, 2, 3], (n) =>
      n % 2 === 0 ? { TAG: "Ok", _0: n * 10 } : { TAG: "Error", _0: `odd: ${n}` }
    );
    expect(oks).toEqual([20]);
    expect(errs).toEqual(["odd: 1", "odd: 3"]);
  });
});
