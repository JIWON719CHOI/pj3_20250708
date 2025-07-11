CREATE TABLE board
(
    id          INT AUTO_INCREMENT     NOT NULL,
    title       VARCHAR(300)           NOT NULL,
    content     VARCHAR(10000)         NOT NULL,
    author      VARCHAR(100)           NOT NULL,
    inserted_at datetime DEFAULT NOW() NOT NULL,
    CONSTRAINT pk_board PRIMARY KEY (id)
);

CREATE TABLE member
(
    email       VARCHAR(255)           NOT NULL,
    password    VARCHAR(255)           NOT NULL,
    nick_name   VARCHAR(255) UNIQUE    NOT NULL,
    info        VARCHAR(3000)          NULL,
    inserted_at datetime DEFAULT NOW() NOT NULL,
    CONSTRAINT pk_member PRIMARY KEY (email)
);

