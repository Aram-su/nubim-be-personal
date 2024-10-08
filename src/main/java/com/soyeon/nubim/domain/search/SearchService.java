package com.soyeon.nubim.domain.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.soyeon.nubim.domain.post.PostService;
import com.soyeon.nubim.domain.post.dto.PostSimpleResponseDto;
import com.soyeon.nubim.domain.user.User;
import com.soyeon.nubim.domain.user.UserMapper;
import com.soyeon.nubim.domain.user.UserService;
import com.soyeon.nubim.domain.user.dto.UserSimpleWithIsFollowedResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {
	private final UserService userService;
	private final PostService postService;
	private final UserMapper userMapper;

	public Page<UserSimpleWithIsFollowedResponseDto> searchUsers(String query, Pageable pageable) {
		Page<User> users = userService.searchUserByNickname(pageable, query);
		return users.map(userMapper::toUserSimpleWithIsFollowedResponseDto);
	}

	public Page<PostSimpleResponseDto> searchPosts(String query, Pageable pageable) {
		return postService.searchPostsByTitleOrContent(pageable, query);
	}
}
