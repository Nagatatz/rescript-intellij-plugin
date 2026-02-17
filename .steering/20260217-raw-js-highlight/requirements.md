# Requirements: %raw() JavaScript ハイライト

## 概要

ReScript の `%raw("...")` および `%%raw(`...`)` 構文内に記述された JavaScript コードを、IDE 上で JavaScript としてシンタックスハイライトする機能を実装する。

## ユーザーストーリー

**ユーザーとして**、`%raw()` 内に記述した JavaScript コードが適切にハイライトされることで、FFI（Foreign Function Interface）コードの視認性を向上させたい。

## 受け入れ条件

- [ ] `%raw("...")` 内の文字列が JavaScript としてハイライトされる
- [ ] `%%raw(`...`)` テンプレートリテラル内も同様に JavaScript ハイライトされる
- [ ] JavaScript プラグインが利用不可な環境でもエラーにならない（通常の文字列ハイライトにフォールバック）
- [ ] ビルドが成功する

## 制約事項

- JavaScript プラグインへの依存は optional とする
- IntelliJ Platform の `MultiHostInjector` API を使用する
- 既存の PSI 構造（STRING_VALUE トークン）を活用し、レクサー/パーサーの変更は不要
