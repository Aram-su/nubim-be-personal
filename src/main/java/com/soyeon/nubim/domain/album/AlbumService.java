package com.soyeon.nubim.domain.album;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soyeon.nubim.common.util.aws.S3PresignedUrlGenerator;
import com.soyeon.nubim.domain.album.dto.AlbumCreateRequestDto;
import com.soyeon.nubim.domain.album.dto.AlbumCreateResponseDto;
import com.soyeon.nubim.domain.album.dto.AlbumReadResponseDto;
import com.soyeon.nubim.domain.album.dto.AlbumUpdateRequestDto;
import com.soyeon.nubim.domain.album.dto.LocationUpdateRequestDto;
import com.soyeon.nubim.domain.album.mapper.AlbumMapper;
import com.soyeon.nubim.domain.album.mapper.LocationMapper;
import com.soyeon.nubim.domain.user.User;
import com.soyeon.nubim.domain.user.UserNotFoundException;
import com.soyeon.nubim.domain.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlbumService {

	private final S3PresignedUrlGenerator s3PresignedUrlGenerator;
	private final AlbumRepository albumRepository;
	private final AlbumMapper albumMapper;
	private final UserService userService;
	private final LocationMapper locationMapper;

	public Optional<Album> findById(Long id) {
		return albumRepository.findById(id);
	}

	@Transactional
	public AlbumCreateResponseDto createAlbum(AlbumCreateRequestDto albumCreateRequestDto) {
		User currentUser = userService.getCurrentUser();

		Album album = albumMapper.toEntity(albumCreateRequestDto, currentUser);

		Album savedAlbum = albumRepository.save(album);
		return albumMapper.toAlbumCreateResponseDto(savedAlbum);
	}

	public AlbumReadResponseDto findByIdWithLocations(Long albumId) {
		Album album = albumRepository.findByIdWithLocations(albumId)
			.orElseThrow(() -> new AlbumNotFoundException(albumId));

		return albumMapper.toAlbumReadResponseDto(album);
	}

	public List<AlbumReadResponseDto> findAlbumsByUserId(Long userId) {
		userService.findById(userId)
			.orElseThrow(() -> new UserNotFoundException(userId));

		List<Album> albums = albumRepository.findByUserUserId(userId);

		List<AlbumReadResponseDto> albumReadResponseDtos = new ArrayList<>(albums.size());
		for (Album album : albums) {
			albumReadResponseDtos.add(albumMapper.toAlbumReadResponseDto(album));
		}
		return albumReadResponseDtos;
	}

	public List<AlbumReadResponseDto> findAlbumsByCurrentUser() {
		String currentUserEmail = userService.getCurrentUserEmail();

		List<Album> albums = albumRepository.findAlbumsByEmail(currentUserEmail);

		List<AlbumReadResponseDto> albumReadResponseDtos = new ArrayList<>(albums.size());
		for (Album album : albums) {
			albumReadResponseDtos.add(albumMapper.toAlbumReadResponseDto(album));
		}

		return albumReadResponseDtos;
	}

	@Transactional
	public AlbumReadResponseDto updateAlbum(Long albumId, AlbumUpdateRequestDto albumUpdateRequestDto) {
		Album album = albumRepository.findByIdWithLocations(albumId)
			.orElseThrow(() -> new AlbumNotFoundException(albumId));

		String newDescription = albumUpdateRequestDto.getDescription();
		album.setDescription(newDescription);

		albumRepository.deleteLocationsByAlbumId(albumId);
		List<LocationUpdateRequestDto> newLocationDtos = albumUpdateRequestDto.getLocations();
		List<Location> newLocations = locationMapper.toEntityListFromUpdateDto(newLocationDtos);
		album.setLocations(newLocations);
		album.bindLocations();

		Album updatedAlbum = albumRepository.save(album);
		return albumMapper.toAlbumReadResponseDto(updatedAlbum);
	}

	public void deleteAlbum(Long albumId) {
		albumRepository.findByIdWithLocations(albumId)
			.orElseThrow(() -> new AlbumNotFoundException(albumId));

		albumRepository.deleteById(albumId);
	}

	public List<String> handlePhotoUploadUrlsGeneration(List<String> contentTypes) {
		return s3PresignedUrlGenerator.generatePresignedUrls(contentTypes);
	}

}
