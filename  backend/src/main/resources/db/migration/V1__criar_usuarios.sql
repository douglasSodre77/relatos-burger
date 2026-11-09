CREATE TABLE usuarios (
                          id                BIGINT AUTO_INCREMENT PRIMARY KEY,
                          nome              VARCHAR(80)  NOT NULL,
                          sobrenome         VARCHAR(120) NOT NULL,
                          email             VARCHAR(160) NOT NULL,
                          senha             VARCHAR(255) NOT NULL,
                          telefone          VARCHAR(11)  NOT NULL,
                          tipo_usuario      ENUM('CLIENTE','ADMIN') NOT NULL DEFAULT 'CLIENTE',
                          tentativas_falhas INT NOT NULL DEFAULT 0,
                          bloqueado_ate     TIMESTAMP NULL,
                          criado_em         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT uk_usuarios_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;