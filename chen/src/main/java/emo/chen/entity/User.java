package emo.chen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user") // 对应数据库表名
public class User {
    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    @TableId(type = IdType.AUTO)
    private Integer user_id;

    private String username;
    private String password;
    private String gender;
    private String telephone;
    private String introduce;
    private String address;

    @Override
    public String toString() {
        return "User{" +
                "user_id=" + user_id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", gender='" + gender + '\'' +
                ", telephone='" + telephone + '\'' +
                ", introduce='" + introduce + '\'' +
                ", address='" + address + '\'' +
                ", role=" + role +
                '}';
    }

    private Integer role;
}
