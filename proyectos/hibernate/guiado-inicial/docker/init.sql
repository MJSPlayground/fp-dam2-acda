ALTER DATABASE biblioteca_guiado CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs;
CREATE DATABASE IF NOT EXISTS biblioteca_final CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs;
GRANT ALL PRIVILEGES ON biblioteca_final.* TO 'acda'@'%';
