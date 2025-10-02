package com.example.tomatomall.po;

import lombok.Data;
import lombok.Setter;
import lombok.Getter;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Data
@Entity
@Setter
@Getter
@Table(name = "specifications")
public class SpecificationPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "规格名称不能为空")
    @Column(nullable = false)
    private String item;

    @NotBlank(message = "规格内容不能为空")
    @Column(nullable = false)
    private String value;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private ProductPO product;
} 