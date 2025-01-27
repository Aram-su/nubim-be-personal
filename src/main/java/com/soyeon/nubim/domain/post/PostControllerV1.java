package com.soyeon.nubim.domain.post;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soyeon.nubim.common.exception_handler.InvalidQueryParameterException;
import com.soyeon.nubim.domain.post.dto.PostCreateRequestDto;
import com.soyeon.nubim.domain.post.dto.PostCreateResponseDto;
import com.soyeon.nubim.domain.post.dto.PostMainResponseDto;
import com.soyeon.nubim.domain.post.dto.PostResponseDto;
import com.soyeon.nubim.domain.user.LoggedInUserService;
import com.soyeon.nubim.domain.user.User;
import com.soyeon.nubim.domain.user.UserService;
import com.soyeon.nubim.domain.user_block.UserBlockValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/posts")
public class PostControllerV1 {

	private final PostService postService;
	private final UserService userService;
	private final LoggedInUserService loggedInUserService;
	private final UserBlockValidator userBlockValidator;

	private static final int DEFAULT_SIMPLE_PAGE_SIZE = 10;
	private static final int DEFAULT_MAIN_PAGE_SIZE = 5;
	private static final String DEFAULT_ORDER_BY = "createdAt";
	private static final int DEFAULT_RECENT_CRITERIA_DAYS = 3;

	@PostMapping
	public ResponseEntity<PostCreateResponseDto> createPost(
		@RequestBody @Valid PostCreateRequestDto postCreateRequestDto) {
		User authorUser = loggedInUserService.getCurrentUser();
		PostCreateResponseDto postCreateResponseDto = postService.createPost(postCreateRequestDto, authorUser);

		return ResponseEntity
			.created(URI.create(String.format("/v1/posts/%d", postCreateResponseDto.getPostId())))
			.body(postCreateResponseDto);
	}

	@Operation(description = "type이 비어있을 경우: 자세한 게시글 type=simple: 미리보기")
	@GetMapping("/{postId}")
	public ResponseEntity<PostResponseDto> getPostDetail(
		@PathVariable Long postId,
		@RequestParam(required = false) String type) {
		if (type == null) {
			return ResponseEntity.ok(postService.findPostMainById(postId));
		} else if (type.equals("simple")) {
			return ResponseEntity.ok(postService.findPostSimpleById(postId));
		} else {
			throw new InvalidQueryParameterException("type");
		}
	}

	@Operation(description = "nickname 을 기준으로 게시글 미리보기 리스트 시간순 정렬 응답, 기본은 내림차순, sort=asc일경우 오름차순")
	@GetMapping("/user/{nickname}")
	public ResponseEntity<Page<PostMainResponseDto>> getPostsByUserNickname(
		@PathVariable String nickname,
		@RequestParam(defaultValue = "0") Long page,
		@RequestParam(defaultValue = "desc") String sort) {
		User targetUser = userService.getUserByNickname(nickname);
		User currentUser = new User(loggedInUserService.getCurrentUserId());
		userBlockValidator.checkBlockRelation(currentUser, targetUser);

		PageRequest pageRequest;
		if (sort.equals("desc")) {
			pageRequest = PageRequest.of(page.intValue(), DEFAULT_SIMPLE_PAGE_SIZE,
				Sort.by(Sort.Direction.DESC, DEFAULT_ORDER_BY));
		} else if (sort.equals("asc")) {
			pageRequest = PageRequest.of(page.intValue(), DEFAULT_SIMPLE_PAGE_SIZE,
				Sort.by(Sort.Direction.ASC, DEFAULT_ORDER_BY));
		} else {
			throw new InvalidQueryParameterException("sort");
		}
		return ResponseEntity.ok(postService.findAllPostsByUserOrderByCreatedAt(targetUser, pageRequest));
	}

	@DeleteMapping("{postId}")
	public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
		postService.deleteById(postId, loggedInUserService.getCurrentUserId());
		return ResponseEntity.ok().build();
	}

	@Operation(description = "메인 화면에서 노출되는 게시글 조회")
	@GetMapping("/main-posts")
	public ResponseEntity<Page<PostMainResponseDto>> getMainPosts(
		@RequestParam(defaultValue = "0") Long page,
		@RequestParam(defaultValue = "follow") @Parameter(description = "[ follow, random ]") String type,
		@RequestParam(required = false) Float randomSeed) {
		User user = loggedInUserService.getCurrentUser();

		if (type.equals("follow")) { // 팔로우 기반 게시글 조회
			PageRequest pageRequest = PageRequest.of(
				page.intValue(), DEFAULT_MAIN_PAGE_SIZE, Sort.by(Sort.Direction.DESC, DEFAULT_ORDER_BY));

			return ResponseEntity.ok(
				postService.findRecentPostsOfFollowees(user, pageRequest, DEFAULT_RECENT_CRITERIA_DAYS));
		} else if (type.equals("random")) { // 랜덤 추천 게시글 조회
			PageRequest pageRequest = PageRequest.of(page.intValue(), DEFAULT_MAIN_PAGE_SIZE);

			return ResponseEntity.ok(postService.findRandomPosts(pageRequest, randomSeed, user));
		} else {
			throw new InvalidQueryParameterException("type");
		}
	}
}