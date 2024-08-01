package com.soyeon.nubim.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserSimpleResponseDto {
	private String username;
	private String nickname;
	private String profileImageUrl;
}
