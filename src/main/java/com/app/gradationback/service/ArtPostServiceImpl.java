package com.app.gradationback.service;

import com.app.gradationback.domain.ArtPostDTO;
import com.app.gradationback.domain.ArtPostVO;
import com.app.gradationback.domain.ArtVO;
import com.app.gradationback.repository.ArtDAO;
import com.app.gradationback.repository.ArtImgDAO;
import com.app.gradationback.repository.ArtPostDAO;
import com.app.gradationback.repository.CommentDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ArtPostServiceImpl implements ArtPostService {

    private final ArtPostDAO artPostDAO;
    private final ArtImgDAO artImgDAO;
    private final ArtDAO artDAO;
    private final CommentDAO commentDAO;

//    작품 게시글 등록 (작품 정보 + 작품 이미지 + 작품 게시글)
    @Override
    public Long register(ArtPostDTO artPostDTO) {
        ArtVO artVO = new ArtVO();
        artVO.setArtTitle(artPostDTO.getArtTitle());
        artVO.setArtCategory(artPostDTO.getArtCategory());
        artVO.setArtMaterial(artPostDTO.getArtMaterial());
        artVO.setArtSize(artPostDTO.getArtSize());
        artVO.setArtDescription(artPostDTO.getArtDescription());
        artVO.setArtEndDate(artPostDTO.getArtEndDate());
        artVO.setUserId(artPostDTO.getUserId());
        artDAO.save(artVO);
        Long artId = artVO.getId();

        ArtPostVO artPostVO = new ArtPostVO();
        artPostVO.setArtId(artId);
        artPostVO.setArtPostDate(new Timestamp(System.currentTimeMillis()));
        artPostVO.setUserId(artPostDTO.getUserId());
        artPostDAO.save(artPostVO);
        return artPostVO.getId();
    }

//    작품 게시글 전체 조회
    @Override
    public List<ArtPostDTO> getArtPostList() {
        return artPostDAO.findAll();
    }

//    작품 게시글 전체 조회 (userId로)
    public List<ArtPostDTO> getArtPostListByUserId(Long userId) {
        return artPostDAO.findAllByUserId(userId);
    }

//    작품 게시글 단일 조회
    @Override
    public Optional<ArtPostDTO> getArtPostById(Long id) {

        return artPostDAO.findById(id).map((post) -> {
            Map<String, Object> params = new HashMap<>();
            params.put("postId", post.getArtId());

            post.setComments(commentDAO.findAllByPostId(params));
            post.setImages(artImgDAO.findAllByArtId(post.getArtId()));
            post.setArtLikeCount(artDAO.findLikeCount(post.getArtId()));
            return post;
        });
    }

//    등록순으로 상위 50개 작품 조회
    public List<ArtPostDTO> getArtListForMain() {
        return artPostDAO.findAllForMain();
    }

//    카테고리 + 드롭다운 + 페이지네이션
    @Override
    public List<ArtPostDTO> getArtListByCategoryAndDropdown(Map<String, Object> params) {

        if (params.get("category").equals("sculpture")) {
            params.put("category", "조각");
        }else if(params.get("category").equals("craft")) {
            params.put("category", "공예");
        }else if(params.get("category").equals("architecture")) {
            params.put("category", "건축");
        }else if(params.get("category").equals("calligraphy")) {
            params.put("category", "서예");
        }else if(params.get("category").equals("painting")) {
            params.put("category", "회화");
        }else {
//            korean
            params.put("category", "한국");
        }

        return artPostDAO.findArtListByCategoryAndDropdown(params).stream().map(post -> {
//            post.setComments(commentDAO.findAllByPostId(post.getArtPostId()));
            post.setImages(artImgDAO.findAllByArtId(post.getId()));
            return post;
        }).toList();
    };

//    내 작품 리스트
    @Override
    public List<ArtPostDTO> getMyArtList(Long userId) {
        return artPostDAO.findAllMyArt(userId);
    }

//    내 작품 좋아요
    @Override
    public List<ArtPostDTO> getLikedArtList(Long userId) {
        return artPostDAO.findAllLikedArt(userId);
    }

//    경매 가능 작품 조회 (좋아요 50개 이상)
    @Override
    public List<ArtPostDTO> getArtListForAuction(Long userId) {
        return artPostDAO.findAllForAuction(userId);
    }

//    작품 게시글 수정
    @Override
    public void edit(ArtPostVO artPostVO) {
        artPostDAO.update(artPostVO);
    }

//    작품 게시글 삭제 (art + artImg 삭제)
    @Override
    public void removeById(Long id) {
        artPostDAO.findById(id).ifPresent(post -> {
            Long postId = post.getId();
            Long artId = post.getArtId();
            commentDAO.deleteAllByPostId(postId);
            artDAO.deleteAllLike(artId);
            artPostDAO.deleteById(postId);
            artImgDAO.deleteAllByArtId(artId);
            artDAO.deleteById(artId);
        });
    }
}
