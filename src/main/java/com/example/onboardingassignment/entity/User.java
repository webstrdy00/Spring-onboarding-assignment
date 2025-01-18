package com.example.onboardingassignment.entity;

import com.example.onboardingassignment.enums.UserRole;
import com.example.onboardingassignment.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends Timestamped{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userName;

    private String password;

    @Column(unique = true, nullable = false)
    private String nickName;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    public User(String userName, String password, String nickName, UserRole userRole) {
        this.userName = userName;
        this.password = password;
        this.nickName = nickName;
        this.role = userRole;
        this.status = UserStatus.ACTIVE;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void delete() {
        this.status = UserStatus.DELETED;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
