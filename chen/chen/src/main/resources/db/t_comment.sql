CREATE TABLE IF NOT EXISTS t_comment (
    comment_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    goods_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content TEXT NOT NULL,
    create_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (goods_id) REFERENCES goods(goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; 