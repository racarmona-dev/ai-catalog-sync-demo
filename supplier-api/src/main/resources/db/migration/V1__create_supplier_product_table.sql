CREATE TABLE supplier_product (
    id    BIGINT         NOT NULL AUTO_INCREMENT,
    name  VARCHAR(255)   NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
