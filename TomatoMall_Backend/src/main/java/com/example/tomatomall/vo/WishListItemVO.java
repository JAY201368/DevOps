package com.example.tomatomall.vo;

import com.example.tomatomall.po.WishListItemPO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WishListItemVO {
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDateTime createdAt;

    public static WishListItemVO fromPO(WishListItemPO po) {
        WishListItemVO vo = new WishListItemVO();
        vo.setId(po.getId());
        vo.setUserId(po.getUser().getId());
        vo.setBookId(po.getBookId());
        vo.setCreatedAt(po.getCreatedAt());
        return vo;
    }
}
