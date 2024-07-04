package com.soyeon.nubim.domain.post;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.soyeon.nubim.domain.album.Album;
import com.soyeon.nubim.domain.album.AlbumNotFoundException;
import com.soyeon.nubim.domain.album.AlbumService;
import com.soyeon.nubim.domain.post.dto.PostCreateRequestDto;
import com.soyeon.nubim.domain.post.dto.PostCreateResponseDto;
import com.soyeon.nubim.domain.user.User;
import com.soyeon.nubim.domain.user.UserNotFoundException;
import com.soyeon.nubim.domain.user.UserService;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class PostMapper {
	private UserService userService;
	private AlbumService albumService;

	public Post toEntity(PostCreateRequestDto postCreateRequestDto) {
		Optional<User> authorUser = userService.findById(postCreateRequestDto.getUserId());
		authorUser.orElseThrow(() -> new UserNotFoundException(postCreateRequestDto.getUserId()));

		Optional<Album> linkedAlbum = albumService.findById(postCreateRequestDto.getAlbumId());
		linkedAlbum.orElseThrow(() -> new AlbumNotFoundException(postCreateRequestDto.getAlbumId()));

		return Post.builder()
			.postTitle(postCreateRequestDto.getPostTitle())
			.postContent(postCreateRequestDto.getPostContent())
			.album(linkedAlbum.get())
			.user(authorUser.get())
			.build();
	}

	public PostCreateResponseDto toPostCreateResponseDto(Post post) {
		return PostCreateResponseDto.builder()
			.postId(post.getPostId())
			.userId(post.getUser().getUserId())
			.albumId(post.getAlbum().getAlbumId())
			.postTitle(post.getPostTitle())
			.postContent(post.getPostContent())
			.build();
	}
}
