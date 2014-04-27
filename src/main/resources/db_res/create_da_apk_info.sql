CREATE TABLE da_apk_info (
apk_id INT(8) NOT NULL AUTO_INCREMENT PRIMARY KEY UNIQUE KEY,
apk_name VARCHAR(256),
apk_crc32 CHAR(8),
apk_md5 CHAR(16),
apk_sha1 CHAR(20),
apk_permissions TEXT,
apk_application TEXT,
apk_main_activity TEXT,
apk_activity TEXT,
apk_receiver TEXT,
apk_service TEXT,
apk_contentprovider TEXT,
apk_malicious_level TINYINT(2),
apk_report_location TEXT,
INDEX(apk_crc32),
INDEX(apk_md5),
INDEX(apk_sha1)
)