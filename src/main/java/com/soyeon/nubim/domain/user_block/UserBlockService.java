package com.soyeon.nubim.domain.user_block;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soyeon.nubim.domain.user.LoggedInUserService;
import com.soyeon.nubim.domain.user.User;
import com.soyeon.nubim.domain.user.UserService;
import com.soyeon.nubim.domain.user_block.dto.UserBlockCreateResponse;
import com.soyeon.nubim.domain.user_block.dto.UserBlockDeleteResponse;
import com.soyeon.nubim.domain.user_block.dto.UserBlockReadResponse;
import com.soyeon.nubim.domain.user_block.dto.UserBlockRequest;
import com.soyeon.nubim.domain.user_block.exception.MultipleUserBlockDeletedException;
import com.soyeon.nubim.domain.user_block.exception.UserBlockDeleteFailException;
import com.soyeon.nubim.domain.userfollow.UserFollowService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserBlockService {

	private static final int DELETE_SUCCESS = 1;
	private static final int DELETE_FAIL = 0;
	private final LoggedInUserService loggedInUserService;
	private final UserService userService;
	private final UserBlockRepository userBlockRepository;
	private final UserBlockMapper userBlockMapper;
	private final UserBlockValidator userBlockValidator;
	private final UserFollowService userFollowService;

	@Transactional
	public UserBlockCreateResponse blockUser(UserBlockRequest userBlockRequest) {
		Long currentUserId = loggedInUserService.getCurrentUserId();
		User blockingUser = new User(currentUserId);
		User blockedUser = userService.findByNickname(userBlockRequest.getBlockedUserNickname());

		userBlockValidator.checkSelfBlock(blockingUser, blockedUser);
		userBlockValidator.validateUserBlockNotExists(blockingUser, blockedUser);

		UserBlock userBlock = userBlockMapper.toEntity(blockingUser, blockedUser);
		UserBlock savedUserBlock = userBlockRepository.save(userBlock);

		userFollowService.deleteFollowBetweenUsers(blockingUser, blockedUser);

		return userBlockMapper.toUserBlockCreateResponse(savedUserBlock);
	}

	public List<UserBlockReadResponse> getBlockedUsers() {
		Long currentUserId = loggedInUserService.getCurrentUserId();
		User blockingUser = new User(currentUserId);

		List<UserBlock> blockedUsers = userBlockRepository.findBlockedUsersByBlockingUser(blockingUser);
		return userBlockMapper.toUserBlockReadResponses(blockedUsers);
	}

	@Transactional
	public UserBlockDeleteResponse unblockUser(UserBlockRequest userBlockRequest) {
		Long currentUserId = loggedInUserService.getCurrentUserId();
		User blockingUser = new User(currentUserId);
		User blockedUser = userService.findByNickname(userBlockRequest.getBlockedUserNickname());

		int deleteResult = userBlockRepository.deleteByBlockingUserAndBlockedUser(blockingUser, blockedUser);

		if (deleteResult == DELETE_SUCCESS) {
			return new UserBlockDeleteResponse("unblock success");
		}
		if (deleteResult == DELETE_FAIL) {
			throw new UserBlockDeleteFailException();
		}
		throw new MultipleUserBlockDeletedException();
	}

}
