package com.soyeon.nubim.domain.post;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.soyeon.nubim.domain.post.dto.PostCreateRequestDto;
import com.soyeon.nubim.domain.post.dto.PostCreateResponseDto;
import com.soyeon.nubim.domain.post.dto.PostDetailResponseDto;
import com.soyeon.nubim.domain.post.dto.PostSimpleResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/v1/posts")
public class PostControllerV1 {

	private PostService postService;

	@PostMapping
	public ResponseEntity<PostCreateResponseDto> createPost(@RequestBody PostCreateRequestDto postCreateRequestDto) {
		PostCreateResponseDto postCreateResponseDto = postService.createPost(postCreateRequestDto);

		return ResponseEntity
			.created(URI.create(String.format("/v1/posts/%d", postCreateResponseDto.getPostId())))
			.body(postCreateResponseDto);
	}

	@Operation(description = "type이 비어있을 경우: 자세한 게시글 type=simple: 미리보기")
	@GetMapping("/{postId}")
	public ResponseEntity<?> getPostDetail(
		@PathVariable Long postId,
		@RequestParam(required = false) String type) {
		if (type == null) {
			return ResponseEntity.ok(postService.findPostDetailById(postId));
		} else if (type.equals("simple")) {
			return ResponseEntity.ok(postService.findPostSimpleById(postId));
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
	}

	// TODO : 배포 전 soft delete로 변경 필요
	@DeleteMapping("{postId}")
	public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
		postService.deleteById(postId);
		return ResponseEntity.ok().build();
	}

}