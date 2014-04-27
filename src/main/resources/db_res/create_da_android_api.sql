CREATE TABLE da_android_api(
api_id INT(8) NOT NULL AUTO_INCREMENT PRIMARY KEY UNIQUE KEY,
api_name VARCHAR(255),
api_retype TEXT,
api_params TEXT,
api_permission TEXT,
api_type INT(1),
INDEX(api_name)
)