# 使用 Java 内置 HTTPS（测试用）
# 1. 生成自签名证书
keytool -genkeypair -alias family-app -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 3650 -dname "CN=qioba.cn, OU=Family, O=Family, L=Beijing, ST=Beijing, C=CN" -storepass family123 -keypass family123

# 2. 将证书放入 resources 目录
# 3. 启用 application-ssl.yml 配置
