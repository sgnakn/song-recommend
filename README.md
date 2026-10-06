# Music Compass

キーワードを選ぶと、ぴったりの曲を探してくれる音楽推薦サイトです。
海図とコンパスをイメージしたデザインで、検索結果はジャケット写真が並ぶカバーフロー形式で表示されます。

## 主な機能

- キーワード検索（スペース区切りで複数指定すると、すべてを含む曲だけを表示するAND検索）
- おすすめキーワードのボタン表示（アクセスや検索のたびにランダムで入れ替わる）
- ジャケット写真のカバーフロー表示（矢印ボタン・ドラッグ・クリックで切り替え）
- 正面の曲に対し自動的にSpotify / Apple Music / YouTube へのリンクを表示
- 検索中の「Now Sailing...」アニメーション

## 使用技術

- Java 17
- Spring Boot 4.1.1（Spring Web / Spring Data JPA）
- H2 Database（ファイル保存）
- HTML / CSS / JavaScript（ページ遷移なしで結果を更新）
- Spotify Web API（ジャケット写真・Spotifyリンクの取得）
- iTunes Search API（Apple Musicリンクの取得）

## セットアップ

### 1. リポジトリを取得

```bash
git clone https://github.com/sgnakn/song-recommend.git
```

### 2. Spotifyのキーを用意

1. [Spotify for Developers](https://developer.spotify.com/dashboard) でアプリを作成し、Client ID と Client Secret を取得します。
2. `src/main/resources/` に `secrets.properties` を作成し、次のように書きます。

```properties
spotify.client-id=あなたのClient ID
spotify.client-secret=あなたのClient Secret
```

`secrets.properties` は `.gitignore` に入っているため、GitHubにはアップロードされません。**キーをコードやチャットに書かないでください。**

### 3. 起動

`SongRecommendApplication.java` を Java Application として実行します（Eclipseの場合は右クリック → Run As → Java Application）。

起動後、ブラウザで http://localhost:8080 を開きます。

## 曲データの追加

曲は `src/main/resources/songs.csv` に1行ずつ書きます。

```csv
title,artist,keywords
涙そうそう,夏川りみ,夏;切ない;バラード
```

- 1列目：曲名、2列目：アーティスト名、3列目：キーワード（複数ある場合は `;` で区切る）
- CSVは、DBに曲が1件もないときだけ起動時に読み込まれます
- すでに登録済みの曲（曲名とアーティスト名が同じ）はスキップされます

### CSVの変更を反映したいとき

1. アプリを停止する
2. DBファイルを削除する：`rm ~/songdb.mv.db`
3. アプリを再起動する

## データベース

H2コンソールで中身を確認できます（アプリ起動中）。

- URL：http://localhost:8080/h2-console
- JDBC URL：`jdbc:h2:file:~/songdb;AUTO_SERVER=TRUE`
- User Name：`sa`（パスワードは空）

テーブルは3つです。

| テーブル | 内容 |
|---|---|
| `song` | 曲名、アーティスト、ジャケット写真URL、各サービスへのリンク |
| `keyword` | キーワード（重複なし） |
| `song_keyword_relation` | 曲とキーワードの対応（`song_id`, `keyword_id`） |

## ディレクトリ構成

```
src/main
├─ java/song_recommend
│   ├─ SongRecommendApplication.java   起動クラス
│   ├─ controller/SongApiController.java   検索APIなどの入口
│   ├─ service/SongService.java   CSV読み込み・外部API呼び出し・検索処理
│   ├─ repository/                DBアクセス
│   └─ model/                     Song, Keyword
└─ resources
    ├─ application.properties
    ├─ songs.csv
    └─ static/                    index.html, 背景SVG, ロゴ
```

## API

| パス | 内容 |
|---|---|
| `GET /api/search?keyword=夏 冬` | スペース区切りのキーワードをすべて持つ曲を返す |
| `GET /api/random-keywords` | ランダムなキーワードを5件返す |