# 1. ビルド用イメージ（マルチステージビルド）
FROM eclipse-temurin:21-jdk AS build

# 作業ディレクトリ作成
WORKDIR /app

# Mavenの依存キャッシュ活用のため、先にpom.xmlだけコピー
COPY pom.xml mvnw ./
COPY .mvn .mvn

# 依存関係のダウンロード
RUN ./mvnw dependency:go-offline

# ソースコードをコピー
COPY src src

# ビルド実行（jarの作成）
RUN ./mvnw package -DskipTests

# 2. 実行用ステージ（軽量のJREイメージを利用）
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# ビルド成果物のjarファイルをコピー
COPY --from=build /app/target/*.jar app.jar

# コンテナ内のアプリ起動ポート設定（RenderはPORT環境変数を使う）
ENV PORT=8080
EXPOSE 8080

# 起動コマンド。Javaアプリを起動。UTF-8設定あり
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
