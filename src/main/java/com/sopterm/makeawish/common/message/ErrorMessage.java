package com.sopterm.makeawish.common.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorMessage {
	EXPIRE_WISH("주간이 끝난 소원 링크입니다."),
	INVALID_WISH("존재하지 않는 소원 링크입니다."),
	EXIST_MAIN_WISH("이미 진행 중인 소원 링크가 있습니다."),
	INVALID_USER("인증되지 않은 회원입니다."),
	NULL_PRINCIPAL("principal 이 null 일 수 없습니다."),
	INVALID_CAKE("존재하지 않는 케이크입니다.");

	private final String message;
}
