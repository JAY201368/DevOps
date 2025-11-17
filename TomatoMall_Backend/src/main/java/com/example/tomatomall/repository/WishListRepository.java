package com.example.tomatomall.repository;

import com.example.tomatomall.po.WishListItemPO;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WishListRepository extends JpaRepository<WishListItemPO, Long> {
    List<WishListItemPO> findByUserId(Long userId);
    Optional<WishListItemPO> findByUserIdAndBookId(Long userId, Long bookId);
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
