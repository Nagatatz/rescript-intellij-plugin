; Contract model: resolved artifact/version IDs are abstract integers.
; The registry provides deterministic expected core package/version and Vitest IDs
; for a given CLI ID. This does not model real SemVer, registry availability,
; the package manager implementation, or application behavior.
(set-logic QF_UFLIA)
(declare-fun corePackage (Int) Int)
(declare-fun coreVersion (Int) Int)
(declare-fun bundledVitest (Int) Int)
(declare-const pinnedCli Int)
(declare-const resolvedCli Int)
(declare-const resolvedAliasPackage Int)
(declare-const resolvedAliasCore Int)
(declare-const resolvedDirectCore Int)
(declare-const resolvedVitest Int)
(declare-const resolvedCoverage Int)

; Exact tuple: assuming exact pins resolve faithfully, disagreement is impossible.
(push)
(assert (= resolvedCli pinnedCli))
(assert (= resolvedAliasPackage (corePackage pinnedCli)))
(assert (= resolvedAliasCore (coreVersion pinnedCli)))
(assert (= resolvedDirectCore (coreVersion pinnedCli)))
(assert (= resolvedVitest (bundledVitest pinnedCli)))
(assert (= resolvedCoverage (bundledVitest pinnedCli)))
(assert (or (distinct resolvedAliasPackage (corePackage resolvedCli))
            (distinct resolvedAliasCore (coreVersion resolvedCli))
            (distinct resolvedDirectCore (coreVersion resolvedCli))
            (distinct resolvedVitest (bundledVitest resolvedCli))
            (distinct resolvedCoverage (bundledVitest resolvedCli))))
(check-sat)
(pop)

; The package manager may select the known CLI and a different core under the
; historical independent ranges. Distinct IDs encode this observed ordering.
(push)
(assert (= resolvedCli 160))
(assert (= (coreVersion 160) 838001))
(assert (= resolvedAliasCore 160))
(assert (distinct resolvedAliasCore (coreVersion resolvedCli)))
(check-sat)
(pop)
