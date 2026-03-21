# 使用 Java 内置 HTTPS（测试用）
# 1. 生成自签名证书
STORE_PASS="${SSL_KEY_STORE_PASSWORD:-changeit}"
keytool -genkeypair -alias family-app -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 3650 -dname "CN=localhost, OU=Family, O=Family, L=Beijing, ST=Beijing, C=CN" -storepass "${STORE_PASS}" -keypass "${STORE_PASS}"

# 2. 将证书放入 resources 目录
# 3. 启用 application-ssl.yml 配置
