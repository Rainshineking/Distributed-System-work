package com.example.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@Entity
@Table(name = "inventory")
public class Inventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long productId;
    
    @Column(nullable = false)
    private Integer stock = 0;
    
    @Column(nullable = false)
    private Integer lockedStock = 0;
    
    @Version
    private Integer version = 0;
    
    @CreationTimestamp
    @Column(updatable = false)
    private Date createTime;
    
    @UpdateTimestamp
    private Date updateTime;
}
