package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.UserPO;
import com.example.tomatomall.po.WishListItemPO;
import com.example.tomatomall.repository.UserRepository;
import com.example.tomatomall.repository.WishListRepository;
import com.example.tomatomall.service.WishListService;
import com.example.tomatomall.vo.WishListItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishListServiceImpl implements WishListService {

    @Autowired
    private WishListRepository wishListRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public WishListItemVO addToWishList(Long userId, Long bookId) {
        // 检查该书是否已在愿望单中
        if (wishListRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new TomatoMallException(400, "此书已在您的愿望单中");
        }

        // 获取用户信息
        UserPO user = userRepository.findById(userId)
                .orElseThrow(() -> new TomatoMallException(404, "用户不存在"));

        // 创建新的愿望单项
        WishListItemPO wishListItem = new WishListItemPO();
        wishListItem.setUser(user);
        wishListItem.setBookId(bookId);

        // 保存到数据库
        WishListItemPO savedItem = wishListRepository.save(wishListItem);
        return WishListItemVO.fromPO(savedItem);
    }

    @Override
    public List<WishListItemVO> getWishList(Long userId) {
        List<WishListItemPO> wishListItems = wishListRepository.findByUserId(userId);
        return wishListItems.stream()
                .map(WishListItemVO::fromPO)
                .collect(Collectors.toList());
    }

    @Override
    public void removeFromWishList(Long userId, Long bookId) {
        WishListItemPO wishListItem = wishListRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new TomatoMallException(404, "该商品不在愿望单中"));
        wishListRepository.delete(wishListItem);
    }

    @Override
    public boolean isInWishList(Long userId, Long bookId) {
        return wishListRepository.existsByUserIdAndBookId(userId, bookId);
    }
}
