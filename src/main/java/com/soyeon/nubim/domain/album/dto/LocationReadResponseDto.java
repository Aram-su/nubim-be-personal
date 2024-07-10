package com.soyeon.nubim.domain.album.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LocationReadResponseDto {
	private Long locationId;
	private Long albumId;
	private Double latitude;
	private Double longitude;
	private LocalDateTime visitedAt;
	private String placeName;
}
