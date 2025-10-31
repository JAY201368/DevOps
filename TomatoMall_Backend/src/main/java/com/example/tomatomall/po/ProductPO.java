package com.example.tomatomall.po;

import lombok.Data;
import lombok.Setter;
import lombok.Getter;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Set;

@Data
@Entity
@Setter
@Getter
@Table(name = "products")
public class ProductPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    @Column(nullable = false)
    private String title;

    @DecimalMin(value = "0.0", message = "商品价格不能小于0")
    @Column(nullable = false)
    private BigDecimal price;

    @Min(value = 0, message = "评分不能小于0")
    @Max(value = 10, message = "评分不能大于10")
    @Column(nullable = false)
    private Double rate;

    private String description;

    private String cover;

    private String detail;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SpecificationPO> specifications;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private StockpilePO stockpile;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private SpecificationPO specification;

    // 添加tags字段，用于存储书籍标签
    private String tags;

    public SpecificationPO getSpecification() {
        return specification;
    }

    // 添加tags的getter和setter方法
    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}