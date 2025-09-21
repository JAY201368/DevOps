package com.example.tomatomall.po;

import lombok.Data;
import lombok.Setter;
import lombok.Getter;
import javax.persistence.*;
import javax.validation.constraints.Min;

@Data
@Entity
@Setter
@Getter
@Table(name = "stockpiles")
public class StockpilePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 0, message = "库存数不能小于0")
    @Column(nullable = false)
    private Integer amount;

    @Min(value = 0, message = "冻结数不能小于0")
    @Column(nullable = false)
    private Integer frozen;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private ProductPO product;
} 