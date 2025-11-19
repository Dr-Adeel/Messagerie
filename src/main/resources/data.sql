DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
                        id INT AUTO_INCREMENT  PRIMARY KEY,
                        username VARCHAR(250) NOT NULL,
                        password VARCHAR(250) NOT NULL,
                        role VARCHAR(250) NOT NULL
);
INSERT INTO app_user (username, password, role) VALUES ('user', '$2y$10$.qkbukzzX21D.bqbI.B2R.tvWP90o/Y16QRWVLodw51BHft7ZWbc.', 'USER'),
                                                     ('dbadmin', '$2y$10$kp1V7UYDEWn17WSK16UcmOnFd1mPFVF6UkLrOOCGtf24HOYt8p1iC', 'ADMIN');