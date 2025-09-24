package com.example.tomatomall.controller;

import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.ResultVO;
import com.example.tomatomall.vo.StockpileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public ResultVO<List<ProductVO>> getAllProducts() {
        try {
            List<ProductVO> products = productService.getAllProducts();
            return ResultVO.buildSuccess(products);
        } catch (Exception e) {
            return ResultVO.buildFailure(e.getMessage(), "400");
        }
    }

    @GetMapping("/{id}")
    public ResultVO<ProductVO> getProductById(@PathVariable Long id) {
        try {
            ProductVO product = productService.getProductById(id);
            return ResultVO.buildSuccess(product);
        } catch (Exception e) {
            return ResultVO.buildFailure(e.getMessage(), "400");
        }
    }

    @PostMapping
    public ResultVO<ProductVO> createProduct(@RequestBody ProductVO productVO, HttpServletRequest request) {
        try {
            // TODO: 检查用户权限是否为管理员
            ProductVO createdProduct = productService.createProduct(productVO);
            return ResultVO.buildSuccess(createdProduct);
        } catch (Exception e) {
            return ResultVO.buildFailure(e.getMessage(), "400");
        }
    }

    @PutMapping
    public ResultVO<String> updateProduct(@RequestBody ProductVO productVO, HttpServletRequest request) {
        try {
            // TODO: 检查用户权限是否为管理员
            productService.updateProduct(productVO);
            return ResultVO.buildSuccess("更新成功");
        } catch (Exception e) {
            return ResultVO.buildFailure(e.getMessage(), "400");
        }
    }

    @PostMapping("/basic")
    public ResultVO<String> updateProductBasicInfo(@RequestBody ProductVO productVO, HttpServletRequest request) {
        try {
            // TODO: 检查用户权限是否为管理员
            productService.updateProductBasicInfo(productVO);
            return ResultVO.buildSuccess("更新商品基本信息成功");
        } catch (Exception e) {
            return ResultVO.buildFailure(e.getMessage(), "400");
        }
    }

    @DeleteMapping("/{id}")
    public ResultVO<String> deleteProduct(@PathVariable Long id, HttpServletRequest request) {
        try {
            // TODO: 检查用户权限是否为管理员
            productService.deleteProduct(id);
            return ResultVO.buildSuccess("删除成功");
        } catch (Exception e) {
            return ResultVO.buildFailure(e.getMessage(), "400");
        }
    }

    @PatchMapping("/stockpile/{productId}")
    public ResultVO<String> updateStockpile(@PathVariable Long productId, @RequestParam Integer amount, HttpServletRequest request) {
        try {
            // TODO: 检查用户权限是否为管理员
            productService.updateStockpile(productId, amount);
            return ResultVO.buildSuccess("调整库存成功");
        } catch (Exception e) {
            return ResultVO.buildFailure(e.getMessage(), "400");
        }
    }

    @GetMapping("/stockpile/{productId}")
    public ResultVO<StockpileVO> getStockpile(@PathVariable Long productId) {
        try {
            StockpileVO stockpile = productService.getStockpile(productId);
            return ResultVO.buildSuccess(stockpile);
        } catch (Exception e) {
            return ResultVO.buildFailure(e.getMessage(), "400");
        }
    }
} 