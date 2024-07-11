package com.soyeon.nubim.domain.comment;

import com.soyeon.nubim.domain.comment.dto.CommentCreateRequestDto;
import com.soyeon.nubim.domain.comment.dto.CommentCreateResponseDto;
import com.soyeon.nubim.domain.comment.dto.CommentResponseDto;
import com.soyeon.nubim.domain.post.PostService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@AllArgsConstructor
@RestController
@RequestMapping("/v1/comments")
public class CommentControllerV1 {
    CommentService commentService;
    PostService postService;

    @PostMapping
    public ResponseEntity<CommentCreateResponseDto> createComment(@RequestBody CommentCreateRequestDto commentCreateRequestDto) {
        CommentCreateResponseDto commentCreateResponseDto = commentService.createComment(commentCreateRequestDto);

        return ResponseEntity
                .created(URI.create("")) // TODO : 조회 api 로 연결
                .body(commentCreateResponseDto);
    }

    @Operation(description = "postId 기반으로 댓글 10개씩 조회, sort : 생성시간 오름차순(기본값): asc, 생성시간 내림차순: desc")
    @GetMapping("/post/{postId}")
    public ResponseEntity<Page<CommentResponseDto>> getCommentsByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") Long page,
            @RequestParam(defaultValue = "asc") String sort) {
        postService.validatePostExist(postId);

        Pageable pageable;
        final int DEFAULT_PAGE_SIZE = 10;

        if (sort.equalsIgnoreCase("asc")) {
            pageable = PageRequest.of(page.intValue(), DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.ASC, "createdAt"));
        } else if (sort.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page.intValue(), DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(commentService.findCommentsByPostIdAndPageable(postId, pageable));
    }

}
