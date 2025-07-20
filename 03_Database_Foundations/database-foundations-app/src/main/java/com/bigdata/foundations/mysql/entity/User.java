package com.bigdata.foundations.mysql.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_status", columnList = "status"),
    @Index(name = "idx_users_created_at", columnList = "created_at")
})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Size(max = 100)
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @NotNull
    @Size(max = 100)
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @NotNull
    @Email
    @Size(max = 255)
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Size(max = 20)
    @Column(name = "phone")
    private String phone;
    
    @NotNull
    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;
    
    @Size(max = 100)
    @Column(name = "city")
    private String city;
    
    @Size(max = 100)
    @Column(name = "country")
    private String country;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public User() {}
    
    public User(String firstName, String lastName, String email, LocalDateTime dateOfBirth, UserStatus status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public LocalDateTime getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDateTime dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
    
    public enum UserStatus {
        ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION
    }
}
