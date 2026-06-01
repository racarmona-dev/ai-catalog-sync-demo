CREATE TABLE product (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    price      DECIMAL(10, 2) NOT NULL,
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    updated_at DATETIME     NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
